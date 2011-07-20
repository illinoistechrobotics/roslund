//    timer.c - generates timer events
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

#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <stdio.h>
#include "timer.h"
#include "events.h"
#include "robot_queue.h"

//---------------------------------------------------------------------------//
// Private Function Prototypes
//
static void *timer_thread_main(void *arg);

//---------------------------------------------------------------------------//
// Private Globals
//
static pthread_t tid = -1; // Thread ID of the timer thread

//---------------------------------------------------------------------------//
// Public Function Implementations
//

int timer_thread_create(robot_queue *q) {
	// create the thread
	if(pthread_create(&tid, NULL, timer_thread_main, (void*)q) != 0) {
		return 0;
	}
	return 1; // exit true

}

int timer_thread_destroy() {
	// kill the thread
	if (pthread_cancel(tid) != 0) {
		return 0;
	}
	// reap the thread
	if (pthread_join(tid, NULL) != 0) {
		return 0;
	}
	return 1;

}

//---------------------------------------------------------------------------//
// Private Function Implementations
//

void *timer_thread_main(void *arg) {
	robot_queue *q = (robot_queue*)arg;
	robot_event ev1;
	robot_event ev2;
	ev1.command = ROBOT_EVENT_TIMER;
	ev1.index = 1;
	ev1.value = 0;
	ev2.command = ROBOT_EVENT_TIMER;
	ev2.index = 2;
	ev2.value = 0;
	int i;
	while(1){
		for(i = 0; i < 9; i++) {
			robot_queue_enqueue(q, &ev1);
			usleep(100000);
		}
		robot_queue_enqueue(q, &ev2);
	}
	return 0;
}

