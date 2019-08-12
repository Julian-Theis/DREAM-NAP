package edu.uic.prominent.processmining.decaypns.log.util;

import java.util.Date;
import org.deckfour.xes.model.XEvent;

public class XEventWrapper {
	private Date key;
	private XEvent event;
	
	public XEventWrapper(Date key, XEvent event){
		this.key = key;
		this.event = event;
	}
	
	public Date getDate(){
		return this.key;
	}
	
	public XEvent getEvent(){
		return event;
	}	
}