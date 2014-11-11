package gr.iti.mklab.tools;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.data.TagInfo;
import gr.iti.mklab.util.Progress;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Class that calculate the tag-cell probabilities for all tags in all cells and saves the results in file
 * @author gkordo
 *
 */
public class ClusterTagsToCells {

	public static Map<String, TagInfo> createMapOfTagsInTrainSet(String dir, Set<String> tagsInSet,String trainFile, int scale, boolean sumToOne){

		Map<String,TagInfo> mapOfTagsInfo = new HashMap<String,TagInfo>();		
		Set<String> usersIDs = new HashSet<String>();
		
		EasyBufferedReader reader = new EasyBufferedReader(trainFile);

		String input;
		String[] tags, title;

		int count = 0;

		Progress prog = new Progress(System.currentTimeMillis(),4099606,10,1);
		
		System.out.println("\nCreating Map Of Tags And Cells\nProgress:");
		
		while ((input= reader.readLine())!=null){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;
			
			tags = input.split("\t")[4].split(",");

			updateMapOfTagInfo(mapOfTagsInfo,usersIDs,input,tags,tagsInSet,scale);

			title = input.split("\t")[3].split("\\+");

			updateMapOfTagInfo(mapOfTagsInfo,usersIDs,input,title,tagsInSet,scale);
		}
		
		reader.close();
		
		(new File(dir+"CellProbsForAllTags")).mkdirs();
		
		writeMapOfTagsInFile(mapOfTagsInfo,dir+"CellProbsForAllTags/cell_tag_prob_scale"+String.valueOf(scale)+".txt",sumToOne);
		
		return mapOfTagsInfo;
	}

	public static void updateMapOfTagInfo(Map<String,TagInfo> mapOfTagsInfo, Set<String> usersIDs,String input, String[] tags, Set<String> tagsInSet, int scale){
		
		for(int i=0;i<tags.length;i++){
			if(tagsInSet.contains(tags[i])&&!tags[i].isEmpty()&&!tags[i].matches("[0-9]+")){

				BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(input.split("\t")[6])).setScale(scale, BigDecimal.ROUND_HALF_UP);
				BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(input.split("\t")[7])).setScale(scale, BigDecimal.ROUND_HALF_UP);

				String cellID = String.valueOf(tmpLonCenter)+"_"+String.valueOf(tmpLatCenter);
				String userID = input.split("\t")[2];
				
				if(!usersIDs.contains(userID)){
					usersIDs.add(userID);
				}
				
				if(mapOfTagsInfo.containsKey(tags[i])){
					mapOfTagsInfo.get(tags[i]).updateMap(cellID,userID);
				}else{
					TagInfo tmpTagInfo = new TagInfo(tags[i]);
					tmpTagInfo.updateMap(cellID, userID);
					mapOfTagsInfo.put(tags[i],tmpTagInfo);
				}
			}
		}
	}
	
	public static void writeMapOfTagsInFile(Map<String,TagInfo> mapOfTagsInfo, String outName, boolean sumToOne){
		String fineLine;

		String outFileName = outName;
		
		EasyBufferedWriter writer = new EasyBufferedWriter(outFileName);

		System.out.println("\nWrite In File");

		Progress prog = new Progress(System.currentTimeMillis(), mapOfTagsInfo.size(),10,60);
		int count = 0;
		
		for(Entry<String, TagInfo> entryCell: mapOfTagsInfo.entrySet()){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;

			fineLine = mapOfTagsInfo.get(entryCell.getKey()).calculateCellProb(sumToOne);
			writer.write(fineLine);
			writer.newLine();
		}
		writer.close();
	}
}

