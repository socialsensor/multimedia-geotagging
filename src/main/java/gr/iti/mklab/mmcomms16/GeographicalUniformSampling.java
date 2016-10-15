package gr.iti.mklab.mmcomms16;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Utils;

@SuppressWarnings("unchecked")
public class GeographicalUniformSampling extends Sampling {

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.GeographicalUniformSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Geographical Uniform Strategy");
		
		GeographicalUniformSampling sampling = 
				new GeographicalUniformSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Set<String>> cells =
				new HashMap<String, Set<String>>();

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){

			BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(
					line.split("\t")[12])).setScale(1, BigDecimal.ROUND_HALF_UP);
			BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(
					line.split("\t")[13])).setScale(1, BigDecimal.ROUND_HALF_UP);

			String cell = tmpLatCenter + " " + tmpLonCenter;
			if(cells.containsKey(cell)){
				cells.get(cell).add(line.split("\t")[1]);
			}else{
				cells.put(cell, new HashSet<String>());
				cells.get(cell).add(line.split("\t")[1]);
			}
		}
		reader.close();
		logger.info(cells.size() + " Cells loaded");

		return cells;
	}

	protected Object writeInFile(Object data) {

		Map<String, Set<String>> cells = (Map<String, Set<String>>) data;

		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/geographical_uniform_sampling.txt");

		int median = Utils.medianSet(cells);

		Set<String> respond = new HashSet<String>();

		for(Entry<String, Set<String>> cell:cells.entrySet()){
			List<String> images = 
					new ArrayList<String>(cell.getValue());
			Collections.shuffle(images);

			for(int i=0;i<median;i++){
				if(i<images.size()){
					respond.add(images.get(i));
					writer.write(images.get(i));
					writer.newLine();
				}else{
					break;
				}
			}
		}
		writer.close();

		return respond;
	}
}
