//    robot_comm.h - interface for robot communications
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

#ifndef ROBOT_COMM_H
#define ROBOT_COMM_H

#include "robot_queue.h"
#include "events.h"

// send_event - send a robot communication datagram
// 	event - pointer to the datagram to send
// 	return - 0 on failure, non-zero otherwise
extern int send_event(robot_event *ev);

// xbee_open - opens the serial port of the xbee for two way communication
//  return - -1 on failure
extern int open_xbee(char *usbport);

// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
extern int xbee_recv_event(robot_event *ev);

#endif //!ROBOT_COMM_H
