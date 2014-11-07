package gr.iti.mklab.method;
import gr.iti.mklab.data.ImageMetadata;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Abstract SimilaritySearch class that includes the needed variables for the performance 
 * of the similarity search and the calculation of the center-of-gravity of the k most similar images 
 * @author gkordo
 *
 */
public abstract class AbstractSimilaritySearch {

	protected int k, a;
	protected double threshold;

	// Constractor initialize the variables k, a and the threshold
	public AbstractSimilaritySearch(int k, int a, double threshold){
		this.k = k;
		this.a = a;
		this.threshold = threshold;
	}
	/**
	 * Calculation of the center-of-gravity of the k most similar images
	 * @param images : the list of image metadata objects
	 * @param mapSim : the map with the k most similar images and their similarity value
	 * @return the estimated location of the query image
	 */
	protected Double[] computeCoordination(List<ImageMetadata> images, Map<Integer,Double> mapSim){

		double [] loc = new double[3];
		Double[] coord = new Double[2];

		int i = 0;
		
		for(Entry<Integer, Double> entry: mapSim.entrySet()){
			i++;
			if(i<t){
				loc[0] += Math.pow(entry.getValue(),a)
						* Math.cos(images.get(entry.getKey()).getCoord()[1] * (Math.PI / 180D))
						* Math.cos(images.get(entry.getKey()).getCoord()[0] * (Math.PI / 180D)) / k;

				loc[1] += Math.pow(entry.getValue(),a)
						* Math.cos(images.get(entry.getKey()).getCoord()[1] * (Math.PI / 180D))
						* Math.sin(images.get(entry.getKey()).getCoord()[0] * (Math.PI / 180D)) / k;

				loc[2] += Math.pow(entry.getValue(),a)
						* Math.sin(images.get(entry.getKey()).getCoord()[1] * (Math.PI / 180D)) / k;
			}
		}
		coord[0] = (Double) (Math.atan2(loc[2], Math.sqrt(Math.pow(loc[0],2) 
				+ Math.pow(loc[1],2))) * (180D/Math.PI));

		coord[1] = (Double) (Math.atan2(loc[1], loc[0]) * (180D/Math.PI));

		return coord;
	}
}
