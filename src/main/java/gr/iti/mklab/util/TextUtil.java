package gr.iti.mklab.util;

import java.util.ArrayList;
import java.util.List;


public class TextUtil {

	public static String cleanTextTitle (String text){

		if ((text !=null ) || (text !="")){

			text = text.trim(); // removes redundant white spaces
			text = text.replaceAll("\\p{Punct}", " ");

			text = text.toLowerCase();
			text = text.replaceAll("\\s{2,}", " ");
			text = text.trim();
			text = text.replaceAll("\\s+", "\\+");

		}
		return text;
	}

	public static String cleanTextTags (String text){

		if ((text !=null ) || (text !="")){

			text = text.trim(); // removes redundant white spaces
			text = text.replaceAll("[\\p{Punct}&&[^\\+]&&[^\\,]]", "");

			text = text.toLowerCase();
			text = text.replaceAll("\\s{2,}", " ");
			text = text.replaceAll("\\,{2,}", ",");
			text = text.trim();
			text = text.replaceAll("\\s+", ",");

		}
		return text;
	}

	public static List<String> combineTagList(String[] tags, String[] title, String actTitle){

		List<String> tagsList = new ArrayList<String>();

		for(int j=0;j<tags.length;j++){
			tagsList.add(tags[j]);
			if(tags[j].split("\\+").length>1){
				for(int k=0;k<tags[j].split("\\+").length;k++){
					if(!tagsList.contains(tags[j].split("\\+")[k])){
						tagsList.add(tags[j].split("\\+")[k]);
					}
				}
			}
		}

		if(!tagsList.contains(actTitle)){
			tagsList.add(actTitle);
		}

		if(title.length>1){
			for(int k=0;k<title.length;k++){
				if(!tagsList.contains(title[k])){
					tagsList.add(title[k]);
				}
			}
		}
		return tagsList;
	}
}
