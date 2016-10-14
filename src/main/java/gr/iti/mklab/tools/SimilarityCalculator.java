package gr.iti.mklab.tools;

import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.util.EasyBufferedReader;
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
 * For a query image, the similarity between the images contained in the train set is calculated based on their corresponding term sets.
 * Class that implements similarity search based on Map-Reduce scheme.
 * @author gkordo
 *
 */
public class SimilarityCalculator{

	private static Set<String> testIDs;
	private static Set<String>users;
	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.SimilaritySearch");
	static java.util.Map<String, List<ImageMetadata>> predictedCellsOfTestImages = new HashMap<String,  List<ImageMetadata>>();

	/**
	 * Contractor of the class.
	 * @param testFile : file that contains the test image's metadata
	 * @param resultFile : file that contains the MLC of every query image
	 */
	public SimilarityCalculator(String testFile, String resultFile){
		loadTestImages(testFile,resultFile);
	}


	/**
	 * Map class that takes the lines of the train file as input and creates key-value pairs,
	 * using as keys the image IDs of the test set images and as values strings that contain
	 * the location of the train images and the calculated similarity.
	 * @author gkordo
	 *
	 */
	public static class MapSimilaritySearch extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

		/**
		 * Required map function
		 * @param key : key value
		 * @param value : input string
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			String line = value.toString();

			if (!testIDs.contains(line.split("\t")[1]) && !users.contains(line.split("\t")[3]) // train image and its user are not contained in the test set
					&& !line.split("\t")[12].isEmpty() && !line.split("\t")[13].isEmpty() // train image contains coordinations
					&& (!line.split("\t")[10].isEmpty() || !line.split("\t")[8].isEmpty())){ // train image contains any textual information

				// get image cell based on its latitude-longitude pair
				BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(
						line.split("\t")[12])).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(
						line.split("\t")[13])).setScale(2, BigDecimal.ROUND_HALF_UP);

				Set<String> trainImageTerms = new HashSet<String>();
				TextUtil.parse(line.split("\t")[10], trainImageTerms);
				TextUtil.parse(line.split("\t")[8], trainImageTerms);
				
				// there is at least estimated location laying inside the borders of cell
				if(predictedCellsOfTestImages.containsKey(tmpLonCenter+"_"+tmpLatCenter)
						&& trainImageTerms.size() > 1){

					// calculate similarity between the train image and all images that lay inside the boarded of the specific cell
					for(ImageMetadata entry : predictedCellsOfTestImages
							.get(tmpLonCenter+"_"+tmpLatCenter)){

						// determine the common terms
						List<String> common = new ArrayList<String>(trainImageTerms);
						common.retainAll(entry.getTags());

						// calculate similarity
						double sjacc = (double) common.size() / (entry.getTags().size() 
								+ trainImageTerms.size() - common.size());
						if(sjacc>0.05){
							output.collect(new Text(entry.getId()), new Text(String.valueOf(sjacc) +
									">" + line.split("\t")[12] + "_"+line.split("\t")[13]));
						}
					}
				}
			}
		}
	}

	/**
	 * Reduce class that get the key-value pairs and sort the similarities for a test image.
	 * @author gkordo
	 *
	 */
	public static class ReduceSimilaritySearch extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

		/**
		 * Required reduce function
		 * @param key : key value
		 * @param value : set of values that share the same key
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			java.util.Map<String,Double> simImages = new HashMap<String,Double>();

			// load values in a topic similarity map
			while (values.hasNext()) {
				String entry = values.next().toString();

				simImages.put(entry.split(">")[1],Double.parseDouble(entry.split(">")[0]));
			}

			// sort similarity map
			simImages = Utils.sortByValues(simImages);

			// write in output file
			output.collect(key, new Text(convertSimMapToStr(simImages)));
		}

		/**
		 * Function that converts similarity map to output string
		 * @param simImages : similarity map
		 * @return a string that contains similarity and location of the train images
		 */
		public String convertSimMapToStr(java.util.Map<String,Double> simImages){
			String out = "";

			for(Entry<String,Double> entry : simImages.entrySet()){
				out += entry.getKey() + ">" + entry.getValue() + " ";
			}

			return out.trim();
		}
	}

	/**
	 * Core function for the job of similarity search.
	 * @param dir : directory of the project
	 * @param trainFile : the file of the train set
	 * @param outFolder : the folder where the tag-set probabilities file will be stored
	 * @param scale : the scale of the grid that is used
	 */
	public void performSimilarityCalculation(String dir, String trainFile, String outFolder) throws Exception {

		logger.info("Process: Similarity Calculation\t|\t"
				+ "Status: INITIALIZE");
		JobConf conf = new JobConf(SimilarityCalculator.class);
		conf.setJobName("similaritysearch");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(MapSimilaritySearch.class);

		conf.setReducerClass(ReduceSimilaritySearch.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// clean the output file directory
		File file = new File(dir + outFolder);
		if (file.exists()) {
			FileUtils.cleanDirectory(file);
			FileUtils.forceDelete(file);
		}

		// create a temporary file containing the train set
		DataManager.createTempFile(dir, trainFile);

		FileInputFormat.setInputPaths(conf, new Path(dir + "temp"));
		FileOutputFormat.setOutputPath(conf, new Path(dir + outFolder));

		logger.info("Process: Similarity Calculation\t|\t"
				+ "Status: STARTED");
		long startTime = System.currentTimeMillis();
		JobClient.runJob(conf);
		logger.info("Process: Similarity Calculation\t|\t"
				+ "Status: COMPLETED\t|\tTotal time: " + 
				(System.currentTimeMillis()-startTime)/60000.0+"m");

		new File(dir + outFolder + "/part-00000").renameTo(
				new File(dir + outFolder + "/image_similarities")); // rename the output file
		DataManager.deleteTempFile(dir); // delete temporary file
	}

	/**
	 * Load test images in a map based on their MLCs. Also update the set of test image IDs and test user IDs.
	 * @param testFile
	 * @param resultFile
	 */
	private void loadTestImages(String testFile, String resultFile){

		EasyBufferedReader readerTest = new EasyBufferedReader(testFile);
		EasyBufferedReader readerResult = new EasyBufferedReader(resultFile);
		String lineT,lineR;

		while ((lineT = readerTest.readLine())!=null && (lineR = readerResult.readLine())!=null){

			if(!lineR.split("\t")[1].equals("N/A")){
				// create an object based on test image metadata
				Set<String> terms = new HashSet<String>();
				TextUtil.parse(lineR.split("\t")[10], terms);
				TextUtil.parse(lineR.split("\t")[8], terms);
				ImageMetadata image = new ImageMetadata(lineT.split("\t")[1], lineT.split("\t")[3], terms);

				// update respective sets
				testIDs.add(lineT.split("\t")[0]);
				users.add(lineT.split("\t")[2]);

				// load image object to the corresponding cell of the map
				if(predictedCellsOfTestImages.containsKey(lineR.split("\t")[1].split(":")[0])){
					predictedCellsOfTestImages.get(lineR.split("\t")[1].split(":")[0]).add(image);
				}else{
					predictedCellsOfTestImages.put(lineR.split("\t")[1].split(":")[0], 
							new ArrayList<ImageMetadata>());
					predictedCellsOfTestImages.get(lineR.split("\t")[1].split(":")[0]).add(image);
				}
			}
		}

		logger.info(users.size()+" different users appeared in " + testIDs.size() + " images");
		readerTest.close();
		readerResult.close();
	}
}