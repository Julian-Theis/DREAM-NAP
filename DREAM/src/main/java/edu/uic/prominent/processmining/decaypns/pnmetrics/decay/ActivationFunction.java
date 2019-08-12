package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class ActivationFunction implements Serializable{

	final static Logger logger = Logger.getLogger(ActivationFunction.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7187885582920232621L;
	private String place;
	private double max;
	private double param;

	public ActivationFunction(String name, double max, double param){
		this.place = name;
		this.setMax(max);
		this.setParam(param);
		
		logger.info("Activation Function for Place " + this.place + " f(t)=" + this.max  + "-" + this.param + "*t");
	}
	
	public ActivationFunction(String name){
		this.place = name;
		this.setMax(10);
		this.setParam(0.00000005);
	}
	
	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getParam() {
		return param;
	}

	public void setParam(double param) {
		this.param = param;
	}
	
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}
	
	public double activate(long time){
		double val = this.max - this.param * time;
		if(val > 0){
			return val;
		}else{
			return 0.0;
		}
	}
}
