//    robot_comm.c - network communication for robot events
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

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <fcntl.h>
#include <semaphore.h>
#include "robot_comm.h"
#include "robot_queue.h"
#include "robot_log.h"
#include "events.h"

// These values should be read-only after the thread has started
// Therefore we do not need to lock them
static int sockfd = -1; // socket file descriptor
static pthread_t tid = 0; // thread id for the network thread
static int client_mode = -1; // boolean if we are in client mode or not
static struct sockaddr_in server; // server machine
// client can be overwritten, when the code is run as a server, so, we ned to
// protect it using a semaphore
static struct sockaddr_in client; // client machine
static sem_t sem_client; // client semaphore


// xbee
static int xbeefd = -1; //xbee file descriptor
static pthread_t xtid = 0; //xbee thread id
static sem_t sem_xbee;
//------------------------------------------------------------------------------
// Local function prototypes

void *net_thread_main(void *arg);

// open_udp_server - binds to a UDP port
// 	port - port to bind to
// 	returns - a file descriptor to the socket
// 		or a number < 0 on error
static int open_udp_server(unsigned short port);

// open_udp_client - opens a UDP connection to a server
// 	hostname - domain name or dotted quad ip
// 	port - udp port to connect to
//
// 	returns - a file descriptor int to the socket or a number < 0 on error
// 		  server - a sockaddr_in struct for use in calls to sendto()
//
static int open_udp_client(char *hostname, unsigned short port);

// recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
static int recv_event(robot_event *ev);

// close_udp - closes the socket
// 	return - 0 on failure, non-zero otherwise
static int close_udp();

// log_event family of function logs a robot datagram using log_string from
// robot_log.
void log_event_received(robot_event *ev);
void log_event_sent(robot_event *ev);
void log_event(robot_event *ev, char was_received);

// xbee main loop
void *xbee_thread_main(void *args);

// xbee_open - opens the serial port of the xbee for two way communication
//  usbport is the /dev/ttyUSB_ given in the command argument -x /dev/ttyUSB0
//  return - -1 on failure, and the fd if successful
static int open_xbee(char *usbport);

// xbee_close - closes the serial port
// return 0 on failure, non-zero otherwise
static int close_xbee();

// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
static int xbee_recv_event(robot_event *ev);

// xbee_recv_event helper function
ssize_t rio_readn(int fd, void *usrbuf, size_t n); 
	
//------------------------------------------------------------------------------
// Function Implementation

int xbee_thread_create(robot_queue *q, char *usbport){
    // initialize the semaphores
    sem_init(&sem_xbee, 0, 1);
    
    // open up the port
    if (open_xbee(usbport) < 0) {
        return 0;
    }
    // create the thread
//    if(pthread_create(&xtid, NULL, xbee_thread_main, q) != 0) {
//        return 0;
//    }
    return 1;
	
}   

int xbee_thread_destroy(){
    if (xtid <= 0) {
        return 0;
    }

	// kill the thread
	if (pthread_cancel(xtid) != 0) {
		return 0;
	}
	// reap the thread
	if (pthread_join(xtid, NULL) != 0) {
		return 0;
	}
	xtid = -1;
	// close the socket
	return close_xbee();

}


int open_xbee(char *usbport) {
	//xbeefd = open(usbport, O_RDWR, 0); // for debug purpose can open a file
    xbeefd = open(usbport, O_RDWR | O_NDELAY | O_ASYNC | O_NOCTTY | O_NONBLOCK, 0);
	if (xbeefd < 0)
	{
		log_errno(1, "Error opening xbee USB port");
		return -1;
	}    
	return xbeefd;
	
}

int close_xbee(){
	int return_code; 
	return_code = !close(xbeefd); // close the file descriptor

	xbeefd = -1;
	return return_code;
	
}

void *xbee_thread_main(void *args){
    robot_queue *q = (robot_queue *)args;
    robot_event ev;

	while(1) {
		xbee_recv_event(&ev);
        robot_queue_enqueue(q, &ev);
		
	}
	
}

