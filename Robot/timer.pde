//    timer.c - generates timer events
//    Copyright (C) 2011  Illinois Institute of Technology Robotics
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

unsigned long  last_sent_10hz = 0;
unsigned long  last_sent_1hz = 0;

void timer(robot_queue *q) {
  // used for failsafe mode
  if(millis() - last_sent_10hz >= 100){    //10 hertz 100 millis
    robot_event ev1;
    ev1.command = ROBOT_EVENT_TIMER;
    ev1.index = 3;                       
    ev1.value = 0;
    last_sent_10hz = millis();
    robot_queue_enqueue(q, &ev1);
  }
  /* no point for use yet
  if(millis() - last_sent_1hz >= 1000){   //1 hertz 1000 millis
    robot_event ev2;
    ev2.command = ROBOT_EVENT_TIMER;
    ev2.index = 4;                       
    ev2.value = 0;
    last_sent_1hz= millis();
    robot_queue_enqueue(q, &ev2);
  }
  */
}


