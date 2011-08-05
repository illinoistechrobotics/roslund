//    events.h - API description for end user functions
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

#ifndef EVENTS_H
#define EVENTS_H

typedef struct {
	unsigned char command; // command 
	unsigned char index; // eg the axis number or button number
	unsigned int value; // eg the axis value or button value
} robot_event;

enum {
    ROBOT_EVENT_CMD                 = 0x00, // Commands
    ROBOT_EVENT_NET                 = 0x10, // Remote information
    ROBOT_EVENT_JOY_AXIS            = 0x20, // Joystick movements
    ROBOT_EVENT_JOY_BUTTON          = 0x30, // Button presses
    ROBOT_EVENT_TIMER		    = 0x40, // Timer events
    ROBOT_EVENT_MOTOR	   	    = 0x50, // Motor events
    ROBOT_EVENT_ADC		    = 0x60, // ADC events
    ROBOT_EVENT_SET_VAR		    = 0x70, // Set variable events
    ROBOT_EVENT_READ_VAR	    = 0x80, // Read variable events

    ROBOT_EVENT_NET_STATUS_OK       = ROBOT_EVENT_NET | 0x00, // OK
    ROBOT_EVENT_NET_STATUS_ERR      = ROBOT_EVENT_NET | 0x01, // Error
    ROBOT_EVENT_NET_STATUS_NOTICE   = ROBOT_EVENT_NET | 0x02, // Notice

    ROBOT_EVENT_CMD_NOOP            = ROBOT_EVENT_CMD | 0x00, // No op
    ROBOT_EVENT_CMD_START           = ROBOT_EVENT_CMD | 0x01, // Start
    ROBOT_EVENT_CMD_STOP            = ROBOT_EVENT_CMD | 0x02, // Stop
    ROBOT_EVENT_CMD_REBOOT          = ROBOT_EVENT_CMD | 0x03, // Reboot
};

// The user should implement these

// on_init runs when the program starts up after the joystick and the network
// has been intitalized
extern void on_init();

// on_button_up is called when a joystick button is released
// button is the button number
extern void on_button_up(robot_event *ev);

// on_button_down is called when a joystick button is pressed
// button is the button number
extern void on_button_down(robot_event *ev);

//on_motor is called when a motor speed is updated
//index is motor number value is motor speed, 127 bias
extern void on_motor(robot_event *ev);

// on_axis_change is called when a joystick axis changes postion
// axis is the axis number, value is the position of the axis from 0-255
// a value of 127 is center
extern void on_axis_change(robot_event *ev);

// on_status_code is called when the remote computer sends a status datagram
extern void on_status_code(robot_event *ev);

// on_command_code is called when the remote computer sends a command datagram
extern void on_command_code(robot_event *ev);

// on_adc_change is called when the remote computer sends a ADC value
extern void on_adc_change(robot_event *ev);

// on_read_variable is called when the computer wants to read a robostix variable
extern void on_read_variable(robot_event *ev);

// on_set_variable is called when the computer wants to set a robostix variable
extern void on_set_variable(robot_event *ev);

// timer that runs each second
extern void on_1hz_timer(robot_event *ev);

// timer that runs each 100 milliseconds
extern void on_10hz_timer(robot_event *ev);

//  timer that runs each 10 millisenconds
extern void on_100hz_timer(robot_event *ev);
#endif // !EVENTS_H
