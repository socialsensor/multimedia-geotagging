package gr.iti.mklab.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.geonames.GeoObject;
import gr.iti.mklab.util.EasyBufferedReader;


/**
 * Given a city/area name it resolves the country where it belongs. 
 * @author papadop
 *
 */
public class Countrycoder extends AbstractGeoService {

	
	//private final Map<String, String> countryVocabulary;
	
	// this map is initialized with a capacity of 190,000 given the default load factor of 0.75 and
	// the number of entries in the cities1000.txt
	private final Map<Integer, GeoObject> gnObjectMap = new HashMap<Integer, GeoObject>(190000);
	
	// this map is initialized with a capacity of 635,100 given the default load factor of 0.75 and 
	// the fact that the number of unique tokens/names in the cities1000.txt are 476,285
	private final Map<String, Set<Integer>> gnObjectMapNameLookup = new HashMap<String, Set<Integer>>(635100);
	
	
	private final Map<String, String> gnAdminNamesMap = new HashMap<String, String>(5195);
	
	public Countrycoder(String gnObjectsFile, String gnCountryInfoFile, String gnAdminNames){
		super(gnCountryInfoFile, Logger.getLogger("eu.socialsensor.geo.Countrycoder"));
		//countryVocabulary = readCountryVocabulary(gnObjectsFile);
		initializeGeoNamesStructures(gnObjectsFile, gnAdminNames);
	}
	
	/**
	 * Given a location name (e.g. city, area), it returns the country to which it belongs
	 * @param locationName 
	 * @return Country where the locationName belongs
	 */
	public String getCountryByLocationName(String locationName){
		//String country = countryVocabulary.get(locationName.toLowerCase());
		//return getCountryByCountryCode(country);
		
		Set<Integer> ids = gnObjectMapNameLookup.get(locationName.toLowerCase());
		if (ids == null){
			return "unknown";
		}
		long maxPopulation = 0;
		GeoObject maxCity = null;
		for (int id : ids) {
			GeoObject city = gnObjectMap.get(id);
			if (city.getPopulation() > maxPopulation) {
				maxPopulation = city.getPopulation();
				maxCity = city;
			}
		}
		return getCountryByCountryCode(maxCity.getCountryCode());
	}
	
	/**
	 * Given a two-char country code, it returns the country name
	 * @param countryCode 2-character ISO country code, e.g. US
	 * @return
	 */
	public String getCountryByCountryCode(String countryCode){
		if (countryCode == null){
			return "unknown";
		} else {
			return countryCodes.get(countryCode);
		}
	}
	
