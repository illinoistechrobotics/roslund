package testCode;

import junit.framework.*;
import general.EventEnum;
import general.RobotEvent;
import general.RobotQueue;
import testCode.Joysim;

public class TestMultiThreadQueue extends TestCase{

	RobotEvent[] ev = new RobotEvent[15];
	RobotQueue queue = new RobotQueue(100);
	
	public TestMultiThreadQueue(String name){
		super(name);
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
	
	public void testJoy(){
		Joysim js = new Joysim(queue);
		RobotEvent ev2;
		Thread joys = js;
		joys.start();
		int i = 0;
		int k = 0;
		while(k<100){
			ev2 = queue.take();
			assertEquals("Event on queue is not the next on",ev2.getValue(),ev[i].getValue());
			//try{
				//Thread.sleep(1);
			//}
			//catch(Exception e){
			//	System.out.println(e);
			//}
			i++;
			if(i>=15)
				i=0;
			k++;
		}
		js.stopThread();
	}
	
	//public void testJoystick2(){
		
	//}
	
	
}
