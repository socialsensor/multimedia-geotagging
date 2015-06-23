package gr.iti.mklab.methods;

import gr.iti.mklab.MultimediaGeotagging;
import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.util.DistanceTwoPoints;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.MyHashMap;
import gr.iti.mklab.util.TextUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Class that implements Cross Validation scheme.
 * @author gkordo
 *
 */
public class CrossValidation {

	private Logger logger = Logger.getLogger("gr.iti.mklab.methods.CrossValidation");
	private String dir;
	private String fileName;
	private int partitions;
	private double range;

	/**
	 * Contractor of the class.
	 * @param dir : directory of the project
	 * @param fileName : name of the input file
	 * @param partitions : total number of the partitions
	 * @throws IOException
	 */
	public CrossValidation(String dir, String fileName, int partitions, double range) throws IOException{
		this.dir = dir;
		this.fileName = fileName;
		this.partitions = partitions;
		this.range = range;

		DataManager.separateForCrossValidation(dir, fileName, partitions);
	}

	/**
	 * Core function that apply feature selection scheme.
	 * @throws Exception
	 */
	public void applyCrossValidation() throws Exception{

		logger.info("apply feature selection scheme");

		for (int i=0;i<partitions;i++){

			logger.info("test partition number: " + i);
			Set<String> testIDs = DataManager.getSetOfImageIDs(dir + "/temp/crossval-" + i);
			Set<String> userIDs = DataManager.getSetOfImageIDs(dir + "/temp/crossval-" + i);

			// calculate tag-cell probabilities of the remaining partitions
			TagCellProbMapRed trainLM = new TagCellProbMapRed(testIDs,userIDs);
			trainLM.calculatorTagCellProb(dir, "/temp", "TagCellProbabilities/crossval/crossval-" + i, 2);

			// split partition in 10 files for better memory allocation
			DataManager.splitDataset(dir + "/temp", "crossval-" + i, testIDs.size()/10);

			// apply language model for every part of the partition
			for (int j=0;j<10;j++){
				MultimediaGeotagging.computeLanguageModel(dir, "/temp/temp/crossval-" + i + "-" + j,
						"crossval-" + i + "-" + j, "TagCellProbabilities/crossval/crossval-" + i + "/tag_cell_prob", "", false, 0.0, 0, false);
			}

			DataManager.deleteTempFile(dir + "/temp"); // delete temporary files
			DataManager.mergeResults(dir + "/resultsLM/", "crossval-" + i, dir + fileName, 10); // merge the results
			
			FileUtils.cleanDirectory(new File(dir + "TagCellProbabilities/crossval/crossval-" + i));
			FileUtils.forceDelete(new File(dir + "TagCellProbabilities/crossval/crossval-" + i));
		}
	}

	//
	public void calculateTagAccuracy(){

		Map<String,Integer[]> tagAccuracies = new HashMap<String,Integer[]>();
		
		// update map of the tag accuracies based on the result of every test partition
		for(int i=0;i<partitions;i++){
			tagAccuracies = computeTagAcc(dir + "/temp/crossval-" + i, dir + "/resultsLM/crossval-" + i, tagAccuracies, range);
		}

		writeTagStat(tagAccuracies);
	}

	/**
	 * Function that calculates the tag accuracies.
	 * @param testFile : test partition
	 * @param resultFile : file that contains the result of a partition
	 * @param tagAccuracies : map of the tag accuracies.
	 * @param range : range that an estimation considered correct
	 * @return
	 */
	private Map<String,Integer[]> computeTagAcc(String testFile, 
			String resultFile, Map<String,Integer[]> tagAccuracies, double range) {

		EasyBufferedReader testReader = new EasyBufferedReader(testFile);
		EasyBufferedReader resultReader = new EasyBufferedReader(resultFile);

		String inTest, inResult;

		while((inTest = testReader.readLine())!=null&&(inResult = resultReader.readLine())!=null){

			Double[] point1 = new Double[2], point2 = new Double[2];

			if(!inTest.split("\t")[6].isEmpty()&&!inTest.split("\t")[7].isEmpty()
					&&inResult.split(";")[0].equals(inTest.split("\t")[0])){

				point1[0] = Double.parseDouble(inTest.split("\t")[7]);
				point1[1] = Double.parseDouble(inTest.split("\t")[6]);

				if(!inResult.isEmpty()){ // a location has been estimated
					point2[0] = Double.parseDouble(inResult.split(";")[1].split("_")[1]);
					point2[1] = Double.parseDouble(inResult.split(";")[1].split("_")[0]);
				}else{ // default result for no estimated location
					point2[0] = 40.75282028252674;
					point2[1] = -73.98282136256299;
				}
				
				double dist = DistanceTwoPoints.computeDistace(point1, point2); // distance between the two points

				int a=0;
				if (dist<=range){ // distance lays inside the given range
					a=1;
				}

				String[] tags = TextUtil.parseImageText(inTest.split("\t")[4], inTest.split("\t")[3]).split(" ");
				
				// update every image tag
				for(String tag:tags){
					if(!tag.isEmpty()){
						Integer[] tmp = {0,0};
						if(tagAccuracies.containsKey(tag)){
							tmp = tagAccuracies.get(tag);
						}
						tmp[0] += a;
						tmp[1] ++;
						tagAccuracies.put(tag, tmp);
					}
				}
			}	
		}
		testReader.close();
		resultReader.close();

		return tagAccuracies;
	}
	
	/**
	 * Function that writes the map of the tag accuracies in a file.
	 * @param tagAccuracies : map of the tag accuracies
	 */
	public void writeTagStat(Map<String,Integer[]> tagAccuracies){
		Map<String, Double> tagStat = new HashMap<String, Double>();

		for(Entry<String, Integer[]> entry : tagAccuracies.entrySet()){
			double tmp = (double)entry.getValue()[0]/entry.getValue()[1];
			tagStat.put(entry.getKey(), tmp);
		}

		tagStat = MyHashMap.sortByValues(tagStat);

		EasyBufferedWriter writer = new EasyBufferedWriter(dir + "/tagAccuracies_range_" + range);

		for(Entry<String, Double> entry : tagStat.entrySet()){
			writer.write(entry.getKey() + " " + entry.getValue() + " " 
					+ tagAccuracies.get(entry.getKey())[0] + " " + tagAccuracies.get(entry.getKey())[1]);
			writer.newLine();
		}
		
		writer.close();
	}
}
