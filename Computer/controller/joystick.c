//    joystick.c - reads the joystick and calls appropraite events
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

// joystick.c
#include <SDL/SDL.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <stdio.h>
#include "robot_log.h"
#include "robot_queue.h"
#include "joystick.h"
#include "events.h"

//---------------------------------------------------------------------------//
// Function Prototypes
//
static int init_joy();
static int close_joy();
static void *joy_thread_main(void *arg);

//---------------------------------------------------------------------------//
// Private Globals
//
static pthread_t tid = 0; // Thread ID of the joystick thread
static SDL_Joystick *joy = NULL; // Joystick to read from

//---------------------------------------------------------------------------//
// Public Function Implementations
//

int joy_thread_create(robot_queue *q) {
	// initialize the Joystick
	if (!init_joy()) {
		return 0;
	}
	// create the thread
	if(pthread_create(&tid, NULL, joy_thread_main, q) != 0) {
		return 0;
	}
	return 1; // exit true

}

int joy_thread_destroy() {
    if (tid <= 0) {
        return 0;
    }

	// kill the thread
	if (pthread_cancel(tid) != 0) {
		return 0;
	}
	// reap the thread
	if (pthread_join(tid, NULL) != 0) {
		return 0;
	}
	return close_joy();

}

//---------------------------------------------------------------------------//
// Private Function Implementations
//

void *joy_thread_main(void *arg) {
	SDL_Event event; // event that SDL_WaitEvent will return
	unsigned char small_range; // value from 0-255 with the axis position

    robot_queue *q = (robot_queue *)arg;
    robot_event ev;

	// Waits for an SDL event and passes it off to our own event handler
    while(SDL_WaitEvent(&event)) {
		switch(event.type) {
			// this is where we call the user implemented events
			case SDL_JOYBUTTONDOWN: // button down
                ev.command = ROBOT_EVENT_JOY_BUTTON;
                ev.index = event.jbutton.button;
                ev.value = 1;
                robot_queue_enqueue(q, &ev);

				break;
			case SDL_JOYBUTTONUP: // button up
                ev.command = ROBOT_EVENT_JOY_BUTTON;
                ev.index = event.jbutton.button;
                ev.value = 0;
                robot_queue_enqueue(q, &ev);

				break;
			case SDL_JOYAXISMOTION: // axis change
				// convert from a signed short to an unsigned char
				small_range = (event.jaxis.value + 32768) >> 8;

                ev.command = ROBOT_EVENT_JOY_AXIS;
                ev.index = event.jaxis.axis;
                ev.value = small_range;

                robot_queue_enqueue(q, &ev);
				break;
		}
	}

	return NULL;
}

// init_joy - initializes the SDL system and the SDL joystick
// 		subsystem
// 	return - TRUE on success, FALSE on error
int init_joy() {
	// Initialize SDL
	// SDL is being a pain in the arse and making us
	// initialize the video subsystem also, so events
	// will work. Polling works without it though.
	if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_JOYSTICK) != 0) {
        log_string(1,  "Could not initialize SDL");
        log_string(-1, "  Is your $DISPLAY set correctly?");
        log_string(-4, "<rant author=\"Jesse\">");
        log_string(-4, "  Argh SDL, wtf do I have to initialize the video subsystem?");
        log_string(-4, "  I spent hours trying to find out why it wouldn't work and");
        log_string(-4, "  I finally find that it wasn't working because my DISPLAY");
        log_string(-4, "  environment variable wasn't set correctly because I was");
        log_string(-4, "  working in screen. This is not a graphical APP, it doesn't");
        log_string(-4, "  need Xorg to run! But noooooo! SDL needs to have its video");
        log_string(-4, "  subsystem running in order to receive events. Change this!");
        log_string(-4, "</rant>");
        return 0;
    }
	// Initialize the joystick subsystem
	if (SDL_InitSubSystem(SDL_INIT_JOYSTICK) != 0) {
        log_string(1, "Could not initialize the SDL_joystick subsystem");
        return 0;
    }

	// Check for joysti1k
	if(SDL_NumJoysticks() == 0) {
		log_string(1, "No joysticks found");
		return 0;
	}

	// Open joystick
	if((joy = SDL_JoystickOpen(0)) == NULL ) {
		log_string(1, "Couldn't open Joystick 0");
		return 0;
	}
	
	// Check that the Joystick is indeed opened
	if(!SDL_JoystickOpened(0)) {
		log_string(1, "Joystick 0 hasn't been opened");
		return 0;
	}

	log_string(-1, "Opened Joystick 0");
	log_string(-1, "Name: %s", SDL_JoystickName(0));
	log_string(-1, "Number of Axes: %d", SDL_JoystickNumAxes(joy));
	log_string(-1, "Number of Buttons: %d", SDL_JoystickNumButtons(joy));
	log_string(-1, "Number of Balls: %d", SDL_JoystickNumBalls(joy));
	log_string(-1, "Number of Hats: %d", SDL_JoystickNumHats(joy));

	SDL_JoystickEventState(SDL_ENABLE);

	return 1;
}

// close_joy - closes the joystick opened in init_joy
// 	joy - the joystick returned by init_joy
int close_joy() {
	// Close if opened
	if((joy != NULL) && (SDL_JoystickOpened(0))) {
		SDL_JoystickClose(joy);
		return 1;
	}
	return 0;
}
