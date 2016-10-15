package gr.iti.mklab.metrics;

import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.TextUtil;
import gr.iti.mklab.util.Utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.log4j.Logger;

/**
 * Class that calculate the locality of the terms and saves the results in file.
 * The implementation employ hadoop map-reduce function.
 * @author gkordo
 *
 */
public class Locality {

	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.Locality");
	private static Set<String> testIDs;
	private static Set<String> users;
	private static int scale;

	public Locality(String testFile, int scale){
		testIDs = DataManager.getSetOfImageIDs(testFile);
		users = DataManager.getSetOfUserID(testFile);
		Locality.scale = scale;
	}

	/**
	 * Map class that takes the lines of the train file as input and creates key-value pairs,
	 * using as keys the tags contained in the images and as values strings that contain
	 * the information regarding the cell and user ID.
	 * @author gkordo
	 *
	 */
	public static class MapLocality extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

		/**
		 * Required map function
		 * @param key : key value
		 * @param value : input string
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			String[] metadata = value.toString().split("\t");

			if (!testIDs.contains(metadata[1]) && !users.contains(metadata[3]) // train image and its user are not contained in the test set
					&& !metadata[12].isEmpty() && !metadata[13].isEmpty() // train image contains coordinations
					&& (!metadata[10].isEmpty() || !metadata[8].isEmpty())){ // train image contains any textual information

				BigDecimal tmpLonCenter = new BigDecimal(
						Double.parseDouble(metadata[12])).setScale(scale, BigDecimal.ROUND_HALF_UP);
				BigDecimal tmpLatCenter = new BigDecimal(
						Double.parseDouble(metadata[13])).setScale(scale, BigDecimal.ROUND_HALF_UP);

				//get image user ID
				String userID = metadata[3];

				// get image tags
				Set<String> terms = new HashSet<String>();
				TextUtil.parse(metadata[10], terms);
				TextUtil.parse(metadata[8], terms);

				// send key-value pairs
				for(String term:terms) {
					if(!term.isEmpty() && term.length() > 2){
						for(int j=-2;j<2;j++){
							for(int k=-2;k<2;k++){
								output.collect(new Text(term), new Text(userID + ">" +
										(tmpLonCenter.doubleValue()+((j)*0.01)) + "_" +
										(tmpLatCenter.doubleValue()+((k)*0.01))));
							}							
						}
					}
				}
			}
		}
	}

	/**
	 * Reduce class that get the key-value pairs and calculate the locality of every term.
	 * @author gkordo
	 *
	 */
	public static class ReduceLocality extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

		/**
		 * Required reduce function
		 * @param key : key value
		 * @param values : set of values that share the same key
		 * @param output : output collector
		 * @param reporter : reporter of the job
		 */
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

			// map of cells that contains the count of the different users for every single cell
			Map<String,Set<String>> cells =  new HashMap<String,Set<String>>();
			int Nt = 0; // total user count

			while (values.hasNext()) {

				String value = values.next().toString();

				// retrieve cell ID and user ID from the value of the pair 
				String user = value.split(">")[0];
				String cell = value.split(">")[1];

				// update of the frequency map
				if(cells.containsKey(cell)){
					if(!cells.get(cell).contains(user)){
						cells.get(cell).add(user);
						Nt++;
					}
				}else{
					cells.put(cell,new HashSet<String>());
					cells.get(cell).add(user);
					Nt++;
				}
			}

			// locality calculation
			double locality = 0.0;
			for(Entry<String, Set<String>> entry : cells.entrySet()){
				int v=entry.getValue().size();
				locality+=v*(v-1)/Nt;

			}

			// send output to collector
			if(locality > 0.0){
				output.collect(key, new Text(locality + ""));
			}
		}
	}

	/**
	 * Core function for the job of tag-cell probabilities calculation.
	 * @param dir : project directory
	 * @param trainFolder : the file of the train set
	 * @throws IOException : file not found
	 */
	public void calculateLocality(String dir, String trainFolder) throws IOException{

		logger.info("Process: Locality weight calculation\t|\t"
				+ "Status: INITIALIZE");
		JobConf conf = new JobConf(Locality.class);
		conf.setJobName("Locality");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(MapLocality.class);
		conf.setReducerClass(ReduceLocality.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// clean the output file directory
		File folder = new File(dir + "temp/locality");
		if (folder.exists()) {
			FileUtils.cleanDirectory(folder);
			FileUtils.forceDelete(folder);
		}

		FileInputFormat.setInputPaths(conf, new Path(dir + trainFolder));
		FileOutputFormat.setOutputPath(conf, new Path(dir + "temp/locality"));

		logger.info("Process: Locality weight calculation\t|\t"
				+ "Status: STARTED");
		long startTime = System.currentTimeMillis();
		JobClient.runJob(conf);

		sortAndStore(dir + "temp/locality/part-00000",
				dir + "Weights/locality_weights");

		logger.info("Process: Locality weight calculation\t|\t"
				+ "Status: COMPLETED\t|\tTotal time: " + 
				(System.currentTimeMillis()-startTime)/60000.0+"m");
	}

	/**
	 * Sort terms based on their locality values and calculate weights.
	 * The locality term weight are stored in the given file.
	 * @param inFile : file of the locality values of the terms
	 * @param outFile : output file
	 */
	private void sortAndStore(String inFile, String outFile){

		// load locality values
		EasyBufferedReader reader = new EasyBufferedReader(inFile);
		Map<String, Double> termLocalityValues = new HashMap<String, Double>();
		String line;
		while ((line = reader.readLine())!=null){
			String term = line.split("\t")[0];
			double locality = Double.parseDouble(line.split("\t")[1]);
			termLocalityValues.put(term, locality);
		}
		reader.close();

		// sort and store weights
		termLocalityValues = Utils.sortByValues(termLocalityValues);
		EasyBufferedWriter writer = new EasyBufferedWriter(outFile);
		int i = 0, totalTerms = termLocalityValues.size();
		for(Entry<String, Double> entry : termLocalityValues.entrySet()){
			writer.write(entry.getKey()+"\t"+(double)(totalTerms-i)/totalTerms);
			writer.newLine();
			i++;
		}
		writer.close();
	}
}
