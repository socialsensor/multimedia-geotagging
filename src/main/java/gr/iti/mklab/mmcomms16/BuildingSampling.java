package gr.iti.mklab.mmcomms16;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

@SuppressWarnings("unchecked")
public class BuildingSampling extends Sampling{

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.BuildingSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Building Strategy");
		
		BuildingSampling sampling = 
				new BuildingSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Set<String> buildingConcepts = new HashSet<String>();

		EasyBufferedReader reader = 
				new EasyBufferedReader("samples/building_concepts.txt");
		String line;
		while((line = reader.readLine())!=null){
			buildingConcepts.add(line);
		}
		reader.close();
		
		Set<String> buildingImages = new HashSet<String>();
		reader = new EasyBufferedReader(testFile);
		while((line = reader.readLine())!=null){
			String imageID = line.split("\t")[0];
			for(String concept:line .split("\t")[1].split(",")){
				if(buildingConcepts.contains(concept.split(":")[0])){
					buildingImages.add(imageID);
				}
			}
		}
		reader.close();
		logger.info(buildingImages.size() + " Building Images loaded");

		return buildingImages;
	}

	protected Object writeInFile(Object data) {

		Set<String> buildingImages = (Set<String>) data;

		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/building_sampling.txt");
		for(String image:buildingImages){
			writer.write(image + "\t");
			writer.newLine();
		}
		writer.close();
		
		return buildingImages;
	}
}