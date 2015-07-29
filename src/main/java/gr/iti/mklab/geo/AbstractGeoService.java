package gr.iti.mklab.geo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;

/**
 * Abstract GeoService class that includes common methods to be inherited by different implementations.
 * @author papadop
 *
 */
public abstract class AbstractGeoService {

	// initial capacity set to 405, since the entries in the country list of 
	// Geonames are 303 and the default load factor of 0.75 is used 
	protected final Map<String, String> countryCodes = new HashMap<String, String>(405);
	protected final Map<String, String> countryNames = new HashMap<String, String>(405);
	protected Logger logger;
	
	public AbstractGeoService(String gnCountryInfoFile, Logger logger){
		this.logger = logger;
		logger.debug("constructor");
		readCountryCodeMap(gnCountryInfoFile);
	}
	
	
	protected void readCountryCodeMap(String countryInfoFile){

		logger.info("opening file: " + countryInfoFile);
		EasyBufferedReader reader = new EasyBufferedReader(countryInfoFile);
		String line = null;
		while ((line = reader.readLine())!= null){
			if (line.startsWith("#")) continue;
			String[] parts = line.split("\t");
			countryCodes.put(parts[0], parts[4]);
			
			// we want to look country names irrespective of casing
			countryNames.put(parts[4].toLowerCase(), parts[0]);
		}
		reader.close();
		logger.info("read: " + countryCodes.size() + " country codes");
		logger.info("closing file: " + countryInfoFile);
		
	}
	
}
