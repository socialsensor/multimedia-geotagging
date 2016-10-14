package gr.iti.mklab.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gr.iti.mklab.data.GeoCell;

import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Utils {

	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

		Collections.sort(entries, Collections.reverseOrder(new Comparator<Map.Entry<K,V>>() {

			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		}));
		//LinkedHashMap will keep the keys in the order they are inserted
		//which is currently sorted on natural ordering
		Map<K,V> sortedMap = new LinkedHashMap<K,V>();

		for(Map.Entry<K,V> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static <K extends Comparable,V extends Comparable> Map<K,V[]> sortByValuesTable(Map<K,V[]> map){
		List<Map.Entry<K,V[]>> entries = new LinkedList<Map.Entry<K,V[]>>(map.entrySet());

		Collections.sort(entries, Collections.reverseOrder(new Comparator<Map.Entry<K,V[]>>() {
			public int compare(Entry<K, V[]> o1, Entry<K, V[]> o2) {
				return o1.getValue()[0].compareTo(o2.getValue()[0]);
			}
		}));
		//LinkedHashMap will keep the keys in the order they are inserted
		//which is currently sorted on natural ordering
		Map<K,V[]> sortedMap = new LinkedHashMap<K,V[]>();

		for(Map.Entry<K,V[]> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static Map<String, GeoCell> sortByMLCValues(Map<String, GeoCell> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, GeoCell>> list = 
				new LinkedList<Map.Entry<String, GeoCell>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, GeoCell>>() {
			public int compare(Map.Entry<String, GeoCell> o1,
					Map.Entry<String, GeoCell> o2) {
				return -(o1.getValue()).getTotalProb().compareTo(o2.getValue().getTotalProb());
			}
		});

		// Convert sorted map back to a Map
		Map<String, GeoCell> sortedMap = new LinkedHashMap<String, GeoCell>();
		for (Iterator<Map.Entry<String, GeoCell>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, GeoCell> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}	

	public static <K, V> HashMap<K,V> getFirstEntryOfSortedMap(Map<K,V> map){
		HashMap <K,V> firstEntry = new HashMap<K,V>();

		for ( Entry<K, V> entry : map.entrySet()){
			firstEntry.put(entry.getKey(), entry.getValue());
			break;
		}		

		return firstEntry;
	}

	public static <K,V> HashMap<V,K> invertKeysValues(Map<K,V> map){

		HashMap<V,K> invertedHashMap = new HashMap<V,K>();

		for(Entry<K, V> entry : map.entrySet()){
			invertedHashMap.put(entry.getValue(), entry.getKey());
		}

		return invertedHashMap;

	}

	public static double median(List<Double> p) 
	{
		Double[] b = new Double[p.size()];
		int i=0;
		for (Double entry: p){
			b[i]=entry;
			i++;
		}
		Arrays.sort(b);
		if (p.size() % 2 == 0) 
		{
			return (b[(b.length / 2)-1] + b[b.length / 2]) / 2.0;
		} 
		else 
		{
			return b[b.length / 2];
		}
	}

	public static <K extends Comparable,V extends Comparable> 
	int medianSet(Map<K, Set<V>> map) 
	{
		int[] b = new int[map.size()];
		int i = 0;
		for (Entry<K, Set<V>> entry: map.entrySet()){
			b[i] = entry.getValue().size();
			i++;
		}
		Arrays.sort(b);
		if (b.length % 2 == 0) 
		{
			return (int) Math.floor((b[(b.length / 2)-1] 
					+ b[b.length / 2]) / 2.0);
		} 
		else
		{
			return b[b.length / 2];
		}
	}

	public static <K extends Comparable,V extends Comparable>
	int medianItem(Map<K, V> map) 
	{
		int[] b = new int[map.size()];

		int i = 0;
		for (Entry<K, V> entry: map.entrySet()){
			b[i] = (Integer) entry.getValue();
			i++;
		}
		Arrays.sort(b);
		if (b.length % 2 == 0) 
		{
			return (int) Math.floor((b[(b.length / 2)-1] 
					+ b[b.length / 2]) / 2.0);
		} 
		else
		{
			return b[b.length / 2];
		}
	}
}
