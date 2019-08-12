package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

public class PerfMeasurement {
	
	private long firingTime;
	private String transitionName;
	
	
	public PerfMeasurement(String transitionName, long firingTime){
		this.transitionName = transitionName;
		this.firingTime = firingTime;
	}
	
	public String name(){
		return transitionName;
	}
	
	public long time(){
		return firingTime;
	}
	
	public String toString(){
		return transitionName + " " + firingTime;
	}
}
