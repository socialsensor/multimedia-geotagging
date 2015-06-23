package gr.iti.mklab;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.methods.CrossValidation;
import gr.iti.mklab.methods.MultipleGrid;
import gr.iti.mklab.methods.LanguageModel;
import gr.iti.mklab.methods.SimilaritySearch;
import gr.iti.mklab.methods.TagCellProbMapRed;
import gr.iti.mklab.tools.Entropy;
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

		logger.info("program started");

		properties.load(new FileInputStream("config.properties"));
		String dir = properties.getProperty("dir");

		String sFolder = properties.getProperty("sFolder");
		String sTrain = properties.getProperty("sTrain");		
		String sTest = properties.getProperty("sTest");
		String hashFile = properties.getProperty("hashFile");
		
		String trainFile = properties.getProperty("trainFile");
		String testFile = properties.getProperty("testFile");

		String process = properties.getProperty("process");
		
		double thetaG = Double.parseDouble(properties.getProperty("thetaG"));
		int thetaT = Integer.parseInt(properties.getProperty("thetaT"));
		
		int coarserScale = Integer.parseInt(properties.getProperty("coarserScale"));
		int finerScale = Integer.parseInt(properties.getProperty("finerScale"));

		String coarserGrid = properties.getProperty("coarserGrid");
		String finerGrid = properties.getProperty("finerGrid");
		
		int k = Integer.parseInt(properties.getProperty("k"));
		String resultFile = properties.getProperty("resultFile");

		// Create the training and test dataset in the defined format
		if(process.contains("create") || process.equals("all")){
			DataManager.createDataSet(dir, sFolder, sTrain, trainFile, hashFile, true);
			DataManager.createDataSet(dir, sFolder, sTest, testFile, hashFile, false);
		}
		
		// Built of the Language Model and feature weighting using spatial entropy
		if(process.contains("train") || process.equals("all")){
			Set<String> testIDs = DataManager.getSetOfImageIDs(dir + testFile);
			Set<String> usersIDs = DataManager.getSetOfUserID(dir + testFile);

			TagCellProbMapRed trainLM = new TagCellProbMapRed(testIDs, usersIDs);
			
			trainLM.calculatorTagCellProb(dir, trainFile, "TagCellProbabilities/scale_" + coarserScale, coarserScale);
			Entropy.createEntropyFile(dir + "TagCellProbabilities/scale_" + coarserScale + "/tag_cell_prob");
			
			trainLM.calculatorTagCellProb(dir, trainFile, "TagCellProbabilities/scale_" + finerScale, finerScale);
			Entropy.createEntropyFile(dir + "TagCellProbabilities/scale_" + finerScale + "/tag_cell_prob");
		}
		
		// Feature selection (Cross Validation)
		if(process.contains("FS") || process.equals("all")){
			CrossValidation crosval = new CrossValidation(dir, trainFile, 10, 1.0);
			
			crosval.applyCrossValidation();
			
			crosval.calculateTagAccuracy();
		}
		
		// Language Model
		if(process.contains("LM") || process.equals("all")){
			computeLanguageModel(dir, testFile, "resultLM_scale" + coarserScale, 
					"TagCellProbabilities/scale_" + coarserScale + "/tag_cell_prob_entropy", 
					dir + "tagAcc_1",true, thetaG, thetaT, true);
			
			computeLanguageModel(dir, testFile, "resultLM_scale" + finerScale, 
					"TagCellProbabilities/scale_" + finerScale + "/tag_cell_prob_entropy", 
					dir + "/tagAccuracies_range_1.0", true, thetaG, thetaT, false);
		}

		// Internal Grid Technique
		if(process.contains("MG") || process.equals("all")){
			MultipleGrid.determinCellIDsForSS(dir + "resultLM/", "resultLM_mg" + coarserGrid + "-" + finerGrid, coarserGrid, finerGrid);
		}

		//Similarity Search
		if(process.contains("SS") || process.equals("all")){
			new SimilarityCalculator(dir + testFile, dir + "resultLM/resultLM_mg" + coarserGrid + "-" + finerGrid)
			.performSimilaritySearch(dir, trainFile, "resultSS");
			
			new SimilaritySearch(dir + testFile, dir + "resultLM/resultLM_mg" + coarserGrid + "-" + finerGrid, 
					dir + "resultSS", dir + resultFile, k, 1);
		}
		
		logger.info("program finished");
	}

	/**
	 * Function that perform language model method for a file provided and in the determined scale
	 * @param dir : directory of the project
	 * @param testFile : the file that contains the testset images
	 * @param resultFile : the name of the file that the results of the language model will be saved
	 * @param tagCellProbsFile : the file that contains the tag-cell probabilities
	 * @param tagAccFile : the file that contains the accuracies of the tags
	 * @param featureSelection : argument that indicates if the feature selection is used or not 
	 * @param thetaG : feature selection accuracy threshold
	 * @param thetaT : feature selection frequency threshold
	 */
	public static void computeLanguageModel(String dir, 
			String testFile, String resultFile, String tagCellProbsFile, 
			String tagAccFile, boolean featureSelection, double thetaG, int thetaT, boolean confidenceFlag){

		new File(dir+"resultsLM").mkdir();
		
		
		EasyBufferedReader reader = new EasyBufferedReader(dir+testFile);

		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"resultsLM/"+resultFile);

		
		EasyBufferedWriter writerCAT = new EasyBufferedWriter(dir+"confidence_associated_tags", !confidenceFlag);
		
		
		logger.info("apply language model in file "+testFile);
		
		// initialization of the Language Model
		LanguageModel lmItem = new LanguageModel(dir, tagCellProbsFile);
		Map<String, Map<String, Double>> tagCellProbsMap = lmItem.organizeMapOfCellsTags(dir + testFile, 
				tagAccFile, featureSelection, thetaG, thetaT);
		
		
		logger.info("calculate the Most Likely Cell for every query images");
		
		String line;
		int count = 0, total = 510000;
		long startTime = System.currentTimeMillis();
		Progress prog = new Progress(startTime,total,10,60, "calculate",logger);

		while ((line = reader.readLine())!=null && count<=total){

			prog.showProgress(count, System.currentTimeMillis());
			count++;

			List<String> tagsList = new ArrayList<String>();
			
			// Pre-procession of the tags and title
			String tags = TextUtil.parseImageText(line.split("\t")[4], line.split("\t")[3]);
			Collections.addAll(tagsList, tags.split(" "));
			
			String result = lmItem.calculateLanguageModel(tagsList, tagCellProbsMap, confidenceFlag);
			
			if(result==null  && line.split("\t").length>8){ // no result from tags and title procession
				tagsList = new ArrayList<String>();
				Collections.addAll(tagsList, line.split("\t")[8].toLowerCase().replaceAll("[\\p{Punct}&&[^\\+]]", "").split("\\+"));

				result = lmItem.calculateLanguageModel(tagsList, tagCellProbsMap, confidenceFlag); // give image's description in the language model (if provided)
			}
			
			writer.write(line.split("\t")[0] + ";");
			if(result!=null && !result.equals("null")){
				writer.write(result.split(";")[0]);
			}else{
				writer.write("N/A");
			}
			writer.newLine();
			
			if(result!=null && !result.equals("null") && confidenceFlag){
				writerCAT.write(line.split("\t")[0] + ";" + result.split(";")[1].replaceAll(" ", ";"));
			}else if(confidenceFlag){
				writerCAT.write("N/A");
			}
			writerCAT.newLine();
		}

		logger.info("total time for language model "+(System.currentTimeMillis()-startTime)/60000.0+"m");
		reader.close();
		writer.close();
		writerCAT.close();
		
	}
}
