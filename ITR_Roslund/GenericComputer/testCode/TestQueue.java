package testCode;

import junit.framework.*;
import general.EventEnum;
import general.RobotEvent;
import general.RobotQueue;

public class TestQueue extends TestCase{

	RobotEvent[] ev = new RobotEvent[15];

	public TestQueue(String name){
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
	
	
	public void testNone() {
		RobotQueue q = new RobotQueue(256);		
		assertEquals("Size not 0", 0, q.getSize());
		assertTrue("successfully dequeued on 0 queue",q.poll()==null);
		assertEquals("Size not 0 after dequeue",0, q.getSize());
	}
	
	public void testOne() {
		RobotQueue q = new RobotQueue(256);
		RobotEvent temp_ev;

		q.put(ev[0]);
		assertEquals("Size not 1 after enqueue.",1,q.getSize());
		
		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.",ev[0].getCommand(), temp_ev.getCommand());

		assertEquals("Size not 0 after last dequeue.",0,q.getSize());
	}
	
	public void testMid() {
		RobotQueue q = new RobotQueue(256);
		RobotEvent temp_ev;
		
		q.put(ev[0]);
		q.put(ev[1]);
		q.put(ev[2]);
		q.put(ev[3]);
		q.put(ev[4]);
		
		assertEquals("Size not 5 after 5 enqueues.", 5, q.getSize());

		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.", ev[0].getCommand(), temp_ev.getCommand());
		assertEquals("Size not 4 after dequeueing an item.",4, q.getSize());

		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.", ev[1].getCommand(), temp_ev.getCommand());
		assertEquals("Size not 3 after dequeueing an item.",3, q.getSize());

		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.", ev[2].getCommand(), temp_ev.getCommand());
		assertEquals("Size not 2 after dequeueing an item.",2, q.getSize());

		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.", ev[3].getCommand(), temp_ev.getCommand());
		assertEquals("Size not 1 after dequeueing an item.",1, q.getSize());

		temp_ev = q.take();
		assertEquals("Dequeued item incorrect.", ev[4].getCommand(), temp_ev.getCommand());
		assertEquals("Size not 0 after dequeueing an item.",0, q.getSize());
	}
	
	public void testOverflow() {
		RobotQueue q = new RobotQueue(14);
		int i;

		for(i = 0; i < 14; ++i) {
			assertTrue("added event " + i,q.add(ev[i]));
		}
		
		assertFalse("added event 15 to overfill buffer",q.add(ev[14]));
		assertEquals("Size not QUEUE_SIZE after 35 enqueues.", 14, q.getSize());
	}
	
	
	public void testOverwrite(){
		RobotQueue q = new RobotQueue(255);
		q.putOverride(ev[7]);
		q.putOverride(ev[8]);
		q.putOverride(ev[9]);
		q.putOverride(ev[10]);
		q.putOverride(ev[11]);
		
		assertEquals("Size not 3 after 5 overwrite enqueues.",3,q.getSize());
		RobotEvent temp_ev = q.take();
		assertEquals("If ev 8 overwirte 7",temp_ev.getCommand(),ev[8].getCommand());		
		assertEquals("If ev 8 overwirte 7",temp_ev.getIndex(),ev[8].getIndex());
		assertEquals("If ev 8 overwirte 7",temp_ev.getValue(),ev[8].getValue());
		
	}
	
}
