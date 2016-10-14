package gr.iti.mklab;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.methods.MultipleGrid;
import gr.iti.mklab.data.GeoCell;
import gr.iti.mklab.methods.LanguageModel;
import gr.iti.mklab.methods.SimilaritySearch;
import gr.iti.mklab.methods.TermCellProbMapRed;
import gr.iti.mklab.metrics.Entropy;
import gr.iti.mklab.metrics.Locality;
import gr.iti.mklab.tools.DataManager;
import gr.iti.mklab.tools.SimilarityCalculator;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Progress;
import gr.iti.mklab.util.TextUtil;

/**
 * The main class that combines all the other class in order to implement the method.
 * For memory allocation issues the main method has been separated in three steps, create,
 * train, FS (Feature Selection), LM (language model), IG (multiple grid technique) and 
 * SS (similarity search).
 * @author gkordo
 *
 */
public class MultimediaGeotagging {

	static Logger logger = Logger.getLogger("gr.iti.mklab.MainPlacingTask");

	public static void main(String[] args) throws Exception{

		Properties properties = new Properties();

		logger.info("Program Started");

		properties.load(new FileInputStream("config.properties"));
		String dir = properties.getProperty("dir");

		String trainFile = properties.getProperty("trainFile");
		String testFile = properties.getProperty("testFile");

		String process = properties.getProperty("process");

		int coarserScale = Integer.parseInt(properties.getProperty("coarserScale"));
		int finerScale = Integer.parseInt(properties.getProperty("finerScale"));

		int k = Integer.parseInt(properties.getProperty("k"));
		String resultFile = properties.getProperty("resultFile");


		// Built of the Language Model
		if(process.contains("train") || process.equals("all")){
			Set<String> testIDs = DataManager.getSetOfImageIDs(dir + testFile);
			Set<String> usersIDs = DataManager.getSetOfUserID(dir + testFile);

			TermCellProbMapRed trainLM = new TermCellProbMapRed(testIDs, usersIDs);

			trainLM.calculatorTermCellProb(dir, trainFile, 
					"TermCellProbs/scale_" + coarserScale, coarserScale);

			trainLM.calculatorTermCellProb(dir, trainFile, 
					"TermCellProbs/scale_" + finerScale, finerScale);
		}

		// Feature Selection and Feature Weighting (Locality and Spatial Entropy Calculation)
		if(process.contains("FS") || process.equals("all")){
			Entropy.calculateEntropyWeights(dir, "TermCellProbs/scale_" 
					+ coarserScale + "/term_cell_probs");
			
			Entropy.calculateEntropyWeights(dir, "TermCellProbs/scale_"
					+ finerScale + "/term_cell_probs");
			
			Locality loc = new Locality(dir + testFile, coarserScale);
			loc.calculateLocality(dir, trainFile);
		}

		// Language Model
		if(process.contains("LM") || process.equals("all")){
			MultimediaGeotagging.computeMLCs(dir, testFile, "resultLM_scale" + coarserScale, 
					"TermCellProbs/scale_" + coarserScale + "/term_cell_probs", 
					"Weights", true);

			MultimediaGeotagging.computeMLCs(dir, testFile, "resultLM_scale" + finerScale, 
					"TermCellProbs/scale_" + finerScale + "/term_cell_probs", 
					"Weights", false);
		}

		// Multiple Grid Technique
		if(process.contains("MG") || process.equals("all")){
			MultipleGrid.determinCellIDsForSS(dir + "resultLM/", 
					"resultLM_mg" + coarserScale + "-" + finerScale,
					"resultLM_scale"+coarserScale, "resultLM_scale"+finerScale);
		}

		//Similarity Search
		if(process.contains("SS") || process.equals("all")){
			new SimilarityCalculator(dir + testFile, dir + 
					"resultLM/resultLM_mg" + coarserScale + "-" + finerScale)
			.performSimilarityCalculation(dir, trainFile, "resultSS");

			new SimilaritySearch(dir + testFile, dir + 
					"resultLM/resultLM_mg" + coarserScale + "-" + finerScale, 
					dir + "resultSS/image_similarities", dir + resultFile, k, 1);
		}

		logger.info("Program Finished");
	}

	/**
	 * Function that perform language model method for a file provided and in the determined scale
	 * @param dir : directory of the project
	 * @param testFile : the file that contains the testset images
	 * @param resultFile : the name of the file that the results of the language model will be saved
	 * @param termCellProbsFile : the file that contains the term-cell probabilities
	 * @param weightFolder : the folder that contains the files of term weights
	 * @param thetaG : feature selection accuracy threshold
	 * @param thetaT : feature selection frequency threshold
	 */
	public static void computeMLCs(String dir, 
			String testFile, String resultFile, String termCellProbsFile, 
			String weightFolder, boolean confidenceFlag){

		logger.info("Process: Language Model MLC\t|\t"
				+ "Status: INITIALIZE\t|\tFile: " + testFile);
		
		new File(dir + "resultsLM").mkdir();
		EasyBufferedReader reader = new EasyBufferedReader(dir + testFile);
		EasyBufferedWriter writer = new EasyBufferedWriter(dir + "resultsLM/" + resultFile);
		EasyBufferedWriter writerCE = new EasyBufferedWriter(dir + "resultsLM/" +
				resultFile + "_conf_evid");

		// initialization of the Language Model
		LanguageModel lmItem = new LanguageModel();
		Map<String, Map<String, Double>> termCellProbsMap = lmItem.organizeMapOfCellsTags(dir + testFile,
				dir + termCellProbsFile, dir + weightFolder);

		logger.info("Process: Language Model MLC\t|\t"
				+ "Status: STARTED");
		
		
		int count = 0, total = 1000000;
		long startTime = System.currentTimeMillis();
		Progress prog = new Progress(startTime,total,10,60, "calculate",logger);
		String line;
		while ((line = reader.readLine())!=null && count<=total){

			prog.showProgress(count, System.currentTimeMillis());
			count++;

			// Pre-procession of the tags and title
			Set<String> terms = new HashSet<String>();
			TextUtil.parse(line.split("\t")[10], terms);
			TextUtil.parse(line.split("\t")[8], terms);

			GeoCell result = lmItem.calculateLanguageModel(terms, termCellProbsMap, confidenceFlag);

			if(result == null){ // no result from tags and title procession

				// give image's description in the language model (if provided)
				result = lmItem.calculateLanguageModel(TextUtil.parse(line.split("\t")[8], terms),
						termCellProbsMap, confidenceFlag);
			}

			// write the results
			if(result != null && !result.equals("null")){
				writer.write(line.split("\t")[0] + "\t" + result.getID());
				if(confidenceFlag)
					writerCE.write(line.split("\t")[0] + "\t" + result.getConfidence() +
							"\t" + result.getConfidence().toString());
			}else{
				writer.write(line.split("\t")[0] + "\tN/A");
				if(confidenceFlag)
					writerCE.write(line.split("\t")[0] + "\tN/A");
			}
			writer.newLine();
			if(confidenceFlag)
				writerCE.newLine();
		}

		logger.info("Process: Language Model MLC\t|\tStatus: COMPLETED\t|\tTotal Time: " +
				(System.currentTimeMillis()-startTime)/60000.0+"m");
		reader.close();
		writer.close();
		writerCE.close();

	}
}