	/**
	 * Get an estimate of the referred location in a piece of text
	 * @param text Arbitrary text string, e.g. the text of a tweet
	 * @return Estimated location in the form of a map with keys 
	 * "country", "city", "area", or empty map if no location is detected
	 */
	public Map<String,String> getLocation(String text){
		
		Map<String, String> locationMap = new HashMap<String, String>();
		
		List<String> tokenList = getTokens(text);
		
		String detectedCountryCode = null;
		// check mentioned countries
		for (int i = 0; i < tokenList.size(); i++) {
			String countryCode = countryNames.get(tokenList.get(i));
			if (countryCode != null){
				detectedCountryCode = countryCode;
				break;
			}
		}
		if (tokenList.size() > 1) {
			for (int i = 0; i < tokenList.size()-1; i++) {
				String countryCode = countryNames.get(tokenList.get(i) + " " + tokenList.get(i+1));
				if (countryCode != null){
					detectedCountryCode = countryCode;
					break;
				}
			}
		}
		
		String detectedCountryCodeByAdminName = null;
		String detectedAdminName = null;
		// check mentioned administrative regions
		for (int i = 0; i < tokenList.size(); i++) {
			String countryCode = gnAdminNamesMap.get(tokenList.get(i));
			if (countryCode != null){
				detectedAdminName = tokenList.get(i);
				detectedAdminName = detectedAdminName.substring(0, 1).toUpperCase() + detectedAdminName.substring(1);
				detectedCountryCodeByAdminName = countryCode;
				break;
			}
		}
		if (tokenList.size() > 1) {
			for (int i = 0; i < tokenList.size()-1; i++) {
				String countryCode = gnAdminNamesMap.get(tokenList.get(i) + " " + tokenList.get(i+1));
				if (countryCode != null){
					detectedAdminName = tokenList.get(i) + " " + tokenList.get(i+1);
					detectedAdminName = detectedAdminName.substring(0, 1).toUpperCase() + detectedAdminName.substring(1);
					detectedCountryCodeByAdminName = countryCode;
					break;
				}
			}
		}
		
		// check mentioned cities
		List<GeoObject> gnObjects = getMentionedGnObjects(tokenList);
		long maxPopulation = 0;
		GeoObject maxGnObject = null;
		for (GeoObject gnObject : gnObjects){
			if (gnObject.getPopulation() > maxPopulation) {
				maxPopulation = gnObject.getPopulation();
				maxGnObject = gnObject;
			}
		}
		
		// if nothing was found, return unknown
		if (maxGnObject == null && detectedCountryCode == null && detectedCountryCodeByAdminName == null) {
			return locationMap;
		}
		
		// if only country detected, return country
		if (maxGnObject == null && detectedCountryCode != null && detectedCountryCodeByAdminName == null) {
			locationMap.put(COUNTRY, getCountryByCountryCode(detectedCountryCode));
			return locationMap;
		}
		
		// if administrative name detected, return administrative name + country 
		if (maxGnObject == null && detectedCountryCodeByAdminName != null) {
			locationMap.put(AREA, detectedAdminName);
			locationMap.put(COUNTRY, getCountryByCountryCode(detectedCountryCodeByAdminName));
			return locationMap;
		}
		
		// if both administrative name detected and city detected, check for consistency
		if (maxGnObject != null && detectedCountryCodeByAdminName != null && detectedCountryCode == null) {
			if (maxGnObject.getCountryCode().equals(detectedCountryCodeByAdminName)){
				locationMap.put(CITY, maxGnObject.getName());
				locationMap.put(COUNTRY, getCountryByCountryCode(maxGnObject.getCountryCode()));
				return locationMap;
			} else {
				//logger.info("Inconsistency found: While country was found to be " + detectedCountryCodeByAdminName + ", city was found to be " + maxGnObject.getName() + ", " + maxGnObject.getCountryCode());
				locationMap.put(AREA, detectedAdminName);
				locationMap.put(COUNTRY, getCountryByCountryCode(detectedCountryCodeByAdminName));
				return locationMap;
			}
		}
		
		// if a city is detected, check for consistency with the detected country (if any) and
		// return city only if it agrees with country, otherwise send only country
		if (maxGnObject != null && detectedCountryCode == null) {
			locationMap.put(CITY, maxGnObject.getName());
			locationMap.put(COUNTRY, getCountryByCountryCode(maxGnObject.getCountryCode()));
			return  locationMap;
		}
		if (maxGnObject != null && detectedCountryCode != null) {
			if (maxGnObject.getCountryCode().equals(detectedCountryCode)){
				locationMap.put(CITY, maxGnObject.getName());
				locationMap.put(COUNTRY, getCountryByCountryCode(maxGnObject.getCountryCode()));
				return  locationMap;
			} else {
				//logger.info("Inconsistency found: While country was found to be " + detectedCountryCode + ", city was found to be " + maxGnObject.getName() + ", " + maxGnObject.getCountryCode());
				locationMap.put(COUNTRY, getCountryByCountryCode(detectedCountryCode));
				return locationMap;
			}
		}
		return locationMap;
	}
	
	public static final String COUNTRY = "country";
	public static final String CITY = "city";
	public static final String AREA = "area";
	
	public static String printLocationMap(Map<String, String> locMap){
		if (locMap.isEmpty()) return "unknown";
		String city = locMap.get(CITY);
		String area = locMap.get(AREA);
		String country = locMap.get(COUNTRY);
		if (city != null) return city + ", " + country;
		if (area != null) return area + ", " + country;
		return country;
	}
	
	
	/**
	 * Given an arbitrary text string, extract the referred names of locations 
	 * @param tokenList List of tokens.
	 * @return List of mentioned LightweightGeoObjects in the list of tokens.
	 */
	public List<GeoObject> getMentionedGnObjects(List<String> tokenList){

		//logger.info("tokenList: " + tokenList);
		Set<Integer> gnIds = new HashSet<Integer>();
		
		for (int i = 0; i < tokenList.size(); i++){
			Set<Integer> ids = gnObjectMapNameLookup.get(tokenList.get(i));
			if (ids != null){
				gnIds.addAll(ids);
			}
			//logger.info("t: " + tokenList.get(i) + " -> " + ids);
		}
		// use also pairs of consecutive tokens
		if (tokenList.size() > 1) {
			for (int i = 0; i < tokenList.size() - 1; i++){
				Set<Integer> ids = gnObjectMapNameLookup.get(tokenList.get(i) + " " + tokenList.get(i+1));
				if (ids != null){
					gnIds.addAll(ids);
				}
				//logger.info("t: " + tokenList.get(i) + " " + tokenList.get(i+1) + " -> " + ids);
			}
		}
		
		List<GeoObject> locations = new ArrayList<GeoObject>(gnIds.size());
		for (Integer id : gnIds) {
			locations.add(gnObjectMap.get(id));
		}
		return locations;
	}
	
