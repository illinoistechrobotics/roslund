//    joystick.h - interface for joystick handling
//    Copyright (C) 2007  Illinois Institute of Technology Robotics
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

#ifndef JOYSTICK_H
#define JOYSTICK_H

#include "robot_queue.h"

#define JOY_AXIS_CENTER 127
#define JOY_MAX_AXES 16
#define JOY_MAX_BUTTONS 16

extern int joy_thread_create(robot_queue *q);
extern int joy_thread_destroy(); 


#endif //!JOYSTICK_H
