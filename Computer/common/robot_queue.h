//    robot_queue.h - specification for the robot event queue
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

#ifndef ROBOT_QUEUE_H
#define ROBOT_QUEUE_H

#include <semaphore.h>
#include "events.h"

#define QUEUE_SIZE 128

typedef struct {
	robot_event array[QUEUE_SIZE];
	int head_index;
	int tail_index;
	int length;
    sem_t lock;
} robot_queue;

// robot_queue_create -- initiaizes a new queue
extern void robot_queue_create(robot_queue *q);

// robot_queue_destroy -- frees resources associated with a queue
extern void robot_queue_destroy(robot_queue *q);

// robot_queue_enqueue - adds an event to the back of the queue
extern int robot_queue_enqueue(robot_queue *q, const robot_event *const ev);

// robot_queue_dequeue - removes an event from the front of the queue
// 	this function populates the variable ev with the item removed
// 	if the queue is empty it returns 0, non-zero on success
extern int robot_queue_dequeue(robot_queue *q, robot_event *ev);

// robot_queue_poll_event - same as robot_queue_dequeue
extern int robot_queue_poll_event(robot_queue *q, robot_event *ev);

// robot_queue_wait_event - if there is at least one event in the queue
// then this function returns immediately. If the queue is empty it waits
// until an event gets enqueued. Then it dequeues that event, returning
// it to the caller
extern int robot_queue_wait_event(robot_queue *q, robot_event *ev);

// shows the size of the queue
extern int robot_queue_get_length(robot_queue *const q);

#endif // !ROBOT_QUEUE_H
