package testCode;

import general.RobotQueue;
import general.RobotEvent;
import general.EventEnum;

public class Joysim extends Thread {
	RobotQueue queue;
	Boolean run = true;
	RobotEvent[] ev = new RobotEvent[15];
	
	public Joysim(RobotQueue q){
		queue = q;
		ev[0] = new RobotEvent(EventEnum.ROBOT_EVENT_CMD,(short)1,25);
		ev[1] = new RobotEvent(EventEnum.ROBOT_EVENT_CMD_NOOP,(short)2,26);
		ev[2] = new RobotEvent(EventEnum.ROBOT_EVENT_CMD_START,(short)3,27);
		ev[3] = new RobotEvent(EventEnum.ROBOT_EVENT_CMD_STOP,(short)4,28);
		ev[4] = new RobotEvent(EventEnum.ROBOT_EVENT_CMD_REBOOT,(short)5,29);
		ev[5] = new RobotEvent(EventEnum.ROBOT_EVENT_NET,(short)6,30);
		ev[6] = new RobotEvent(EventEnum.ROBOT_EVENT_NET,(short)7,31);
		ev[7] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_AXIS,(short)8,32);
		ev[8] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_AXIS,(short)8,33);
		ev[9] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_AXIS,(short)11,34);
		ev[10] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_AXIS,(short)11,35);
		ev[11] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_AXIS,(short)12,36);
		ev[12] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_BUTTON,(short)13,37);
		ev[13] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_BUTTON,(short)13,38);
		ev[14] = new RobotEvent(EventEnum.ROBOT_EVENT_JOY_BUTTON,(short)15,39);
		
	}
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
		}
	}
	
	public void run(){
		while(run){
			for(int i = 0; i < 15; ++i) {
				//System.out.println(i);
				queue.put(ev[i]);
				
			}
			try{
				Thread.sleep(10);
			}
			catch(Exception e){
			}
		}
	}
}
