package gr.iti.mklab.util;

import java.util.Set;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class TextUtil {

	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static Set<String> parseTweet (String text, Set<String> wordSet){

		if (text!=null&&!text.isEmpty()){		
			String cText = "";

			for(String word:text.trim().split(" ")){
				if(!word.matches("[0-9]+")&&!word.contains("http")){
					cText+=word+" ";
				}
			}	
			cText = cText.trim();
			cText = cText.replaceAll("-"," ").replaceAll("'"," ");
			cText = cText.replaceAll("[\\p{Punct}]", "");
			cText = cText.toLowerCase();
			cText = deAccent(cText);

			if(!cText.isEmpty()){
				for(String word:cText.split(" ")){
					if(!word.matches("[0-9]+")&&!word.isEmpty()){
						wordSet.add(word);
					}
				}
			}	

		}
		return wordSet;
	}
}
