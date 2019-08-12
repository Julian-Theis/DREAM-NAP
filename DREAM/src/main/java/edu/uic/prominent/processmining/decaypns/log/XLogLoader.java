package edu.uic.prominent.processmining.decaypns.log;

import static com.raffaeleconforti.log.util.LogImporter.importFromFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.out.XesXmlSerializer;

import au.com.bytecode.opencsv.CSVReader;
import edu.uic.prominent.processmining.decaypns.log.util.XEventWrapper;

/**
 * XLogLoader to load logs from different sources
 * 
 * @author Julian
 */
public class XLogLoader {

	private XLog log;
	private XFactory factory;
	private XOrganizationalExtension organizationalExtension;
	private XConceptExtension conceptExtension;
	
	private Map<String, Integer> config;
	private Map<String, Integer> resources;
	private boolean hasResources;
	private CSVReader reader;
	
	/**
	 * Create an instance with non-default configuration:
	 * 
	 *     Map<String, Integer> config = new HashMap<String, Integer>();
	 *     config.put("Timestamp", 1);
	 *     config.put("Activity", 0);
	 *     config.put("Case", 2);
	 * 
	 * @param config2 Map<String,Integer>
	 */
	public XLogLoader(Map<String, Integer> config){
		this.config = config;
		this.hasResources = false;
	}
	
	public XLogLoader(Map<String, Integer> config, Map<String, Integer> resources){
		this.config = config;
		this.resources = resources;
		this.hasResources = true;
	}
	
	/**
	 * Create instance with default configuration where
	 * Timestamp = 0
	 * Activity = 2
	 * Case = 3
	 */
	public XLogLoader(){
		config = new HashMap<String, Integer>();
		config.put("Timestamp", 1);
		config.put("Activity", 0);
		config.put("Case", 2);
		this.hasResources = false;
		
		
		resources = new HashMap<String, Integer>();
		resources.put("Resource1", 3);
		resources.put("Resource2", 4);
		resources.put("Resource3", 5);
		resources.put("Resource4", 6);
		resources.put("Resource5", 7);
		resources.put("Resource6", 8);
		resources.put("Resource7", 9);
		resources.put("Resource8", 10);
		resources.put("Resource9", 11);
		resources.put("Resource10", 12);
		resources.put("Resource11", 13);
		resources.put("Resource12", 14);
		resources.put("Resource13", 15);
		resources.put("Resource14", 16);
		resources.put("Resource15", 17);
		
	}
	
