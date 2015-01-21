package gr.iti.mklab.tools;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.TextUtil;
import gr.iti.mklab.data.TagInfo;
import gr.iti.mklab.util.Progress;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Class that calculate the tag-cell probabilities for all tags in all cells and saves the results in file
 * @author gkordo
 *
 */
public class ClusterTagsToCells {

	static Logger logger = Logger.getLogger("gr.iti.mklab.tools.ClusterTagsToCells");
	
	public static Map<String, TagInfo> createMapOfTagsInTrainSet(String dir, Set<String> tagsInSet,String trainFile, int scale, boolean sumToOne){

		Map<String,TagInfo> mapOfTagsInfo = new HashMap<String,TagInfo>();		
		Set<String> usersIDs = new HashSet<String>();
		
		EasyBufferedReader reader = new EasyBufferedReader(trainFile);

		String input;
		String[] tags;

		int count = 0;

		Progress prog = new Progress(System.currentTimeMillis(),10,1,"create");
		
		logger.info("creating map of tags and cells");
		
		long sTime = System.currentTimeMillis();
		
		while ((input= reader.readLine())!=null){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;
			
			tags = TextUtil.combineTagList(input.split("\t")[4], input.split("\t")[3]).split(" ");

			updateMapOfTagInfo(mapOfTagsInfo,usersIDs,input,tags,tagsInSet,scale);

		}
		
		logger.info("map of tags and cells created in " + (System.currentTimeMillis()-sTime)/1000.0 + "s");
		
		reader.close();
		
		(new File(dir+"CellProbsForAllTags")).mkdirs();
		
		calculateTagCellProbs(mapOfTagsInfo,dir+"CellProbsForAllTags/cell_tag_prob_scale"+String.valueOf(scale)+".txt",sumToOne);
		
		logger.info("total time for calculate tag-cell probablities " + (System.currentTimeMillis()-sTime)/60000 + "min");
		
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
	
	public static void calculateTagCellProbs(Map<String,TagInfo> mapOfTagsInfo, String outName, boolean sumToOne){
		String fineLine;
		
		EasyBufferedWriter writer = new EasyBufferedWriter(outName);

		logger.info("calculating tag-cell probabilities");

		long sTime = System.currentTimeMillis();
		
		Progress prog = new Progress(System.currentTimeMillis(), mapOfTagsInfo.size(),10,60,"calculate");
		int count = 0;
		
		for(Entry<String, TagInfo> entryCell: mapOfTagsInfo.entrySet()){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;

			fineLine = mapOfTagsInfo.get(entryCell.getKey()).calculateCellProb(sumToOne);
			writer.write(fineLine);
			writer.newLine();
		}
		
		logger.info("tag-cell probablities calculated in " + (System.currentTimeMillis()-sTime)/60000 + "min");
		writer.close();
	}
}

