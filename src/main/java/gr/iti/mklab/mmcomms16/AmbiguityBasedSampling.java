package gr.iti.mklab.mmcomms16;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Utils;

@SuppressWarnings("unchecked")
public class AmbiguityBasedSampling extends Sampling{

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.AmbiguityBasedSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Ambiguity-based Strategy");
		
		AmbiguityBasedSampling sampling = 
				new AmbiguityBasedSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Double> ambiguous =
				computeCityEntropies(loadOccurrences(testFile));
		logger.info(ambiguous.size() + " Towns loaded");
		
		Map<String, Boolean> images = new
				HashMap<String, Boolean>();
		double median = Utils.medianItemDouble(ambiguous);
		
		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){
			String imageID = line.split("\t")[0];
			for(String place:line .split("\t")[1].split(",")){
				if(place.split(":").length>2 
						&& place.split(":")[2].contains("Town")){
					if(ambiguous.containsKey(place.split(":")[1]) &&
							ambiguous.get(place.split(":")[1])>median){
						images.put(imageID, true);
					}else{
						images.put(imageID, false);
					}
				}
			}
		}
		reader.close();
		return images;
	}

	protected Object writeInFile(Object data) {

		Map<String, Boolean> images = 
				(Map<String, Boolean>) data;

		Map<Boolean, Set<String>> respond = new
				HashMap<Boolean, Set<String>>();
		
		respond.put(true, new HashSet<String>());
		respond.put(false, new HashSet<String>());
		
		EasyBufferedWriter writerA = new EasyBufferedWriter(
				"samples/ambiguous_sampling.txt");
		EasyBufferedWriter writerN = new EasyBufferedWriter(
				"samples/non_ambiguous_sampling.txt");
		for(Entry<String, Boolean> image:images.entrySet()){
			respond.get(image.getValue()).add(image.getKey());
			if(image.getValue()){
				writerA.write(image.getKey());
				writerA.newLine();
			}else{
				writerN.write(image.getKey());
				writerN.newLine();
			}
		}
		writerA.close();
		writerN.close();
		
		return respond;
	}

	private static double computeEntropyNaive(
			final List<Double> probabilities, int total) {
		double entropy = 0.0;
		for (Double p:probabilities) {
			p /= total;
			if(p!=0.0){
				entropy -= p * Math.log(p);
			}
		}
		return entropy;
	}

	private static Map<String, Double> computeCityEntropies(
			Map<String, Map<String, Integer>> townNames) {
		Map<String, Double> ambiguous = new HashMap<String, Double>();

		for(Entry<String, Map<String, Integer>> town:townNames.entrySet()){
			List<Double> p = new ArrayList<Double>();
			int total = 0;
			for(Entry<String, Integer> code:town.getValue().entrySet()){
				p.add((double) code.getValue());
				total += code.getValue();
			}
			double entropy = computeEntropyNaive(p, total);
			if(entropy > 0.0)
				ambiguous.put(town.getKey(), entropy);
		}

		return ambiguous;
	}

	private static Map<String, Map<String, Integer>> 
	loadOccurrences(String testFile) {

		Map<String, Map<String, Integer>> townNames = 
				new HashMap<String, Map<String, Integer>>();

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){
			for(String place:line .split("\t")[1].split(",")){
				if(place.split(":").length>2 && place.split(":")[2].contains("Town")){
					String townCode = place.split(":")[0];
					String townName = place.split(":")[1];

					if(townNames.containsKey(townName)){
						if(townNames.get(townName).containsKey(townCode)){
							townNames.get(townName).put(townCode, 
									townNames.get(townName).get(townCode) + 1);
						}else{
							townNames.get(townName).put(townCode, 1);
						}
					}else{
						townNames.put(townName, new HashMap<String, Integer>());
						townNames.get(townName).put(townCode, 1);
					}
				}
			}
		}
		reader.close();

		return townNames;
	}
}
