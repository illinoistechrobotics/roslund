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

import java.util.*;

public enum EventEnum {
    ROBOT_EVENT_CMD                 (0x00), // Commands 
    ROBOT_EVENT_CMD_NOOP			(0x01), 
    ROBOT_EVENT_CMD_START			(0x02),
    ROBOT_EVENT_CMD_STOP			(0x03),
    ROBOT_EVENT_CMD_REBOOT			(0x04),
    ROBOT_EVENT_CMD_SHUTDOWN		(0x05),
    ROBOT_EVENT_CMD_FAILSAFE		(0x06),
    
    ROBOT_EVENT_NET                 (0x10), // Remote information
    ROBOT_EVENT_NET_STATUS_OK		(0x11), 
    ROBOT_EVENT_NET_STATUS_ERR		(0x12),
    ROBOT_EVENT_NET_STATUS_NOTICE	(0x13),
    
    ROBOT_EVENT_JOY_AXIS            (0x20), // Joystick movements
    ROBOT_EVENT_JOY_BUTTON          (0x30), // Button presses
    ROBOT_EVENT_JOY_HAT				(0x31), // D-pad pressed
    ROBOT_EVENT_JOY_STATUS			(0x32), // Joystick status
    
    ROBOT_EVENT_TIMER               (0x40), // Timer events
    ROBOT_EVENT_MOTOR               (0x50), // Motor events
    ROBOT_EVENT_ADC                 (0x60), // ADC events
    ROBOT_EVENT_SET_VAR             (0x70), // Set variable events
    ROBOT_EVENT_READ_VAR            (0x80), // Read variable events
	ROBOT_EVENT_VAR					(0x90); // Send variable
    
    // Feel free to add more commands but set different values. Try to do it with the available commands first 
    // Don't remove events please 
    
    private int value;
    
    /**
     * This is for reverse lookup 
     * If you have a value it will return the Enum
     */
    private static final Map<Integer,EventEnum> lookup = new HashMap<Integer,EventEnum>();
    static {
    	for(EventEnum s : EnumSet.allOf(EventEnum.class))
         lookup.put(s.getValue(), s);
    }
    public static EventEnum getEvent(int value){
    	return lookup.get(value);
    }
        
    private EventEnum(int v){
    	this.value = v;
    }
    
    public int getValue(){
    	return this.value;
    }
    
}
