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

import gui.Display;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

//TODO:
//find out if joystick is unplugged in linux
//refresh joysticks in linux

public class Joystick extends Thread {
	private RobotQueue queue = null;
	private Controller joy = null;
	private Display dis = null;
	private volatile Boolean run = true;
	
	public Joystick(RobotQueue q, Controller cs, Display d){
		this.queue = q;
		this.joy = cs;
		this.dis = d;
		if(joy != null){
			updateJoystickAll();
		}
	}
	
	/**
	 * returns an array of controllers that are Joysticks and Gamepads
	 * can include keyboard,mouse and other input devices.  
	 */
	public static Controller[] getJoysticks(){ 
		Controller[] cs;
		if(System.getProperty("os.name").contains("nux")){	
			ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment(); 
			cs = ce.getControllers(); 
		}
		else{
			DirectInputEnvironmentPlugin diep = new DirectInputEnvironmentPlugin();
			cs = diep.getControllers();
		}
		Controller[] st = new Controller[cs.length];
		int j=0;
		for(int i=0; i<cs.length; i++){
			//this will only output the controllers
			if(cs[i].getType() == Controller.Type.STICK || cs[i].getType() == Controller.Type.GAMEPAD){
				st[j] = cs[i];
				j++;
			}
		}
		for(int i=j; i<st.length; i++){
			st[i] = null;
		}
		return st;
	}
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
		}
	}
	
	/**
	 * "listens in" on the joystick and adds event if there was a change
	 * populates the Robot_Event with proper command, index, and value.
	 * axis index (X=0,Y=1,X1=2,Y1=3) value 0-255 up and right is 255, down and left is 0
	 * buttons index equals button number-1 i.e(1 on controller has index 0) value 0 released(up) 1 pressed(down)
	 * d_pad(hat) index 0, value 0 neutral 1-8 (1 starting a 9 o'clock position and moving clockwise)
	 * also updates the gui with joystick values hack for linux since drivers label things differently
	 */
	public void run(){
		RobotEvent ev = new RobotEvent();
		run = true;
		while(run){
			try{
				if(joy.poll()==false){
					RobotEvent joy_ev = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_STATUS,(short)0,0);
					queue.put(joy_ev);
					return;
				}
			}
			catch(Exception e){
				System.err.println("Can not open joystick");
				return;
			}
			EventQueue event_q = joy.getEventQueue();
			Event joy_event = new Event();
			while(event_q.getNextEvent(joy_event)){
				Component comp = joy_event.getComponent();						
				String command = comp.getName();
				if(command.equals("X Axis") || command.equals("x")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)0);
					ev.setValue((int)((joy_event.getValue() + 1.0) * (255.0) / (2.0))); //convert from 1.0 to -1.0 to 255 to 0
					dis.updateAxisGUI(ev);
				}
				else if(command.equals("Y Axis") || command.equals("y")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)1);
					ev.setValue((int)((joy_event.getValue() - 1.0) * (-255.0) / (2.0))); //inverts the value up 255 down 0
					dis.updateAxisGUI(ev);
				}
				else if(command.equals("Z Axis") || command.equals("z")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)2);
					ev.setValue((int)((joy_event.getValue() + 1.0) * (255.0) / (2.0)));
					dis.updateAxisGUI(ev);
				}
				else if(command.equals("Z Rotation")|| command.equals("rz")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)3);
					ev.setValue((int)((joy_event.getValue() - 1.0) * (-255.0) / (2.0)));
					dis.updateAxisGUI(ev);
				}
				else if(command.contains("Button")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)Integer.parseInt(command.substring(command.indexOf(' ')+1)));
					ev.setValue((int)joy_event.getValue());  //convert 1 to -1 to 255 to 0
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Trigger")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)0);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Thumb")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)1);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Thumb 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)2);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Top")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)3);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Top 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)4);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Pinkie")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)5);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)6);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)7);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base 3")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)8);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base 4")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)9);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base 5")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)10);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Base 6")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)11);
					ev.setValue((int)joy_event.getValue());
					dis.updateButtonGUI(ev);
				}
				else if(command.equals("Hat Switch") || command.equals("pov")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_HAT);
					ev.setIndex((short)0);
					float val = joy_event.getValue();
					if(val == 0.0){
						ev.setValue(0);
					}
					else if(val == 0.125){
						ev.setValue(2);
					}
					else if(val == .25){
						ev.setValue(3);
					}
					else if(val == .375){
						ev.setValue(4);
					}
					else if(val == .5){
						ev.setValue(5);
					}
					else if(val == .625){
						ev.setValue(6);
					}
					else if(val == .75){
						ev.setValue(7);
					}
					else if(val == .875){
						ev.setValue(8);
					}
					else if(val == 1.0){
						ev.setValue(1);
					}
					dis.updateHatGUI(ev);
				}
				try{
					//check to see if there is no duplicate joy event if so override 
					//if we don't override we get overridden with joystick events and there is a backlog of events
					if(ev.getCommand()==EventEnum.ROBOT_EVENT_JOY_AXIS){
						queue.putOverride(ev);
					}
					else{
						queue.put(ev);
					}
				}
				catch(Exception e){
					System.out.println("Error");
				}
			}
			
			try{
				Thread.sleep(10);
			}
			catch(Exception e){	
			}
			
		}
	}
	
	//this is used only at start up to get all initial values otherwise it shouldn't be called
	private void updateJoystickAll(){

		RobotEvent ev = new RobotEvent();
		ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
		ev.setIndex((short)0);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.X).getPollData() - 1.0) * (-255.0) / (2.0)));
		dis.updateAxisGUI(ev);
		ev.setIndex((short)1);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.Y).getPollData() - 1.0) * (-255.0) / (2.0)));
		dis.updateAxisGUI(ev);
		ev.setIndex((short)2);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.Z).getPollData() - 1.0) * (-255.0) / (2.0)));
		dis.updateAxisGUI(ev);
		ev.setIndex((short)3);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.RZ).getPollData() - 1.0) * (-255.0) / (2.0)));
		dis.updateAxisGUI(ev);
		
		if(System.getProperty("os.name").contains("win")){
			ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
			ev.setIndex((short)0);
			ev.setValue((int)(joy.getComponent(Identifier.Button._0).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)1);
			ev.setValue((int)(joy.getComponent(Identifier.Button._1).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)2);
			ev.setValue((int)(joy.getComponent(Identifier.Button._2).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)3);
			ev.setValue((int)(joy.getComponent(Identifier.Button._3).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)4);
			ev.setValue((int)(joy.getComponent(Identifier.Button._4).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)5);
			ev.setValue((int)(joy.getComponent(Identifier.Button._5).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)6);
			ev.setValue((int)(joy.getComponent(Identifier.Button._6).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)7);
			ev.setValue((int)(joy.getComponent(Identifier.Button._7).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)8);
			ev.setValue((int)(joy.getComponent(Identifier.Button._8).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)9);
			ev.setValue((int)(joy.getComponent(Identifier.Button._9).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)10);
			ev.setValue((int)(joy.getComponent(Identifier.Button._10).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)11);
			ev.setValue((int)(joy.getComponent(Identifier.Button._11).getPollData()));
			dis.updateButtonGUI(ev);
		}
		else if(System.getProperty("os.name").contains("nux")){
			ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
			ev.setIndex((short)0);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TRIGGER).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)1);
			ev.setValue((int)(joy.getComponent(Identifier.Button.THUMB).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)2);
			ev.setValue((int)(joy.getComponent(Identifier.Button.THUMB2).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)3);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TOP).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)4);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TOP2).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)5);
			ev.setValue((int)(joy.getComponent(Identifier.Button.PINKIE).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)6);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)7);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE2).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)8);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE3).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)9);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE4).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)10);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE5).getPollData()));
			dis.updateButtonGUI(ev);
			ev.setIndex((short)11);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE6).getPollData()));
			dis.updateButtonGUI(ev);
		}
		
		ev.setCommand(EventEnum.ROBOT_EVENT_JOY_HAT);
		ev.setIndex((short)0);
		
		float val = joy.getComponent(Identifier.Axis.POV).getPollData();
		if(val == 0.0){
			ev.setValue(0);
		}
		else if(val == 0.125){
			ev.setValue(2);
		}
		else if(val == .25){
			ev.setValue(3);
		}
		else if(val == .375){
			ev.setValue(4);
		}
		else if(val == .5){
			ev.setValue(5);
		}
		else if(val == .625){
			ev.setValue(6);
		}
		else if(val == .75){
			ev.setValue(7);
		}
		else if(val == .875){
			ev.setValue(8);
		}
		else if(val == 1.0){
			ev.setValue(1);
		}
		dis.updateHatGUI(ev);			
	}
	

	boolean checkJoystick(){
		if(System.getProperty("os.name").contains("win")){
			DirectInputEnvironmentPlugin diep = new DirectInputEnvironmentPlugin();
			Controller[] cs = diep.getControllers();
			int j=0;
			for(int i=0; i<cs.length; i++){
				//this will only output the controllers
				if(cs[i].getType() == Controller.Type.STICK || cs[i].getType() == Controller.Type.GAMEPAD){
					j++;
				}
			}
		
			if(j==0){
				RobotEvent ev = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_STATUS,(short)0,0);
				queue.put(ev);
				this.stopThread();
				return false;
			}
			return true;
		}
		return true;
	}
}
