package gr.iti.mklab.data;

import gr.iti.mklab.util.MyHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.*;
import java.util.Set;

/**
 * The class that contains all the information for a specific tag, such as the actual tag, 
 * the map of tag-cell probabilitiesrelative to the different users that used it,
 * the total count of the different users that used it in all cells 
 * and the total different users that used it in all dataset.
 * @author gkordo
 * 
 */
public class TagInfo extends HashMap<String, Double>{

	private static final long serialVersionUID = 1L;
	protected Map<String,Set<String>> mapOfTagsUserPerCell;
	protected Set<String> totalUsersUseTag;
	private String tag;
	private int timesFound;

	/**
	 * Contractor initialize the needed variables
	 * @param tag
	 */
	public TagInfo(String tag){
		this.mapOfTagsUserPerCell = new HashMap<String,Set<String>>();
		this.totalUsersUseTag = new HashSet<String>();
		this.tag = tag;
		this.timesFound = 0;
	}

	/**
	 * Update the tag-cell probability map, when a new entry arrives.
	 * @param cellID : the cell that the tag was found
	 * @param userID : the user that used the tag
	 */
	public void updateMap(String cellID, String userID) {
		if(!totalUsersUseTag.contains(userID)){totalUsersUseTag.add(userID);}

		if(mapOfTagsUserPerCell.containsKey(cellID)){ 
			if(!mapOfTagsUserPerCell.get(cellID).contains(userID)){ // update the existing records, if the user hasn't already found in the exact cell
				timesFound++;
				Set<String> tmpUserInCell = mapOfTagsUserPerCell.get(cellID);
				tmpUserInCell.add(userID);
				mapOfTagsUserPerCell.put(cellID, tmpUserInCell);
			}
		}else{ // create new records for the new entries
			timesFound++;
			Set<String> tmpUserInCell = new HashSet<String>();
			tmpUserInCell.add(userID);
			mapOfTagsUserPerCell.put(cellID,tmpUserInCell);
		}
	}

	public int getTotalUser(){
		return totalUsersUseTag.size();
	}
	
	/**
	 * Calculation of the tag-cell probabilities.
	 * @param sumToOne : indicates that either total count or just the total of different users will be used for the calculation of the tag-cell probabilities
	 * @return a string line with all the tag-cell probabilities sorted
	 */
	public String calculateCellProb(boolean sumToOne){
		Map<String,Double> cellsProbs = new HashMap<String,Double>();
		String cellsProbsSortedStr = "";
		
		double div = (double)totalUsersUseTag.size();
		if(sumToOne){
			div = (double)timesFound;
		}
		
		for(Entry<String, Set<String>> entryCell: mapOfTagsUserPerCell.entrySet()){
			String tmpCellID = entryCell.getKey();

			Double tmpCellProc = ((double)(mapOfTagsUserPerCell.get(tmpCellID).size()))/div;

			cellsProbs.put(tmpCellID,tmpCellProc);
		}

		Map<String, Double> cellsProbsSorted = MyHashMap.sortByValues(cellsProbs);
		
		
		cellsProbsSortedStr = tag + " " + convertMapToString(cellsProbsSorted);

		return cellsProbsSortedStr;
	}


	public String convertMapToString(Map<String,Double> map){
		String out = "";

		for(Entry<String, Double> entryCell: map.entrySet()){
			if(map.get(entryCell.getKey()) >= 0.0001){
				String tempCellProb = entryCell.getKey()+">"+map.get(entryCell.getKey());

				out += (" "+tempCellProb);
			}
		}

		return out;
	}
}
