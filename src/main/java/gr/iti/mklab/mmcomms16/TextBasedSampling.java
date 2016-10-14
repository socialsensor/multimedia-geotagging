package gr.iti.mklab.mmcomms16;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Utils;

@SuppressWarnings("unchecked")
public class TextBasedSampling extends Sampling {

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.TextBasedSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Text-based Strategy");
		
		TextBasedSampling sampling = new TextBasedSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Integer> images = 
				new HashMap<String, Integer>();

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);	
		String line;
		while((line = reader.readLine())!=null){
			int tags = (!line.split("\t")[10].isEmpty()
					?line.split("\t")[10].split(",").length:0);
			int title =	(!line.split("\t")[8].isEmpty()
					?line.split("\t")[8].split("\\+").length:0);

			images.put(line.split("\t")[1], tags+title);
		}
		reader.close();
		logger.info(images.size() + " Images loaded");

		return images;
	}

	protected Object writeInFile(Object data) {

		Map<String, Integer> images = 
				(Map<String, Integer>) data;

		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/text_based_sampling.txt");

		Set<String> respond = new HashSet<String>();

		int median = Utils.medianItemInt(images);
		
		for(Entry<String, Integer> image:images.entrySet()){
			if(image.getValue() >= median){
				respond.add(image.getKey());
				writer.write(image.getKey());
				writer.newLine();
			}
		}
		writer.close();

		return respond;
	}
}
