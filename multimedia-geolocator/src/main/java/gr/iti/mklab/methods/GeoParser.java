package gr.iti.mklab.methods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.GeoCellCoder;
import gr.iti.mklab.util.MyHashMap;
import gr.iti.mklab.util.TextUtil;

import org.apache.log4j.Logger;

/**
 * Class that extracts the geographical labels from plain text.
 * @author gkordo
 *
 */
public class GeoParser {

	protected Map<Long,Set<String>> labels = new HashMap<Long,Set<String>>();
	protected static Logger logger = Logger.getLogger("gr.iti.mklab.methods.GeoParser");

	/**
	 * Function that extracts the geographical labels from plain text based on the
	 * representative cells calculated from the geotagging approach.
	 * @param text : plain text
	 * @param cells : representative cells
	 * @return
	 */
	public Set<String> extractGeoLabels(String text, Set<Long> cells){

		text = TextUtil.parseTweetLite(text);

		Map<String, Integer> helpMap = new HashMap<String, Integer>();

		for(Long cell:cells){
			if(labels.containsKey(cell)){
				for(String label:labels.get(cell)){
					label = TextUtil.parseTweetLite(label);
					if(text.contains(label) && label.length() > 4){
						helpMap.put(label, label.length());
					}else if(label.split(" ").length > 1){
						for(String term:label.split(" ")){
							if(text.contains(term) && term.length() > 4){
								helpMap.put(label, label.length());
							}
						}
					}
				}
			}
		}

		helpMap = MyHashMap.sortByValues(helpMap);

		return !helpMap.isEmpty()?compareLabelsWithText(text, helpMap)
				:new HashSet<String>();
	}

	/**
	 * Function that extracts the geographical labels from plain text from a
	 * map of candidate labels..
	 * @param text : plain text
	 * @param helpMap : candidate labels
	 * @return
	 */
	private Set<String> compareLabelsWithText(String text, Map<String, Integer> helpMap) {

		Set<String> export = new HashSet<String>();

		// longest phrases extracted first
		for(Entry<String, Integer> label:helpMap.entrySet()){
			if(text.contains(label.getKey())){
				export.add(label.getKey());
				text = text.replace(label.getKey(), " ");
			}
		}

		// extract based on individual word similarity
		for(Entry<String, Integer> label:helpMap.entrySet()){

			double similarity = 0.0;

			for(String term:label.getKey().split(" ")){
				if(text.contains(term) 
						&& term.length() > 4){
					similarity += 1.0/label.getKey().split(" ").length;
				}
			}

			if(similarity > 0.6){
				export.add(label.getKey());
			}
		}
		return export;
	}

	/**
	 * Class constructor.
	 * @param osmDataFile
	 */
	public GeoParser(String osmDataFile){
		loadOSFData(osmDataFile);
	}

	/**
	 * Function that loads OSM data.
	 * @param file
	 */
	private void loadOSFData(String file){

		EasyBufferedReader reader = new EasyBufferedReader(file);

		logger.info("opening file" + file);
		logger.info("loading OSM data");

		String line;
		while ((line = reader.readLine())!=null){

			Set<String> tmp = new HashSet<String>();
			long cellCode = GeoCellCoder.cellEncoding(line.split("\t")[0]);

			for(String category:line.split("\t")){
				if(category.split("=").length > 1){
					for(String entry:category.split("=")[1].split(",")){
						if(!entry.isEmpty()
								&& !entry.matches("[0-9]+")){
							tmp.add(entry); // load OSM labels
						}
					}
				}
			}

			labels.put(cellCode, tmp);
		}
		reader.close();
		logger.info("closing file" + file);
	}
}
