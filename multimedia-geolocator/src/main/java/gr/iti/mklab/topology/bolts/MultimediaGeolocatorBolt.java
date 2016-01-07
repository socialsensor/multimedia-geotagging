/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.topology.bolts;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import gr.iti.mklab.data.Cell;
import gr.iti.mklab.util.CellCoder;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.TextUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Implementation of Multimedia-Geolocator Bolt
 * @author gkordo
 */
public class MultimediaGeolocatorBolt extends AbstractGeolocatorBolt {


	protected static Logger logger = Logger.getLogger("gr.iti.mklab.topology.bolts.MultimediaGeolocatorBolt");

	/**
	 * Class constructor
	 * @param strExampleEmitFieldsId : emitted fields name
	 * @param restletURL : restlet URL
	 */
	public MultimediaGeolocatorBolt(String strExampleEmitFieldsId, String restletURL) {
		super(strExampleEmitFieldsId, restletURL);
	}

	/**
	 * Function that prepare the map that contains the information that will be added to the main message.
	 * @param mlc : the most likely cell
	 * @param id : the id of the item
	 * @return a map that contains all the information
	 */
	protected Map<String,Object> prepareEstimatedLocation(Cell mlc, String id){

		double[] result = (mlc!=null?CellCoder.cellDecoding(mlc.getID()):null);

		Map<String,Object> estimatedLocation = new HashMap<>();
		estimatedLocation.put("itinno:item_id", id); // item ID

		String estimatedPoint = (result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A"); 

		estimatedLocation.put("location", estimatedPoint); // Estimated location
		estimatedLocation.put("confidence", result != null ? mlc.getConfidence() : null); // Confidence

		String locationCountryCity = (result != null ? super.rgeoService
				.getCityAndCountryByLatLon(result[0], result[1]) : "N/A");
		estimatedLocation.put("geonames:loc_id", !locationCountryCity.equals("N/A") 
				? locationCountryCity.split("_")[0] : "N/A"); // Geoname Location Id
		estimatedLocation.put("geonames:name", !locationCountryCity.equals("N/A") 
				? locationCountryCity.split("_")[1] : "N/A"); // City, Country

		estimatedLocation.put("evidence", mlc!=null ? mlc.getEvidence() : "N/A"); // Evidence

		return estimatedLocation;
	}

	/**
	 * Function that store the location estimation of an emitted item
	 * @param dir : file directory
	 * @param fileName : file name
	 * @param text : item's text
	 * @param mlc : the most likely cell
	 */
	private void storeEstimation(String dir, String fileName, String text, Cell mlc){
		
		new File(dir).mkdir();
		EasyBufferedWriter writer = new EasyBufferedWriter(dir + fileName);
		
		writer.write("Item clean text: " + TextUtil.parseTweet(text, new HashSet<String>()).toString());
		writer.newLine();

		double[] result = (mlc!=null?CellCoder.cellDecoding(mlc.getID()):null);
		writer.write("Estimated Location: " + 
				(result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A")); // Estimated location
		writer.newLine();

		writer.write("Confidence: " + (result != null ? mlc.getConfidence() : null)); // Confidence
		writer.newLine();
		
		String locationCountryCity = (result != null ? super.rgeoService
				.getCityAndCountryByLatLon(result[0], result[1]) : "N/A");
		
		writer.write("City name: " + (!locationCountryCity.equals("N/A") 
				? locationCountryCity.split("_")[1] : "N/A")); // City, Country
		writer.newLine();
		
		writer.write("Evidence: " + (result != null ? mlc.getEvidence().toString() : "N/A"));

		writer.close();
	}
	
	/**
	 * Function that store the emitted item
	 * @param dir : file directory
	 * @param fileName : file name
	 * @param message : the emitted item
	 */
	private void storeTweet(String dir, String fileName, Map<Object, Object> message){
		
		new File(dir).mkdir();
		EasyBufferedWriter writer = new EasyBufferedWriter(dir + fileName);
		
		writer.write(message.toString().replaceAll("\n", "\\\\n"));
		
		writer.close();
	}
	
	@Override
	public void execute(Tuple tuple) {

		// Retrieve hash map tuple object from Tuple input at index 0, index 1 will be message delivery tag (not used here)
		Map<Object, Object> inputMap = (HashMap<Object, Object>) tuple.getValue(0);
		// Get JSON object from the HashMap from the Collections.singletonList
		Map<Object, Object> message = (Map<Object, Object>) inputMap.get("message");

		// extract tweet text
		String text = (String) message.get("text");
		
		super.collector.ack(tuple);
		//logger.info(text);
		
		Cell mlc = null;
		if (text!=null && !text.isEmpty()) {
			
			// calculate the Most Likely Cell
			// tokenize and pre-process the words contained in the tweet text
			Set<String> words = new HashSet<String>();
			
			mlc = super.languageModel.calculateLanguageModel(TextUtil.parseTweet(text,words));
		}
		
		// store estimation
		storeEstimation("/home/georgekordopatis/storm_test_logs/estimations/",
					(String)message.get("id_str") + ".est", text, mlc);
		
		// update tweet message
		message.put("certh:loc_set", prepareEstimatedLocation(mlc, (String) message.get("id_str")));

		// store tweet
		storeTweet("/home/georgekordopatis/storm_test_logs/tweets/",
				(String)message.get("id_str") + ".tweet", message);
		
		// emit updated tweet
		super.collector.emit(tuple, new Values(message));
	}

	/**
	 * Declare output field name (in this case simple a string value that is
	 * defined in the constructor call)
	 *
	 * @param declarer standard Storm output fields declarer
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(strExampleEmitFieldsId));
	}
}
