package gr.iti.mklab.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gr.iti.mklab.util.EasyBufferedReader;

/**
 * Data manager
 * @author gkordo
 *
 */
public class DataManager {

	static HashSet<String>imageSetIDs = new HashSet<String>();
	
	// create a set with the individual tags that contained in a dataset
	public static Set<String> getSetOfDiffrentTags(String file){

		Set<String> tagsIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;
		String[] tags, title;

		while ((input= reader.readLine())!=null){

			imageSetIDs.add(input.split("\\t")[0]);

			tags = input.split("\\s")[4].split(",");

			title = input.split("\\s")[3].split("\\+");

			for(int i=0;i<tags.length;i++){
				if(!tagsIncludedInFile.contains(tags[i])&&!tags[i].isEmpty()&&!tags[i].matches("[0-9]+")){
					tagsIncludedInFile.add(tags[i]);
				}
			}

			for(int i=0;i<title.length;i++){
				if(!tagsIncludedInFile.contains(title[i])&&!title[i].isEmpty()&&!title[i].matches("[0-9]+")){
					tagsIncludedInFile.add(title[i]);
				}
			}
		}
		System.out.println("Tags Included In File: "+tagsIncludedInFile.size());
		reader.close();

		return tagsIncludedInFile;
	}

	public static void compareTwoStringSets(Set<String> set1, Set<String> set2){

		ArrayList<String> arrayOfSet1 = new ArrayList<String>(set1);

		int count = 0;
		for(int i=0;i<arrayOfSet1.size();i++){
			if(set2.contains(arrayOfSet1.get(i))){
				count++;
			}
		}
		System.out.println("Same Tags In Two Sets: "+count);
	}
	
	// return the IDs of the image in the test set
	public static Set<String> getTestSetIDs(){
		return imageSetIDs;
	}
}
