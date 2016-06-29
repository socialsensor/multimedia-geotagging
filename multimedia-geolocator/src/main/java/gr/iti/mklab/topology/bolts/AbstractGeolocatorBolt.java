package gr.iti.mklab.topology.bolts;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;
import gr.iti.mklab.geo.GeoCell;
import gr.iti.mklab.methods.GeoParser;
import gr.iti.mklab.methods.LanguageModel;
import gr.iti.mklab.methods.ReverseGeocoder;
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
	protected String localDirectory;

	protected LanguageModel languageModel;
	protected ReverseGeocoder rgeoService;
	protected GeoParser geoParser;
	
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
			localDirectory = topologyJarFile.getParent()
					+ "/multi-geo-utils";
			new File(localDirectory).mkdirs();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String[] files = { "wordcellprobs.txt", "cities1000.txt",
				"countryInfo.txt", "osmCellLabels.txt"};
		for (String file : files) {
			FileLoader.getFile(restletURL + "/static/multi-geo-utils/" + file,
					localDirectory + "/" + file);
		}
		
		// initialize Language Model
		this.languageModel = new LanguageModel(localDirectory + "/wordcellprobs.txt");
		
		// initialize Geoname Services
		this.rgeoService = new ReverseGeocoder(localDirectory +"/cities1000.txt", 
				localDirectory +"/countryInfo.txt");
		
		this.geoParser = new GeoParser(localDirectory + "/osmCellLabels.txt");
	}
	
	protected abstract Object prepareEstimatedLocation(GeoCell mlc, String id, String text);
}
