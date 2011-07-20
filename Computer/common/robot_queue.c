//    robot-queue.c - implements the event queue on the robot
//    Copyright (C) 2007 Illinois Institute of Technology Robotics
//      <robotics@iit.edu>
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


// TODO:
// protect all functions that use sem_wait by blocking all signals, the user
// may want to use our functions within a signal handler. A deadlock will
// occur if the semaphore is already taken. Eg:
// Main Routine takes the semaphore
// Main routine is interrupted by a signal
// signal handler waits for main routine to release the semaphore
// ^^^^ This never happens so, the signal handler waits forever
//
// We need to block signals before we take, and unblock after we release
//
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>
#include <signal.h>
#include "events.h"
#include "robot_queue.h"

static int inc_tail_index(robot_queue *q);
static int inc_head_index(robot_queue *q);
static int lock (robot_queue *q);
static int unlock (robot_queue *q);

// robot_queue_create -- initiaizes a new queue
void robot_queue_create(robot_queue *q) {
	q->head_index = 0;
	q->tail_index = 0;
	q->length = 0;
	sem_init(&(q->lock), 0, 1);
}

// robot_queue_destroy -- frees resources created associated with a queue
void robot_queue_destroy(robot_queue *q) {
	sigset_t   signal_mask;  /* signals to block         */

	// List of signals to block
	sigemptyset (&signal_mask);
	sigaddset (&signal_mask, SIGINT);
	sigaddset (&signal_mask, SIGTERM);
	sigaddset (&signal_mask, SIGHUP);

	if (lock(q)) {
		sem_destroy(&(q->lock));
		// unblock signals
		pthread_sigmask(SIG_UNBLOCK, &signal_mask, NULL);
	}
	// don't post, the semaphore has been destroyed.
}

static int lock (robot_queue *q) {
	sigset_t   signal_mask;  /* signals to block         */

	// List of signals to block
	sigemptyset (&signal_mask);
	sigaddset (&signal_mask, SIGINT);
	sigaddset (&signal_mask, SIGTERM);
	sigaddset (&signal_mask, SIGHUP);
	// Block signals and lock the queue
	if (pthread_sigmask(SIG_BLOCK, &signal_mask, NULL) == 0) { 
		return !(sem_wait(&(q->lock)));
	} else {
		return 0;
	}
}

static int unlock (robot_queue *q) {
	sigset_t   signal_mask;  /* signals to block         */

	// List of signals to block
	sigemptyset (&signal_mask);
	sigaddset (&signal_mask, SIGINT);
	sigaddset (&signal_mask, SIGTERM);
	sigaddset (&signal_mask, SIGHUP);

	// Unlock the queue and unblock signals
	if (sem_post(&(q->lock)) == 0) {
		return !pthread_sigmask(SIG_UNBLOCK, &signal_mask, NULL);
	} else {
		return 0;
	}
}

// robot_queue_enqueue - adds an event to the back of the queue
int robot_queue_enqueue(robot_queue *q, const robot_event *const ev) {
	int tail_index, head_index, i;
	if (lock(q)) {
		head_index = q->head_index;
		tail_index = q->tail_index;
		i = head_index;
		if(ev->command == ROBOT_EVENT_MOTOR || ev->command == ROBOT_EVENT_JOY_AXIS){
			if(q->length){
				while(i != tail_index){
					if(q->array[i].command == ev->command && q->array[i].index == ev->index){
						q->array[i].value = ev->value;
						unlock(q);
						return 1;
					}
					i++;
					while(i >= QUEUE_SIZE){
						i -= QUEUE_SIZE;
					}
				}
			}
		}
		memcpy(q->array + tail_index, ev, sizeof(q->array[tail_index])); // copy

		inc_tail_index(q);
		unlock(q);
		return 1;
	} else {
		return 0;
	}
}

// robot_queue_dequeue - removes an event from the front of the queue
// 	this function populates the variable ev with the item removed
int robot_queue_dequeue(robot_queue *q, robot_event *ev) {
	int head_index;
	int ret = 0;

	if (lock(q)) {
		head_index = q->head_index;

		if(q->length > 0) {
			memcpy(ev, q->array + head_index, sizeof(q->array[head_index])); //copy
			inc_head_index(q); // remove the head event of the queue
			ret = 1;
		}
		if (unlock(q)) return ret;
		else return 0;
	} else {
		return 0;
	}

}
// alias to robot_queue_dequeue
int robot_queue_poll_event(robot_queue *q, robot_event *ev) {
	return robot_queue_dequeue(q, ev);
}

// waits for an event to occur from the event queue and then returns the event.
int robot_queue_wait_event(robot_queue *q, robot_event *ev) {
	while(!robot_queue_poll_event(q, ev)) {
		usleep(10000); // sleep for 10 microseconds
	}

	return 1;
}

// shows the size of the queue
int robot_queue_get_length(robot_queue *const q) {
	int len;
	if (lock(q)) {
		len = q->length;
		if (unlock(q)) return len;
		else return -1;
	} else {
		return -1;
	}
}

// inc_tail_index opens up a spot for an event to be added
// the return value is the index where the caller can place their
// new event
int inc_tail_index(robot_queue *q) {
	++(q->tail_index); // simple increment
	while(q->tail_index >= QUEUE_SIZE) { // loop around to the front of the
		q->tail_index -= QUEUE_SIZE ; // array if we overflow
	}

	++(q->length); // add to the length
	while(q->length > QUEUE_SIZE) { // Overwrite the oldest event if the
		inc_head_index(q);  // Queue is full
	}

	return q->tail_index;
}

// inc_head_index - removes an event from the heaqd of the queue
int inc_head_index(robot_queue *q) {
	if(q->length > 0) {
		++(q->head_index); // simple increment
		// loop around to the front if we overflow
		while(q->head_index >= QUEUE_SIZE) {
			q->head_index -= QUEUE_SIZE;
		}

		--(q->length); // decrement the length
	}

	return q->head_index;
}
