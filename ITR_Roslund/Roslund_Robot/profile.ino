//    profile.c - sets the profiles for the joystick and pin outs
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

// defines the digital pins if they are input or output
// input, output, 2 for rx tx pins 0,1, PWM pins 3,5,6,9,10,11 pins 5 and 6 share 
// with millis() and micros() so have diffrent pwm times then 3,9,10,11

//sets the controller and other constant variable profiles if more than one can exists
//feel free to change/remove/add profiles and variables 
//variable definitions are located in robot.h
void setProfile(char data){
  switch(data) {
  case 'a':
  default:
    CON_XAXIS 		= 0x00;
    CON_YAXIS 		= 0x01;
    CON_RAXIS 		= 0x02;
    CON_X1AXIS          = 0x02;
    CON_Y1AXIS          = 0x03;
    CON_TURBO1 		= 0x05;
    CON_TURBO2 		= 0x07;
    CON_ARM_UP 		= 0x04;
    CON_ARM_DOWN 	= 0x06;
    CON_GRIP 		= 0x01;
    CON_FRONT		= 0x02;
    CON_EXTRA		= 0x08; 
    CON_REAR	    	= 0x03;
    break;
  }
}

//set which pin is what and sets them as output or input
void setPinProfile(char data){
  switch(data){
  case 'a':
  default:
#ifdef POWER_LED_
    POWER_LED_PIN   = 13;    //Sets which pin should flash to indicate powered on 
    pinMode(POWER_LED_PIN, OUTPUT);  
#endif
    break;
  }
}
