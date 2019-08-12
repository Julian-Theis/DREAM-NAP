package edu.uic.prominent.processmining.decaypns.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Predictions {

	List<double[]> markings;
	List<Integer> trueLabels;
	List<Integer> predictedLabels;
	
	public Predictions(String file) {
		this.markings = new ArrayList<double[]>();
		this.trueLabels = new ArrayList<Integer>();
		this.predictedLabels = new ArrayList<Integer>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        String[] arr = line.split(",");
		        double[] marking = new double[arr.length-2];
		        for(int i = 0; i < arr.length; i++) {
		        	if(i < marking.length) {
		        		marking[i] = Double.parseDouble(arr[i]);
		        	}else if(i == marking.length) {
		        		trueLabels.add(Integer.parseInt(arr[i]));
		        	}else{
		        		predictedLabels.add(Integer.parseInt(arr[i]));
		        	}
		        }
		        markings.add(marking);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int size() {
		return this.markings.size();
	}
	
	public double[] getMarking(int marking) {
		return this.markings.get(marking);
	}
	
	public int getTrueLabel(int index) {
		return this.trueLabels.get(index);
	}
	
	public int getPredictedLabel(int index) {
		return this.predictedLabels.get(index);
	}
}
