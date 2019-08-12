package edu.uic.prominent.processmining.decaypns.pnmetrics.decay;

import java.util.ArrayList;
import java.util.List;

public class TimeInfo {
	private List<Long> times;
	
	public long mean;
	public long stddev;
	public long range;
	
	public TimeInfo(){
		times = new ArrayList<Long>();
	}
	
	public void addTime(long l){
		times.add(l);
	}
	
	public void calculate(){
		Long sum = 0L;
		for(Long l : times){
			sum += l;
		}
		this.mean = (long) sum/this.times.size();
	}	
}
