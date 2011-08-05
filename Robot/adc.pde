//    joystick.c - reads the joystick and calls appropraite events
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

//need to define ADC in adc.h
#ifdef ADC_

#define ADC_COUNT 1 //Number of ADC's to poll starting from ADC 0 to 5

void adc_read(robot_queue *q) {
  static unsigned long last_adc_read = 0;
  
  static int ADCVals[6] = {0,0,0,0,0,0};
  int inval;
  robot_event ev;
  int i;
  ev.command = ROBOT_EVENT_ADC;

  //Waits the polling interval, then polls all ADC's in use
  if(millis() - last_adc_read >= 20){ //50 Hertz
    for(i = 0; i < ADC_COUNT; i++) {
      inval = analogRead(i);
      if(inval != ADCVals[i]){ //only if different send event
        ADCVals[i] = (unsigned char)inval;
        ev.index = i;
        ev.value = ADCVals[i];
        robot_queue_enqueue(q, &ev);
      }
    }
  }
}
#endif

