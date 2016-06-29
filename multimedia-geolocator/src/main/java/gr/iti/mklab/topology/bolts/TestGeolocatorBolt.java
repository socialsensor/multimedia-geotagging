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
import gr.iti.mklab.geo.GeoCell;
import gr.iti.mklab.util.GeoCellCoder;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.TextUtil;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Test class of Multimedia-Geolocator Bolt
 * @author gkordo
 */
public class TestGeolocatorBolt extends AbstractGeolocatorBolt {

	private String resPath;
	protected static Logger logger = Logger.getLogger("gr.iti.mklab.topology.bolts.TestGeolocatorBolt");

	/**
	 * Class constructor
	 * @param strExampleEmitFieldsId : emitted fields name
	 * @param restletURL : restlet URL
	 */
	public TestGeolocatorBolt(String strExampleEmitFieldsId, String restletURL, String resPath) {
		super(strExampleEmitFieldsId, restletURL);

		this.resPath = resPath;
	}

	/**
	 * Function that prepare a JSON Element that contains the information that will be added to the main message.
	 * @param mlc : the most likely cell
	 * @param id : the id of the item
	 * @return a JSON Element that contains all the information
	 */
	protected JSONObject prepareEstimatedLocation(GeoCell mlc, String id, String text){

		double[] result = (mlc!=null?GeoCellCoder.cellDecoding(mlc.getID()):null);

		JSONObject estimatedLocation = new JSONObject();
		estimatedLocation.put("itinno:item_id",id); // item ID

		String estimatedPoint = (result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A"); 
		
		estimatedLocation.put("location", estimatedPoint); // Estimated location
		estimatedLocation.put("confidence", 
				result != null ? mlc.getConfidence() : null); // Confidence
		
		String locationCountryCity = (result != null ? super.rgeoService
				.getCityAndCountryByLatLon(result[0], result[1]) : "N/A");
		estimatedLocation.put("geonames:loc_id", 
				result != null ? locationCountryCity.split("_")[0] : "N/A"); // Id
		estimatedLocation.put("geonames:name", 
				result != null ? locationCountryCity.split("_")[1] : "N/A"); // City, Country
		
		estimatedLocation.put("evidence", 
				result != null ? mlc.getEvidence().toString() : "N/A"); // Evidence

		estimatedLocation.put("geo_labels", mlc!=null ? geoParser.
				extractGeoLabels(text, mlc.getClusters()) : "N/A"); // Geoparsed Labels
		
		return estimatedLocation;
	}

	/**
	 * Write results in file
	 * @param text : item's text
	 * @param mlc : most likely cell
	 */
	private void writeInFile(String text, GeoCell mlc){
		EasyBufferedWriter writer = new EasyBufferedWriter(resPath +
				"/" + System.currentTimeMillis());
		writer.write("Item clean text: " + TextUtil.parseTweet(text));
		writer.newLine();

		double[] result = (mlc!=null?GeoCellCoder.cellDecoding(mlc.getID()):null);
		writer.write("Estimated Location: " + 
				(result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A")); // Estimated location
		writer.newLine();

		writer.write("Confidence: " + (result != null ? mlc.getConfidence() : null)); // Confidence
		writer.newLine();

		String locationCountryCity = super.rgeoService.getCityAndCountryByLatLon(result[0], result[1]);
		writer.write("City name: " + (result != null ? locationCountryCity.split("_")[1] : "N/A")); // City, Country
		writer.newLine();

		writer.write("Evidence: " + (result != null ? mlc.getEvidence().toString() : "N/A")); // Evidences
		writer.newLine();
		
		writer.write("Geoparsed Labels: " + (result != null ? geoParser.
				extractGeoLabels(text, mlc.getClusters()) : "N/A")); // Geoparsed Labels
		
		writer.close();
	}

	@Override
	public void execute(Tuple tuple) {

		// Get JSON Element from the emitted tuple
		JSONObject message = new JSONObject((String) tuple.getValue(0));

		// extract tweet text
		String text = message.get("text").toString();
		collector.ack(tuple);

		GeoCell mlc = null;
		if (!text.isEmpty()) {

			// tokenize the words contained in the tweet text
			// calculate the Most Likely Cell
			Set<String> words = new HashSet<String>();
			mlc = super.languageModel.calculateLanguageModel(TextUtil.parseTweet(text));
		}

		// update JSON Element
		message.put("certh:loc_set", 
				prepareEstimatedLocation(mlc, 
						(String) message.get("itinno:item_id"), text));

		// write the emitted message in a topic file (the location must exist)
		writeInFile(text,mlc);

		// emit updated tweet
		super.collector.emit(new Values(message));
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
