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
import gui.Display;

/**
 * This is where robot specific code goes
 */
public class Events {
	
	public Communication comm = null;
	public Display dis = null;
	
	public Events(Communication c, Display d){
		this.comm = c;
		this.dis = d;
	}
	
	// on_status_code is called when the remote computer sends a status datagram
	public void on_net_status(RobotEvent ev){
		switch(ev.getCommand()){
		  case ROBOT_EVENT_NET:
			  break;
		  case ROBOT_EVENT_NET_STATUS_OK: 
			  break;
		  case ROBOT_EVENT_NET_STATUS_ERR:
			  //System.err.println("Lost Communication with robot");
			  break;
		  case ROBOT_EVENT_NET_STATUS_NOTICE:
			  break;
		}
	}

	// on_command_code is called when the remote computer sends a command datagram
	public void on_command_code(RobotEvent ev){
		switch(ev.getCommand()) {
		  case ROBOT_EVENT_CMD_NOOP:
		    comm.okStatus(); //tells that the robot is connected 
		    break;
		  case ROBOT_EVENT_CMD_START:
				comm.sendEvent(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_START,(short)0,0));
		    break;
		  case ROBOT_EVENT_CMD_STOP:
			  comm.sendEvent(ev);
			  comm.closeSerial();
		    break;
		  case ROBOT_EVENT_CMD_REBOOT:
			  comm.sendEvent(ev);
		    break;
		  default:
		    // unknown command code datagram
		    break;
		  }
	}
	
	// on_axis_change is called when a joystick axis changes postion
	// index is the axis number, value is the position of the axis from 0-255
	// a value of 127 is center
	int max = 0; //maximum value protection (for normalizing the motors)
	int mot1 = 0, mot2 = 0, mot3 = 0, mot4 = 0; //Motor values (for mechanum steering)
	int xAxis = 127, yAxis = 127, rAxis = 127, y1Axis = 127; //x (lateral) y(forward) and r (rotational) axis values
	int xNew = 0, yNew = 0, rNew =0; // 0 centered axis values
	
	// on_axis_change is called when a joystick axis changes postion
	// index is the axis number, value is the position of the axis from 0-255
	// a value of 127 is center
	public void on_axis_change2(RobotEvent ev){
		if(ev.getIndex()==0){
			dis.lblData_Mot1.setText(Integer.toString(ev.getValue()-127));
			dis.sdlMot1.setValue(ev.getValue()-127);
		}
		if(ev.getIndex()==1){
			dis.lblData_Mot2.setText(Integer.toString(ev.getValue()-127));
			dis.sdlMot2.setValue(ev.getValue()-127);
		}
		if(ev.getIndex()==2){
			dis.lblData_Mot3.setText(Integer.toString(ev.getValue()-127));
			dis.sdlMot3.setValue(ev.getValue()-127);
		}
		if(ev.getIndex()==3){
			dis.lblData_Mot4.setText(Integer.toString(ev.getValue()-127));
			dis.sdlMot4.setValue(ev.getValue()-127);
		}
	}
	
	
	public void on_axis_change(RobotEvent ev){
		RobotEvent new_ev = new RobotEvent();
		short axis = ev.getIndex();
		int value = ev.getValue();
		if(value == 255) value = 254;
		if(value == 0) value = 1;
		 
		//send_event(ev);
	                                                                 //for turbo hack
		
		if(axis == Profile.CON_XAXIS.getValue() || axis == Profile.CON_YAXIS.getValue() || axis == Profile.CON_RAXIS.getValue() || axis == 255) {
	        if(axis == Profile.CON_YAXIS.getValue())
	            yAxis = value;
	        if(axis == Profile.CON_XAXIS.getValue())
	            xAxis = value;
	        if(axis == Profile.CON_RAXIS.getValue())
	            rAxis = value;
	        xNew = (int)xAxis - 127;
	        yNew = (int)yAxis - 127;
	        rNew = (int)rAxis - 127;
	        mot1 = -(-yNew - xNew + rNew);
	        mot2 = (-yNew + xNew + rNew);
	        mot3 = -(-yNew - xNew - rNew);
	        mot4 = (yNew - xNew + rNew);
	        if(Math.abs(mot1) >  127 || Math.abs(mot2) > 127 || Math.abs(mot3) > 127 || Math.abs(mot4) > 127) {
	            max = 0;
	            if(Math.abs(mot1)>max)
	                max = Math.abs(mot1);
	            if(Math.abs(mot2)>max)
	                max = Math.abs(mot2);
	            if(Math.abs(mot3)>max)
	                max = Math.abs(mot3);
	            if(Math.abs(mot4)>max)
	                max = Math.abs(mot4);
	            mot1=(int)((float)mot1/max*127);
	            mot2=(int)((float)mot2/max*127);
	            mot3=(int)((float)mot3/max*127);
	            mot4=(int)((float)mot4/max*127);
	        }
			mot1 = (mot1*2)/(4 - turbo);
			mot2 = (mot2*2)/(4 - turbo);
			mot3 = (mot3*2)/(4 - turbo);
			mot4 = (mot4*2)/(4 - turbo);
			
			
			// send four axes out
	        new_ev.setCommand(EventEnum.ROBOT_EVENT_MOTOR);

	        new_ev.setIndex((short)0); 
	        new_ev.setValue(mot1 + 127);
	        dis.lblData_Mot1.setText(Integer.toString(mot1));
	        dis.sdlMot1.setValue(mot1);
			comm.sendEvent(new_ev);

	        new_ev.setIndex((short)1); 
	        new_ev.setValue(mot2 + 127);
	        dis.lblData_Mot2.setText(Integer.toString(mot2));
	        dis.sdlMot2.setValue(mot2);
			comm.sendEvent(new_ev);

	        new_ev.setIndex((short)2); 
	        new_ev.setValue(mot3 + 127);
	        dis.lblData_Mot3.setText(Integer.toString(mot3));
	        dis.sdlMot3.setValue(mot3);
			comm.sendEvent(new_ev);

	        new_ev.setIndex((short)3); 
	        new_ev.setValue(mot4 + 127);
	        dis.lblData_Mot4.setText(Integer.toString(mot4));
	        dis.sdlMot4.setValue(mot4);
			comm.sendEvent(new_ev);
		}
	
	}
	
	int turbo = 0;
	// on_button_up is called when a joystick button is released
	// button is the button number
	public void on_button_up(RobotEvent ev){
		if(ev.getIndex() == Profile.CON_TURBO1.getValue() || ev.getIndex() == Profile.CON_TURBO2.getValue()){
			turbo--;
	        RobotEvent temp = new RobotEvent();
	        temp.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
	        temp.setIndex((short)255);
	        temp.setValue(0);
	        on_axis_change(temp);  //update the motor values when turbo is pressed 
	    } 
		comm.sendEvent(ev);
	}

	// on_button_down is called when a joystick button is pressed
	// button is the button number
	public void on_button_down(RobotEvent ev){
		if(ev.getIndex() == Profile.CON_TURBO1.getValue() || ev.getIndex() == Profile.CON_TURBO2.getValue()){
			turbo++;
	        RobotEvent temp = new RobotEvent();
	        temp.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
	        temp.setIndex((short)255);
	        temp.setValue(0);
	        on_axis_change(temp);  //update the motor values when turbo is pressed
	    }
		comm.sendEvent(ev);
	}
	
	//when the d-pad is pressed
	public void on_joy_hat(RobotEvent ev){
		
	}
	
	//when the joystick has problems
	public void on_joy_status(RobotEvent ev){
		comm.sendEvent(new RobotEvent(EventEnum.ROBOT_EVENT_CMD_FAILSAFE,(short)0,0));
		System.out.println("adfasdf");
	}
	
	// timer that runs each second
	public void on_1hz_timer(RobotEvent ev){

	}

	// timer that runs every .1 second (100ms)
	public void on_10hz_timer(RobotEvent ev){
	
	}
	
	// timer that runs every .04 second (40ms)
	public void on_25hz_timer(RobotEvent ev){
		
	}
	
	// timer that runs every .02 second (20ms)
	public void on_50hz_timer(RobotEvent ev){
		
	}
	
	// timer that runs every .01 second (10ms)
	public void on_100hz_timer(RobotEvent ev){
		
	}
	
	// on_read_variable is called when the computer wants to read a robo variable
	public void on_read_variable(RobotEvent ev){
		
	}

	// on_set_variable is called when the computer wants to set a robo variable
	public void on_set_variable(RobotEvent ev){
		
	}
	
	public void on_variable(RobotEvent ev){
		
	}
	
	// on_shutdown runs when the robot should shutdown and a manual hard reset needed to turn back on. 
	public void on_shutdown(){
		//comm.sendEvent(ev); //uncomment if you want to send shutdown event to robot. stop event is sent before it reacher this point
	}
}
