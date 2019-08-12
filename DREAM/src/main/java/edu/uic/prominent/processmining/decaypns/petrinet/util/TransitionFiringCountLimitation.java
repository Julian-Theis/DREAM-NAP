package edu.uic.prominent.processmining.decaypns.petrinet.util;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TransitionFiringCountLimitation implements Serializable{

	private String transition;
	private int count;
	private int limitation;
	
	public TransitionFiringCountLimitation(String transition, int limitation){
		this.transition = transition;
		this.limitation = limitation;
		this.count = 0;
	}
	
	public void resetCount(){
		this.count = 0;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public void increment(){
		this.count++;
	}
	
	public int getLimitation(){
		return this.limitation;
	}
	
	public String getTransition(){
		return transition;
	}
}