	/**
	 * Import XES log
	 * 
	 * @param o String of the filename, must be in logs/ directory
	 * @return XLog
	 */
    public XLog importXES(Object o) {
        try {
            if(o instanceof String) {
                return importFromFile(new XFactoryNaiveImpl(), ("logs/" + (String) o));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public XLog importCSV(String filename, boolean isLong, boolean isTimestamp, String timestampFormat){
    	return importCSV(filename, isLong, isTimestamp,  timestampFormat, 0);
    }
	
    /**
     * Import XLog from a CSV file
     * 
     * @param filename of the log (file must be in logs/ directory)
     * @param isLong (is time information given as a long value?)
     * @param isTimestamp (is it a timestamp or just time information?)
     * @param timestampFormat (e.g. yyyy/MM/dd HH:mm:s.S)
     * @return
     */
	public XLog importCSV(String filename, boolean isLong, boolean isTimestamp, String timestampFormat, int numResources){
		//String defaultFormat = "yyyy/MM/dd HH:mm:s.S";
		if(this.hasResources){
			numResources = resources.size();
		}
		
		try{
			File initialFile = new File(filename);
		    InputStream input = new FileInputStream(initialFile);
			reader = new CSVReader(new InputStreamReader(input), ',');
			initializeLog();
			
			SimpleDateFormat formatter = new SimpleDateFormat(timestampFormat);
			
			
			Map<String,List<XEventWrapper>> maplistevent = new HashMap<String, List<XEventWrapper>>();
			
			int numline=0;
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if(numline>0){
					String time = nextLine[config.get("Timestamp")];
					
					Date date = null;
					
					if(isTimestamp){
						if(isLong){
					        String dateText = formatter.format(new Date(Long.parseLong(time)));
					        date = (Date) formatter.parse(dateText);
						}else{
							date = (Date) formatter.parse(time);
						}
					}else{
						double seconds = Double.parseDouble(time);
						long millis = (long)(seconds * 1000);
						String dateText = formatter.format(new Date(millis));
				        date = (Date) formatter.parse(dateText);
					}

					
					String activity = nextLine[config.get("Activity")];
					String case1 = nextLine[config.get("Case")];
										
					Map<String, String> resis = new HashMap<String, String>();
					for(int i = 1; i <= numResources; i++){
						resis.put("Resource" + i, nextLine[resources.get("Resource" + i)]);
					}
					
					if(activity.trim().length() != 0){
						if(!maplistevent.containsKey(case1)){
							List<XEventWrapper> elist = new ArrayList<XEventWrapper>();
							elist.add(new XEventWrapper(date, makeEvent(activity, date, resis)));
							maplistevent.put(case1, elist);
						}else{
							List<XEventWrapper> elist = maplistevent.get(case1);
							elist.add(new XEventWrapper(date, makeEvent(activity, date, resis)));
							maplistevent.put(case1, elist);
						}
					}
				}
				numline++;
			}
			
			for(String key : maplistevent.keySet()){
				List<XEventWrapper> elist = maplistevent.get(key);
				Collections.sort(elist, new Comparator<XEventWrapper>(){
					@Override
				    public int compare(XEventWrapper lhs, XEventWrapper rhs) {
				        if (lhs.getDate().getTime() < rhs.getDate().getTime())
				            return -1;
				        else if (lhs.getDate().getTime() == rhs.getDate().getTime())
				            return 0;
				        else
				            return 1;
				    }
				});
				XTrace xtrace = this.createAndAddTrace(String.valueOf(key));
				for(XEventWrapper event: elist ){
					xtrace.add(event.getEvent());
				}
			}
			
		}catch(Exception e){
			 System.out.println("ERROR - something went wrong converting the CSV to XES: " + e.getMessage());
	         e.printStackTrace();
		}
		return log;
	}
	
	
	
	
	
	
	private void initializeLog() {
		factory = XFactoryRegistry.instance().currentDefault();
		conceptExtension = XConceptExtension.instance();
		organizationalExtension = XOrganizationalExtension.instance();
		log = factory.createLog();
		log.getExtensions().add(conceptExtension);
		log.getExtensions().add(organizationalExtension);		
	}
	
	private XEvent makeEvent(String activity, Date timestamp, Map<String, String> resources)  {
		XAttributeMap attMap = new XAttributeMapImpl();
		putLiteral(attMap, XConceptExtension.KEY_NAME, activity);
		putLiteral(attMap, "lifecycle:transition", "complete");
		putTimestamp(attMap, XTimeExtension.KEY_TIMESTAMP, timestamp);
		
		for(String key : resources.keySet()){
			putLiteral(attMap, key, resources.get(key));
		}
		
		XEvent newEvent = new XEventImpl(attMap);
		return newEvent;
	}
	
	private void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}
	
	private void putTimestamp(XAttributeMap attMap, String key, Date value) {
		attMap.put(key, new XAttributeTimestampImpl(key, value));
	}
	
	/**
	 * Add an extra trace to the log
	 * 
	 * @param name
	 * @return
	 */
	public XTrace createAndAddTrace(String name) {
		XTrace trace = factory.createTrace();
		log.add(trace);
		trace.getAttributes().put(XConceptExtension.KEY_NAME,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, name, conceptExtension));
		return trace;
	}
	
	/**
	 * Save the XLog documennt as an XES file
	 * 
	 * @param filename (String, will be saved in logs/ directory)
	 */
    public void saveAsXES(String filename){
    	try{
			XesXmlSerializer serializer = new XesXmlSerializer();
			OutputStream out = new FileOutputStream("logs/" + filename);
			serializer.serialize(this.log, out);
			out.close();
    	}catch(Exception e){
    		System.out.println("ERROR - something went wrong saving the XES: " + e.getMessage());
	        e.printStackTrace();
    	}
    }
}

