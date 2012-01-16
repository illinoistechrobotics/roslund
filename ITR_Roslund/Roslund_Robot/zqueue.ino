//    robot-queue.c - implements the event queue on the robot
//    Copyright (C) 2011 Illinois Institute of Technology Robotics
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

// robot_queue_create -- initiaizes a new queue
void robot_queue_create(robot_queue *q) {
  q->head_index = 0;
  q->tail_index = 0;
  q->length = 0;
}

// robot_queue_enqueue - adds an event to the back of the queue
int robot_queue_enqueue(robot_queue *q, const robot_event *const ev) {
  int tail_index, head_index, i;
  head_index = q->head_index;
  tail_index = q->tail_index;
  i = head_index;
  // used to speed up joy event and motor event
  // if same joy or motor event exist in queue replae the value
  // otherwise very slow response time and lag
  if(ev->command == ROBOT_EVENT_MOTOR || ev->command == ROBOT_EVENT_JOY_AXIS){        
    if(q->length){                                                                    
      while(i != tail_index){
        if(q->array[i].command == ev->command && q->array[i].index == ev->index){
          q->array[i].value = ev->value;
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
  return 1; 
}

// robot_queue_dequeue - removes an event from the front of the queue
// 	this function populates the variable ev with the item removed
// 	if the queue is empty it returns 0, non-zero on success
int robot_queue_dequeue(robot_queue *q, robot_event *ev) {
  int head_index;
  int ret = 0;

  head_index = q->head_index;
  if(q->length > 0) {
    memcpy(ev, q->array + head_index, sizeof(q->array[head_index])); //copy
    inc_head_index(q); // remove the head event of the queue
    ret = 1;
  }
  return ret;
}
// alias to robot_queue_dequeue
int robot_queue_poll_event(robot_queue *q, robot_event *ev) {
  return robot_queue_dequeue(q, ev);
}

// shows the size of the queue
int robot_queue_get_length(robot_queue *const q) {
  return q->length;
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

