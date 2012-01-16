package general;

public class RobotQueue {
	
	private class Node{
		public RobotEvent data;
		public Node next;
		
		public Node(RobotEvent d){
			this.data = d;
		}
	}
	
	private int size;
	private int max_size;
	private Node first;
	private Node last;
	
	public RobotQueue(int ms){
		this.max_size = ms;
		this.size = 0;
		this.first = null;
		this.last = null;
	}
	
	public synchronized int getSize(){
		return this.size;
	}
	
	public void put(RobotEvent d){
		while(!this.add(d)){
			try{
				Thread.sleep(5);
			}
			catch(Exception e){
				return;
			}
		}
	}
	
	public synchronized boolean add(RobotEvent da){
		if(size >= max_size){
			return false;
		}
		RobotEvent newda = da.copy();
		Node temp = new Node(newda);
		size++;
		if(last == null){
			first = temp;
			last = temp;
		}
		else{
			last.next = temp;
			last = temp;
		}
		Node cursor = first;
		while(cursor!=null){
			cursor = cursor.next;
		}
		return true;
	}
	
	
	public void putOverride(RobotEvent data){
		while(!this.addOverride(data)){
			try{
				Thread.sleep(5);
			}
			catch(Exception e){
				return;
			}
		}
	}
	
	//overrides a command with the same command and index with the new value
	public synchronized boolean addOverride(RobotEvent da){
		RobotEvent newda = da.copy();
		Node temp = new Node(newda);
		Node cursor = first;
		while(cursor != null){
			if(cursor.data.getCommand() == temp.data.getCommand() &&
					cursor.data.getIndex() == temp.data.getIndex()){
				cursor.data.setValue(temp.data.getValue());
				return true;
			}
			cursor = cursor.next;
		}
		if(size >= max_size){
			return false;
		}
		size++;
		if(last == null){
			first = temp;
			last = temp;
		}
		else{
			last.next = temp;
			last = temp;
		}
		return true;
	}
	
	public RobotEvent take(){
		RobotEvent temp = null;
		while(true){
			temp = this.poll();
			if(temp == null){
				try{
					Thread.sleep(5);
				}
				catch(Exception e){
					return null;
				}
			}
			else{
				return temp;
			}
		}	
	}
	
	public synchronized RobotEvent poll(){
		if(size == 0){
			return null;
		}
		else{
			RobotEvent tmp = null;
			tmp = first.data;
			first = first.next;
			size --;
			if(first == null){
				last = null;
			}
			return tmp;
		}
	}
	
	public synchronized void flush(){
		size = 0;
		first = null;
		last = null;
	}
	
}
