package gr.iti.mklab.mmcomms16;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

@SuppressWarnings("unchecked")
public class GeographicallyFocusedSampling extends Sampling{

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.GeographicallyFocusedSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: Geographically Focused Strategy");
		
		GeographicallyFocusedSampling sampling = 
				new GeographicallyFocusedSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Map<String, Set<String>>> places =
				new HashMap<String, Map<String, Set<String>>>();

		places.put("continents", new HashMap<String, Set<String>>());
		places.put("countries", new HashMap<String, Set<String>>());

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){
			String imageID = line.split("\t")[0];
			for(String place:line .split("\t")[1].split(",")){
				if(place.split(":").length>2 && place.contains("Timezone")){
					String continent = place.split(":")[1].split("%")[0];

					switch(continent) {
					case "Pacific" :
						continent = "America";
						break;
					case "Atlantic" : 
						continent = "America";
						break;
					case "Indian" : 
						continent = "Asia"; 
						break;
					}
					if(places.get("continents").containsKey(continent)){
						places.get("continents").get(continent).add(imageID);
					}else{
						places.get("continents").put(continent, new HashSet<String>());
						places.get("continents").get(continent).add(imageID);
					}
				}

				if(place.split(":").length>2 && place.contains("Country")){
					String country = place.split(":")[1].split("%")[0];
					if(places.get("countries").containsKey(country)){
						places.get("countries").get(country).add(imageID);
					}else{
						places.get("countries").put(country, new HashSet<String>());
						places.get("countries").get(country).add(imageID);
					}
				}
			}
		}
		reader.close();
		logger.info(places.get("continents").size() + " Continents loaded");
		logger.info(places.get("countries").size() + " Countries loaded");

		return places;
	}

	protected Object writeInFile(Object data) {

		Map<String, Map<String, Set<String>>> places = 
				(Map<String, Map<String, Set<String>>>) data;

		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/geographically_focused_sampling_continents.txt");
		for(Entry<String, Set<String>> continent:places.get("continents").entrySet()){
			writer.write(continent.getKey() + "\t");
			for(String images:continent.getValue()){
				writer.write(images + " ");
			}
			writer.newLine();
		}
		writer.close();

		writer = new EasyBufferedWriter(
				"samples/geographically_focused_sampling_countries.txt");

		for(Entry<String, Set<String>> country:places.get("countries").entrySet()){
			writer.write(country.getKey() + "\t");
			for(String images:country.getValue()){
				writer.write(images + " ");
			}
			writer.newLine();
		}
		writer.close();

		return places;
	}
}
