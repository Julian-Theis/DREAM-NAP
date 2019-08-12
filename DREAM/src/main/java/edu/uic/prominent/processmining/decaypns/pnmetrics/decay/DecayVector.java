package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import edu.uic.prominent.processmining.decaypns.petrinet.Place;

public class DecayVector implements Serializable{
	final static Logger logger = Logger.getLogger(DecayVector.class);
	
	private static final long serialVersionUID = -2905434505216962745L;
	private int size;
	private double[] vector;
	private double[] vectorCounter;
	private List<Place> places;
	List<Activation> activations;
	
	Map<String, ActivationFunction> activationFunctions;
	
	List<String> resources;
	
	private long lasttime;
	
	/**
	 * Create a DecayVector with default Activation Functions and parameters
	 * @param places
	 */
	public DecayVector(List<Place> places){
		this.size = places.size();
		this.places = places;
		this.vector = new double[this.size];
		this.vectorCounter = new double[this.size];
		this.activations = new ArrayList<Activation>();
		
		this.setDefaultActivationFunctions();
		
		resources = new ArrayList<String>();
		resetVectorCounter();
	}
	
	public DecayVector(List<Place> places, Map<Place, EstimationStatistic> estimatedParameters){
		this.size = places.size();
		this.places = places;
		this.vector = new double[this.size];
		this.vectorCounter = new double[this.size];
		this.activations = new ArrayList<Activation>();
		
		this.setActivationFunctions(estimatedParameters);
		resources = new ArrayList<String>();
		
		resetVectorCounter();
	}
	
	private void setDefaultActivationFunctions(){
		activationFunctions = new HashMap<String, ActivationFunction>();
		for(Place p : places){
			activationFunctions.put(p.name(), new ActivationFunction(p.name()));
		}
	}
	
	private void setActivationFunctions(Map<Place, EstimationStatistic> estimatedParameters){
		activationFunctions = new HashMap<String, ActivationFunction>();
		for(Place p : places){
			if(estimatedParameters.containsKey(p)){
				activationFunctions.put(p.name(), new ActivationFunction(p.name(), 10.0, estimatedParameters.get(p).parameter));
			}else{
				activationFunctions.put(p.name(), new ActivationFunction(p.name()));
			}
			
			//System.out.println(p.name());
			//System.out.println(estimatedParameters.get(p).parameter);
		}
	}
	
	public double[] getVector(){
		return this.vector;
	}
	
	public double[] getCounterVector(){
		return this.vectorCounter;
	}
	
	public void resetVectorCounter(){
		for(int i = 0; i < vector.length; i++){
			vectorCounter[i] = 0.0;
		}
		lasttime = 0L;
	}
 	
	public void reset(){
		this.activations = new ArrayList<Activation>();
		for(int i = 0; i < vector.length; i++){
			vector[i] = 0.0;
			resetVectorCounter();
		}
		resources = new ArrayList<String>();
	}
	
	public void addResource(String resource){
		this.resources.add(resource);
	}
	
	public List<String> getResources(){
		return this.resources;
	}
	
	private void initialVector(){
		for(int i = 0; i < vector.length; i++){
			vector[i] = 0.0;
		}
	}
	
	public void moveTime(long time) {
		long difference = time - this.lasttime;
		//System.out.println(this.lasttime + " " + time + " " + difference)
		updateActivations(difference);
		calculateVector();
		this.lasttime = time;
	}
	
	public void placeActivation(Place p, long time){
		if(this.activations.isEmpty()){
			this.lasttime = time;
		}

		long difference = time - this.lasttime;
		
		updateActivations(difference);
		
		int index = getIndexOfPlace(p);
		
		vectorCounter[index] = vectorCounter[index] + 1;
		
		Activation a = new Activation(index, 0L);
		activations.add(a);
		
		calculateVector();

		this.lasttime = time;
	}
	
	private void calculateVector(){
		initialVector();
		for(Activation a : activations){
			this.vector[a.index] = this.activate(a.time, a.index);
		}
	}
	
	private void updateActivations(long difference){
		for(Activation a : activations){
			a.setTime(a.getTime() + difference);
		}
	}
	
	private int getIndexOfPlace(Place p){
		int index = -1;
		for(int i = 0; i < this.places.size(); i++){
			if(places.get(i).name().equals(p.name())){
				index = i;
				break;
			}
		}
		return index;
	}
	
	private String getPlaceByIndex(int index){
		return places.get(index).name();
	}
	
	
	private double activate(long time, int placeIndex){
		return activationFunctions.get(getPlaceByIndex(placeIndex)).activate(time);
	}
	
	public void printVector(){
		String out = "";
		for(int i = 0; i < vector.length; i++){
			out += vector[i] + ", ";
		}
		for(int i = 0; i < vectorCounter.length; i++){
			out += vectorCounter[i] + ", ";
		}		
		logger.info("Printvector " + out);
	}	
}
