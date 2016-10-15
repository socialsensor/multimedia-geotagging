package gr.iti.mklab.main;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.mmcomms16.AmbiguityBasedSampling;
import gr.iti.mklab.mmcomms16.BuildingSampling;
import gr.iti.mklab.mmcomms16.GeographicalUniformSampling;
import gr.iti.mklab.mmcomms16.GeographicallyFocusedSampling;
import gr.iti.mklab.mmcomms16.TextBasedSampling;
import gr.iti.mklab.mmcomms16.TextDiversitySampling;
import gr.iti.mklab.mmcomms16.UserUniformSampling;
import gr.iti.mklab.mmcomms16.VisualSampling;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.Utils;
import net.sf.geographiclib.Geodesic;

/**
 * Function that calculate the result of a geolocation method based on the Karney's algorithm.
 * @author gkordo
 *
 */
public class Evaluation {

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.VisualSampling");

	/**
	 * Initialize the map of the results based on the given values.
	 * Lower range = 10^minRange
	 * Higher ranger = 10^maxRange
	 * 
	 * @param minRangeScale
	 * @param maxRangeScale
	 * @return
	 */
	private static Map<Integer, Integer> initializeResultMap(int minRangeScale, int maxRangeScale){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int i=minRangeScale;i<maxRangeScale;i++){
			map.put(i, 0);
		}
		return map;
	}

	/**
	 * Print the precision for different ranges, based on the total number of items provided.
	 * 
	 * @param estimationResultMap
	 * @param totalItems
	 */
	private static void printPrecisionResults(Map<Integer, Integer> estimationResultMap, 
			int totalItems, int minRangeScale, int maxRangeScale){

		for(int i=minRangeScale;i<maxRangeScale;i++){
			double precision = Math.pow(10, i);

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);

			logger.info("Precision @ " + precision + "km: " 
					+ df.format((float)estimationResultMap.get(i)/totalItems*100)+"%");
		}
	}

	/** 
	 * Main function
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception{

		Properties properties = new Properties();

		properties.load(new FileInputStream("eval.properties"));

		// input files
		String testFile = properties.getProperty("testFile");
		String placeFile = properties.getProperty("placeFile");
		String conceptFile = properties.getProperty("conceptFile");

		String resultFile = properties.getProperty("resultFile");

		// minimum and maximum ranges
		int minRangeScale = Integer.parseInt(properties.getProperty("minRangeScale"));
		int maxRangeScale = Integer.parseInt(properties.getProperty("maxRangeScale"));

		String sampling = properties.getProperty("sampling");

		Set<String> collection = null;

		// Sampling Strategies
		switch(sampling) {

		case "GUS" : // Geographical Uniform Sampling

			collection = (Set<String>) GeographicalUniformSampling.sample(testFile);
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			break;

		case "UUS" : // User Uniform Sampling

			collection = (Set<String>) UserUniformSampling.sample(testFile);
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			break;

		case "TBS" : // Text-based Sampling

			collection = (Set<String>) TextBasedSampling.sample(testFile);
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			break;

		case "TDS" : // Text Diversity Sampling

			collection = (Set<String>) TextDiversitySampling.sample(testFile);
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			break;

		case "GFS" : // Geographically Focused Sampling

			Map<String, Map<String, Set<String>>> places = (Map<String,
					Map<String, Set<String>>>) GeographicallyFocusedSampling.sample(placeFile);
			logger.info("----------Continents----------");
			evaluateMultiSets(resultFile, places.get("continents"), minRangeScale, maxRangeScale);
			logger.info("----------Countries----------");
			evaluateMultiSets(resultFile, places.get("countries"), minRangeScale, maxRangeScale);
			break;

		case "ABS" : // Ambiguity-based Sampling
			
			Map<Boolean, Set<String>> ambiuous = (Map<Boolean, Set<String>>)
			AmbiguityBasedSampling.sample(placeFile);
			logger.info("----------Ambiguous----------");
			evaluateSingleSet(resultFile, ambiuous.get(true), minRangeScale, maxRangeScale, false);
			logger.info("----------Non-Ambiguous----------");
			evaluateSingleSet(resultFile, ambiuous.get(false), minRangeScale, maxRangeScale,false);
			break;

		case "VS" : // Visual Sampling
			
			Map<String, Set<String>> concepts = (Map<String, Set<String>>)
			VisualSampling.sample(conceptFile);
			logger.info("----------Concepts----------");
			evaluateMultiSets(resultFile, concepts, minRangeScale, maxRangeScale);
			break;

		case "BS" : // Building Sampling
			
			collection = (Set<String>) BuildingSampling.sample(conceptFile);
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			break;

		default: // No Sampling
			logger.info("Sampling: No Strategy");
			evaluateSingleSet(resultFile, collection, minRangeScale, maxRangeScale, false);
			
		}

	}

	/**
	 * Calculate the precition and median error on a collection of images
	 * @param resultFile : file of the results
	 * @param collection : collection of image IDs
	 * @param minRangeScale : minimum precision range
	 * @param maxRangeScale : minimum precision range
	 * @param oneLine : print results in one line
	 */
	private static void evaluateSingleSet(String resultFile, Set<String> collection, 
			int minRangeScale, int maxRangeScale, boolean oneLine){

		// function that calculates the distance between two lat/lon points
		Geodesic geo = new Geodesic(6378.1370D, 298.257223563);		

		// Initialize result containers
		Map<Integer, Integer> estimationResultMap = initializeResultMap(minRangeScale, maxRangeScale);
		List<Double> distances = new ArrayList<Double>();

		// Estimated and total item counters
		int estimations = 0;

		// File reader
		EasyBufferedReader reader = new EasyBufferedReader(resultFile);
		String line;
		while((line = reader.readLine()) != null){
			if(collection == null || collection.contains(line.split("\t")[0])){
				try{
					// Pairs of lat/lon points
					Double[] estimatedLocation = 
						{Double.parseDouble(line.split("\t")[4]),
								Double.parseDouble(line.split("\t")[3])};
					Double[] groundTruthLocation = 
						{Double.parseDouble(line.split("\t")[2]),
								Double.parseDouble(line.split("\t")[1])};


					// calculate distance
					double distance = geo.Inverse(groundTruthLocation[0], groundTruthLocation[1],
							estimatedLocation[0], estimatedLocation[1]).s12;

					// store results
					for(int i=minRangeScale;i<maxRangeScale;i++){
						if(distance < Math.pow(10, i)){
							estimationResultMap.put(i, estimationResultMap.get(i) + 1);
						}
					}
					distances.add(distance);
					estimations++;

				}catch (ArrayIndexOutOfBoundsException e){ // line is not in the right format
					System.out.println(e.getMessage());
				}
			}
		}
		reader.close();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		// print results
		if(oneLine){
			logger.info("Items: " + estimations + "\tP@1km: " + df.format(
					(float)estimationResultMap.get(0)/estimations*100) + "%\tm.error: " +
					df.format(Utils.median(distances)) + "km");
		}else{
			logger.info("Total items: " + estimations);
			printPrecisionResults(estimationResultMap, estimations, minRangeScale, maxRangeScale);
			logger.info("Median Distance Error: " + df.format(Utils.median(distances)) + "km");
		}
	}

	/**
	 * 
	 * @param resultFile : file of the results
	 * @param collections : several collections of image IDs
	 * @param minRangeScale : minimum precision range
	 * @param maxRangeScale : minimum precision range
	 */
	private static void evaluateMultiSets(String resultFile, 
			Map<String, Set<String>> collections, int minRangeScale, int maxRangeScale) {
		for(Entry<String, Set<String>> entry:collections.entrySet()){
			logger.info(entry.getKey());
			evaluateSingleSet(resultFile, entry.getValue(), 0, 1, true);
		}
	}
}