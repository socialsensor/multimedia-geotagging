package gr.iti.mklab.tools;

import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.util.EasyBufferedReader;
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
 * Class that implements similarity search based on Map-Reduce scheme.
 * For a query image, the similarity between the images contained in the train set is calculated based on their corresponding tags.
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

			if (!testIDs.contains(line.split("\t")[0]) && !users.contains(line.split("\t")[2]) // train image and its user are not contained in the test set
					&& !line.split("\t")[6].isEmpty() && !line.split("\t")[7].isEmpty() // train image contains coordinations
					&& (!line.split("\t")[4].isEmpty() || !line.split("\t")[3].isEmpty())){ // train image contains any textual information

				// get image cell based on its latitude-longitude pair
				BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(line.split("\t")[6])).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(line.split("\t")[7])).setScale(2, BigDecimal.ROUND_HALF_UP);

				// there is at least estimated location laying inside the borders of cell
				if(predictedCellsOfTestImages.containsKey(tmpLonCenter+"_"+tmpLatCenter)){

					// calculate similarity between the train image and all images that lay inside the boarded of the specific cell
					for(ImageMetadata entry : predictedCellsOfTestImages.get(tmpLonCenter+"_"+tmpLatCenter)){

						Set<String> trainImageTags = new HashSet<String>();

						Collections.addAll(trainImageTags, TextUtil.parseImageText(line.split("\t")[4], line.split("\t")[3]).split(" "));

						double counter = 0.0;

						// determine the common tags
						for (String tag:entry.getTags()){
							if (trainImageTags.contains(tag)){
								counter += 1.0;
							}
						}

						// calculate similarity
						double sjacc = counter / (entry.getTags().size() 
								+ trainImageTags.size() - counter);
						if(sjacc>0.05){
							output.collect(new Text(entry.getId()), new Text(String.valueOf(sjacc) + ">" + line.split("\t")[7] + "_"+line.split("\t")[6]));
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
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

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
			simImages = MyHashMap.sortByValues(simImages);

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
	public void performSimilaritySearch(String dir, String trainFile, String outFolder) throws Exception {

		JobConf conf = new JobConf(SimilarityCalculator.class);
		conf.setJobName("similaritysearch");

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
		logger.info("create temporery copy of the file " + trainFile);
		DataManager.createTempFile(dir, trainFile);

		FileInputFormat.setInputPaths(conf, new Path(dir + "temp"));
		FileOutputFormat.setOutputPath(conf, new Path(dir + outFolder));

		JobClient.runJob(conf);
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

			if(!lineR.split(";")[1].equals("na")){

				// create an object based on test image metadata
				List<String> tags = new ArrayList<String>();
				Collections.addAll(tags, TextUtil.parseImageText(lineT.split("\t")[4], lineT.split("\t")[3]).split(" "));
				ImageMetadata image = new ImageMetadata(lineT.split("\t")[0],lineT.split("\t")[2], tags);

				// update respective sets
				testIDs.add(lineT.split("\t")[0]);
				users.add(lineT.split("\t")[2]);

				// load image object to the corresponding cell of the map
				if(predictedCellsOfTestImages.containsKey(lineR.split(";")[1].split(">")[0])){
					List<ImageMetadata> tmp = predictedCellsOfTestImages.get(lineR.split(">")[0]);
					tmp.add(image);
					predictedCellsOfTestImages.put(lineR.split(";")[1].split(">")[0], tmp);
				}else{
					List<ImageMetadata> tmp = new ArrayList<ImageMetadata>();
					tmp.add(image);
					predictedCellsOfTestImages.put(lineR.split(";")[1].split(">")[0], tmp);
				}
			}
		}

		logger.info(users.size()+" different users appeared in " + testIDs.size() + " images");
		readerTest.close();
		readerResult.close();
	}
}