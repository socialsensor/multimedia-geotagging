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

	protected final String storeDirectory;
	protected final String rmqExchange;
	protected final boolean storeBoolean;

	protected static Logger logger = Logger.getLogger("gr.iti.mklab.topology.bolts.MultimediaGeolocatorBolt");

	/**
	 * Class constructor
	 * @param strExampleEmitFieldsId : emitted fields name
	 * @param restletURL : restlet URL
	 */
	public MultimediaGeolocatorBolt(String strExampleEmitFieldsId, String restletURL, 
			String storeDirectory, String rmqExchange, boolean storeBoolean) {
		super(strExampleEmitFieldsId, restletURL);
		this.storeDirectory = storeDirectory;
		new File(storeDirectory).mkdir();
		this.rmqExchange = rmqExchange;
		this.storeBoolean = storeBoolean;
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
		estimatedLocation.put("itinno:item_id", id != null ? id : "N/A"); // item ID

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
	private void storeEstimation(String text, Cell mlc, String id){

		EasyBufferedWriter writer = new EasyBufferedWriter(storeDirectory + rmqExchange + ".logs", true);

		writer.write(id != null ? id : "N/A"); // Object Id
		writer.write("\t");
		
		writer.write(text.replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t"));
		writer.write("\t");

		double[] result = (mlc!=null?CellCoder.cellDecoding(mlc.getID()):null);
		writer.write((result != null ? result[0] + "_" + result[1] : "N/A")); // Estimated location
		writer.write("\t");

		writer.write((result != null ? String.valueOf(mlc.getConfidence()) : "N/A")); // Confidence
		writer.write("\t");

		String locationCountryCity = (result != null ? super.rgeoService
				.getCityAndCountryByLatLon(result[0], result[1]) : "N/A");

		writer.write((!locationCountryCity.equals("N/A") 
				? locationCountryCity.split("_")[1] : "N/A")); // City, Country
		writer.write("\t");

		writer.write((result != null ? mlc.getEvidence().toString() : "N/A"));
		writer.newLine();
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
			mlc = super.languageModel.calculateLanguageModel(TextUtil.parseTweet(text));
		}

		// store estimation
		if(storeBoolean)
			storeEstimation(text, mlc, (String)message.get("id_str"));

		// update tweet message
		message.put("certh:loc_set", prepareEstimatedLocation(mlc, (String)message.get("id_str")));

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
