package gr.iti.mklab.methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import gr.iti.mklab.tools.CenterOfGravity;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Progress;
import gr.iti.mklab.util.EasyBufferedReader;

/**
 * Class that estimates the final location for every query image
 * @author gkordo
 *
 */
public class SimilaritySearch extends CenterOfGravity{

	private Map<String,String> estimatedCellMap = new HashMap<String,String>();
	private Map<String,String> similarities = new HashMap<String,String>();
	private static Logger logger = Logger.getLogger("gr.iti.mklab.methods.SimilaritySearch");

	/**
	 * Contractor of the class.
	 * @param multipleGridFile : file that contains the results of the multiple grid technique
	 * @param similarityFile : file that contains the similar images of every query images 
	 * @param testFile : file that contains the test image's metadata
	 * @param outputFile : name of the output file
	 * @param k : number of similar images based on the center-of-gravity is calculated
	 * @param a : variable required for center-of-gravity calculation
	 */
	public SimilaritySearch(String testFile,String multipleGridFile, 
			String similarityFile, String outputFile, int k, int a) {
		super(a);

		logger.info("Process: Location Estimation\t|\t"
				+ "Status: INITIALIZE");
		loadEstimatedCells(multipleGridFile);
		logger.info("Process: Location Estimation\t|\t"
				+ "Status: STARTED");
		estimateLocation(similarityFile,k);
		writeResultsInFile(testFile, outputFile);
		logger.info("Process: Location Estimation\t|\t"
				+ "Status: COMPLETED");
	}

	/**
	 * Function that loads the estimated cells from the Multiple Grid Technique.
	 * @param multipleGridFile : ile that contains the results of the multiple grid technique
	 */
	private void loadEstimatedCells(String multipleGridFile) {

		EasyBufferedReader reader = new EasyBufferedReader(multipleGridFile);

		String line;
		while ((line = reader.readLine())!=null){
			if((!line.split("\t")[1].equals("N/A"))){
				estimatedCellMap.put(line.split("\t")[0], line.split("\t")[1]);
			}
		}

		reader.close();
	}

	/**
	 * Final location estimation of the images contained in the test set 
	 * @param similarityFile : file that contains the similar images of every query images 
	 * @param cellFile : file that contains the results of the multiple grid technique
	 * @param k : number of similar images based on the center-of-gravity is calculated
	 */
	private void estimateLocation(String similarityFile, int k) {

		EasyBufferedReader reader = new EasyBufferedReader(similarityFile);

		Progress prog = new Progress(System.currentTimeMillis(), 1000000, 100, 1, "calculate", logger);
		int count=0;
		String line;

		// Calculate the final results
		while ((line = reader.readLine())!=null){
			prog.showProgress(count, System.currentTimeMillis());
			if(estimatedCellMap.containsKey(line.split("\t")[0])){
				similarities.put(line.split("\t")[0], 
						findSimilarImages(line, estimatedCellMap.get(line.split("\t")[0]), k));
			}
			count++;
		}
		reader.close();
	}

	/**
	 * Location estimation for a query image.
	 * @param line : line that contain the similarity of the train images
	 * @param cells : estimated cells from the multiple grid technique
	 * @param k : number of similar images based on the center-of-gravity is calculated
	 * @return estimated location
	 */
	private static String findSimilarImages(String line, String cells, int k){

		List<String> images = new ArrayList<String>();
		Collections.addAll(images, line.split("\t")[1].split(" "));

		Map<String,Double> similarity = new HashMap<String,Double>(k);
		Map<String,Double> similarityCoarser = new HashMap<String,Double>(k);

		boolean flag = false;
		Double[] result = new Double[2];

		// final estimation
		for(String image:images){
			if(similarity.size()<k){
				if(!cells.split(">")[0].equals(cells.split(">")[1])){
					if(deterimCell(image.split(">")[0],cells)){
						similarity.put(image.split(">")[0], Double.parseDouble(image.split(">")[1]));
					}else if(similarityCoarser.size()<k && similarity.isEmpty()){
						similarityCoarser.put(image.split(">")[0], Double.parseDouble(image.split(">")[1]));
					}
				}else {
					similarity.put(image.split(">")[0], Double.parseDouble(image.split(">")[1]));
				}
			}else{
				flag = true;
				result = computeCoordination(similarity);
				break;
			}
		}

		if(similarity.size()>0 && !flag){
			flag = true;
			result = computeCoordination(similarity);
		}else if(similarityCoarser.size()>0 && !flag){
			flag = true;
			result = computeCoordination(similarityCoarser);
		}

		// final return
		if(flag){
			return result[1] + "\t" + result[0];
		}else{
			return cells.split(">")[0].replace("_", "\t");
		}
	}

	/**
	 * Function that determines if the given point lays inside a define cell.
	 * @param point : latitude-longitude pair
	 * @param cell : grid's cell
	 * @return a boolean that contain the information
	 */
	private static boolean deterimCell(String point, String cell){

		boolean cellID = false;

		Double[] pointLoc = {Double.parseDouble(point.split("_")[0]), Double.parseDouble(point.split("_")[1])};
		Double[] cellLoc = {Double.parseDouble(cell.split("_")[0]), Double.parseDouble(cell.split("_")[1])};

		if((pointLoc[0]>=(cellLoc[0]-0.0005)) && (pointLoc[0]<=(cellLoc[0]+0.0005))
				&&(pointLoc[1]>=(cellLoc[1]-0.0005)) && (pointLoc[1]<=(cellLoc[1]+0.0005))){
			cellID = true;
		}

		return cellID;
	}

	/**
	 * Function that write the result in a file
	 * @param testFile : file that contains the test image's metadata
	 * @param outputFile : name of the output file
	 */
	private void writeResultsInFile(String testFile, String outputFile) {

		EasyBufferedReader reader = new EasyBufferedReader(testFile);
		EasyBufferedWriter writer = new EasyBufferedWriter(outputFile);		

		String line;
		// for every query image
		while ((line = reader.readLine())!=null){

			writer.write(line.split("\t")[0]);

			if(similarities.containsKey(line.split("\t")[0])){ // the location have been estimated
				writer.write(line.split("\t")[1] + "\t" +
						line.split("\t")[12] + "\t" + line.split("\t")[13] + "\t" +
						similarities.get(line.split("\t")[0]));
				writer.newLine();
			}else{ // no estimation
				writer.write(line.split("\t")[1] + "\t" +
						line.split("\t")[12] + "\t" + line.split("\t")[13]
						+ "\t-73.98282136256299\t40.75282028252674");
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}	
}
