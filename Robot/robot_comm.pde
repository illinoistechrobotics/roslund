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

  Serial.print(0x55,BYTE);                 //Start byte
  Serial.print(',');
  Serial.print(ev->command,HEX);               //command byte
  Serial.print(',');
  Serial.print(ev->index,HEX);                 //index byte
  Serial.print(',');
  Serial.print(ev->value,HEX);                 //vlaue two bytes
  Serial.print(',');
  //Serial.print((byte)(ev->value >> 8), BYTE);    //upper value byte
  Serial.println(checksum,HEX);                  //check sum

    return 1;
}

// xbee_recv_event - receive a robot comm datagram
// 	event - pointer to datagram to overwrite
// 	return - 0 on failure, no-zero otherwise

char buf[126] = "U,40,1,0,41\nU,40,1,0,41\nU,40,1,0,41\n"; //yes i know but each datagram can be up to 16 bytes 
int count = 0;
int start = 0;  
int newline = 0;

int xbee_recv_event(robot_queue *q){

  robot_event *ev;
  unsigned int checksum = 0;

//  while(Serial.available() > 0 && count < 125){
//    buf[count]=Serial.read();
//    count++;
//  }
  
  buf[39]= 'U';
  count = 39;
  //loop through to find 0x55 and \n
  for(int i=newline; i <= count; i++){
    if(buf[i] == 'U'){ //start byte
      start = i;
      break;
    }
  }

  for(int i=start+1; i <= count; i++){
    if(buf[i] == '\n'){
      newline = i;
      break;
    }
  }
  
  if(newline <= start){ //new line not found or found before the start byte
    if(count == 125){ //buf full and no vaild datagram found, clear the buffer
      //need to copy the suff after the start byte to begining 
      if(start == 0){ //found no start byte and buf full clear all of the buffer
        count = 0;    //no need to copy since the buf is grabage 
        start = 0;
        newline = 0;
      }
      else{ 
        memmove(&buf[0], &buf[start], count-start); //???????????????????? writing own will be more efficient 
        count = count-start;
        start = 0;
        newline = 0;
      }
    }
    return 0;
  }

  //found valid datagram
  //parse datagram comma delimited
  for(int i=start; i<newline; i++){
    Serial.print(buf[i], HEX);
    
  }
  Serial.print('\n');
  
  
  Serial.println("");
  start = 0;
  newline = 0;
  return 1;
  
  char *newbuf = &buf[start];
  char *temp;
  temp = strsep(&newbuf, "\n");
  newbuf = temp;
  temp = strsep(&newbuf, ",");
  xtoi(temp, &ev->command);
  temp = strsep(&newbuf, ",");
  xtoi(temp, &ev->index);
  temp = strsep(&newbuf, ",");
  xtoi(temp, &ev->value);
  temp = strsep(&newbuf, ",");
  xtoi(temp, &checksum);

  unsigned int checksum2 = (ev->command + ev->index + ev->value + byte(ev->value >> 8)) % 255;

  if(checksum2 == checksum){
    robot_queue_enqueue(q,ev);
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
    for(i=szlen-1; i>=0 ;i--)
    {
      if (isxdigit(*(xs+i)))
      {
        if (*(xs+i)>=97)
        {
          xv = ( *(xs+i) - 97) + 10;
        }
        else if ( *(xs+i) >= 65)
        {
          xv = (*(xs+i) - 65) + 10;
        }
        else
        {
          xv = *(xs+i) - 48;
        }
        *result += (xv * fact);
        fact *= 16;
      }
      else
      {
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

// same as above but for chars
int xtoi(const char* xs, unsigned char* result)
{
  size_t szlen = strlen(xs);
  int i, xv, fact;

  if (szlen > 0)
  {
    // Converting more than 8bit hexadecimal value?
    if (szlen>2) return 2; // exit

    // Begin conversion here
    *result = 0;
    fact = 1;

    // Run until no more character to convert
    for(i=szlen-1; i>=0 ;i--)
    {
      if (isxdigit(*(xs+i)))
      {
        if (*(xs+i)>=97)
        {
          xv = ( *(xs+i) - 97) + 10;
        }
        else if ( *(xs+i) >= 65)
        {
          xv = (*(xs+i) - 65) + 10;
        }
        else
        {
          xv = *(xs+i) - 48;
        }
        *result += (xv * fact);
        fact *= 16;
      }
      else
      {
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

