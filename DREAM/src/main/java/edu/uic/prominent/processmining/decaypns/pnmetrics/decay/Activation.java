package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

public class Activation {
	public int index;
	public long time;
	
	public Activation(int index, long time){
		this.index = index;
		this.time = time;
	}
	
	public int getIndex(){
		return this.index;
	}
	
	public long getTime(){
		return this.time;
	}
	
	public void setTime(long time){
		this.time = time;
	}
}
