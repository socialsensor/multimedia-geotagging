package gr.iti.mklab.methods;

import gr.iti.mklab.util.CellCoder;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.MyHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	protected Map<Long,Set<String>> cellWords;
	protected Map<String,Double> entropyWords;

	private static NormalDistribution gdWeight;

	protected String wordCellProbsFile;
	protected Logger logger;


	//Contractor stores the name of the file that contains the word-cell probabilities.
	public LanguageModel(String wordCellProbsFile){
		this.logger = Logger.getLogger("gr.iti.mklab.methods.LanguageModel");
		this.wordCellProbsFile = wordCellProbsFile;
		this.entropyWords = new HashMap<String,Double>();
	}
	//The function that compose the other functions to calculate and return the Most Likely Cell for a query sentence.
	public String calculateLanguageModel(List<String> sentenceWords, Map<String,Map<Long,Double>> wordCellProbsMap) {

		Map<Long, Double> cellMap = calculateCellsProbForImageTags(sentenceWords, wordCellProbsMap);

		String mlc = findMLC(cellMap);

		if(mlc!=null){
			return mlc;
		}else{
			return null;
		}

	}

	//The function that specifies the Most Likely Cell.
	public String findMLC(Map<Long, Double> cellMap) {

		cellMap = MyHashMap.sortByValues(cellMap);
		String words = "";
		Long mlc = null;
		Double confidence = null;

		if (!cellMap.isEmpty()){
			mlc = Long.parseLong(cellMap.keySet().toArray()[0].toString());

			for(String word:cellWords.get(mlc)){
				words += word + " ";
			}

			confidence = calculateConfidence(cellMap,mlc,0.3);
		}
		return (mlc!=null&&!words.isEmpty()?mlc+"\t"+confidence+"\t"+words.trim().replaceAll("\\s", "\\,"):null);
	}

	//Calculate confidence for the estimated location
	public double calculateConfidence(Map<Long, Double> cellMap, Long mlc, double l) {
		Double sum = 0.0, total = 0.0;

		for(Entry<Long, Double> entry:cellMap.entrySet()){
			double[] mCell = CellCoder.cellDecoding(mlc);
			double[] cell = CellCoder.cellDecoding(entry.getKey());
			if((cell[0]>=(mCell[0]-l))&&(cell[0]<=(mCell[0]+l))
					&&(cell[1]>=(mCell[1]-l))&&(cell[1]<=(mCell[1]+l))){
				sum += entry.getValue();
			}
			total += entry.getValue();
		}

		return sum/total;
	}

	//This is the function that calculate the cell probabilities.
	public Map<Long, Double> calculateCellsProbForImageTags (List<String> sentenceWords, Map<String,Map<Long,Double>> wordCellProbsMap) {

		Map<Long,Double> cellList = new HashMap<Long,Double>();
		cellWords = new HashMap<Long,Set<String>>();

		Long cell;
		for(String word:sentenceWords){
			if(wordCellProbsMap.containsKey(word)){
				double entropyValue= entropyWords.get(word);
				for(Entry<Long, Double> entry: wordCellProbsMap.get(word).entrySet()){
					cell = entry.getKey();
					if(cellList.containsKey(cell)){
						Double tmp = cellList.get(cell);
						tmp += entry.getValue()*gdWeight.density(entropyValue);;
						cellList.put(cell,tmp);
						cellWords.get(cell).add(word);
					}else{
						Double tmp = entry.getValue()*gdWeight.density(entropyValue);;
						cellList.put(cell,tmp);
						Set<String> tmpSet = new HashSet<String>();
						tmpSet.add(word);
						cellWords.put(cell,tmpSet);
					}
				}
			}
		}
		return cellList;
	}

	/**
	 * This function load the word-cell probabilities file and create the respective map.
	 * The generated map allocate a significant amount of memory.
	 */
	public Map<String,Map<Long,Double>> organizeWordCellProbsMap(){

		EasyBufferedReader reader = new EasyBufferedReader(wordCellProbsFile);

		Map<String,Map<Long,Double>> wordCellProbsMap = new HashMap<String,Map<Long,Double>>();;

		String input = reader.readLine();
		String word;

		List<Double> p = new ArrayList<Double>();

		logger.info("opening file" + wordCellProbsFile);
		logger.info("loading cells' probabilities for all tags");

		long t0 = System.currentTimeMillis();

		while ((input = reader.readLine())!=null){

			word = input.split("\t")[0];

			entropyWords.put(word, Double.parseDouble(input.split("\t")[1])); // load spatial entropy value of the tag 

			p.add(Double.parseDouble(input.split("\t")[1])); // load spatial entropy value of the tag for the Gaussian weight function

			String[] inputCells = input.split("\t")[2].split(" ");
			HashMap<Long, Double> tmpCellMap = new HashMap<Long,Double>();

			for(int i=0;i<inputCells.length;i++){
				long cellCode = CellCoder.cellEncoding(inputCells[i].split(">")[0]);
				String cellProb = inputCells[i].split(">")[1];
				tmpCellMap.put(cellCode, Double.parseDouble(cellProb));
			}
			wordCellProbsMap.put(word, tmpCellMap);
		}

		gdWeight = new NormalDistribution(
				new Mean().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()]))),
				
				new StandardDeviation().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()])))); // create the Gaussian weight function
		logger.info(wordCellProbsMap.size()+" words loaded in " + (System.currentTimeMillis()-t0)/1000.0 + "secs");
		logger.info("closing file" + wordCellProbsFile);

		reader.close();

		return wordCellProbsMap;
	}
}
