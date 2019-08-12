package edu.uic.prominent.processmining.decaypns.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.uic.prominent.processmining.decaypns.log.EventLog;
import edu.uic.prominent.processmining.decaypns.log.EventLogParser;
import edu.uic.prominent.processmining.decaypns.log.XLogLoader;
import edu.uic.prominent.processmining.decaypns.pnmetrics.util.Constants;

public class BenchmarkDataset {
	final static Logger logger = Logger.getLogger(BenchmarkDataset.class);
	
	public Map<String, Map<String, Integer>> benchmarkConfigurations;
	public Map<String, Map<String, Integer>> benchmarkResources;
	public Map<String, Map<String, String>> benchmarkMetadata;
	
	public BenchmarkDataset(){
		this.setUpBenchmarkConfigurations();
	}
	
	private void setUpBenchmarkConfigurations(){
		benchmarkConfigurations = new HashMap<String, Map<String, Integer>>();
		benchmarkResources = new HashMap<String, Map<String, Integer>>();
		benchmarkMetadata = new HashMap<String, Map<String, String>>();
		
		Map<String, Integer> resources = new HashMap<String, Integer>();
		
		// Helpdesk
			Map<String, Integer> config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("helpdesk", config);
			
			Map<String, String> metadata = new HashMap<String, String>();
			metadata.put("isLong", "false");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "0");
			benchmarkMetadata.put("helpdesk", metadata);
			
			resources = new HashMap<String, Integer>();
			benchmarkResources.put("helpdesk", resources);
		//--------->
			
			
		// bpic12_work_complete
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_work_complete", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_work_complete", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_work_complete", resources);
		//--------->
			
		// bpic12_all_complete
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_all_complete", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_all_complete", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_all_complete", resources);
		//--------->
			
			
		// bpic12_a
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_a", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_a", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_a", resources);
		//--------->

		//--------->
			
			
		// bpic12_o
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_o", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_o", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_o", resources);
		//--------->
			
			
		// bpic12_all
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_all", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_all", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_all", resources);
		//--------->
			
			// bpic12_all
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_all_lfc", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_all_lfc", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_all_lfc", resources);
		//--------->
			
		// bpic12_work_all
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_work_all", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_work_all", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_work_all", resources);
		//--------->
			
