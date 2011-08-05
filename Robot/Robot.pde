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

#include "robot_comm.h"
#include "events.h"
#include "robot_queue.h"

//#define WATCHDOG_      //To use a Watchdog timer (to use a watchdog timer, optiboot bootloader is needed)
#ifdef WATCHDOG_
#include <avr/wdt.h> //for watch dog
#endif

#define POWER_LED_
#ifdef POWER_LED_
#define POWER_LED_PIN 13     //Sets which pin should flash to indicate powered on 
#endif
//#define ADC_           //To use ADC code

#define BAUD 57600

robot_queue qu;
robot_event event;

void failsafe_mode(robot_queue *q);
int failcount;

void setup() {
  setProfile('s');
  setPinProfile('a');
  // create the queue
  robot_queue_create(&qu);
  // initialize the serial to the specified baud rate 
  open_serial(BAUD);
  
#ifdef WATCHDOG_
  wdt_enable(WDTO_250MS); //watch dog initalize 
  wdt_reset(); //watchdog timer reset 
#endif

  // init event
  on_init(&qu);
}

void loop() {

  // put your main code here, to run repeatedly: 
  xbee_recv_event(&qu);
  timer(&qu);
  if(robot_queue_dequeue(&qu, &event)){
    //send_event(&event);                  //for debug
    switch (event.command & 0xF0) {
    case ROBOT_EVENT_CMD:
      on_command_code(&event);
      break;
    case ROBOT_EVENT_NET:
      on_status_code(&event);
      break;
    case ROBOT_EVENT_JOY_AXIS:
      on_axis_change(&event);
      break;
    case ROBOT_EVENT_JOY_BUTTON:
      if(event.value)	
        on_button_down(&event);
      else
        on_button_up(&event);
      break;
    case ROBOT_EVENT_TIMER:
      if(event.index == 1){      // 10 hertz 100 millis
        on_10hz_timer(&event);
      }
      else if(event.index == 2){ // 1 hertz 1000 millis;
        on_1hz_timer(&event);
      }
      else if(event.index == 3){ // 100 hertz 10 millis
        on_100hz_timer(&event);
      }
      break;
    case ROBOT_EVENT_MOTOR:
      on_motor(&event);
      break;
    case ROBOT_EVENT_ADC:
      on_adc_change(&event);
      break;
    case ROBOT_EVENT_SET_VAR:
      on_set_variable(&event);   //for sending data in other than defined above
      break;
    case ROBOT_EVENT_READ_VAR:
      on_read_variable(&event);  //for sending data back to controller other than defined above
      break;
    default:
      break;  //not vaild command (maybe send error)????
    }
  }
#ifdef ADC_
  adc_read(&qu);
#endif

}

void failsafe_mode(robot_queue *q) {
  robot_event ev;
  ev.command = ROBOT_EVENT_MOTOR;
  ev.index = 0;
  ev.value = 127;
  int i;
  for(i = 0; i<5; i++){
    ev.index = i;
    robot_queue_enqueue(q, &ev);
  }
}

