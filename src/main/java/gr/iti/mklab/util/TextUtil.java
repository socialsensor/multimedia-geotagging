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
			text = text.replaceAll("\\+{2,}", "\\+");
			text = text.replaceAll("\\+", " ");
			text = text.trim();
			text = text.replaceAll("\\s+", "\\+");


			if(!tagsList.contains(text)&&!text.replaceAll("\\+", "").matches("[0-9]+")){
				tagsList.add(text);
				out += text+" ";
			}

			String[] title = text.split("\\+");

			if(title.length>1){
				for(int k=0;k<title.length;k++){
					if(!tagsList.contains(title[k])&&!title[k].matches("[0-9]+")){
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
			text = text.replaceAll("\\s{2,}", " ");
			text = text.replaceAll("\\+{2,}", "\\+");
			text = text.replaceAll("\\,{2,}", ",");
			text = text.trim();
			text = text.replaceAll("\\s+", ",");

			String[] tags = text.split(",");


			for(int j=0;j<tags.length;j++){
				if(!tags[j].replaceAll("\\+", "").matches("[0-9]+")&&!tags[j].isEmpty()){
					if(tags[j].substring(0, 1).equals("+")&&!tags[j].isEmpty()){
						tags[j] = tags[j].substring(1,tags[j].length());
					}
					if(!tagsList.contains(tags[j])){
						tagsList.add(tags[j]);
						out += tags[j]+" ";
						if(tags[j].split("\\+").length>1){
							for(int k=0;k<tags[j].split("\\+").length;k++){
								if(!tagsList.contains(tags[j].split("\\+")[k])&&!tags[j].split("\\+")[k].matches("[0-9]+")){
									tagsList.add(tags[j].split("\\+")[k]);
									out += tags[j].split("\\+")[k]+" ";
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
