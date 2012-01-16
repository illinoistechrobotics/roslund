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

//source: http://www.arduino.cc/playground/Interfacing/Java

package general;

import java.io.*;
import java.util.*;
import gnu.io.*;
import gui.Display;

@SuppressWarnings("rawtypes")
public class Communication implements SerialPortEventListener {
	
	private SerialPort serialPort = null;
	private InputStream input = null;
	private OutputStream output = null;
	private RobotQueue recv = null;
	private Display dis = null;
	
	public Communication(RobotQueue r){
		this.recv = r;
	}
	
	/**
	 * Opens the Serial port with the baud and port_name given
	 */
	public boolean OpenSerial(int baud, String port){
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		
		while (portEnum.hasMoreElements()){
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if(currPortId.getName().equals(port)){
				portId = currPortId;		
				break;
			}
		}
		
		if(portId == null){
			System.err.println("Can not open serial port");
			return false;
		}
		
		try{
			serialPort = (SerialPort) portId.open(this.getClass().getName(),2000);
			
			serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			Thread.sleep(1500);
		}
		catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	/**
	 * returns an array of all the serial ports available 
	 */
	public static CommPortIdentifier[] getSerialPorts(){
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		int i=0;
		while(portEnum.hasMoreElements()){
			i++;
			portEnum.nextElement();
		}
		
		CommPortIdentifier[] portId = new CommPortIdentifier[i];
		Enumeration portEnum2 = CommPortIdentifier.getPortIdentifiers();
		
		i=0;
		while (portEnum2.hasMoreElements()){
			portId[i] = (CommPortIdentifier) portEnum2.nextElement();
			i++;
		}
		return portId;
	}
		
	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void closeSerial() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. 
	 * Read the data and parse it into a Robot_Event and add to queue
	 */
	private final int BUF_SIZE = 1024;
	private byte[] buf = new byte[BUF_SIZE+1];
	private int count = 0;
	private int start = 0;
	private int newline = 0;
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {	
				int available = input.available();
				if(count < BUF_SIZE){
		            count = count + input.read(buf, count, (available > BUF_SIZE-count) ? BUF_SIZE-count : available);
		        }

		        start = newline;
		        for(int i=newline; i<=count; i++){
		            if(buf[i] == 'U' || buf[i] == '$'){ //start byte
		                start = i;
		                break;
		            }
		        }
		        for(int i=start; i<=count; i++){
		            if(buf[i] == '\n'){ //end byte
		                newline = i;
		                break;
		            }
		        }
		        
		        if(newline <= start){
		            if(count == BUF_SIZE){ //buf full an no valid datagram found, clear the buffer
		                //need to copy the stuff after the start byte to the beginning
		                if((count - start) <= 128){
		                    System.arraycopy(buf, start, buf, 0, count-start);
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
		            return;
		        }
		        
		        if(buf[start] == '$'){
		        	buf[newline] = '\0';
		        	System.out.println(new String(buf,start+1,newline-start));
		        }

		        //found valid datagram
		        //parse datagram comma delimited
		        buf[newline] = ',';
		        String newbuf = new String(buf, start, newline-start);
		        StringTokenizer st = new StringTokenizer(newbuf, ",");
		        int[] data = new int[5];

		        int i=0;
		        while(st.hasMoreElements() && i<5){
		        	String temp = st.nextToken();
		        	try{
		        		data[i] = Integer.parseInt(temp,16);
		        	}
		        	catch(NumberFormatException e){
		        	}
		        	i++;
		        }

		        RobotEvent ev = new RobotEvent();
		        int checksum;

		        ev.setCommand(EventEnum.getEvent(data[1]));
		        ev.setIndex((short)data[2]);
		        ev.setValue((int)data[3]);
		        checksum = data[4];

				int checksum2 = (int)(((int)ev.getCommand().getValue()&0xFF + (int)ev.getIndex()&0xFF + (int)(ev.getValue() & 0x00FF) + (int)((ev.getValue()&0xFF00) >> 8)) % 255);

		        if(checksum2 == checksum){
		            //log event received
		            recv.put(ev);
		        }
			} 
			catch (Exception e) {
			}
		}
	}
	
	
	public synchronized void sendEvent(RobotEvent ev){
		try{
			output.write(ev.toStringSend().getBytes());   //write needs a byte array instead of a string
			long milli = (long)(1.0/((float)serialPort.getBaudRate()/(8.0*16.0))*1000.0);
			long nano = (long)(1.0/((float)serialPort.getBaudRate()/(8.0*16.0))*1000000000.0) - milli*1000000;
			Thread.sleep((int)milli,(int)nano);
		}
		catch(Exception e){
		}
		
	}
	
	public void setDisplay(Display d){
		this.dis = d;
	}
	
	public void sendStatus(){
		sendEvent(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_NOOP,(short)0,0));
	}
	
	private int robotConnect = 0;
	public void checkStatus(){
		robotConnect++;
		if(robotConnect > 2){
			robotConnect = 3;
			recv.put(new RobotEvent(EventEnum.ROBOT_EVENT_NET_STATUS_ERR,(short)0,0));
			dis.changeRobotStatus(1);
		}
	}
	
	public void okStatus(){
		robotConnect = 0;
	    dis.changeRobotStatus(0);
	}
}
