package gr.iti.mklab.method;

import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.util.MyHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Class that implements similarity search for a query image in a list of images calculating the similarity between the corresponding tags
 * @author gkordo
 *
 */
public class SimilaritySearch extends AbstractSimilaritySearch{
	
	// Constractor initializes the required values of the method
	public SimilaritySearch(int k, int a, double threshold){
		super(k,a,threshold);
	}

	public Double[] computeSimilarity(String imageTags, 
			List<ImageMetadata> images){

		Map<Integer,Double> mapSim = new HashMap<Integer,Double>();

		String [] testImageTags = imageTags.split(",");

		// calculate similarity for every image of the given set
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

		// check if the silimar images are less than k
		if(mapSim.size()<k){
			size = mapSim.size();
		}

		// check how many images are above the provided threshold
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
