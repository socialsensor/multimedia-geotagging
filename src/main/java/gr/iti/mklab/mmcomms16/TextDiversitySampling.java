package gr.iti.mklab.mmcomms16;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import info.debatty.java.lsh.MinHash;

@SuppressWarnings("unchecked")
public class TextDiversitySampling extends Sampling {

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.TextDiversitySampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Text Diversity Strategy");
		
		TextDiversitySampling sampling = new TextDiversitySampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<List<Integer>, List<String>> buckets =
				new HashMap<List<Integer>, List<String>>();
		Map<String, Integer> tags =
				new HashMap<String, Integer>();
		int n = 510914;
		MinHash mh = new MinHash(0.1, n);
		
		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);	
		String line;
		while((line = reader.readLine())!=null){
			String imageID = line.split("\t")[1];
			String imageTags = line.split("\t")[10];
			boolean[] vector = new boolean[n];

			for(String tag:imageTags.split(",")){
				if(!tags.containsKey(tag)){
					tags.put(tag, tags.size());
				}
				vector[tags.get(tag)] = true;
			}
			
			List<Integer> hash = IntStream.of((mh.signature(vector)
					)).boxed().collect(Collectors.toList());
			if(buckets.containsKey(hash)){
				buckets.get(hash).add(imageID);
			}else{
				buckets.put(hash, new ArrayList<String>());
				buckets.get(hash).add(imageID);
			}
		}
		reader.close();
		logger.info(buckets.size() + " Buckets created");

		return buckets;
	}

	protected Object writeInFile(Object data) {

		Map<List<Integer>, List<String>> buckets =
				(Map<List<Integer>, List<String>>)  data;

		Set<String> respond = new HashSet<String>();
		
		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/text_diversity_sampling.txt");

		for(Entry<List<Integer>, List<String>> bucket
				:buckets.entrySet()){
			List<String> images = bucket.getValue();
			Collections.shuffle(images);
			
			respond.add(images.get(0));
			writer.write(images.get(0));
			writer.newLine();
		}
		writer.close();
		
		return respond;
	}
	
}
