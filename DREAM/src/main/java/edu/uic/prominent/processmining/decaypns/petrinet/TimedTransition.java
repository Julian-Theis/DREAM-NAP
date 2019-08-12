package edu.uic.prominent.processmining.decaypns.petrinet;

import org.nd4j.linalg.api.rng.distribution.BaseDistribution;

@SuppressWarnings("serial")
public class TimedTransition extends Transition{

	private BaseDistribution distribution;
	private String distributionType;
	
	
	public TimedTransition(String name, Object distribution) {
		super(name);
		this.distribution = (BaseDistribution) distribution;
		this.distributionType = distribution.getClass().getName();
	}
	
	public TimedTransition(Transition t, Object distribution) {
		super(t.name(), t.getInputs(), t.getOutputs(), t.isVisible());
		this.distribution = (BaseDistribution) distribution;
		this.distributionType = distribution.getClass().getName();
	}
	
	public TimedTransition(String name, boolean visible) {
		super(name, visible);
	}
	
	public double sample(){
		return Math.abs(distribution.sample());
	}
	
	public String getDistributionType(){
		return this.distributionType;
	}

}
