package edu.uic.prominent.processmining.decaypns;

import edu.uic.prominent.processmining.decaypns.util.BenchmarkDataset;

public class CleanUp {
	public static void main(String args[]) {
		String benchmark = args[0];
		String folder = "models";
		int folds = 10;
		BenchmarkDataset dataset = new BenchmarkDataset();
		dataset.cleanUp(benchmark, folder, 2, folds);
	}
}