int net_thread_server_create(robot_queue *q, unsigned short port) {
	// initialize the semaphores
	sem_init(&sem_client, 0, 1);

	// open up the port
	if (open_udp_server(port) < 0) {
		return 0;
	}
	// create the thread
	if(pthread_create(&tid, NULL, net_thread_main, q) != 0) {
		return 0;
	}
	return 1; // exit true

}

int net_thread_client_create(robot_queue *q, char *hostname, unsigned short port) {
	// initialize the semaphores
	sem_init(&sem_client, 0, 1);

	// open the port
	if (open_udp_client(hostname, port) < 0) {
		return 0;
	}
	// create the thread
	if(pthread_create(&tid, NULL, net_thread_main, q) != 0) {
		return 0;
	}
	return 1; // exit true

}

int net_thread_destroy() {
    if (tid <= 0) {
        return 0;
    }

	// kill the thread
	if (pthread_cancel(tid) != 0) {
		return 0;
	}
	// reap the thread
	if (pthread_join(tid, NULL) != 0) {
		return 0;
	}
	tid = -1;
	// close the socket
	return close_udp();

}

void *net_thread_main(void *arg) {
    robot_queue *q = (robot_queue *)arg;
    robot_event ev;

	while(1) {
		recv_event(&ev);
        robot_queue_enqueue(q, &ev);
	}
}

