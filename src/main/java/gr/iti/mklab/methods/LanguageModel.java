package gr.iti.mklab.methods;

import gr.iti.mklab.data.GeoCell;
import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.Utils;
import gr.iti.mklab.util.Progress;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * This class is the core of the algorithm. It is the implementation of the language model.
 * The Most Likely Cell of the given image is calculated.
 * @author gkordo
 *
 */
public class LanguageModel {

	protected Map<String, Double[]> selectedTermWeights;

	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.LanguageModel");

	// The function that compose the other functions to calculate and return the Most Likely Cell for a query tweet.
	public GeoCell calculateLanguageModel(Set<String> sentenceWords, 
			Map<String, Map<String, Double>> termCellProbsMap, boolean confidenceFlag) {

		Map<String, GeoCell> cellMap = calculateCellsProbForImageTags(sentenceWords,
				termCellProbsMap);

		GeoCell mlc = findMLC(cellMap, confidenceFlag);

		return mlc;
	}

	// find the Most Likely Cell.
	private GeoCell findMLC(Map<String, GeoCell> cellMap, boolean confidenceFlag) {

		cellMap = Utils.sortByMLCValues(cellMap);

		GeoCell mlc = null;

		if (!cellMap.isEmpty()){
			String mlcId = cellMap.keySet().toArray()[0].toString();

			mlc = cellMap.get(mlcId);
			
			if(confidenceFlag)
			mlc.setConfidence((float) calculateConfidence(cellMap, mlcId, 0.3));
		}

		return mlc;
	}

	// Calculate confidence for the estimated location
	private static double calculateConfidence(Map<String, GeoCell> cellMap, 
			String mlc, double l) {

		Double sum = 0.0, total = 0.0;

		for(Entry<String, GeoCell> entry:cellMap.entrySet()){
			double[] mCell = {Double.parseDouble(mlc.split("_")[0]),
					Double.parseDouble(mlc.split("_")[1])};
			double[] cell = {Double.parseDouble(entry.getKey().split("_")[0]),
					Double.parseDouble(mlc.split("_")[1])};
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
	private Map<String, GeoCell> calculateCellsProbForImageTags (Set<String> terms,
			Map<String, Map<String, Double>> termCellProbsMap) {

		Map<String, GeoCell> cellMap = new HashMap<String, GeoCell>();

		for(String term:terms){
			if(termCellProbsMap.containsKey(term)){
				double locality= selectedTermWeights.get(term)[1];
				double entropy= selectedTermWeights.get(term)[0];

				for(Entry<String, Double> entry: termCellProbsMap.get(term).entrySet()){
					String cell = entry.getKey();
					if(cellMap.containsKey(cell)){
						cellMap.get(cell).addProb(entry.getValue()
								*(0.8*locality+0.2*entropy), term);
					}else{
						GeoCell tmp = new GeoCell(cell);
						tmp.addProb(entry.getValue()
								*(0.8*locality+0.2*entropy), term);
						cellMap.put(cell,tmp);
					}
				}
			}
		}
		return cellMap;
	}

	/**
	 *  initialize Language Model
	 * @param testFile : file that contains test image metadata
	 * @param tagAccFile : the file that contains the accuracies of the tags
	 * @param featureSelection : argument that indicates if the feature selection is used or not 
	 * @param thetaG : feature selection accuracy threshold
	 * @param thetaT : feature selection frequency threshold
	 * @return
	 */
	public Map<String,Map<String,Double>> organizeMapOfCellsTags(String testFile, 
			String probFile, String weightFolder){

		// Feature Selection
		loadTermWeights(weightFolder);

		logger.info("loading cells' probabilities for all tags from " + probFile);

		long startTime = System.currentTimeMillis();
		Progress prog = new Progress(startTime,10,1,"loading",logger);
		
		Map<String,Map<String,Double>> tagCellProbsMap = new HashMap<String,Map<String,Double>>();
		Set<String> termsInTestSet = DataManager.getSetOfTerms(testFile);
		
		EasyBufferedReader reader = new EasyBufferedReader(probFile);
		String line;
		// load tag-cell probabilities from the given file
		while ((line = reader.readLine())!=null){
			prog.showMessege(System.currentTimeMillis());
			String term = line.split("\t")[0];

			if(line.split("\t").length>1 && termsInTestSet.contains(term) 
					&& selectedTermWeights.containsKey(term)){				
				Map<String, Double> tmpCellMap = new HashMap<String,Double>();
				for(String cell:line.split("\t")[2].split(" ")){
					tmpCellMap.put(cell.split(">")[0], Double.parseDouble(cell.split(">")[1]));
				}
				tagCellProbsMap.put(term, tmpCellMap);
			}
		}
		logger.info(tagCellProbsMap.size() + " tags loaded in " + 
				(System.currentTimeMillis()-startTime)/1000.0 + "s");
		reader.close();

		return tagCellProbsMap;
	}
	
	private void loadTermWeights(String folder){
		
		// load locality weight of the terms
		EasyBufferedReader reader = new EasyBufferedReader(folder + "/locality_weights");
		String line;
		while ((line = reader.readLine())!=null){
			Double[] temp = {0.0, Double.parseDouble(line.split("\t")[1])};
			selectedTermWeights.put(line.split("\t")[0], temp);
		}
		reader.close();
		
		// load spatial entropy weight of the terms
		reader = new EasyBufferedReader(
				folder + "/spatial_entropy_weights");
		while ((line = reader.readLine())!=null){
			if(selectedTermWeights.containsKey(line.split("\t")[0]))
				selectedTermWeights.get(line.split("\t")[0])[0] = 
				Double.parseDouble(line.split("\t")[1]);
		}
		reader.close();
	}
}
