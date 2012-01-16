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

package genericRobot;


import java.util.*;

/**
 * Constants defined for the controller that can be changed during runtime by using setProfile/setValue
 * Useful if there are two different controller/other profiles that need to be changed on the file
 */
public enum Profile {
	CON_XAXIS	(0x00),
	CON_YAXIS	(0x01),
	CON_RAXIS	(0x02),
	CON_X1AXIS	(0x02),
	CON_Y1AXIS	(0x03),
	CON_TURBO1	(0x05),
	CON_TURBO2	(0x07),
	CON_ARM_UP	(0x04),
	CON_ARM_DOWN(0x06),
	CON_GRIP	(0x01),
	CON_FRONT	(0x02),
	CON_EXTRA	(0x08),
	CON_REAR	(0x03);

    private int value;
    
    private static final Map<Integer,Profile> lookup = new HashMap<Integer,Profile>();

    static {
    	for(Profile s : EnumSet.allOf(Profile.class))
         lookup.put(s.getValue(), s);
    }
    
    private Profile(int v){
    	this.value = v;
    }
    
    public void setValue(int v){
    	this.value = v;
    }
    
    public int getValue(){
    	return value;
    }
 
    public static Profile getProfile(int value){
    	return lookup.get(value);
    }
    
    public static void setProfile(int v){
    	switch(v){
    	case 1:
    	default:
    		CON_XAXIS.value 	= 0x00;
    		CON_YAXIS.value 	= 0x01;
    		CON_RAXIS.value 	= 0x02;
    		CON_X1AXIS.value	= 0x02;
    		CON_Y1AXIS.value	= 0x03;
    		CON_TURBO1.value 	= 0x05;
    		CON_TURBO2.value 	= 0x07;
    		CON_ARM_UP.value 	= 0x04;
    		CON_ARM_DOWN.value 	= 0x06;
    		CON_GRIP.value 		= 0x01;
    		CON_FRONT.value 	= 0x02;
    		CON_EXTRA.value 	= 0x08;
    		CON_REAR.value 		= 0x03;
    	}
    }
}
