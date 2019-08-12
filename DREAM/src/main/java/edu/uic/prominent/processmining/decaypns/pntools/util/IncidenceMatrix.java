package edu.uic.prominent.processmining.decaypns.pntools.util;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class IncidenceMatrix {

	public int d[][];

	final static Logger logger = Logger.getLogger(IncidenceMatrix.class);

	public IncidenceMatrix(JSONObject matrix) {
		try {
			JSONArray rows = matrix.getJSONArray("rows");
			int trans = rows.length();
			int places = rows.getJSONArray(0).length();
			this.d = new int[trans][places];
			
			JSONArray row;
			for(int r = 0; r < trans; r++) {
				row = rows.getJSONArray(r);
				for(int c = 0; c < places; c++) {
					d[r][c] = row.getInt(c);
				}
			}			
		} catch (Exception e) {
			Log.error(e);
		}
	}

	public void printMatrix() {
		for (int i = 0; i < d.length; i++)
			for (int j = 0; j < d[i].length; j++)
				logger.info(d[i][j] + " ");
	}
	
	public int numPlaces() {
		return d[0].length;
	}
	
	public int numTransitions() {
		return d.length;
	}
	
	public int getValue(int transition, int place) {
		return d[transition][place];
	}
}
