package edu.uic.prominent.processmining.decaypns.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.uic.prominent.processmining.decaypns.log.util.Event;
import edu.uic.prominent.processmining.decaypns.log.util.Trace;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.Constants;


/**
 * EventLog class consisting of Traces
 * 
 * @author Julian
 *
 */
public class EventLog {
	final static Logger logger = Logger.getLogger(EventLog.class);
	
	private Map<Trace, Integer> traceCounts;
	
	private String ordering;
	
	public EventLog(){
		traceCounts = new HashMap<Trace, Integer>();
		ordering = Constants.ORDERING_UNORDERED;
	}
	
	public void setOrdering(String ordering){
		this.ordering = ordering;
	}
	
	public Set<Entry<Trace, Integer>> getLog(){
		if(ordering == Constants.ORDERING_UNORDERED){
			return unsorted(this.traceCounts);
		}if(ordering == Constants.ORDERING_RANDOM){
			return random(this.traceCounts);
		}else if(ordering == Constants.ORDERING_BYTIMESTAMPASC){
			return timestampAsc(this.traceCounts);
		}else if(ordering == Constants.ORDERING_BYTIMESTAMPDESC){
			return timestampDesc(this.traceCounts);
		}else if(ordering == Constants.ORDERING_BYCASEIDASC){
			return caseAsc(this.traceCounts);
		}else if(ordering == Constants.ORDERING_BYCASEIDDESC){
			return caseDesc(this.traceCounts);
		}else{
			return null;
		}
	}
	
	/**
	 * Add a trace to the Event Log
	 * @param trace of class Trace
	 */
	public void addTrace(Trace trace) {
		traceCounts.put(trace, incrementCount(trace));
	}

	/**
	 * Get number of traces which are identical with the given one
	 * 
	 * @param trace of class Trace, given trace
	 * @return Integer
	 */
	public Integer getCount(Trace trace) {
		return traceCounts.get(trace);
	}
		
	/**
	 * Get number of traces of the event log
	 * 
	 * @return Integer
	 */
	public Integer getNumTraces(){
		return traceCounts.size();
	}

	/**
	 * For each trace 
	 * 
	 * @param action
	 */
	public void forEach(Consumer<? super Entry<Trace, Integer>> action){
		getLog().forEach(action);
	}

	/**
	 * Increment the count of a specific trace
	 * 
	 * @param trace of class Trace, specific one to increment
	 * @return Integer incremented
	 */
	private Integer incrementCount(Trace trace) {
		Integer count = traceCounts.get(trace);
		count = (count == null) ? 0 : count;
		return ++count;
	}

