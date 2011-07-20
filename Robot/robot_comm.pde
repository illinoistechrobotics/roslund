//    robot_comm.c - network communication for robot events
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

int open_serial(int b) {
  Serial.begin(b);
}

// send_event - send a robot communication datagram
// 	cmd - command to send
// 	value - optional value associated with some commands
// 	return - 0 on failure, non-zero otherwise
int send_event(robot_event *ev) {

  byte checksum = (unsigned char)((ev->command + ev->index + ev->value + (ev->value >>8)) % 255);

  Serial.print(0xAA, BYTE);                      //Start byte
  Serial.print(ev->command, BYTE);               //command byte
  Serial.print(ev->index, BYTE);                 //index byte
  Serial.print((byte)(ev->value), BYTE);         //lower vlaue byte
  Serial.print((byte)(ev->value >> 8), BYTE);    //upper value byte
  Serial.print(checksum, BYTE);                  //check sum

  return 1;
}

// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise
int xbee_recv_event(robot_queue *q){

  robot_event *ev;
  int checksum = 0;

  while(Serial.available() >= 6){
    if (Serial.read() == 0xAA){
      break;
    }
  }
  if(Serial.available() >=5){
    ev->command = Serial.read();
    ev->index = Serial.read();
    ev->value = Serial.read() + (int)(Serial.read()) << 8;

    checksum = (ev->command + ev->index + ev->value + (byte)(ev->value >> 8)) % 255;

    if(checksum == Serial.read()){
      robot_queue_enqueue(q, ev);
      return 1;
    }
  }
  return 0;
}

