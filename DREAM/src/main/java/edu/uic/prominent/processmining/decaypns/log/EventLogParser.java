package edu.uic.prominent.processmining.decaypns.log;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.log.util.XLogReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Event log parser to load event logs
 * 
 * @author Julian
 *
 */
public class EventLogParser {
	
	/**
	 * Load event log from file (XES)
	 * 
	 * @param fileName String (must be located in logs/ folder)
	 * @return EventLog
	 */
	public EventLog getEventLogFromFile(String fileName) {
		try {
			return eventLogFromFile("logs/" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventLogParseException("Problem with parsing the event log");
		}
	}

	private EventLog eventLogFromFile(String fileName) throws Exception {
		EventLog eventLog = new EventLog();
		List<XTrace> xLog = XLogReader.openLog(fileName);

		//// CHANGES !! ADD FOR EACH TRACE THE TRACE ID AND TIMESTAMP FOR FIRST EVENT
		for(int i = 0; i < xLog.size(); i++){
			XTrace x = xLog.get(i);
			int key = Integer.parseInt(x.getAttributes().get("concept:name").toString());			
			Trace trace = toTrace(x, key);
			eventLog.addTrace(trace);
			
		}
		
		return eventLog;
	}

	/** 
	 * Converts XEvent to Event
	 * 
	 * @param xTrace
	 * @return Trace
	 */
	private Trace toTrace(List<XEvent> xTrace, int caseId) {
		long initTime = 0L;
		String timeString = xTrace.get(0).getAttributes().get("time:timestamp").toString();
		// Check if .000 is missing. Add it if true
			int count = StringUtils.countMatches(timeString, ".");
			if(count == 0){
				String[] timeStringSplit = timeString.split("\\-"); 
				timeString = timeStringSplit[0] + "-" + timeStringSplit[1] + "-" + timeStringSplit[2] + ".000-" + timeStringSplit[3];
			}	
		//---->
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
		try {
		    Date d = f.parse(timeString);
		    initTime = d.getTime();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		Trace trace = new Trace(caseId, initTime);
		for (XEvent xEvent : xTrace) {
			XAttribute time = xEvent.getAttributes().get("time:timestamp");
		
			Map<String, String> resources = new HashMap<String, String>();
			for(String key : xEvent.getAttributes().keySet()){
				if(key.contains("Resource")){
					resources.put(key, xEvent.getAttributes().get(key).toString());
				}
			}
			
			Event event = new Event(name(xEvent), time, resources);
			
			trace.addEvent(event);
		}
		return trace;
	}

	private String name(XAttributable xAttributable) {
		return XConceptExtension.instance().extractName(xAttributable);
	}

	private class EventLogParseException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public EventLogParseException(String message) {
			super(message);
		}
	}

}

