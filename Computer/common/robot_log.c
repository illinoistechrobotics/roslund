//    robot_log.c - logging facilities for the robot
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

#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <errno.h>
#include <netdb.h>
#include "robot_log.h"

int log_level = 0;

//-----------------------------------------------------------------------------
// Public functions
//
void log_string(int level, char *format, ...) {
	va_list argp;

	if (log_level <= level) {

		va_start(argp, format);
		vfprintf(stderr, format, argp);
		va_end(argp);

		fprintf(stderr, "\n");
	}
}

void log_errno(int level, char *format, ...) {
	va_list argp;

	if (log_level <= level) {

		va_start(argp, format);
		vfprintf(stderr, format, argp);
		va_end(argp);

		fprintf(stderr, ": %s\n", strerror(errno));
	}
}

void log_dns_error(int level, char *format, ...) {
	va_list argp;

	if (log_level <= level) {

		va_start(argp, format);
		vfprintf(stderr, format, argp);
		va_end(argp);


		fprintf(stderr, ": DNS error %d\n", h_errno);
	}
}
