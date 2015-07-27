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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	protected JsonElement prepareEstimatedLocation(Cell mlc, String id){

		double[] result = (mlc!=null?CellCoder.cellDecoding(mlc.getID()):null);

		JsonElement estimatedLocation = new JsonObject();
		estimatedLocation.getAsJsonObject().addProperty("itinno:item_id",id); // item ID

		String locationCountryCity = super.rgeoService.getCityAndCountryByLatLon(result[0], result[1]);
		estimatedLocation.getAsJsonObject().addProperty("geonames:loc_id", 
				result != null ? locationCountryCity.split("_")[0] : "N/A"); // Id
		estimatedLocation.getAsJsonObject().addProperty("geonames:name", 
				result != null ? locationCountryCity.split("_")[1] : "N/A"); // City, Country

		String estimatedPoint = (result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A"); 
		estimatedLocation.getAsJsonObject().addProperty("location", estimatedPoint); // Estimated location
		estimatedLocation.getAsJsonObject().addProperty("confidence", 
				result != null ? mlc.getConfidence() : null); // Confidence
		estimatedLocation.getAsJsonObject().addProperty("evidence", 
				result != null ? mlc.getEvidence().toString() : "N/A"); // Evidence

		return estimatedLocation;
	}

	/**
	 * Write results in file
	 * @param text : item's text
	 * @param mlc : most likely cell
	 */
	private void writeInFile(String text, Cell mlc){
		EasyBufferedWriter writer = new EasyBufferedWriter(resPath +
				"/" + System.currentTimeMillis());
		writer.write("Item clean text: " + TextUtil.cleanText(text));
		writer.newLine();

		double[] result = (mlc!=null?CellCoder.cellDecoding(mlc.getID()):null);
		writer.write("Estimated Location: " + 
				(result != null ? "POINT(" + result[0] + " " + result[1] + ")" : "N/A")); // Estimated location
		writer.newLine();

		writer.write("Confidence: " + (result != null ? mlc.getConfidence() : null)); // Confidence
		writer.newLine();
		
		String locationCountryCity = super.rgeoService.getCityAndCountryByLatLon(result[0], result[1]);
		writer.write("City name: " + (result != null ? locationCountryCity.split("_")[1] : "N/A")); // City, Country
		writer.newLine();
		
		writer.write("Evidence: " + (result != null ? mlc.getEvidence().toString() : "N/A"));

		writer.close();
	}

	@Override
	public void execute(Tuple tuple) {

		// Get JSON Element from the emitted tuple
		JsonElement message = new JsonParser().parse((String) tuple.getValue(0)).getAsJsonObject();

		// extract tweet text
		String text = message.getAsJsonObject().get("text").toString();
		collector.ack(tuple);

		Cell mlc = null;
		if (!text.isEmpty()) {

			// tokenize the words contained in the tweet text
			List<String> tagsList = new ArrayList<String>();
			Collections.addAll(tagsList, TextUtil.cleanText(text).split("\\s"));

			// calculate the Most Likely Cell
			mlc = super.languageModel.calculateLanguageModel(tagsList);
		}

		// update JSON Element
		message.getAsJsonObject().add("certh:loc_set", 
				prepareEstimatedLocation(mlc, 
						(String) message.getAsJsonObject().get("itinno:item_id").getAsString()));

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
