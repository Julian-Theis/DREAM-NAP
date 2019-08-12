package edu.uic.prominent.processmining.decaypns.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;

public class ResourceReplayer {
	
	final static Logger logger = Logger.getLogger(ResourceReplayer.class);

	private HashMap<String,List<String>> uniqueResourceMap;
	private List<String> excludeResources;
	private EventLog log;
	private List<HashMap<String,double[]>> samples;
	
	private String benchmark;
	private int numResources;
	
	public ResourceReplayer(EventLog log, String benchmark, int numResources){
		this.uniqueResourceMap = new HashMap<String, List<String>>();
		this.samples = new ArrayList<HashMap<String, double[]>>(); 
		this.excludeResources = new ArrayList<String>();
		this.log = log;
		this.benchmark = benchmark;
		this.numResources = numResources;
		createUniqueResourceMap();
	}
	
	public ResourceReplayer(EventLog log, String benchmark, int numResources, String exclusions){
		this.uniqueResourceMap = new HashMap<String, List<String>>();
		this.samples = new ArrayList<HashMap<String, double[]>>(); 
		this.excludeResources = new ArrayList<String>();
		for(String exclusion : exclusions.split(",")){
			excludeResources.add(exclusion);
		}
		this.log = log;
		this.benchmark = benchmark;
		this.numResources = numResources;
		createUniqueResourceMap();
	}
	
	public ResourceReplayer(EventLog log, String benchmark, int numResources, String exclusions, HashMap<String, List<String>> uniqueResourceMap){
		this.uniqueResourceMap = uniqueResourceMap;
		this.samples = new ArrayList<HashMap<String, double[]>>(); 
		this.excludeResources = new ArrayList<String>();
		for(String exclusion : exclusions.split(",")){
			excludeResources.add(exclusion);
		}
		this.log = log;
		this.benchmark = benchmark;
		this.numResources = numResources;
	}
	
	public void process(){
		createResourceCountingVectors();
	}
	
	
	private void createUniqueResourceMap(){
		Trace trace = null;
		Iterator<String> resourceIterator;
		String resource;
		List<String> uniqueList;
		String resourceVal;
		
		long start = System.currentTimeMillis();
		for(Entry<Trace, Integer> entry : log.getLog())
		{
			trace = entry.getKey();
			for(Event event : trace.trace){
				resourceIterator = event.resources().keySet().iterator();
				while(resourceIterator.hasNext()){
					resource = resourceIterator.next();
					
					if(!excludeResources.contains(resource)){
						if(!uniqueResourceMap.containsKey(resource)){
							uniqueList = new ArrayList<String>();
							uniqueResourceMap.put(resource, uniqueList);
						}else{
							uniqueList = uniqueResourceMap.get(resource);
						}
						resourceVal = event.resources().get(resource);
						if(!uniqueList.contains(resourceVal)){
							uniqueList.add(resourceVal);
						}
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Unique Resources calc took " + ((end-start)/1000.0) + " seconds");
		for(String key : uniqueResourceMap.keySet()){
			logger.info(key + " vector size: " + uniqueResourceMap.get(key).size());
		}
	}
	
	private void createResourceCountingVectors(){
		Trace trace = null;
		Iterator<String> resourceIterator;
		String resource;
		List<String> uniqueList;
		double[] vec;
		
		long start = System.currentTimeMillis();
		for(Entry<Trace, Integer> entry : log.getLog())
		{
			trace = entry.getKey();
			HashMap<String, double[]> sample = new HashMap<String, double[]>();
			for(String key : uniqueResourceMap.keySet()){
				sample.put(key, new double[uniqueResourceMap.get(key).size()]);
			}		
			for(Event event : trace.trace){
				resourceIterator = event.resources().keySet().iterator();
				while(resourceIterator.hasNext()){
					resource = resourceIterator.next();
					if(!excludeResources.contains(resource)){						
						uniqueList = uniqueResourceMap.get(resource);
						vec = sample.get(resource);
						if(uniqueList.contains(event.resources().get(resource)))
							vec[uniqueList.indexOf(event.resources().get(resource))]++;
					}
				}
				this.samples.add(sample);
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Resource Counting took " + ((end-start)/1000.0) + " seconds");
	}
	

	
	
	public void createDataset(String suffix){
		long start = System.currentTimeMillis();
		List<String> dataset = new ArrayList<String>();
		String data = "";
		String resourceKey;
		double[] vec;
		for(HashMap<String, double[]> sample : this.samples){
			data = "";
			for(int i = 1; i <= this.numResources; i++){
				resourceKey = "Resource" + i;
				if(!this.excludeResources.contains(resourceKey)){
					vec = sample.get(resourceKey);
					for(double d : vec){
						data = data + d + ";";
					}
				}
			}
			dataset.add(data);
		}
		long end = System.currentTimeMillis();
		logger.info("Create dataset took " + ((end-start)/1000.0) + " seconds");
		
		start = System.currentTimeMillis();
		try {
			File fouttrain = new File("decaydata/" + this.benchmark + "_" + suffix + "_resources.csv");
			FileOutputStream fostrain = new FileOutputStream(fouttrain);
			BufferedWriter bwtrain = new BufferedWriter(new OutputStreamWriter(fostrain));
			for (String str : dataset) {
					bwtrain.write(str);
					bwtrain.newLine();
			}
			bwtrain.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		end = System.currentTimeMillis();
		logger.info("Write dataset took " + ((end-start)/1000.0) + " seconds");
	}
	
	public HashMap<String, List<String>> getUniqueResourceMap(){
		return this.uniqueResourceMap;
	}
}