	/**
	 * Utility method for splitting a piece of text to tokens
	 * @param text
	 * @return List of tokens
	 */
	protected List<String> getTokens(String text){
		String[] tokens = text.toLowerCase().split("[\\s\\p{Punct}]");
		List<String> tokenList = new ArrayList<String>(tokens.length);
		for (int i = 0; i < tokens.length; i++){
			if (tokens[i].trim().length() < 1){
				continue;
			}
			tokenList.add(tokens[i].trim());
		}
		return tokenList;
	}
	
	/**
	 * 
	 * @param gnObjectsFilename
	 * @return
	 */
	protected Map<String, String> readCountryVocabulary(String gnObjectsFilename) {
		Map<String, String> countryVoc = new HashMap<String, String>();
		
		EasyBufferedReader reader = new EasyBufferedReader(gnObjectsFilename);
		String line = null;
		int count = 0;
		long t0 = System.currentTimeMillis();
		logger.info("loading of objects started");
		while ((line = reader.readLine()) != null){
			if (line.trim().length() < 1) continue;
			count++;
			GeoObject city = new GeoObject(line);
			countryVoc.put(city.getName().toLowerCase(), city.getCountryCode());
			countryVoc.put(city.getAsciiName().toLowerCase(), city.getCountryCode());
			for (String alternateName : city.getAlternateNames()) {
				String existingName = countryVoc.get(alternateName.toLowerCase());
				if (existingName == null) {
					countryVoc.put(alternateName.toLowerCase(), city.getCountryCode());
				}
			}
			
		}
		logger.info(count + " objects loaded in " + (System.currentTimeMillis()-t0)/1000.0 + "secs");
		
		reader.close();
		logger.info("file " + gnObjectsFilename + "closed ");
		return countryVoc;
	}
	
	
	protected final void initializeGeoNamesStructures(String gnObjectsFilename, String gnAdminNames){
		
		EasyBufferedReader reader = new EasyBufferedReader(gnObjectsFilename);
		String line = null;
		int count = 0;
		long t0 = System.currentTimeMillis();
		logger.info("loading of objects started");
		while ((line = reader.readLine()) != null){
			if (line.trim().length() < 1) continue;
			count++;
			GeoObject city = new GeoObject(line);
			gnObjectMap.put(city.getId(), city);
			addGeonameObjectToNameLookupMap(city, city.getName().toLowerCase());
			addGeonameObjectToNameLookupMap(city, city.getAsciiName().toLowerCase());
			for (String alternateName : city.getAlternateNames()) {
				// if alternate name is below two characters don't add it
				if (alternateName.length() > 1) {
					addGeonameObjectToNameLookupMap(city, alternateName.toLowerCase());
				}
			}
		}
		
		logger.info(count + " objects loaded in " + (System.currentTimeMillis()-t0)/1000.0 + "secs");
		logger.info(gnObjectMapNameLookup.size() + " tokens loaded to the Geonames Name Lookup map.");
		reader.close();
		logger.info("file " + gnObjectsFilename + " closed");
		
		reader = new EasyBufferedReader(gnAdminNames);
		line = null;
		count = 0;
		logger.info("loading admin names started");
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() < 1) continue;
			count++;
			String[] parts = line.split("\t");
			String[] subparts = parts[0].split("\\.");
			gnAdminNamesMap.put(parts[2].toLowerCase(), subparts[0]);
			if (!parts[1].equals(parts[2])){
				gnAdminNamesMap.put(parts[1].toLowerCase(), subparts[0]);
			}
		}
		logger.info(count + " admin names loaded");
		reader.close();
		logger.info("file " + gnAdminNames + " closed");
	}
	
	protected void addGeonameObjectToNameLookupMap(GeoObject gnObject, String nameToAdd){
		Set<Integer> sameNameObjects = gnObjectMapNameLookup.get(nameToAdd);
		if (sameNameObjects == null){
			sameNameObjects = new HashSet<Integer>();
			sameNameObjects.add(gnObject.getId());
			gnObjectMapNameLookup.put(nameToAdd, sameNameObjects);
		} else {
			sameNameObjects.add(gnObject.getId());
		}
	}
}
