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

import general.Communication;
import general.EventEnum;
import general.RobotEvent;
import general.RobotQueue;

import gui.Display;

@SuppressWarnings("unused")
public class ITR_Computer {
	
	/**
	 * Creates "global" objects
	 * Then goes into loop and reads commands and outputs data
	 */
	public static void main(String[] args) {
		final int MAX_QUEUE_SIZE = 128;		
		RobotQueue recv_q = new RobotQueue(MAX_QUEUE_SIZE); 
		Communication comm = new Communication(recv_q);
		Display dis = new Display(recv_q,comm);
		Events event = new Events(comm,dis);
		
		boolean run = true;
		while(run){
			try{
				RobotEvent ev = recv_q.take();
				switch (ev.getCommand()){
				case ROBOT_EVENT_CMD_NOOP:
				case ROBOT_EVENT_CMD_STOP:
				case ROBOT_EVENT_CMD_START:
				case ROBOT_EVENT_CMD_REBOOT:
				case ROBOT_EVENT_CMD:
					event.on_command_code(ev);
					break;
				case ROBOT_EVENT_NET:
				case ROBOT_EVENT_NET_STATUS_OK: 
				case ROBOT_EVENT_NET_STATUS_ERR:
				case ROBOT_EVENT_NET_STATUS_NOTICE:
					event.on_net_status(ev);
					break;
				case ROBOT_EVENT_JOY_AXIS:
					event.on_axis_change(ev);
					break;
				case ROBOT_EVENT_JOY_BUTTON:
					if(ev.getValue() == 1)
						event.on_button_down(ev);
					else if (ev.getValue() == 0)
						event.on_button_up(ev);
					break;
				case ROBOT_EVENT_JOY_HAT:
					event.on_joy_hat(ev);
					break;
				case ROBOT_EVENT_JOY_STATUS:
					event.on_joy_status(ev);
					break;
				case ROBOT_EVENT_TIMER:
					if(ev.getIndex() == 1)
						event.on_1hz_timer(ev);
					else if(ev.getIndex() == 2)
						event.on_10hz_timer(ev);
					else if (ev.getIndex() == 3)
						event.on_25hz_timer(ev);
					else if (ev.getIndex() == 4)
						event.on_50hz_timer(ev);
					else if (ev.getIndex() == 5)
						event.on_100hz_timer(ev);
					break;					
				case ROBOT_EVENT_READ_VAR:
					event.on_read_variable(ev);
					break;
				case ROBOT_EVENT_SET_VAR:
					event.on_set_variable(ev);
					break;
				case ROBOT_EVENT_VAR:
					event.on_variable(ev);
				case ROBOT_EVENT_CMD_SHUTDOWN:
					run = false;
					event.on_shutdown();
					break;
				}
			}
			catch(Exception e){
				System.out.println("Error");
			}
		}
		System.exit(0);		
	}

}
