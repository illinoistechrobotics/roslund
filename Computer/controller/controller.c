//    controller.c - runs the controlling end of a robot
//    Copyright (C) 2007  Illinois Institute of Technology Robotics
//	  <robotics@iit.edu>
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License along
//    with this program; if not, write to the Free Software Foundation, Inc.,
//    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

#include <sys/types.h>
#include <signal.h>
#include <unistd.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include "timer.h"
#include "robot_comm.h"
#include "robot_log.h"
#include "joystick.h"
#include "events.h"
#include "profile.h"

#define Xbee

static robot_queue *sig_queue;

void term_handler(int signal);
void usage(char *program_name);

int main(int argc, char *argv[])
{
	//server's address
	char *server_name = "192.168.1.99";
	unsigned int server_port = 0;
	char *usb_port;
	log_level = 0;
    bool shutdown = false;
	
	 int opt;

    // queue stuff
    robot_queue q;
    robot_event ev;
	
	setProfile('p');
	while((opt = getopt(argc, argv, "j:n:p:v:x:")) != -1)
		switch (opt)
		{
			case 'n':
				server_name = optarg;
			 	break;
			case 'p':
				server_port = atoi(optarg);
			 	break;
			case 'v':
				log_level = atoi(optarg);
			 	break;
			case 'j':
				setProfile(optarg[0]);
				break;
			case 'x':
				 usb_port = optarg;
				 break;
			case '?':
				usage(argv[0]);
				exit(1);
				break;
				
		}

	if(server_port == 0){
		server_port = 31337;
	}
    robot_queue_create(&q);
    sig_queue = &q;

    // initialize threads
	if(!joy_thread_create(&q)) {
		log_string(2, "Cannot create the joystick thread");
	}
	
#ifdef Xbee
	if(!xbee_thread_create(&q, usb_port)){
		log_string(2, "Cannot create the xbee thread");
	}
#else
	if(!net_thread_client_create(&q, server_name, server_port)) {
		log_string(2, "Cannot create the network client thread");
	}
#endif

	if(!timer_thread_create(&q)) {
		log_string(2, "cannot create the timer thread");
	}
	// Install the signal handlers
	if(signal(SIGHUP, term_handler) == SIG_ERR)
		log_errno(0, "Error setting the SIGHUP handler");
	if(signal(SIGINT, term_handler) == SIG_ERR)
		log_errno(0, "Error setting the SIGINT handler");
	if(signal(SIGTERM, term_handler) == SIG_ERR)
		log_errno(0, "Error setting the SIGTERM handler");

    // send in the start command
    ev.command = ROBOT_EVENT_CMD_START;
    ev.index = 0;
    ev.value = 0;
    robot_queue_enqueue(&q, &ev);

	// main loop, checks for a joystick update
	// and runs the events
	while(!shutdown) {
        if (!robot_queue_wait_event(&q, &ev))
            shutdown = true;
        switch(ev.command) {
            case ROBOT_EVENT_CMD_START:
                on_init();
                break;
            case ROBOT_EVENT_JOY_AXIS:
                on_axis_change(&ev);
                break;
            case ROBOT_EVENT_JOY_BUTTON:
                if(ev.value)
                    on_button_down(&ev);
                else
                    on_button_up(&ev);
                break;
            case ROBOT_EVENT_CMD_STOP:
                shutdown = true;
                on_shutdown();
                break;
            case ROBOT_EVENT_TIMER:
                if(ev.index == 1) {
                    on_10hz_timer(&ev);
                }
                if(ev.index == 2) {
                    on_1hz_timer(&ev);
                }
				break;
			case ROBOT_EVENT_ADC:
				on_adc_change(&ev);
				break;
			case ROBOT_EVENT_READ_VAR:
				//on_read_variable(&ev);
				break;
            default:
                break;
        }
	}

#ifdef Xbee
	xbee_thread_destroy();
#else
	net_thread_destroy();
#endif

	joy_thread_destroy();

    robot_queue_destroy(&q);

    return 0;
}

// handles signals that tell the controller to shutdown
void term_handler(int signal) { // Signal handler
    robot_event ev;

    ev.command = ROBOT_EVENT_CMD_STOP;
    ev.index = 0;
    ev.value = 0;

    robot_queue_enqueue(sig_queue, &ev);
}


void usage(char *program_name) {
	log_string(3, "Usage: %s [-n host (192.168.1.100)] [-p port (31337)] [-v verbosity (0)]", program_name);
}
