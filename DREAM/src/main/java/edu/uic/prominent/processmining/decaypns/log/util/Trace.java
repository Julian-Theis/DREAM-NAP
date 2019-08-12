package edu.uic.prominent.processmining.decaypns.log.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Trace consisting of Events
 * 
 * @author Julian
 *
 */
public class Trace{
	public List<Event> trace;
	public int caseId;
	public long timestamp;

	public Trace(int caseId, long timestamp) {
		this.trace =  new ArrayList<>();
		this.caseId = caseId;
		this.timestamp = timestamp;
	}

	/**
	 * Add an event
	 * 
	 * @param event Event
	 */
	public void addEvent(Event event) {
		event.addCaseId(caseId);
		trace.add(event);
	}

	/**
	 * Perform action on each event
	 * 
	 * @param action
	 */
	public void forEach(Consumer<Event> action) {
		trace.forEach(action);
	}
	
	public int length(){
		return trace.size();
	}

	public boolean equals(Trace o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (this.toEventString().equals(o.toEventString()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(trace);
	}

	@Override
	public String toString() {
		return trace.toString();
	}
	
	public String toEventString() {
		String eventString = "";
		for(Event e : trace){
			eventString += e.name();
		}
		if(eventString.equals("")){
			eventString = "NULL";
		}
		return eventString;
	}
	
}

