package gr.iti.mklab.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.*;


@SuppressWarnings("rawtypes")
public class MyHashMap extends HashMap<Object, Object> {
	
	private static final long serialVersionUID = 8005524467946015401L;
	
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
      
        Collections.sort(entries, Collections.reverseOrder(new Comparator<Map.Entry<K,V>>() {

            @SuppressWarnings("unchecked")
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
        	@SuppressWarnings("unchecked")
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

}
