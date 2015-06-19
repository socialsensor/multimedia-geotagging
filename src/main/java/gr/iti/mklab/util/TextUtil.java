package gr.iti.mklab.util;

import java.util.HashSet;
import java.util.Set;


public class TextUtil {

	public static String parserTitle (String text, Set<String> tagsList){

		String out = "";

		if ((text !=null ) || (text !="")){

			text = text.trim(); // removes redundant white spaces
			text = text.replaceAll("[\\p{Punct}&&[^\\+]]", "");

			text = text.toLowerCase();
			text = text.replaceAll("\\s+", "\\+");
			text = text.replaceAll("\\+{2,}", "\\+");
			text = text.trim();
			
			if(!text.isEmpty()&&text.substring(0, 1).equals("+")){
				text = text.substring(1,text.length());
			}
			
			if(!tagsList.contains(text) && !text.replaceAll("\\+", "").matches("[0-9]+")){
				tagsList.add(text);
				out += text+" ";
			}

			String[] title = text.split("\\+");

			if(title.length>1){
				for(int k=0;k<title.length;k++){
					if(!tagsList.contains(title[k]) && !title[k].matches("[0-9]+")){
						tagsList.add(title[k]);
						out += title[k]+" ";
					}
				}
			}	

		}
		return out.trim();
	}

	public static String parserTags (String text, Set<String> tagsList){
		String out = "";

		if ((text !=null ) || (text !="")){

			text = text.trim(); // removes redundant white spaces
			text = text.replaceAll("[\\p{Punct}&&[^\\+]&&[^\\,]]", "");

			text = text.toLowerCase();
			text = text.replaceAll("\\s+", ",");
			text = text.replaceAll("\\+{2,}", "\\+");
			text = text.replaceAll("\\,{2,}", ",");
			text = text.trim();

			String[] tags = text.split(",");

			for(String tag:tags){
				if(!tag.replaceAll("\\+", "").matches("[0-9]+") && !tag.isEmpty()){
					if(tag.substring(0, 1).equals("+")){
						tag = tag.substring(1,tag.length());
					}
					if(!tagsList.contains(tag)){
						tagsList.add(tag);
						out += tag + " ";
						if(tag.split("\\+").length>1){
							for(int k=0;k<tag.split("\\+").length;k++){
								if(!tagsList.contains(tag.split("\\+")[k])&&!tag.split("\\+")[k].matches("[0-9]+")){
									tagsList.add(tag.split("\\+")[k]);
									out += tag.split("\\+")[k]+" ";
								}
							}
						}
					}
				}
			}	
		}
		return out.trim();
	}

	public static String parseImageText(String inTags, String inTitle){

		Set<String> tagsList = new HashSet<String>();

		inTags = TextUtil.parserTags(inTags,tagsList);
		inTitle = TextUtil.parserTitle(inTitle,tagsList);

		return (inTags+" "+inTitle).trim();
	}
}
