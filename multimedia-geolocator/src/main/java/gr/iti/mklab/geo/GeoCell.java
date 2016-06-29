package gr.iti.mklab.geo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gr.iti.mklab.geo.Cluster;
import gr.iti.mklab.util.MyHashMap;

/**
 * Class that implements the earth cells.
 * @author gkordo
 *
 */
public class GeoCell {

	private Double totalProb;
	private Long id;
	private Float confidence;
	private Map<String, Float> evidence;
	private Map<Integer, Cluster> clusters;
	
	/**
	 * Constructor of the class where the id is specified and the
	 * evidence and the summation of the probabilities are initialized.
	 * @param id : cell ID
	 */
	public GeoCell(Long id){
		this.id = id;
		this.evidence = new HashMap<String, Float>();
		this.totalProb = 0.0;
		this.clusters = new HashMap<Integer, Cluster>();
	}

	/**
	 * 
	 * @return the cell ID
	 */
	public Long getID(){
		return id;
	}

	/**
	 * Set the value of the confidence of choosing that cell.
	 * @param confidence : value of confidence
	 */
	public void setConfidence(Float confidence){
		this.confidence = confidence;
	}

	/**
	 * 
	 * @return the confidence of the cell
	 */
	public Float getConfidence(){
		return confidence;
	}

	/**
	 * 
	 * @return the summation of all probabilities
	 */
	public Double getTotalProb() {
		return totalProb;
	}

	/**
	 * Add the given probability to the summation and store the word.
	 * @param prob : probability of the word
	 * @param word : actual word
	 */
	public void addProb(double prob, String word) {
		totalProb += prob;
		this.evidence.put(word, (float) prob);
	}
	
	/**
	 * 
	 * @return the sorted map of the word and their probabilities
	 */
	public Map<String, Float> getEvidence(){
		Map<String, Float> unsortMap = new HashMap<String, Float>();
		for(Entry<String, Float> word:evidence.entrySet()){
			if(word.getValue()/totalProb>0.0001){
				unsortMap.put(word.getKey(), (float) (word.getValue()/totalProb));
			}
		}
		return MyHashMap.sortByValues(unsortMap);
	}
	
	/**
	 * Clustering function that clusters a cell distribution based on their distance
	 * @param cellMap : cell distribution
	 * @param margin : margin that indicated the neighbor cells
	 */
	public void clustering(Map<Long, GeoCell> cellMap, double margin){
		
		int countID = 0;

		GeoCell tmpCell = cellMap.get(
				Long.parseLong(cellMap.keySet().toArray()[0].toString()));
		clusters.put(countID,new Cluster(tmpCell));
		
		for(Entry<Long, GeoCell> cell:cellMap.entrySet()){
			if(cell.getValue().getTotalProb()>0.0001){
				tmpCell = cell.getValue();

				boolean flag = false;

				for(Entry<Integer, Cluster> cluster:clusters.entrySet()){
					if(cluster.getValue().isInNeighboorhood(tmpCell, margin)){
						flag = true;
						break;
					}
				}

				if(!flag){
					countID++;
					clusters.put(countID,new Cluster(tmpCell));
				}
			}
		}
	}

	/**
	 * 
	 * @return the representative cells for every generated cluster
	 */
	public Set<Long> getClusters() {
		
		Set<Long> cells = new HashSet<Long>();
		
		cells.add(id);
		for(Entry<Integer, Cluster> cluster:clusters.entrySet()){
			if(cluster.getValue().size()>1){
				cells.add(cluster.getValue().getRepresentativeCell());
			}
		}
		
		return cells;
	}
}
