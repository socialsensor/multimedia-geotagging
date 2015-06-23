package gr.iti.mklab.methods;

import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.MyHashMap;
import gr.iti.mklab.util.Progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

/**
 * This class is the core of the algorithm. It is the implementation of the language model.
 * The Most Likely Cell of the given image is calculated.
 * @author gkordo
 *
 */
public class LanguageModel {

	private static NormalDistribution gd;

	private static Map<String,Double> entropyTags;
	private static Map<String,Set<String>> cellTags;

	protected String file;

	static Logger logger = Logger.getLogger("gr.iti.mklab.method.LanguageModel");

	// Constructor initializes the needed maps
	public LanguageModel(String dir, String file){
		this.file = dir+file;
	}

	/**
	 * Calculate the probability of every cell based on the given tags of the query image
	 * @param imageTags : the tags and title of an query image
	 * @return the most probable cell
	 */
	public String calculateLanguageModel(List<String> imageTags, Map<String,Map<String,Double>> tagCellProbsMap, boolean confidenceFlag) {

		Map<String, Double[]> cellMap = calculateTagCellProbabilities(imageTags,tagCellProbsMap);

		String mlc = findMostLikelyCell(cellMap, confidenceFlag);

		if(mlc!=null){
			return mlc;
		}else{
			return null;
		}
	}

	/**
	 * Calculate the probability of every cell based on the given tags of the query image
	 * @param cellMap : map with the cell probabilities
	 * @return the most probable cell
	 */
	public String findMostLikelyCell(Map<String, Double[]> cellMap, boolean confidenceFlag) {

		cellMap = MyHashMap.sortByValuesTable(cellMap); // descending sort of cell probabilities

		String mlc = null;
		String tags = "";
		Double confidence = null;

		if (!cellMap.isEmpty()){
			mlc = cellMap.keySet().toArray()[0].toString();

			if(confidenceFlag){
				for(String tag:cellTags.get(mlc)){
					tags += tag + " ";
				}
				confidence = calculateConfidence(cellMap,mlc,0.3);
			}
		}
		
		if(confidenceFlag){
			return (mlc!=null&&!tags.isEmpty()?mlc+";"+confidence+" "+tags.trim().replaceAll("\\s", "\\,"):null);
		}else{
			return mlc;
		}
	}

	//Calculate confidence for the estimated location
	public double calculateConfidence(Map<String, Double[]> cellMap, String mlc, double l) {
		Double sum = 0.0, total = 0.0;

		for(Entry<String, Double[]> entry : cellMap.entrySet()){
			double[] mlcCell = {Double.parseDouble(mlc.split("_")[0]), Double.parseDouble(mlc.split("_")[1])};
			double[] cell = {Double.parseDouble(entry.getKey().split("_")[0]), Double.parseDouble(entry.getKey().split("_")[1])};
			if((cell[0]>=(mlcCell[0]-l)) && (cell[0]<=(mlcCell[0]+l))
					&& (cell[1]>=(mlcCell[1]-l)) && (cell[1]<=(mlcCell[1]+l))){
				sum += entry.getValue()[0];
			}
			total += entry.getValue()[0];
		}

		return sum/total;
	}

