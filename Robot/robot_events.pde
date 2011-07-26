//    robot_events.c - User implementation of robot events
//    Copyright (C) 2011 Illinois Institute of Technology Robotics
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

// robot_events.c
//
// This file contains all the user defined code for events triggered by
// a change of state. On the robot side, a change of state occurs
// when a datagram is received that communicates an event on the
// controller's side.
//
// Local events are triggered on initialization, and shutdown
//
// See common/events.h for complete information on events.
//

void on_init(robot_queue *q) {
  robot_event ev;
  ev.command = ROBOT_EVENT_CMD_START;
  ev.index = 0;
  ev.value = 0;
  send_event(&ev);
  
  ev.command = ROBOT_EVENT_MOTOR;
  ev.index = 0;
  ev.value = 127;
  int i;
  for(i = 0; i<5; i++){
    ev.index = i;
    robot_queue_enqueue(q, &ev);
  }
  
}

void on_shutdown() {
  robot_event ev;
  ev.command = ROBOT_EVENT_CMD_STOP;
  ev.index = 0;
  ev.value = 0;
}

void on_button_up(robot_event *ev) {
  if(ev->index == CON_ARM_UP){
    digitalWrite(2,LOW);
  }
  if(ev->index == CON_ARM_DOWN){
    digitalWrite(4,LOW);
  }
}

int gripper = 0;
void on_button_down(robot_event *ev) {	

  if(ev->index == CON_ARM_UP){
    digitalWrite(2,HIGH);
  }
  if(ev->index == CON_ARM_DOWN){
    digitalWrite(4,HIGH);
  }
  if(ev->index == CON_GRIP){
    gripper = (gripper + 1) % 2;
    digitalWrite(7,gripper);
  }
}

void on_axis_change(robot_event *ev){
  
}

void on_adc_change(robot_event *ev){
  
}

#define MIN_MOTOR_SPEED 34
#define MAX_MOTOR_SPEED 154

void on_motor(robot_event *ev) {

  if(ev->index == 0){
    ev->value = map(ev->value, 0, 255, MIN_MOTOR_SPEED, MAX_MOTOR_SPEED);
    analogWrite(3, ev->value);
  }
  if(ev->index == 1){
    ev->value = map(ev->value, 0, 255, 64, 184); //due to pin broken
    analogWrite(6, ev->value);
  }
  if(ev->index == 2){
    ev->value = map(ev->value, 0, 255, MIN_MOTOR_SPEED, MAX_MOTOR_SPEED);
    analogWrite(10, ev->value);
  }
  if(ev->index == 3){
    ev->value = map(ev->value, 0, 255, MIN_MOTOR_SPEED, MAX_MOTOR_SPEED);
    analogWrite(11, ev->value);
  }
}

void on_status_code(robot_event *ev) {
  switch(ev->command) {
  case ROBOT_EVENT_NET_STATUS_OK:
    break;
  case ROBOT_EVENT_NET_STATUS_ERR:
    break;
  case ROBOT_EVENT_NET_STATUS_NOTICE:
    break;
  default:
    // unknown status datagram
    break;
  }

}

void on_1hz_timer(robot_event *ev){
}

void on_10hz_timer(robot_event *ev){
}

void on_command_code(robot_event *ev) {
  robot_event send_ev;
  switch(ev->command) {
  case ROBOT_EVENT_CMD_NOOP:
    break;
  case ROBOT_EVENT_CMD_START:
    break;
  case ROBOT_EVENT_CMD_STOP:
    break;
  case ROBOT_EVENT_CMD_REBOOT:
    break;
  default:
    // unknown command code datagram
    break;
  }
}


