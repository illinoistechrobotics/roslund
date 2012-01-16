//    robot.c - receives commands, and moves motors.
//    Roslund
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

//for faster serial communication use the FastSerial library
//fast serial isn't included directly since each person need to copy files into the arduino lbrary folder
//will find link soon. it is located in the ardupiolt mega libary file somewhere 
#include "robot.h"  //check the robot.h file to set configuration such as baud rate, 

/*********************************
 Arduino Xbee(AB91)   Computer Xbee (AB92)
 CH    C              CH    C
 ID    1337           ID    1337
 DH    0              DH    0
 DL    0              DL    0
 MY    0              MY    0
 D3    5              D3    3
 IU    0              IU    (not set)
 IA    FFFF           IA    (not set)
 R0    10             R0    10
 BD    6              BD    6
 IC    (not set)      IC    8
 RR    (not set)      RR    3                   
*/

// All robot specific code should be placed in the events file and other files "user" created
// Try not to change any code with files marked with a z. Those are crutial files and
// do not need to be changed. Change them only if you are familler with the code. 

robot_queue qu;
robot_event event;

void setup() {
  setProfile('a');
  setPinProfile('a');
  // create the queue
  robot_queue_create(&qu);
  // initialize the serial to the specified baud rate and sets up the xbee in profile.h
  open_serial(BAUD);
  
#ifdef WATCHDOG_
  wdt_enable(WDTO_250MS);  //watch dog initalize timer needs to be reset every 250 ms done with 10Hz timer 
  wdt_reset();             //watchdog timer reset 
#endif

  // init event
  on_init(&qu);
}

void loop() {
  xbee_recv_event(&qu);
  timer(&qu);
  //place other function that should be called very loop cycle
  //most adcs can't update more often than 100hz anyway so you can place that in the on_100hz_timer
  //if you use the on_100hz_timer don't forget to uncomment it from timer or else it will never be called
  //also don't assume 100hz timer will run at 100 hz. it will almost run slower than that but not by much
  //so if you are using something with time don't assume 10 milliseconds was the last time since it was 
  //called keep track of time with your own variables
  if(robot_queue_dequeue(&qu, &event)){
    switch (event.command) {
    case ROBOT_EVENT_JOY_AXIS:
      on_axis_change(&event);
      break;
    case ROBOT_EVENT_JOY_BUTTON:
      if(event.value)	
        on_button_down(&event);
      else
        on_button_up(&event);
      break;
    //case ROBOT_EVENT_TIMER:
      //on_1hz_timer,on_10hz_timer,...,on_100hz_timer are called directly from timer 
    //  break;
    case ROBOT_EVENT_MOTOR:
      on_motor(&event);
      break;
    case ROBOT_EVENT_SET_VAR:
      on_set_variable(&event);   //for sending data in other than defined above
      break;
    case ROBOT_EVENT_READ_VAR:
      on_read_variable(&event);  //for sending data back to controller other than defined above
      break;
    case ROBOT_EVENT_CMD:
    case ROBOT_EVENT_CMD_NOOP:
    case ROBOT_EVENT_CMD_START:
    case ROBOT_EVENT_CMD_STOP:
    case ROBOT_EVENT_CMD_REBOOT:
    case ROBOT_EVENT_CMD_SHUTDOWN:
      on_command_code(&event);
      break;
    case ROBOT_EVENT_NET:
    case ROBOT_EVENT_NET_STATUS_OK:
    case ROBOT_EVENT_NET_STATUS_ERR:
    case ROBOT_EVENT_NET_STATUS_NOTICE:
      on_status_code(&event);
      break;
    default:
      break;  //not vaild command (maybe send error)????
    }
  }
}

//Place code here to put the robot in a safe state(i.e stopping the motors ..)
//failsafe_mode will run at 10hz untill communications is restored 
void failsafe_mode(robot_queue *q) {
  failsafeMode = true; 
}