		// bpic12_work_all_lfc
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_work_all_lfc", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "2");
			metadata.put("excludeResources", "");
			benchmarkMetadata.put("bpic12_work_all_lfc", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			benchmarkResources.put("bpic12_work_all_lfc", resources);
		//--------->
			
		// bpic13_i
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic13_i", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "7");
			metadata.put("excludeResources", "Resource1,Resource4,Resource7");
			benchmarkMetadata.put("bpic13_i", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			resources.put("Resource3", 5);
			resources.put("Resource4", 6);
			resources.put("Resource5", 7);
			resources.put("Resource6", 8);
			resources.put("Resource7", 9);
			benchmarkResources.put("bpic13_i", resources);
		//--------->
			
		// bpic13_i
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic13_i_lfc", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "7");
			metadata.put("excludeResources", "Resource1,Resource4,Resource7");
			benchmarkMetadata.put("bpic13_i_lfc", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			resources.put("Resource3", 5);
			resources.put("Resource4", 6);
			resources.put("Resource5", 7);
			resources.put("Resource6", 8);
			resources.put("Resource7", 9);
			benchmarkResources.put("bpic13_i_lfc", resources);
		//--------->
			
		// bpic13_p
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic13_p", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_RANDOM);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "8");
			//metadata.put("excludeResources", "");
			metadata.put("excludeResources", "Resource1,Resource4,Resource7");
			benchmarkMetadata.put("bpic13_p", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			resources.put("Resource3", 5);
			resources.put("Resource4", 6);
			resources.put("Resource5", 7);
			resources.put("Resource6", 8);
			resources.put("Resource7", 9);
			resources.put("Resource8", 10);
			benchmarkResources.put("bpic13_p", resources);
		//--------->
			
			// bpic13_p_lfc
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic13_p_lfc", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_RANDOM);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "8");
			//metadata.put("excludeResources", "");
			metadata.put("excludeResources", "Resource1,Resource4,Resource7");
			benchmarkMetadata.put("bpic13_p_lfc", metadata);
			
			resources = new HashMap<String, Integer>();
			resources.put("Resource1", 3);
			resources.put("Resource2", 4);
			resources.put("Resource3", 5);
			resources.put("Resource4", 6);
			resources.put("Resource5", 7);
			resources.put("Resource6", 8);
			resources.put("Resource7", 9);
			resources.put("Resource8", 10);
			benchmarkResources.put("bpic13_p_lfc", resources);
		//--------->
			
			
		// bpic12_all_append
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_all_append", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			benchmarkMetadata.put("bpic12_all_append", metadata);
		//--------->
			
			
		// bpic12_work_all_append
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("bpic12_work_all_append", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			benchmarkMetadata.put("bpic12_work_all_append", metadata);
		//--------->
			
			
		// helpdesk_inverse
			config = new HashMap<String, Integer>();
			config.put("Timestamp", 2);
			config.put("Activity", 1);
			config.put("Case", 0);
			benchmarkConfigurations.put("helpdesk_inverse", config);
			
			metadata = new HashMap<String, String>();
			metadata.put("isLong", "true");
			metadata.put("isTimestamp", "true");
			metadata.put("timestampFormat", "yyyy-MM-dd HH:mm:ss");
			metadata.put("ordering", Constants.ORDERING_BYCASEIDASC);
			metadata.put("timebase", Constants.DECAYVECTORPREDICTION_TIMEBASE_T0);
			metadata.put("numResources", "0");
			benchmarkMetadata.put("helpdesk_inverse", metadata);
			
			resources = new HashMap<String, Integer>();
			benchmarkResources.put("helpdesk_inverse", resources);
			
	}
	
	public EventLog getLog(String key){
		String logfile = "logs/" + key + "/" + key + ".csv";
		XLogLoader logLoader = new XLogLoader(benchmarkConfigurations.get(key), benchmarkResources.get(key));
    	logLoader.importCSV(logfile,
    			Boolean.parseBoolean(benchmarkMetadata.get(key).get("isLong")), 
    			Boolean.parseBoolean(benchmarkMetadata.get(key).get("isTimestamp")),
    			benchmarkMetadata.get(key).get("timestampFormat"));
    	
    	logLoader.saveAsXES(key + "/" + key + ".xes");
    	return new EventLogParser().getEventLogFromFile(key + "/" + key + ".xes");
	}

	public EventLog getLogTrain(String key) {
		String logfile = "logs/" + key + "/" + key + "_train.csv";
		XLogLoader logLoader = new XLogLoader();
    	logLoader.importCSV(logfile,true,false, "yyyy-MM-dd HH:mm:ss");
    	logLoader.saveAsXES(key + "/" + key + "_train.xes");
    	return new EventLogParser().getEventLogFromFile(key + "/" + key + "_train.xes");
	}
	
	public EventLog getLogTest(String key) {
		String logfile = "logs/" + key + "/" + key + "_test.csv";
		XLogLoader logLoader = new XLogLoader();
    	logLoader.importCSV(logfile,true,false, "yyyy-MM-dd HH:mm:ss");
    	logLoader.saveAsXES(key + "/" + key + "_test.xes");
    	return new EventLogParser().getEventLogFromFile(key + "/" + key + "_test.xes");
	}
	
	// CV functions
	public EventLog getLogCVTrain(String key, int fold) {
		String logfile = "logs/" + key + "/" + key + "_kfoldcv_" + fold + "_train.csv";
		XLogLoader logLoader = new XLogLoader();
    	logLoader.importCSV(logfile,true,false, "yyyy-MM-dd HH:mm:ss", Integer.parseInt(benchmarkMetadata.get(key).get("numResources")));
    	logLoader.saveAsXES(key + "/" + key + "_kfoldcv_" + fold + "_train.xes");
    	return new EventLogParser().getEventLogFromFile(key + "/" + key + "_kfoldcv_" + fold + "_train.xes");
	}
	
	public EventLog getLogCVTest(String key, int fold) {
		String logfile = "logs/" + key + "/" + key + "_kfoldcv_" + fold + "_test.csv";
		XLogLoader logLoader = new XLogLoader();
    	logLoader.importCSV(logfile,true,false, "yyyy-MM-dd HH:mm:ss", Integer.parseInt(benchmarkMetadata.get(key).get("numResources")));
    	logLoader.saveAsXES(key + "/" + key + "_kfoldcv_" + fold + "_test.xes");
    	return new EventLogParser().getEventLogFromFile(key + "/" + key + "_kfoldcv_" + fold + "_test.xes");
	}
	
	public void writeCVMinedPetriNetParameters(String benchmarkName, JSONObject json){
		try {
			File fout = new File("logs/" + benchmarkName + "/" + benchmarkName + "_CVminedPNParameters.json");
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(json.toString());	
			bw.close();
			logger.info("Cross Validated Petri Net Mining: Wrote parameters for " + benchmarkName + " to file.");
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
	
	public JSONObject getParameterJSONfromCVMinedPetriNetParameters(String benchmarkName, int fold){
		JSONObject obj = null;
		try {
	        File f = new File("logs/" + benchmarkName + "/" + benchmarkName + "_CVminedPNParameters.json");
	        if (f.exists()){
	            InputStream is = new FileInputStream("logs/" + benchmarkName + "/" + benchmarkName + "_CVminedPNParameters.json");
	            String jsonTxt = IOUtils.toString(is, "UTF-8");
	            JSONObject json = new JSONObject(jsonTxt);    
	            obj = json.getJSONObject(""+fold);
	        }
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return obj;
	}
	
	public void cleanUp(String benchmark, String folderName, int algo, int folds){
		JSONObject obj = null;
		String pattern = benchmark + "_" + algo + "_kfoldcv_";
		try {
	        File f = new File("logs/" + benchmark + "/" + benchmark + "_CVminedPNParameters.json");
	        if (f.exists()){
	            InputStream is = new FileInputStream("logs/" + benchmark + "/" + benchmark + "_CVminedPNParameters.json");
	            String jsonTxt = IOUtils.toString(is, "UTF-8");
	            obj = new JSONObject(jsonTxt);    
	        }
	        
			List<String> modelsToKeep = new ArrayList<String>();
			for(int i = 0; i < folds; i++){
				Double param1 = obj.getJSONObject(""+i).getDouble("param1");
				Double param2 = obj.getJSONObject(""+i).getDouble("param2");
				String name = benchmark + "_" + algo + "_kfoldcv_" + i + "_" + param1 + "_" + param2 + "_model.pnml";
				modelsToKeep.add(name);
			}
			
			File folder = new File(folderName);
			File[] listOfFiles = folder.listFiles();
			
			for (int i = 0; i < listOfFiles.length; i++) {
				  if (listOfFiles[i].isFile() && 
						  listOfFiles[i].getName().contains(pattern) && 
						  !modelsToKeep.contains(listOfFiles[i].getName())) {
					  listOfFiles[i].delete();
				  }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getFileNameOfPn(String benchmark, int algo, int fold) {
		String name = "";
		try {
	        File f = new File("logs/" + benchmark + "/" + benchmark + "_CVminedPNParameters.json");
	        if (f.exists()){
	            InputStream is = new FileInputStream("logs/" + benchmark + "/" + benchmark + "_CVminedPNParameters.json");
	            String jsonTxt = IOUtils.toString(is, "UTF-8");
	            JSONObject obj = new JSONObject(jsonTxt);    
	            
				Double param1 = obj.getJSONObject(""+fold).getDouble("param1");
				Double param2 = obj.getJSONObject(""+fold).getDouble("param2");
				name = benchmark + "_" + algo + "_kfoldcv_" + fold + "_" + param1 + "_" + param2 + "_model.pnml";
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
		return name;
	}
}