// open_udp_server - binds to a UDP port
// 	port - port to bind to
// 	returns - a file descriptor to the socket
// 		or a number < 0 on error
int open_udp_server(unsigned short port) {
	// close an already open socket
	if(sockfd >= 0) {
		close(sockfd);
	}

	// open a new socket
	if((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
		log_errno(1, "Error opening socket");
		return -1;
	}

	// bind to the socket
	bzero(&server, sizeof(server)); // make a clean memory area
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = htonl(INADDR_ANY); // listen on any address
	server.sin_port = htons(port); // on the given port
	if (bind(sockfd, (struct sockaddr *)&server, sizeof(server)) < 0) {
		log_errno(1, "Error binding to socket on port %d", port);
		return -1;
	}

	// zero the client structure
	bzero(&client, sizeof(client));
	
	client_mode = 0;
	return sockfd;
}

// open_udp_client - opens a UDP connection to a server
// 	hostname - domain name or dotted quad ip
// 	port - udp port to connect to
//
// 	returns - a file descriptor int to the socket or a number < 0 on error
//
int open_udp_client(char *hostname, unsigned short port) {
	struct hostent *server_attr; // attributes returned by gethostbyname

	// close an already open socket
	if(sockfd >= 0) {
		close(sockfd);
	}

	// open a new socket
	if((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
		log_errno(1, "Error opening socket");
		return -1;
	}

	// get the server's address
	if ((server_attr = gethostbyname(hostname)) == NULL) {
		log_dns_error(1, "Error, no such host \"%s\"", hostname);
		return -1;
	}
	bzero(&server, sizeof(struct sockaddr)); // make a clean memory area
	server.sin_family = AF_INET;
	bcopy(server_attr->h_addr_list[0], // connect to the given host
			&server.sin_addr.s_addr, server_attr->h_length);
	server.sin_port = htons(port); // on the given port

	client_mode = 1;
	return sockfd;
}

// send_event - send a robot communication datagram
// 	cmd - command to send
// 	value - optional value associated with some commands
// 	return - 0 on failure, non-zero otherwise
int send_event(robot_event *ev) {
#ifdef Xbee	
	if(xbeefd < 0) {
		return 0;
	}
		
	unsigned char checksum = (unsigned char)((ev->command + ev->index + ev->value + (ev->value >>8)) % 255);
	char temp[30];    //these are large to prevent overflow
    char data[100];
    char *comma = ",";

    data[0] = 'U';
    data[1] = '\0';
    strcat(data, comma);
    sprintf(temp, "%X", (int)ev->command);
    strcat(data, temp);
    strcat(data, comma);
    sprintf(temp, "%X", (int)ev->index);
    strcat(data, temp);
    strcat(data, comma);
    sprintf(temp, "%X", (int)ev->value);
    strcat(data, temp);
    strcat(data, comma);
    sprintf(temp, "%X", (int)checksum);
    strcat(data, temp);
    strcat(data, "\n");

    if (write(xbeefd, data, strlen(data)) < 0)
	{
		log_errno(0, "Error sending on xbee");
		return 0;
	}
    usleep(2800);  //based on 57600 baud need to make more flexiable
	
	return 1;
#else
	struct sockaddr_in remote;

	if(client_mode) {
		remote = server;
	} else {
		sem_wait(&sem_client);
		remote = client;
		sem_post(&sem_client);
	}

	if(sockfd < 0) {
		return 0;
	}
	if (remote.sin_addr.s_addr == 0) {
		log_errno(-1, "Unknown remote host, no clients have connected yet.");
		return 0;
	}

	if(sendto(sockfd, ev, sizeof(robot_event), 0, (struct sockaddr *)&remote , sizeof(remote)) < 0) {
		log_errno(0, "Error sending on socket.");
		return 0;
	}

	log_event_sent(ev);

	return 1;
#endif		

}

// recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
int recv_event(robot_event *ev) {
	struct sockaddr_in remote;
	socklen_t remotelen;

	remotelen = sizeof(remote); // needs to be initialized

	if(sockfd < 0) {
		return 0;
	}

	// wait until we receive a packet
	if (recvfrom(sockfd, ev, sizeof(robot_event), 0, (struct sockaddr *)&remote, &remotelen)  < 0) {
		return 0;
	} else {
		log_event_received(ev); // log it
		if(!client_mode) { // server mode - we don't know who our controller is, so set the remote
				   // machine to the last person who wrote us something.
			sem_wait(&sem_client);
			client = remote;
			sem_post(&sem_client);
		} // otherwise do nothing with remote
	}
	return 1;

}

// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
#define xbee_read_size (sizeof(robot_event)+1)  //??????????????????????this might not be needed
int xbee_recv_event(robot_event *ev){
	
	char buf[126];
	int readn = 0;
    int count = 0;
	int temp = 0;
    int i;
    int start = 0;
    int newline = 0;
	fd_set rfds;

    FD_ZERO(&rfds);
    FD_SET(xbeefd,&rfds);

    while(1){
        if(select(xbeefd + 1, &rfds, NULL, NULL, NULL)){
            if(count < 125){
                count = count + read(xbeefd, &buf[count], 126-count);
            }
            for(i=0; i<=count; i++){
                if(buf[i] == 0x55){
                    start = count;
                    break;
                }
            }
            for(i=start; i<=count; i++){
                if(buf[i] == '\n'){
                    newline = count;
                    break;
                }
            }
            if(newline <= start){
                if(count == 125){
                    if(start == 0){
                        count = 0;
                        start = 0;
                        newline = 0;
                    }
                    else{
                        memmove(&buf[0], &buf[start], count-start);
                        count = count-start;
                        start = 0;
                        newline = 0;
                    }
                }
            }
            else{
                char newbuf[126];
                memcpy(&newbuf[0], &buf[start], newline-start);
                newbuf[newline-start] = '\0';
                printf("%s",newbuf);
                fflush(stdout);
                count = 0;
            }
        }
    }

    //???????????????????????????????????
    //this needs to be updated for ascci charactures 
    //???????????????????????????????????
	while(readn < xbee_read_size){
		temp = rio_readn(xbeefd, buf + readn, xbee_read_size - readn);
		if(temp < 0){
			return 0;
		}
		else{
			readn =+ temp;
		}
	}
	if (((buf[0] + buf[1] + buf[2] + buf[3]) % 255) == buf[4]){
		ev->command = buf[0];
		ev->index = buf[1];
		ev->value = (buf[3] << 8) + buf[2];
		log_event_received(ev);
	}
    
	return 1;
	
}

/*
 * rio_readn - robustly read n bytes (unbuffered)
 * helper function for xbee_recv_event buffered serial port 
 */
/* $begin rio_readn */
//???????????????????????????????????????????
//may need to be changed 
//???????????????????????????????????????????
ssize_t rio_readn(int fd, void *usrbuf, size_t n) 
{
    size_t nleft = n;
    ssize_t nread;
    char *bufp = usrbuf;

    while (nleft > 0) {
	if ((nread = read(fd, bufp, nleft)) < 0) {
		return -1;      /* errno set by read() */ 
	} 
	else if (nread == 0)
	    break;              /* EOF */
	nleft -= nread;
	bufp += nread;
    }
    return (n - nleft);         /* return >= 0 */
}
/* $end rio_readn */


// close_udp - closes the socket
// 	return - 0 on failure, non-zero otherwise
int close_udp() {
	int return_code; 
	return_code = !close(sockfd); // close the file descriptor

	client_mode = -1; // make client_mode undefined
	sockfd = -1;
	return return_code;
}

void log_event_received(robot_event *ev) {
	log_event(ev, 1);
}
void log_event_sent(robot_event *ev) {
	log_event(ev, 0);
}
void log_event(robot_event *ev, char was_received) {
	const char *const server_str = "ROBOT";
	const char *const client_str = "CNTLR";
	const char *const sent_str = " --->";
	const char *const receive_str = " <---";
	char buffer[80]; // string to strncat into
	char numberbuffer[20]; // string to snprintf into

	if(log_level >= 0) {
		return;
	}

	// initialize buffer to an empty string
	*buffer = '\0';

	// insert client_str or server_str
	if(client_mode) {
		strncat(buffer, client_str, sizeof(buffer));
	} else {
		strncat(buffer, server_str, sizeof(buffer));
	}

	// insert sent or received
	if(was_received) {
		strncat(buffer, receive_str, sizeof(buffer));
	} else {
		strncat(buffer, sent_str, sizeof(buffer));
	}


	// insert the command
	switch(ev->command) {
		case ROBOT_EVENT_NET_STATUS_OK:
			strncat(buffer, " STATUS_OK", sizeof(buffer));
			break;
		case ROBOT_EVENT_NET_STATUS_ERR:
			strncat(buffer, " STATUS_ERR", sizeof(buffer));
			break;
		case ROBOT_EVENT_NET_STATUS_NOTICE:
			strncat(buffer, " STATUS_NOTICE", sizeof(buffer));
			break;
		case ROBOT_EVENT_CMD_NOOP:
			strncat(buffer, " CMD_NOOP", sizeof(buffer));
			break;
		case ROBOT_EVENT_CMD_START:
			strncat(buffer, " CMD_START", sizeof(buffer));
			break;
		case ROBOT_EVENT_CMD_STOP:
			strncat(buffer, " CMD_STOP", sizeof(buffer));
			break;
		case ROBOT_EVENT_CMD_REBOOT:
			strncat(buffer, " CMD_REBOOT", sizeof(buffer));
			break;
		default:
			if(ev->command == ROBOT_EVENT_JOY_AXIS) { // it's an axis
				snprintf(numberbuffer, sizeof(numberbuffer), " AXIS_%02X", ev->index);
					strncat(buffer, numberbuffer, sizeof(buffer));
			} else if (ev->command == ROBOT_EVENT_JOY_BUTTON) { // it's a button
				snprintf(numberbuffer, sizeof(numberbuffer), " BUTTON_%02X", ev->index);
					strncat(buffer, numberbuffer, sizeof(buffer));
			} else if (ev->command == ROBOT_EVENT_MOTOR) { // its a motor
				snprintf(numberbuffer, sizeof(numberbuffer), " Motor_%02X", ev->index);
					strncat(buffer, numberbuffer, sizeof(buffer));
			} else { // unknown
				snprintf(numberbuffer, sizeof(numberbuffer), " %02X", ev->command);
					strncat(buffer, numberbuffer, sizeof(buffer));
			}
			break;
	}

	// insert the value
	snprintf(numberbuffer, sizeof(numberbuffer), ":%02X", ev->value);
	strncat(buffer, numberbuffer, sizeof(buffer));

	// log our formatted string
	log_string(-1, buffer);
}
