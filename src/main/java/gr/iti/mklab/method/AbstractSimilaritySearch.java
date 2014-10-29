package gr.iti.mklab.method;
import gr.iti.mklab.data.ImageMetadata;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractSimilaritySearch {

	protected int k, a;
	protected double threshold;

	public AbstractSimilaritySearch(int k, int a, double threshold){
		this.k = k;
		this.a = a;
		this.threshold = threshold;
	}

	protected Double[] computeCoordination(List<ImageMetadata> images, Map<Integer,Double> mapSim, int t){

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
