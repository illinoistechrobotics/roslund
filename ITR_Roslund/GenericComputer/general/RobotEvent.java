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

package general;

public class RobotEvent {

	private EventEnum command = null;
	private short index = 0;
	private int value = 0;
	
	/**
	 * One new Robot_Event object for each event
	 * since java doesn't support unsigned types the values are larger than they should be
	 * actual values that are used on the arduino and will be sent are
	 * (may add node 1 byte sender 1 byte receiver)
	 * command 1 byte
	 * index   1 byte
	 * value   2 bytes
	 */
	public RobotEvent(){
	}
	
	public RobotEvent(EventEnum c, short i, int v){
		this.command = c;
		this.index = i;
		this.value = v;
	}
	
	public EventEnum getCommand(){
		return command;
	}
	public short getIndex(){
		return index;
	}
	public int getValue(){
		return value;
	}
	
	public void setCommand(EventEnum c){
		this.command = c;
	}
	public void setIndex(short i){
		this.index = i;
	}
	public void setValue(int v){
		this.value = v;
	}
	
	/**
	 * Is the packet compiled that is sent over the serial port
	 * U,(command),(index),(value),(checksum)\n
	 * checksum is the (command+index+highbyteValue+lowbyteValue) % 255
	 * All values are sent as Hex values comma delimited with a newline terminated
	 * Start byte is U binary(01010101)
	 */
	public String toStringSend(){
		int checksum = (int)(((int)command.getValue()&0xFF + (int)index&0xFF + (int)(value & 0x00FF) + (int)((value&0xFF00) >> 8)) % 255);
		StringBuffer st = new StringBuffer("U,");
		st.append(Integer.toString(command.getValue()&0xFF,16));
		st.append(",");
		st.append(Integer.toString(index&0xFF, 16));
		st.append(",");
		st.append(Integer.toString(value&0xFFFF,16));
		st.append(",");
		st.append(Integer.toString(checksum&0xFF,16));
		st.append("\n");
		return st.toString();
	}
	
	public String toString(){
		return command + "," + index + "," + value;
	}
	
	public RobotEvent copy(){
		return new RobotEvent(this.command,this.index,this.value);
	}
	
}
