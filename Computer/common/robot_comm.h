//    robot_comm.h - interface for robot communications
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

#ifndef ROBOT_COMM_H
#define ROBOT_COMM_H

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "robot_queue.h"
#include "events.h"


extern int net_thread_server_create(robot_queue *q, unsigned short port);
extern int net_thread_client_create(robot_queue *q, char *hostname, unsigned short port);
extern int net_thread_destroy();

extern int xbee_thread_create(robot_queue *q, char *usbport, unsigned int b);
extern int xbee_thread_destroy();

// send_dgm - send a robot communication datagram
// 	dgm - pointer to the datagram to send
// 	return - 0 on failure, non-zero otherwise
extern int send_event(robot_event *ev);

extern int isXbee;

#endif //!ROBOT_COMM_H
