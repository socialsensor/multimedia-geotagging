package gr.iti.mklab.method;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.MyHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Entropy {
	
	public static void createEntropyFile(String fileTagCell){
		
		EasyBufferedReader reader = new EasyBufferedReader(fileTagCell);		

		Map<String,Double> entropyMap = new HashMap<String,Double>();
		Map<String,String[]> output = new HashMap<String,String[]>();

		String input;
		String[] inputLine;

		while ((input=reader.readLine())!=null){

			inputLine = input.split(" ");
			int cellNum = inputLine.length-2;

			double[] p = new double[cellNum];

			for(int i=0; i<cellNum ;i++){
				p[i] = Double.parseDouble(inputLine[i+2].split(">")[1]);

			}

			double entropy;
			entropy = computeEntropyNaive(p);
			entropyMap.put(inputLine[0],entropy);
			output.put(inputLine[0], inputLine);
		}

		Map<String, Double> cellsProbsSorted = MyHashMap.sortByValues(entropyMap);
		
		writeUpdatedFile(cellsProbsSorted, output, fileTagCell);
		
		reader.close();		
	}

	public static void writeUpdatedFile(Map<String, Double> cellsProbsSorted, Map<String,String[]> output, String fileTagCell){
		
		EasyBufferedWriter writer = new EasyBufferedWriter(fileTagCell.replace(".txt","")+"_entropy.txt");
		int i=0;
		
		for(Entry<String, Double> entryCell: cellsProbsSorted.entrySet()){
			i++;
			String tag = entryCell.getKey();
			writer.write(tag+" "+i+"_"+entryCell.getValue()+" ");
			int cellNum = output.get(tag).length;
			
			for(int j=2; j<cellNum ;j++){
				writer.write(output.get(tag)[j]+" ");	
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	private static double computeEntropyNaive(final double[] probabilities) {
		double entropy = 0.0;
		for (int i = 0; i < probabilities.length; i++) {
			final double p = probabilities[i];
			if(p!=0.0){
				entropy -= p * Math.log(p);
			}
		}
		return entropy;
	}
}