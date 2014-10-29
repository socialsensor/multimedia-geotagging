package gr.iti.mklab.data;

import gr.iti.mklab.util.MyHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TagInfo extends HashMap<String, Double>{

	private static final long serialVersionUID = 1L;
	protected Map<String,Set<String>> mapOfTagsUserPerCell;
	protected Set<String> totalUsersUseTag;
	private String tag;
	private int timesFound;

	public TagInfo(String tag){
		this.mapOfTagsUserPerCell = new HashMap<String,Set<String>>();
		this.totalUsersUseTag = new HashSet<String>();
		this.tag = tag;
		this.timesFound = 0;
	}

	public void updateMap(String cellID, String userID) {
		if(!totalUsersUseTag.contains(userID)){totalUsersUseTag.add(userID);}

		if(mapOfTagsUserPerCell.containsKey(cellID)){
			if(!mapOfTagsUserPerCell.get(cellID).contains(userID)){
				timesFound++;
				Set<String> tmpUserInCell = mapOfTagsUserPerCell.get(cellID);
				tmpUserInCell.add(userID);
				mapOfTagsUserPerCell.put(cellID, tmpUserInCell);
			}
		}else{
			timesFound++;
			Set<String> tmpUserInCell = new HashSet<String>();
			tmpUserInCell.add(userID);
			mapOfTagsUserPerCell.put(cellID,tmpUserInCell);
		}
	}

	public int getTotalUser(){
		return totalUsersUseTag.size();
	}

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
	
	public double computeTagProb(int totalUsers){
		
		return (double)totalUsersUseTag.size()/(double)totalUsers;
		
	}
}
