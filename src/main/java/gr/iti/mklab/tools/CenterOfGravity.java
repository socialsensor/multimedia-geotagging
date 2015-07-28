package gr.iti.mklab.tools;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract class that execute the calculation of the center-of-gravity of the most similar images 
 * @author gkordo
 *
 */
public abstract class CenterOfGravity {

	protected static int a;

	// Contractor initialize a variable
	public CenterOfGravity(int a){
		CenterOfGravity.a = a;
	}
	
	/**
	 * Calculation of the center-of-gravity of the k most similar images
	 * @param mapSim : the map with the k most similar images and their similarity values
	 * @return the estimated location of the query image
	 */
	protected static Double[] computeCoordination(Map<String,Double> mapSim){

		double [] loc = new double[3];
		Double[] c = new Double[2];
		int k = mapSim.size();

		for (Entry<String, Double> entry:mapSim.entrySet()){

			double sim = entry.getValue();
			double lat = Double.parseDouble(entry.getKey().split("_")[1]);
			double lon = Double.parseDouble(entry.getKey().split("_")[0]);

			loc[0] += Math.pow(sim,a)
					* Math.cos(lat * (Math.PI / 180D))
					* Math.cos(lon * (Math.PI / 180D)) / k;

			loc[1] += Math.pow(sim,a)
					* Math.cos(lat * (Math.PI / 180D))
					* Math.sin(lon * (Math.PI / 180D)) / k;

			loc[2] += Math.pow(sim,a)
					* Math.sin(lat * (Math.PI / 180D)) / k;

			c[0] = (Double) (Math.atan2(loc[2], Math.sqrt(Math.pow(loc[0],2) 
					+ Math.pow(loc[1],2))) * (180D/Math.PI));

			c[1] = (Double) (Math.atan2(loc[1], loc[0]) * (180D/Math.PI));
		}
		return c;
	}
}

