package gr.iti.mklab.test;

import gr.iti.mklab.geo.ReverseGeocoder;
import gr.iti.mklab.methods.LanguageModel;
import gr.iti.mklab.util.CellCoder;
import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This is an example of the algorithm. It takes as input a file whose every line is a different sentence.
 * Every sentence is used as a query for an language model that is built, in order to calculate the most possible cell
 * based on pre-calculated word-cell probabilities. This identical cell is the final location estimation for every sentence.
 * The output is a file that contains all the estimated location(cells) and the countries that they belong for all sentences.
 * Each line of the output file corresponds to the respective line of the input file.
 * @author gkordo
 *
 */
public class MultimediaGeolocator {

	/**
	 * This is the main function, where all the initialization of the needed variables take place.
	 * @param args[0] : the root directory of the project, where the util file must be contained. 
	 * @param args[1] : input file that contains the query sentences. Every sentence have to be in individual line, otherwise they are considered as one. 
	 * @param args[2] : the pathname of the output file. It is going to be stored in the specified path with the given name.
	 */
	public static void main(String[] args){

		Logger logger = Logger.getLogger("gr.iti.mklab.methods.TestGeoPrediction");

		logger.info("program started");

		String dir = args[0];
		String testFile = args[1];
		String resultFile = args[2];

		String wordCellProbsFile = dir+"/multi-geo-utils/wordcellprobs.txt";
		String citiesFile = dir+"/multi-geo-utils/cities1000.txt";
		String countryInfoFile = dir+"/multi-geo-utils/countryInfo.txt";

		testLanguageModel(dir,testFile,resultFile,wordCellProbsFile,citiesFile, countryInfoFile);

		logger.info("program finished");
	}

	/**
	 * This is the functions that the location estimation for every sentence takes place. The language model is been loaded and is used for querying every sentence in input file.
	 * @param dir : the root directory.
	 * @param testFile : input file.
	 * @param resultFile : output file.
	 * @param wordCellProbsFile : name of the file that contains the probabilities of every cell for every word.
	 * @param citiesFile : Geonames file to be used as a geo-index by the ReverseGeocoder.
	 * @param countryInfoFile : Geonames file that contains informations for every country.
	 */
	public static void testLanguageModel(String dir, String testFile, String resultFile, String wordCellProbsFile, String citiesFile, String countryInfoFile){

		EasyBufferedReader reader = new EasyBufferedReader(testFile);
		EasyBufferedWriter writer = new EasyBufferedWriter(resultFile);

		/*
		 * Initialization of the language model and the load of the word-cell probabilities.
		 * The generated map allocate a significant amount of memory so it is recommended to 
		 * be stored as a local variable instead of a global/static variable, in order to be
		 * easier manageable from the system.
		 */
		LanguageModel lmItem = new LanguageModel(wordCellProbsFile);
		Map<String, Map<Long, Double>> wordCellProbsMap = lmItem.organizeWordCellProbsMap();

		ReverseGeocoder rgeoService = new ReverseGeocoder(citiesFile, countryInfoFile);		

		String input;
		//perform language model for every query sentence.
		while ((input = reader.readLine())!=null){

			List<String> tagsList = new ArrayList<String>();
			Collections.addAll(tagsList, TextUtil.cleanText(input).split("\\s"));

			String estimatedCell = lmItem.calculateLanguageModel(tagsList, wordCellProbsMap);

			//if there is no estimation the return from the calculation function is null.
			if(estimatedCell!=null){
				double[] result = CellCoder.cellDecoding(Long.parseLong(estimatedCell.split(" ")[0]));
				writer.write(String.valueOf(result[0]+" "+result[1]+"\t"+
						rgeoService.getCityAndCountryByLatLon(result[0], result[1]))+"\t"+
						estimatedCell.split("\t")[1]+"\t"+estimatedCell.split("\t")[2]);
			}else{
				writer.write("N/A");
			}
			writer.newLine();
		}
		reader.close();
		writer.close();
	}
}
