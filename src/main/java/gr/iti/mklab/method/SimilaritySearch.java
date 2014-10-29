package gr.iti.mklab.method;
import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.util.MyHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class SimilaritySearch extends AbstractSimilaritySearch{

	public SimilaritySearch(int k, int a, double threshold){
		super(k,a,threshold);
	}


	public Double[] computeSimilarity(String imageTags, 
			List<ImageMetadata> images){

		Map<Integer,Double> mapSim = new HashMap<Integer,Double>();

		String [] testImageTags = imageTags.split(",");

		for (int i=0; i<images.size(); i++){

			double counter = 0.0;
			
			Set<String> trainImageTags = new HashSet<String>(Arrays.asList(images.get(i).getTags().split(",")));

			for (int j=0; j<testImageTags.length; j++){

				if (trainImageTags.contains(testImageTags[j])){
					counter += 1.0;
				}
			}

			double sjacc = counter / (testImageTags.length 
					+ trainImageTags.size() - counter);

			mapSim.put(i,sjacc);

		}

		int t = 0, size = k;
		Double[] coord = null;

		MyHashMap.sortByValues(mapSim);

		if(mapSim.size()<k){
			size = mapSim.size();
		}

		for (int i=0; i<size; i++){
			if (mapSim.get(i)>threshold){
				t++;
			}
		}

		if (t!=0){
			coord = computeCoordination(images, mapSim, t);
		}
		else{
			if (!mapSim.isEmpty()){
				coord = computeCoordination(images, mapSim, 1);
			}
		}
		
		return coord;
	}
}
