package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import java.util.List;

import org.apache.log4j.Logger;

public class DecayVectorPrediction {
	final static Logger logger = Logger.getLogger(DecayVectorPrediction.class);
	
	public double[] vector;
	public double[] vectorCounter;
	public double[] marking;
	public String event;
	public int caseId;
	long time;
	
	public List<String> resources;
	
	public DecayVectorPrediction(double[] vector, double[] vectorCounter, double[] marking, String event, long time, int caseId, List<String> resources){
		this.vector = vector;
		this.vectorCounter = vectorCounter;
		this.marking = marking;
		this.event = event;
		this.time = time;
		this.caseId = caseId;
		
		this.resources = resources;
	}
	
	public void addResource(String resource){
		this.resources.add(resource);
	}
		
	public void setEvent(String event){
		this.event = event;
	}
	
	public void setTime(long time){
		this.time = time;
	}
	
	@Override
	public String toString(){
		String str = "";
		for(double a : vector){
			str = str + "" + a + ";";
		}
		for(double a : vectorCounter){
			str = str + "" + a + ";";
		}
		for(double a : marking){
			str = str + "" + a + ";";
		}
		str += event ; //+ ";" + caseId;
		
		for(String res : resources){
			str += ";" + res;
		}
		return str;
	}


}
