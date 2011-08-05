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

int open_serial(unsigned long b) {
  Serial.begin(b);
}

// send_event - send a robot communication datagram
// 	cmd - command to send
// 	value - optional value associated with some commands
// 	return - 0 on failure, non-zero otherwise
int send_event(robot_event *ev) {

  byte checksum = (unsigned char)((ev->command + ev->index + byte(ev->value) + byte(ev->value >>8)) % 255);

  Serial.print(0x55,BYTE);                     //Start byte
  Serial.print(',');
  Serial.print(ev->command,HEX);               //command byte
  Serial.print(',');
  Serial.print(ev->index,HEX);                 //index byte
  Serial.print(',');
  Serial.print(ev->value,HEX);                 //vlaue two bytes
  Serial.print(',');
  Serial.print(checksum,HEX);                  //check sum
  Serial.print('\n');                          //newline
  
  return 1;
}

#define BUF_SIZE 256
// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise

int xbee_recv_event(robot_queue *q){

  static char buf[BUF_SIZE + 1];
  static int count = 0;
  static int start = 0;  
  static int newline = 0;
  
  while(Serial.available() > 0 && count < BUF_SIZE){
      buf[count]=Serial.read();
      count++;
  }

  start = newline;
  //loop through to find 0x55 and \n
  for(int i=newline; i <= count; i++){
    if(buf[i] == 'U'){ //start byte
      start = i;
      break;
    }
  }

  for(int i=start+1; i <= count; i++){
    if(buf[i] == '\n'){ //end byte
      newline = i;
      break;
    }
  }

  if(newline <= start){ //new line not found or found before the start byte
    if(count == BUF_SIZE){ //buf full and no vaild datagram found, clear the buffer
      //need to copy the suff after the start byte to begining 
      if((count - start) <= 16){//if the last start byte is less than 16 away from end then copy array else no point
        memcpy(&buf[0], &buf[start], count-start); 
        count = count-start;
        start = 0;
        newline = 0;
      }
      else{ //buf full and last start byte is more than max packet size clear all of the buffer
        count = 0;    //no need to copy since the buf is grabage 
        start = 0;
        newline = 0;
      }
    }
    return 0;
  }

  //found valid datagram
  //parse datagram comma delimited

  char *newbuf = &buf[start];
  char *temp = newbuf;
  unsigned int data[5];
  buf[newline] = ',';

  int j=0;
  int i=0;

  do{
    i++;
    if(newbuf[i] == ','){
      newbuf[i] = '\0';
      if(j > 5){
        return 0;
      }
      xtoi(temp, &data[j]);
      j++;
      i++;
      temp = &newbuf[i];
    }
  }
  while(&newbuf[i-1] != &buf[newline]);

  unsigned int checksum = ((unsigned char)data[1] + (unsigned char)data[2] + byte(data[3]) + byte(data[3] >> 8)) % 255;

  if(checksum == data[4]){
    robot_event ev;
    ev.command = (unsigned char)data[1];
    ev.index = (unsigned char)data[2];
    ev.value = data[3];

    robot_queue_enqueue(q,&ev);
    return 1;
  }
  else{
    return 0;
  }
}

// Converts a hexadecimal string to integer
//  0    - Conversion is successful
//  1    - String is empty
//  2    - String has more than 8 bytes
//  4    - Conversion is in process but abnormally terminated by 
//         illegal hexadecimal character
// source: http://devpinoy.org/blogs/cvega/archive/2006/06/19/xtoi-hex-to-integer-c-function.aspx
int xtoi(const char* xs, unsigned int* result)
{
  size_t szlen = strlen(xs);
  int i, xv, fact;

  if (szlen > 0)
  {
    // Converting more than 16bit hexadecimal value?
    if (szlen>4) return 2; // exit

    // Begin conversion here
    *result = 0;
    fact = 1;

    // Run until no more character to convert
    for(i=szlen-1; i>=0 ;i--){
      if (isxdigit(*(xs+i))){
        if (*(xs+i)>=97){
          xv = ( *(xs+i) - 97) + 10;
        }
        else if ( *(xs+i) >= 65){
          xv = (*(xs+i) - 65) + 10;
        }
        else{
          xv = *(xs+i) - 48;
        }
        *result += (xv * fact);
        fact *= 16;
      }
      else{
        // Conversion was abnormally terminated
        // by non hexadecimal digit, hence
        // returning only the converted with
        // an error value 4 (illegal hex character)
        return 4;
      }
    }
  }
  // Nothing to convert
  return 1;
}

//returns 1 if valid hex value lower and upper case
//returns 0 if not
int isxdigit(char ch){
  if((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')){
    return 1;
  }
  else{
    return 0;
  }
}

