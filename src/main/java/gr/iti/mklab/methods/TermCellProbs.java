package gr.iti.mklab.methods;

import gr.iti.mklab.tools.InterfaceTermCellProb;
import gr.iti.mklab.util.Utils;
import gr.iti.mklab.util.TextUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.log4j.Logger;

/**
 * Class that calculate the term-cell probabilities for all term in all cells and saves the results in file.
 * The implementation employ hadoop map-reduce function.
 * @author gkordo
 *
 */
public class TermCellProbs implements InterfaceTermCellProb{

	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.TermCellProbCalculator");
	private static Set<String> testIDs;
	private static Set<String> users;
	private static int scale;

	/**
	 * Contractor of the class get the set of image IDs and the user IDs of the images in the test set.
	 * @param testIDs : set of test image IDs
	 * @param users	: set of test user IDs
	 */
	public TermCellProbs(Set<String> testIDs, Set<String> users){
		TermCellProbs.testIDs = testIDs;
		TermCellProbs.users = users;
	}	

	/**
	 * Map class that takes the lines of the train file as input and creates key-value pairs,
	 * using as keys the terms contained in the images and as values strings that contain
	 * the information regarding the cell and user ID.
	 * @author gkordo
	 *
	 */
	public static class MapTermCellProb extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

		/**
		 * Required map function
		 * @param key : key value
		 * @param value : input string
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			String[] metadata = value.toString().split("\t");

			if (!testIDs.contains(metadata[1]) && !users.contains(metadata[3]) // train image and its user are not contained in the test set
					&& !metadata[12].isEmpty() && !metadata[13].isEmpty() // train image contains coordinations
					&& (!metadata[10].isEmpty() || !metadata[8].isEmpty())){ // train image contains any textual information

				// get image cell based on its latitude-longitude pair
				BigDecimal cellLonCenter = new BigDecimal(Double.parseDouble(
						metadata[12])).setScale(scale, BigDecimal.ROUND_HALF_UP);
				BigDecimal cellLatCenter = new BigDecimal(Double.parseDouble(
						metadata[13])).setScale(scale, BigDecimal.ROUND_HALF_UP);

				String cellID = cellLonCenter+"_"+cellLatCenter;

				//get image user ID
				String userID = metadata[3];

				// get image tags
				Set<String> terms = new HashSet<String>();
				TextUtil.parse(metadata[10], terms);
				TextUtil.parse(metadata[8], terms);

				for(String term:terms){
					if(!term.isEmpty() && term.length() > 2){
						output.collect(new Text(term), new Text(cellID+">"+userID)); // key-value pair
					}
				}
			}
		}
	}


	/**
	 * Reduce class that get the key-value pairs and calculate the term-cell probabilities of every term.
	 * @author gkordo
	 *
	 */
	public static class ReduceTermCellProb extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

		/**
		 * Required reduce function
		 * @param key : key value
		 * @param values : set of values that share the same key
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			// frequency map that contains the count of the different users for every single cell
			Map<String,Set<String>> termFreq = new HashMap<String,Set<String>>(); 
			int Nt = 0; // total user count

			// process every value that corresponds to a specific key
			while (values.hasNext()) {

				String entry = values.next().toString();

				// retrieve cell ID and user ID from the value of the pair 
				String cellID = entry.split(">")[0];
				String userID = entry.split(">")[1];

				// update of the frequency map
				if (termFreq.containsKey(cellID)){
					if(!termFreq.get(cellID).contains(userID)){
						Nt++;
						termFreq.get(cellID).add(userID);
					}
				}else{
					Nt++;
					termFreq.put(cellID,new HashSet<String>());
					termFreq.get(cellID).add(userID);
				}
			}

			// calculation of the tag-cell probabilities map for every cell
			Map<String,Double> cellsProbs = new HashMap<String,Double>();
			for(Entry<String, Set<String>> entryCell : termFreq.entrySet()){
				String cellID = entryCell.getKey();
				Double cellProb = ((double)(entryCell.getValue().size()))/Nt;
				cellsProbs.put(cellID,cellProb);
			}

			// sorting of the tag-cell probabilities map
			Map<String, Double> cellsProbsSorted = Utils.sortByValues(cellsProbs);

			// convert tag-cell probabilities map in string in order to be saved in the output file
			String out = convertMapToString(cellsProbsSorted,termFreq);

			// send output to collector
			output.collect(key, new Text(out));
		}

		/**
		 * Function that convert tag-cell probabilities map in output string.
		 * @param cellsProbs : tag-cell probabilities map
		 * @param termFreq : frequency map
		 * @return a string contains cell IDs accompanied with tag-cell probabilities
		 */
		public static String convertMapToString(Map<String,Double> cellsProbs,
				Map<String, Set<String>> termFreq){
			String out = "";
			for(Entry<String, Double> entryCell: cellsProbs.entrySet()){
				if(cellsProbs.get(entryCell.getKey()) >= 0.00001){
					String tempCellIDProb = entryCell.getKey() 
							+ ">" + cellsProbs.get(entryCell.getKey()) 
							+ ">" + termFreq.get(entryCell.getKey()).size();

					out += (tempCellIDProb + " ");
				}
			}
			return out.trim();
		}
	}

	/**
	 * Core function for the job of tag-cell probabilities calculation.
	 * @param dir : directory of the project
	 * @param trainFolder : the file of the train set
	 * @param outFolder : the folder where the tag-set probabilities file will be stored
	 * @param scale : the scale of the grid that is used
	 */
	public void calculatorTermCellProb(String dir, String trainFolder,
			String outFolder, int scale) throws IOException{

		logger.info("Process: Term-Cell Propabilities Calculation\t|\t"
				+ "Status: INITIALIZE");
		
		TermCellProbs.scale = scale;

		// initialize Job
		JobConf conf = new JobConf(TermCellProbs.class);
		conf.setJobName("termcellprobmapred");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(MapTermCellProb.class);
		conf.setReducerClass(ReduceTermCellProb.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// clean the output file directory
		File folder = new File(dir + outFolder);
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder);
			FileUtils.forceDelete(folder);
		}

		FileInputFormat.setInputPaths(conf, new Path(dir + trainFolder));
		FileOutputFormat.setOutputPath(conf, new Path(dir + outFolder));

		// start Job
		logger.info("Process: Term-Cell Propabilities Calculation\t|\t"
				+ "Status: STARTED");
		long startTime = System.currentTimeMillis();
		JobClient.runJob(conf);
		logger.info("Process: Term-Cell Propabilities Calculation\t|\t"
				+ "Status: COMPLETED\t|\tTotal time: " + 
				(System.currentTimeMillis()-startTime)/60000.0+"m");

		new File(dir + outFolder + "/part-00000").renameTo(
				new File(dir + outFolder + "/term_cell_probs")); // rename the output file
	}
}