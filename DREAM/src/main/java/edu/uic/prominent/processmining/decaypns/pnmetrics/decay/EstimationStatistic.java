package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class EstimationStatistic implements Serializable {
	private static final long serialVersionUID = -3668192013595867436L;
	final static Logger logger = Logger.getLogger(EstimationStatistic.class);
	
	public long min;
	public long max;
	public double mean;
	public double stddev;
	public double parameter;
	
	public EstimationStatistic(){
	}
	
	public void calculateParameter(){
		if(mean != -1.0){
			parameter = 10/(mean);//-stddev); //+(stddev*stddev));
			logger.info("Estimated Parameter: " + parameter);
		}else{
			parameter = 10/(((double) max)); ///2.0);
			logger.info("Estimated Parameter: " + parameter);
		}
	}
}
