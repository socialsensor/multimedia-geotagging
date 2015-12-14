package gr.iti.mklab.topology.bolts;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;
import gr.iti.mklab.data.Cell;
import gr.iti.mklab.geo.ReverseGeocoder;
import gr.iti.mklab.methods.LanguageModel;
import uniko.west.util.FileLoader;

/**
 * Abstract class of Multimedia-Geolocator Bolt
 * @author gkordo
 */
public abstract class AbstractGeolocatorBolt extends BaseRichBolt {

	protected OutputCollector collector;
	protected boolean initialized;

	protected final String strExampleEmitFieldsId;
	protected final String restletURL;
	protected String localTopicModelDirectory;

	protected LanguageModel languageModel;
	protected ReverseGeocoder rgeoService;
	
	public AbstractGeolocatorBolt(String strExampleEmitFieldsId, String restletURL) {
		super();

		this.restletURL = restletURL;
		this.strExampleEmitFieldsId = strExampleEmitFieldsId;
	}
	
	
	@Override
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
		
		// copy necessary files into the class directory
		try {
			File topologyJarFile = new File(MultimediaGeolocatorBolt.class
					.getProtectionDomain().getCodeSource().getLocation()
					.toURI().getPath());
			localTopicModelDirectory = topologyJarFile.getParent()
					+ "/multi-geo-utils";
			new File(localTopicModelDirectory).mkdirs();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String[] topicModelFiles = { "wordcellprobs.txt", "cities1000.txt", "countryInfo.txt"};
		for (String topicModelFile : topicModelFiles) {
			FileLoader.getFile(restletURL + "/static/multi-geo-utils/" + topicModelFile,
					localTopicModelDirectory + "/" + topicModelFile);
		}
		
		// initialize Language Model
		this.languageModel = new LanguageModel(localTopicModelDirectory + "/wordcellprobs.txt");
		
		// initialize Geoname Services
		this.rgeoService = new ReverseGeocoder(localTopicModelDirectory +"/cities1000.txt", 
				localTopicModelDirectory +"/countryInfo.txt");
	}
	
	protected abstract Object prepareEstimatedLocation(Cell mlc, String id);
}