	/**
	 * The function that apply the language model on the given tag set
	 * @param imageTags : the tags and title of an query image
	 * @return 
	 */
	public Map<String, Double[]> calculateTagCellProbabilities (List<String> imageTags, Map<String,Map<String,Double>> tagCellProbsMap) {

		Map<String,Double[]> cellMap = new HashMap<String,Double[]>();
		cellTags = new HashMap<String,Set<String>>();

		String cell;
		for(String tag:imageTags){
			if(tagCellProbsMap.containsKey(tag)){
				for(Entry<String, Double> entry : tagCellProbsMap.get(tag).entrySet()){ // the probability summation for the specific cell has been initialized
					cell = entry.getKey();
					if(cellMap.containsKey(cell)){
						Double[] tmp = cellMap.get(cell);
						tmp[0] += entry.getValue()*(gd.density(entropyTags.get(tag))); // sum of the weighted tag-cell probabilities
						tmp[1] += 1.0;
						cellMap.put(cell,tmp);

						cellTags.get(cell).add(tag);
					}else{ // initialization of the probability summation for the particular cell
						Double[] tmp = new Double[2];
						tmp[0] = entry.getValue()*(gd.density(entropyTags.get(tag))); // initialization of the summation of the weighted tag-cell probabilities
						tmp[1] = 1.0;
						cellMap.put(cell,tmp);

						Set<String> tmpSet = new HashSet<String>();
						tmpSet.add(tag);
						cellTags.put(cell,tmpSet);
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
	public Map<String,Map<String,Double>> organizeMapOfCellsTags(String testFile, String tagAccFile, boolean featureSelection, double thetaG, int thetaT){

		EasyBufferedReader reader = new EasyBufferedReader(file);

		Map<String,Map<String,Double>> tagCellProbsMap = new HashMap<String,Map<String,Double>>();
		entropyTags = new HashMap<String,Double>();

		String line;
		String tag;

		List<Double> p = new ArrayList<Double>();

		Set<String> tagsInTestSet = DataManager.getSetOfTags(testFile);
		Set<String> selectedTags = new HashSet<String>();

		if(featureSelection){
			selectedTags = selectTagAccuracies(tagAccFile, thetaG, thetaT); // feature selection
		}

		logger.info("loading cells' probabilities for all tags from " + file);

		long startTime = System.currentTimeMillis();
		Progress prog = new Progress(startTime,10,1,"loading",logger);

		// load tag-cell probabilities from the given file
		while ((line = reader.readLine())!=null){

			prog.showMessege(System.currentTimeMillis());

			tag = line.split("\t")[0];

			if(line.split("\t").length>1 && tagsInTestSet.contains(tag) 
					&& (selectedTags.contains(tag) || !featureSelection)){

				entropyTags.put(tag, Double.parseDouble(line.split("\t")[2])); // load spatial entropy value of the tag 

				p.add(Double.parseDouble(line.split("\t")[2])); // load spatial entropy value of the tag for the Gaussian weight function

				String[] associatedCells = line.split("\t")[1].split(" ");
				HashMap<String, Double> tmpCellMap = new HashMap<String,Double>();

				for(String cell:associatedCells){
					tmpCellMap.put(cell.split(">")[0], Double.parseDouble(cell.split(">")[1]));
				}

				tagCellProbsMap.put(tag, tmpCellMap);
			}
		}

		gd = new NormalDistribution(
				new Mean().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()]))),
				new StandardDeviation().evaluate(ArrayUtils.toPrimitive(p.toArray(new Double[p.size()])))); // create the Gaussian weight function
		logger.info(tagCellProbsMap.size()+" tags loaded in "+(System.currentTimeMillis()-startTime)/1000.0+"s");
		reader.close();

		return tagCellProbsMap;
	}

	/**
	 * Select tags based on their accuracies.
	 * @param file : tag accuracies file
	 * @param thetaG : accuracy threshold
	 * @param thetaU : times found threshold
	 * @return
	 */
	private static Set<String> selectTagAccuracies(String file, double thetaG, int thetaT){
		EasyBufferedReader reader = new EasyBufferedReader(file);
		Set<String> tagSet = new HashSet<String>();

		String line;
		int total = 0;

		while((line= reader.readLine()) != null){
			if(Double.parseDouble(line.split(" ")[1])>thetaG // theta geo
					&& Double.parseDouble(line.split(" ")[2])>thetaT){ // theta times found
				tagSet.add(line.split(" ")[0]);
			}
			total++;
		}

		logger.info(tagSet.size() + " tags selected from the total of " + total + " tags");
		logger.info("Ratio of the selected tags : " + (double)tagSet.size()/total);
		reader.close();

		return tagSet;
	}
}
