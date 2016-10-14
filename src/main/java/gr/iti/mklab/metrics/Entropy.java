package gr.iti.mklab.metrics;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

/**
 * Entropy class update the file that contains the tag-cell probabilities with the spatial entropy of every individual tag.
 * Calculate the spatial tag entropy for all of the tags. Entropy is used for feature weighting.
 * @author gkordo
 *
 */
public class Entropy {

	static Logger logger = Logger.getLogger("gr.iti.mklab.method.Entropy");

	/**
	 * Calculate the Spatial Entropy weights of the LM terms
	 * @param dir : project directory
	 * @param fileTermCell : Term-Cell probability file
	 */
	public static void calculateEntropyWeights(String dir, String fileTermCell){

		logger.info("Process: Spatial Entropy weights calculation\t|\t"
				+ "Status: INITIALIZE");

		new File(dir + "Weights").mkdir();
		
		// Term Spatial Entropy calculation
		EasyBufferedReader reader = new EasyBufferedReader(dir + fileTermCell);
		Map<String, Double> termSpatialEntropy = new HashMap<String, Double>();
		long sTime = System.currentTimeMillis();
		String line;
		while ((line=reader.readLine())!=null){
			termSpatialEntropy.put(line.split("\t")[0],
					computeEntropyNaive(line.split("\t")[1].split(" ")));
		}
		reader.close();
		double[] termSpatialEntropyValues = termSpatialEntropy
				.values().stream().mapToDouble(d -> d).toArray();
		NormalDistribution gd = new NormalDistribution( // Gaussian function for re-weighting
				new Mean().evaluate(termSpatialEntropyValues),
				new StandardDeviation().evaluate(termSpatialEntropyValues));
		double gdMax = gEntropyMax(gd, termSpatialEntropyValues); // Nomalize factor

		logger.info("Process: Spatial Entropy weights calculation\t|\t"
				+ "Status: STARTED");
		// Spatial Entropy weights calculation of terms
		EasyBufferedWriter writer = new EasyBufferedWriter(
				dir + "Weights/spatial_entropy_weights");
		for(Entry<String, Double> term:termSpatialEntropy.entrySet()){
				writer.write(term.getKey() + "\t" +
						gd.density(term.getValue())/gdMax);
				writer.newLine();
		}

		logger.info("Process: Spatial Entropy weights calculation\t|\t"
				+ "Status: COMPLETED\t|\tTotal time: " +
				(System.currentTimeMillis()-sTime)/1000.0 + "s");
		writer.close();	
	}

	/**
	 * Shannon entropy formula
	 * @param probabilities : probability distribution
	 * @return
	 */
	private static double computeEntropyNaive(String[] probabilities) {
		double entropy = 0.0;
		for (int i=0;i< probabilities.length;i++) {
			final double p = Double.parseDouble(probabilities[i].split(">")[1]);
			if(p != 0.0){
				entropy -= p * Math.log(p);
			}
		}
		return entropy;
	}
	
	/**
	 * Calculate the max probability value applying the Gaussian functionon the
	 * probability distribution
	 * @param gd : Gaussian function
	 * @param p : probability distribution
	 * @return max weight
	 */
	private static Double gEntropyMax(NormalDistribution gd, double[] p){
		Double max = 0.0;
		for(Double tmp:p){
			if(max < gd.density(tmp)){
				max = gd.density(tmp);
			}
		}
		return max;
	}
}