	@Override
	public String toString() {
		return traceCounts.toString();
	}
	
	
	private static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<Trace,Integer>> timestampAsc(Map<Trace,Integer> map) {
		SortedSet<Map.Entry<Trace,Integer>> sortedEntries = new TreeSet<Map.Entry<Trace, Integer>>(
	        new Comparator<Map.Entry<Trace,Integer>>() {
	            @Override 
	            public int compare(Map.Entry<Trace,Integer> e1, Map.Entry<Trace, Integer> e2) {
	            	if(e1.getKey().timestamp > e2.getKey().timestamp)
	        			return 1;
	        		else if(e1.getKey().timestamp == e2.getKey().timestamp)
	        			return 0;
	        		else
	        			return -1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	private static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<Trace,Integer>> timestampDesc(Map<Trace,Integer> map) {
		SortedSet<Map.Entry<Trace,Integer>> sortedEntries = new TreeSet<Map.Entry<Trace, Integer>>(
	        new Comparator<Map.Entry<Trace,Integer>>() {
	            @Override 
	            public int compare(Map.Entry<Trace,Integer> e1, Map.Entry<Trace, Integer> e2) {
	            	if(e1.getKey().timestamp > e2.getKey().timestamp)
	        			return -1;
	        		else if(e1.getKey().timestamp == e2.getKey().timestamp)
	        			return 0;
	        		else
	        			return 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	private static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<Trace,Integer>> caseDesc(Map<Trace,Integer> map) {
		SortedSet<Map.Entry<Trace,Integer>> sortedEntries = new TreeSet<Map.Entry<Trace, Integer>>(
	        new Comparator<Map.Entry<Trace,Integer>>() {
	            @Override 
	            public int compare(Map.Entry<Trace,Integer> e1, Map.Entry<Trace, Integer> e2) {
	            	if(e1.getKey().caseId > e2.getKey().caseId)
	        			return -1;
	        		else if(e1.getKey().caseId == e2.getKey().caseId)
	        			return 0;
	        		else
	        			return 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	private static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<Trace,Integer>> caseAsc(Map<Trace,Integer> map) {
		SortedSet<Map.Entry<Trace,Integer>> sortedEntries = new TreeSet<Map.Entry<Trace, Integer>>(
	        new Comparator<Map.Entry<Trace,Integer>>() {
	            @Override 
	            public int compare(Map.Entry<Trace,Integer> e1, Map.Entry<Trace, Integer> e2) {
	            	if(e1.getKey().caseId < e2.getKey().caseId)
	        			return -1;
	        		else if(e1.getKey().caseId == e2.getKey().caseId)
	        			return 0;
	        		else
	        			return 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	private static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<Trace,Integer>> random(Map<Trace,Integer> map) {	
		SortedSet<Map.Entry<Trace,Integer>> sortedEntries = new TreeSet<Map.Entry<Trace, Integer>>(
	        new Comparator<Map.Entry<Trace,Integer>>() {
	            @Override 	            
	            public int compare(Map.Entry<Trace,Integer> e1, Map.Entry<Trace, Integer> e2) {
		            int max = 100;
		            int min = -100;
		            int r = (int) Math.round(Math.random() * (max - min) + min);
		            //System.out.println(r);
	            	//if(Math.random() < 0.5) {
	            	//	return -2;
	            	//}else{
	            	//	return 1;
	            	//}
		            return r;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	private static <K,V extends Comparable<? super V>>
	Set<Entry<Trace, Integer>> unsorted(Map<Trace,Integer> map) {	
		
		Set<Map.Entry<Trace,Integer>> set = new HashSet<Map.Entry<Trace,Integer>>();
		for(Trace t : map.keySet()){
			Entry<Trace, Integer> entry = new AbstractMap.SimpleEntry<Trace, Integer>(t, 0);
			set.add(entry);
		}
	    return set;
	}

	/**
	 * Create a train and test dataset and save it as csv. 
	 * testRatio is the amount of test data
	 * @param testRatio
	 */
	public void trainTestSplit(String benchmarkName, double testRatio) {
		Set<Entry<Trace, Integer>> entries = this.getLog();
		
		List<String> data = new ArrayList<String>();		
		for (Entry<Trace, Integer> entry : entries) {
			for(Event e : entry.getKey().trace){
				String str = e.name() + "," + e.time() + "," + e.getCaseId();
				data.add(str);
			}
		}
		
		double trainEntries = entries.size()/(1-testRatio);
		
		try {
			File fouttrain = new File("logs/" + benchmarkName + "/" + benchmarkName + "_train.csv");
			FileOutputStream fostrain = new FileOutputStream(fouttrain);
			BufferedWriter bwtrain = new BufferedWriter(new OutputStreamWriter(fostrain));
			
			File fouttest = new File("logs/" + benchmarkName + "/" + benchmarkName + "_test.csv");
			FileOutputStream fostest = new FileOutputStream(fouttest);
			BufferedWriter bwtest = new BufferedWriter(new OutputStreamWriter(fostest));
			
			int c = 0;			
			for (String str : data) {
				if(c < trainEntries){
					bwtrain.write(str);
					bwtrain.newLine();
				}else{
					bwtest.write(str);
					bwtest.newLine();
				}
				c++;
			}
			bwtrain.close();
			bwtest.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	
	
	/**
	 * Split the dataset into a kfold datasets
	 * @param benchmarkName
	 * @param k
	 */
	public void kFoldCrossValidationSplit(String benchmarkName, int k) {
		ArrayList<Entry<Trace, Integer>> entries = new ArrayList<Entry<Trace, Integer>>(this.getLog());

		//System.out.println("actual entries size " + entries.size());
		
		double foldLength = ((double) entries.size())/(double) k;
		//System.out.println("fold length " + foldLength);
		ArrayList<List<Entry<Trace, Integer>>> folds = new ArrayList<List<Entry<Trace, Integer>>>();
		
		for(int i = 0; i < k; i++){
			List<Entry<Trace, Integer>> fold = new ArrayList<Entry<Trace, Integer>>();
			int startIndex = ((int) (i*foldLength));
			int endIndex = (int) ((i+1) * foldLength)-1;
			//System.out.println("fold " + i + " " + i*foldLength);
			//System.out.println("fold " + i + " start " + startIndex + " -- end " + endIndex);
			for(int a = startIndex; a <= endIndex; a++){
				fold.add(entries.get(a));
			}
			folds.add(fold);
		}
		
		Map<Integer, List<Integer>> trainFolds = new HashMap<Integer, List<Integer>>();
		Map<Integer, Integer> testFolds = new HashMap<Integer, Integer>();
		for(int i = 0; i < k; i++){
			List<Integer> f = new ArrayList<Integer>();
			for(int c = 0; c < k; c++){
				if(c==i)
					testFolds.put(i, c);
				else
					f.add(c);
			}
			trainFolds.put(i, f);
		}
		
		
		for(int i = 0; i < k; i++){
			try {
				
				File fouttrain = new File("logs/" + benchmarkName + "/" + benchmarkName + "_kfoldcv_"+ i +"_train.csv");
				FileOutputStream fostrain = new FileOutputStream(fouttrain);
				BufferedWriter bwtrain = new BufferedWriter(new OutputStreamWriter(fostrain));
				List<Integer> trainFold = trainFolds.get(i);
				for(Integer t : trainFold){
					for (Entry<Trace, Integer> entry : folds.get(t)) {
						for(Event e : entry.getKey().trace){
							String str = e.name() + "," + e.time() + "," + e.getCaseId();
							
							if(e.resources().size() > 0){
								for(int a = 1; a <= e.resources().size(); a++){
									 str += "," + e.resources().get("Resource" + a);
								}
							}
							
							bwtrain.write(str);
							bwtrain.newLine();
						}	
					}
				}
				bwtrain.close();
				
				
				File fouttest = new File("logs/" + benchmarkName + "/" + benchmarkName + "_kfoldcv_"+ i +"_test.csv");
				FileOutputStream fostest = new FileOutputStream(fouttest);
				BufferedWriter bwtest = new BufferedWriter(new OutputStreamWriter(fostest));
				int testFold = testFolds.get(i);
				for (Entry<Trace, Integer> entry : folds.get(testFold)) {
					for(Event e : entry.getKey().trace){
						String str = e.name() + "," + e.time() + "," + e.getCaseId();
						
						if(e.resources().size() > 0){
							for(int a = 1; a <= e.resources().size(); a++){
								 str += "," + e.resources().get("Resource" + a);
							}
						}
						
						bwtest.write(str);
						bwtest.newLine();
					}	
				}
				bwtest.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}



