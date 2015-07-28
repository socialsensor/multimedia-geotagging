package gr.iti.mklab.tools;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

import org.apache.log4j.Logger;


/**
 * Entropy class update the file that contains the tag-cell probabilities with the spatial entropy of every individual tag.
 * Calculate the spatial tag entropy for all of the tags. Entropy is used for feature weighting.
 * @author gkordo
 *
 */
public class Entropy {
	
	static Logger logger = Logger.getLogger("gr.iti.mklab.method.Entropy");
	
	// Calculate the spatial tag entropy
	public static void createEntropyFile(String fileTagCell){

		EasyBufferedReader reader = new EasyBufferedReader(fileTagCell);		

		EasyBufferedWriter writer = new EasyBufferedWriter(fileTagCell+"_entropy");

		String input;
		String[] inputLine;

		logger.info("update file " + fileTagCell);
		logger.info("add tags' entropy values");

		int j=0;

		long sTime = System.currentTimeMillis();

		while ((input=reader.readLine())!=null){

			if(input.split("\t").length>1){
				inputLine = input.split("\t")[1].trim().split(" ");
				int cellNum = inputLine.length;

				double[] p = new double[cellNum];
				
				// Shannon entropy for the specific tag
				for(int i=0; i<cellNum ;i++){
					p[i] = Double.parseDouble(inputLine[i].split(">")[1]);
				}

				j++;

				double entropy = computeEntropyNaive(p);

				writer.write(input.split("\t")[0]+"\t"+entropy+"\t"+input.split("\t")[1].trim());
				writer.newLine();
			}
		}

		logger.info("file updated with tags' entropy values for "+j+" tags");
		logger.info("total time needed " + (System.currentTimeMillis()-sTime)/1000.0 + "s");
		writer.close();
		reader.close();		
	}
	
	/**
	 * Shannon entropy formula
	 * @param probabilities : distribution of the probabilities
	 * @return
	 */
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