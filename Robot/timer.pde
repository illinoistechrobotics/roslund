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

void timer(robot_queue *q) {
  static unsigned long  last_sent_1hz = 0;
  static unsigned long  last_sent_10hz = 0;
  static unsigned long  last_sent_100hz = 0;
  static unsigned int power_led_state = 0;
  
  // used for failsafe mode
  if(millis() - last_sent_10hz >= 100){    //10 hertz 100 millis
    robot_event ev1;
    ev1.command = ROBOT_EVENT_TIMER;
    ev1.index = 1;                       
    ev1.value = 0;
    last_sent_10hz = millis();
    robot_queue_enqueue(q, &ev1);    
#ifdef WATCHDOG_
    wdt_reset(); //watchdog timer reset 
#endif
    //flash led
#ifdef POWER_LED_
    power_led_state++;
    power_led_state&=B00000001; //more effecient than taking modolus
    digitalWrite(POWER_LED_PIN,power_led_state);
#endif
  }
  /* no point for use yet
  if(millis() - last_sent_1hz >= 1000){   //1 hertz 1000 millis
    robot_event ev2;
    ev2.command = ROBOT_EVENT_TIMER;
    ev2.index = 2;                       
    ev2.value = 0;
    last_sent_1hz= millis();
    robot_queue_enqueue(q, &ev2);
  }
  */
  /* no point for use yet
  if(millis() - last_sent_1hz >= 10){   //100 hertz 10 millis
    robot_event ev2;
    ev2.command = ROBOT_EVENT_TIMER;
    ev2.index = 3;                       
    ev2.value = 0;
    last_sent_100hz= millis();
    robot_queue_enqueue(q, &ev2);
  }
  */
  
}


