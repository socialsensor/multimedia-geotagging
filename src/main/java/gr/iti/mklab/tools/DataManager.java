package gr.iti.mklab.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.TextUtil;

/**
 * Data manager
 * @author gkordo
 *
 */
public class DataManager {

	static HashSet<String>imageSetIDs = new HashSet<String>();
	static Logger logger = Logger.getLogger("gr.iti.mklab.tools.DataManager");

	// create a set with the individual tags that contained in a dataset
	public static Set<String> getSetOfDiffrentTags(String file){

		Set<String> tagsIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;
		String[] tags;

		logger.info("deterim the diffrent contained in file " + file);
		while ((input= reader.readLine())!=null){

			imageSetIDs.add(input.split("\\t")[0]);

			tags = TextUtil.combineTagList(input.split("\t")[4], input.split("\t")[3]).split(" ");
			
			Collections.addAll(tagsIncludedInFile, tags);
			
		}
		logger.info(tagsIncludedInFile.size()+" total tags included in file");
		reader.close();

		return tagsIncludedInFile;
	}

	public static void compareTwoStringSets(Set<String> set1, Set<String> set2){

		ArrayList<String> arrayOfSet1 = new ArrayList<String>(set1);

		logger.info("compare tag sets");
		
		int count = 0;
		for(int i=0;i<arrayOfSet1.size();i++){
			if(set2.contains(arrayOfSet1.get(i))){
				count++;
			}
		}
		logger.info(count+"same tags in two sets");
	}
	
	// return the IDs of the images in the test set
	public static Set<String> getTestSetIDs(){
		return imageSetIDs;
	}
}
