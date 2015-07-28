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
import gr.iti.mklab.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		String locationCountryCity = super.rgeoService.getCityAndCountryByLatLon(result[0], result[1]);
		estimatedLocation.put("geonames:loc_id", result != null ? locationCountryCity
				.split("_")[0] : "N/A"); // Geoname Location Id
		estimatedLocation.put("geonames:name", result != null ? locationCountryCity
				.split("_")[1] : "N/A"); // City, Country

		estimatedLocation.put("evidence", mlc.getEvidence()); // Evidence

		return estimatedLocation;
	}


	@Override
	public void execute(Tuple tuple) {

		// Retrieve hash map tuple object from Tuple input at index 0, index 1 will be message delivery tag (not used here)
		Map<Object, Object> inputMap = (HashMap<Object, Object>) tuple.getValue(0);
		// Get JSON object from the HashMap from the Collections.singletonList
		Map<Object, Object> message = (Map<Object, Object>) inputMap.get("message");

		// extract tweet text
		String text = (String) message.get("text");
		collector.ack(tuple);

		Cell mlc = null;
		if (text!=null && text.isEmpty()) {

			// tokenize and pre-process the words contained in the tweet text
			List<String> words = new ArrayList<String>();
			Collections.addAll(words, TextUtil.cleanText(text).split("\\s"));

			// calculate the Most Likely Cell
			mlc = super.languageModel.calculateLanguageModel(words);
		}

		// update tweet message
		message.put("certh:loc_set", prepareEstimatedLocation(mlc, (String) message.get("itinno:item_id")));

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
