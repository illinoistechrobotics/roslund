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

public class Timer extends Thread {
	private RobotQueue queue = null;
	private Communication comm = null;
	private volatile Boolean run = true;
	private Boolean timer100hz = false;
	private Boolean timer50hz = false;
	private Boolean timer25hz = false;
	private Boolean timer10hz = false;
	private Boolean timer1hz = false;
	
	public Timer(RobotQueue q, Communication c){
		this.queue = q;
		this.comm = c;
	}
	
	public void setTimers(Boolean hz100, Boolean hz50, Boolean hz25, Boolean hz10, Boolean hz1){
		timer100hz = hz100;
		timer50hz = hz50;
		timer25hz = hz25;
		timer10hz = hz10;
		timer1hz = hz1;
	}
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
		}
	}
	
	/**
	 * Creates a 10hz timer and a 1hz timer then puts them on the queue
	 * these timers may not be perfect but are close enough to work for our purpose
	 */
	public void run(){
		run = true;
		RobotEvent ev1 = new RobotEvent(EventEnum.ROBOT_EVENT_TIMER,(short)1,0);
		RobotEvent ev2 = new RobotEvent(EventEnum.ROBOT_EVENT_TIMER,(short)2,0);
		RobotEvent ev3 = new RobotEvent(EventEnum.ROBOT_EVENT_TIMER,(short)3,0);
		RobotEvent ev4 = new RobotEvent(EventEnum.ROBOT_EVENT_TIMER,(short)4,0);
		RobotEvent ev5 = new RobotEvent(EventEnum.ROBOT_EVENT_TIMER,(short)5,0);

		while(run){
			try{		
				for(int i=0; i<100; i++){
					if(timer100hz)
						queue.put(ev5);
					if(i%2==0 && timer50hz){
						queue.put(ev4);
					}
					if(i%4==0 && timer25hz){
						queue.put(ev3);
					}
					if(i%10==0){
						if(timer10hz)
							queue.put(ev2);
						comm.sendStatus();
					}		
					Thread.sleep(10);
				}
				if(timer100hz)
					queue.put(ev5);
				if(timer50hz)
					queue.put(ev4);
				if(timer25hz)
					queue.put(ev3);
				if(timer10hz)
					queue.put(ev2);
				if(timer1hz)
					queue.put(ev1);
				comm.checkStatus();
			}
			catch(Exception e){	
			}
		}
	}
}
