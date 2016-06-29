package gr.iti.mklab.topology.bolts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import gr.iti.mklab.util.EasyBufferedWriter;

public class JsonStrorageBolt extends BaseRichBolt{

	private OutputCollector collector;
	protected boolean initialized;

	private String rmqExchange;
	private String storeDirectory;
	private boolean storeBoolean;
	private String strExampleEmitFieldsId;

	/**
	 * Class constructor
	 * @param strExampleEmitFieldsId : emitted fields name
	 * @param restletURL : restlet URL
	 */
	public JsonStrorageBolt(String strExampleEmitFieldsId, String storeDirectory, String rmqExchange, boolean storeBoolean) {
		super();

		this.strExampleEmitFieldsId = strExampleEmitFieldsId;
		this.storeDirectory = storeDirectory;
		this.rmqExchange = rmqExchange;
		this.storeBoolean = storeBoolean;
	}

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;

		if (!initialized) {
			try {
				init();
			} catch (IOException e) {
				e.printStackTrace();
			}
			initialized = true;
		}
	}

	/**
	 * Initialize function of the core objects of the module.
	 * @throws IOException
	 */
	private void init() throws IOException {
		if(storeBoolean){
			new File(storeDirectory).mkdir();
			new File(storeDirectory + "/jsons/").mkdir();
			new File(storeDirectory + "/jsons/" + rmqExchange).mkdir();
		}
	}

	/**
	 * Function that store the location estimation of an emitted item
	 * @param dir : file directory
	 * @param fileName : file name
	 * @param text : item's text
	 * @param mlc : the most likely cell
	 */
	private void storeEstimation(Map<Object, Object> message){

		EasyBufferedWriter writer = new EasyBufferedWriter(storeDirectory + rmqExchange + ".logs", true);

		String id = (String) message.get("id_str");
		String text = (String) message.get("text");

		Map<Object, Object> location = (Map<Object, Object>) message.get("certh:loc_set");
		String point = (String) location.get("location");
		String confidence = (String) location.get("confidence");
		String geoname = (String) location.get("geonames:name");
		String evidence = (String) location.get("evidence").toString();
		String geolabels = (String) location.get("geo_labels").toString();

		writer.write(id != null ? id : "N/A"); // Object Id
		writer.write("\t");

		writer.write(text.replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t"));
		writer.write("\t");

		writer.write(point); // Estimated location
		writer.write("\t");

		writer.write(confidence); // Confidence
		writer.write("\t");

		writer.write(geoname); // City, Country
		writer.write("\t");

		writer.write(evidence); // Evidence
		writer.write("\t");
		
		writer.write(geolabels); // Geoparsed Labels
		writer.newLine();
		writer.close();
	}



	@Override
	public void execute(Tuple tuple) {

		// Retrieve hash map tuple object from Tuple input at index 0, index 1 will be message delivery tag (not used here)
		Map<Object, Object> inputMap = (HashMap<Object, Object>) tuple.getValue(0);
		// Get JSON object from the HashMap from the Collections.singletonList
		Map<Object, Object> message = (Map<Object, Object>) inputMap.get("message");
	
		// JSONObject jsonMessage = new JSONObject((HashMap<String, Object>) tuple.getValue(0));
		
		if(storeBoolean){

			try (PrintStream testOut = new PrintStream(new File(
					storeDirectory + "/jsons/" + rmqExchange + "/"
					+ tuple.hashCode() + ".json"), "UTF8")) {
				testOut.println(tuple.toString().replaceAll("\n", "\\\\n"));
				// testOut.println(jsonMessage.toString().getBytes());
			} catch (FileNotFoundException ex) {
				Logger.getLogger(MultimediaGeolocatorBolt.class.getName()).log(
						Level.SEVERE, null, ex);
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(MultimediaGeolocatorBolt.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			
			storeEstimation(message);
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(this.strExampleEmitFieldsId));
	}

}
