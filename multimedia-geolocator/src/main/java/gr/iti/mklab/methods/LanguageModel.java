package gr.iti.mklab.methods;

import gr.iti.mklab.geo.GeoCell;
import gr.iti.mklab.util.GeoCellCoder;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.MyHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * It is the implementation of the language model. Here the word-cell probabilities are loaded and all calculation for the estimated location take place.
 * The model calculate the cell probabilities summing up the word-cell probabilities for every different cell based on the words that are contained in the query sentence.
 * 		  S
 * p(c) = Σ p(w|c)*N(e)
 * 		 w=1
 * The cell with that maximizes this summation considering as the Most Likely Cell for the query sentence.
 * @author gkordo
 *
 */
public class LanguageModel {

	protected Map<String,Map<Long,Double>> wordCellProbsMap;

	protected Map<String,Double[]> wordWeights;
	protected NormalDistribution gdWeight;

	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.LanguageModel");

	// The function that compose the other functions to calculate and return the Most Likely Cell for a query tweet.
	public GeoCell calculateLanguageModel(Set<String> sentenceWords) {

		Map<Long, GeoCell> cellMap = calculateCellsProbForImageTags(sentenceWords);

		GeoCell mlc = findMLC(cellMap);

		return mlc;
	}

	// find the Most Likely Cell.
	private GeoCell findMLC(Map<Long, GeoCell> cellMap) {

		cellMap = MyHashMap.sortByMLCValues(cellMap);

		GeoCell mlc = null;

		if (!cellMap.isEmpty()){
			Long mlcId = Long.parseLong(cellMap.keySet().toArray()[0].toString());
			
			mlc = cellMap.get(mlcId);
			mlc.setConfidence((float) calculateConfidence(cellMap,mlcId,0.3));
			mlc.clustering(cellMap, 0.01);
		}

		return mlc;
	}

	// Calculate confidence for the estimated location
	private static double calculateConfidence(Map<Long, GeoCell> cellMap, Long mlc, double l) {
		
		Double sum = 0.0, total = 0.0;

		for(Entry<Long, GeoCell> entry:cellMap.entrySet()){
			double[] mCell = GeoCellCoder.cellDecoding(mlc);
			double[] cell = GeoCellCoder.cellDecoding(entry.getKey());
			if((cell[0] >= (mCell[0]-l)) && (cell[0] <= (mCell[0]+l))
					&& (cell[1] >= (mCell[1]-l)) && (cell[1] <= (mCell[1]+l))){
				sum += entry.getValue().getTotalProb();
			}
			total += entry.getValue().getTotalProb();
		}

		return sum/total;
	}

	/**
	 * This is the function that calculate the cell probabilities.
	 * @param sentenceWords : list of words contained in tweet text
	 * @return a map of cell
	 */
	private Map<Long, GeoCell> calculateCellsProbForImageTags (Set<String> sentenceWords) {

		Map<Long,GeoCell> cellMap = new HashMap<Long,GeoCell>();

		Long cell;
		for(String word:sentenceWords){
			if(wordCellProbsMap.containsKey(word)){
				double locality= wordWeights.get(word)[1];
				double entropy= wordWeights.get(word)[0];
				
				for(Entry<Long, Double> entry: wordCellProbsMap.get(word).entrySet()){
					cell = entry.getKey();
					if(cellMap.containsKey(cell)){
						cellMap.get(cell).addProb(entry.getValue()
								*(0.8*locality+0.2*gdWeight.density(entropy)), word);
					}else{
						GeoCell tmp = new GeoCell(cell);
						tmp.addProb(entry.getValue()
								*(0.8*locality+0.2*gdWeight.density(entropy)), word);
						cellMap.put(cell,tmp);
					}
				}
			}
		}
		return cellMap;
	}

	/**
	 * This is the constructor function load the word-cell probabilities file and create
	 * the respective map. The generated map allocate a significant amount of memory.
	 * @param wordCellProbsFile : file that contains the word-cell probabilities
	 */
	public LanguageModel(String wordCellProbsFile){

		EasyBufferedReader reader = new EasyBufferedReader(wordCellProbsFile);

		wordCellProbsMap = new HashMap<String,Map<Long,Double>>();;

		wordWeights = new HashMap<String,Double[]>();

		String line;
		String word;

		List<Double> p = new ArrayList<Double>();

		logger.info("opening file" + wordCellProbsFile);
		logger.info("loading cells' probabilities for all tags");

		int count = 0;
		long t0 = System.currentTimeMillis();

		while ((line = reader.readLine())!=null){

			word = line.split("\t")[0];

			Double[] weights = {Double.parseDouble(line.split("\t")[1])/0.31023, count/586237.0};
			
			wordWeights.put(word, weights); // load spatial entropy value of the tag 

			p.add(Double.parseDouble(line.split("\t")[1])); // load spatial entropy value of the tag for the Gaussian weight function

			String[] inputCells = line.split("\t")[2].split(" ");
			Map<Long, Double> tmpCellMap = new HashMap<Long,Double>();

			for(int i=0;i<inputCells.length;i++){
				long cellCode = GeoCellCoder.cellEncoding(inputCells[i].split(">")[0]);
				String cellProb = inputCells[i].split(">")[1];
				tmpCellMap.put(cellCode, Double.parseDouble(cellProb));
			}
			
			count++;
			wordCellProbsMap.put(word, tmpCellMap);
		}

		gdWeight = new NormalDistribution(
				new Mean().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()]))),
				new StandardDeviation().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()])))); // create the Gaussian weight function
		logger.info(wordCellProbsMap.size() + " words loaded in " + (System.currentTimeMillis()-t0)/1000.0 + "s");
		logger.info("closing file" + wordCellProbsFile);

		reader.close();
	}
}
