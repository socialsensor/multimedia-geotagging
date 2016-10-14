package gr.iti.mklab.mmcomms16;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

@SuppressWarnings("unchecked")
public class VisualSampling extends Sampling{

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.VisualSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Visual Strategy");
		
		VisualSampling sampling = 
				new VisualSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Set<String>> concepts =
				new HashMap<String, Set<String>>();

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){
			String imageID = line.split("\t")[0];
			for(String concept:line .split("\t")[1].split(",")){
				if(concepts.containsKey(concept.split(":")[0])){
					concepts.get(concept.split(":")[0]).add(imageID);
				}else{
					concepts.put(concept.split(":")[0], new HashSet<String>());
					concepts.get(concept.split(":")[0]).add(imageID);
				}
			}
		}
		reader.close();
		logger.info(concepts.size() + " Concepts loaded");

		return concepts;
	}

	protected Object writeInFile(Object data) {

		Map<String, Set<String>> concepts = 
				(Map<String, Set<String>>) data;

		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/visual_sampling.txt");
		for(Entry<String, Set<String>> concept:concepts.entrySet()){
			writer.write(concept.getKey() + "\t");
			for(String images:concept.getValue()){
				writer.write(images + " ");
			}
			writer.newLine();
		}
		writer.close();
		
		return concepts;
	}
}
