package gr.iti.mklab.methods;

import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.tools.InterfaceTagCellProb;
import gr.iti.mklab.util.MyHashMap;
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
 * Class that calculate the tag-cell probabilities for all tags in all cells and saves the results in file.
 * The implementation employ hadoop map-reduce function.
 * @author gkordo
 *
 */
public class TagCellProbMapRed implements InterfaceTagCellProb{

	private static Set<String> testIDs;
	private static Set<String> users;
	private static int scale;
	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.TagCellProbCalculator");

	/**
	 * Contractor of the class get the set of image IDs and the user IDs of the images in the test set.
	 * @param testIDs : set of test image IDs
	 * @param users	: set of test user IDs
	 */
	public TagCellProbMapRed(Set<String> testIDs, Set<String> users){
		TagCellProbMapRed.testIDs = testIDs;
		TagCellProbMapRed.users = users;
	}	

	/**
	 * Map class that takes the lines of the train file as input and creates key-value pairs,
	 * using as keys the tags contained in the images and as values strings that contain
	 * the information regarding the cell and user ID.
	 * @author gkordo
	 *
	 */
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

		/**
		 * Required map function
		 * @param key : key value
		 * @param value : input string
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			String line = value.toString();

			if (!testIDs.contains(line.split("\t")[0]) && !users.contains(line.split("\t")[2]) && line.split("\t").length>7 // train image and its user are not contained in the test set
					&& !line.split("\t")[6].isEmpty() && !line.split("\t")[7].isEmpty() // train image contains coordinations
					&& (!line.split("\t")[4].isEmpty() || !line.split("\t")[3].isEmpty())){ // train image contains any textual information

				// get image cell based on its latitude-longitude pair
				BigDecimal cellLonCenter = new BigDecimal(Double.parseDouble(line.split("\t")[6])).setScale(scale, BigDecimal.ROUND_HALF_UP);
				BigDecimal cellLatCenter = new BigDecimal(Double.parseDouble(line.split("\t")[7])).setScale(scale, BigDecimal.ROUND_HALF_UP);

				String cellID = cellLonCenter+"_"+cellLatCenter;

				//get image user ID
				String userID = line.split("\t")[2];

				// get image tags
				String[] tags = TextUtil.parseImageText(line.split("\t")[4],line.split("\t")[3]).split(" ");				

				for(String tag:tags){
					if(!tag.isEmpty()){
						output.collect(new Text(tag), new Text(cellID+">"+userID)); // key-value pair
					}
				}
			}
		}
	}


	/**
	 * Reduce class that get the key-value pairs and calculate the tag-cell probabilities for every tag.
	 * @author gkordo
	 *
	 */
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

		/**
		 * Required reduce function
		 * @param key : key value
		 * @param value : set of values that share the same key
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			// frequency map that contains the count of the different users for every single cell
			java.util.Map<String,List<String>> tagFreq = new HashMap<String,List<String>>(); 
			int tagTotal = 0; // total user count

			// process every value that corresponds to a specific key
			while (values.hasNext()) {

				String entry = values.next().toString();

				// retrieve cell ID and user ID from the value of the pair 
				String cellID = entry.split(">")[0];
				String userID = entry.split(">")[1];

				// update of the frequency map
				if (tagFreq.containsKey(cellID)){
					if(!tagFreq.get(cellID).contains(userID)){
						tagTotal++;
						List<String> tmp = tagFreq.get(cellID);
						tmp.add(userID);
						tagFreq.put(cellID,tmp);
					}
				}else{
					tagTotal++;
					List<String> tmp = new ArrayList<String>();					
					tmp.add(userID);
					tagFreq.put(cellID,tmp);
				}
			}

			// calculation of the tag-cell probabilities map for every cell
			java.util.Map<String,Double> cellsProbs = new HashMap<String,Double>();
			for(Entry<String, List<String>> entryCell : tagFreq.entrySet()){
				String tmpCellID = entryCell.getKey();

				Double tmpCellProc = ((double)(entryCell.getValue().size()))/tagTotal;

				cellsProbs.put(tmpCellID,tmpCellProc);
			}

			// sorting of the tag-cell probabilities map
			java.util.Map<String, Double> cellsProbsSorted = MyHashMap.sortByValues(cellsProbs);

			// convert tag-cell probabilities map in string in order to be saved in the output file
			String out = convertMapToString(cellsProbsSorted,tagFreq);

			// write in output file
			output.collect(key, new Text(out));
		}

		/**
		 * Function that convert tag-cell probabilities map in output string.
		 * @param cellsProbs : tag-cell probabilities map
		 * @param tagFreq : frequency map
		 * @return a string contains cell IDs accompanied with tag-cell probabilities
		 */
		public static String convertMapToString(java.util.Map<String,Double> cellsProbs, java.util.Map<String, List<String>> tagFreq){
			String out = "";
			for(Entry<String, Double> entryCell: cellsProbs.entrySet()){
				if(cellsProbs.get(entryCell.getKey()) >= 0.00001){
					String tempCellIDProb = entryCell.getKey() + ">" + cellsProbs.get(entryCell.getKey()) + ">" + tagFreq.get(entryCell.getKey()).size();

					out += (tempCellIDProb + " ");
				}
			}
			return out.trim();
		}
	}

	/**
	 * Core function for the job of tag-cell probabilities calculation.
	 * @param dir : directory of the project
	 * @param trainFile : the file of the train set
	 * @param outFolder : the folder where the tag-set probabilities file will be stored
	 * @param scale : the scale of the grid that is used
	 */
	public void calculatorTagCellProb(String dir, String trainFile, String outFolder, int scale) throws IOException{

		TagCellProbMapRed.scale = scale;

		// initialize Job
		JobConf conf = new JobConf(TagCellProbMapRed.class);
		conf.setJobName("tagcellprobmapred");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// clean the output file directory
		File file = new File(dir + outFolder);
		if (file.exists()) {
			FileUtils.cleanDirectory(file);
			FileUtils.forceDelete(file);
		}

		// create a temporary file containing the train set
		file = new File(dir + "/temp");
		if (!file.exists()) {
			logger.info("create temporery copy of the file " + trainFile);
			DataManager.createTempFile(dir, trainFile);
		}
		
		FileInputFormat.setInputPaths(conf, new Path(dir + "temp"));
		FileOutputFormat.setOutputPath(conf, new Path(dir + outFolder));

		// start Job
		JobClient.runJob(conf);

		new File(dir + outFolder + "/part-00000").renameTo(new File(dir + outFolder + "/tag_cell_prob")); // rename the output file
		DataManager.deleteTempFile(dir); // delete temporary file
	}
}