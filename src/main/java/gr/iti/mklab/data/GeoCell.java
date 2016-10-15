package gr.iti.mklab.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gr.iti.mklab.util.Utils;

/**
 * Class that implements the earth cells.
 * @author gkordo
 *
 */
public class GeoCell {

	private Double totalProb;
	private String id;
	private Float confidence;
	private Map<String, Float> evidence;
	
	/**
	 * Constructor of the class where the id is specified and the
	 * evidence and the summation of the probabilities are initialized.
	 * @param id : cell ID
	 */
	public GeoCell(String id){
		this.id = id;
		this.evidence = new HashMap<String, Float>();
		this.totalProb = 0.0;
	}

	/**
	 * 
	 * @return the cell ID
	 */
	public String getID(){
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
		return Utils.sortByValues(unsortMap);
	}
}
