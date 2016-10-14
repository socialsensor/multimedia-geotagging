package gr.iti.mklab.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;


public class TextUtil {
	
	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
	public static Set<String> parse (String text, Set<String> terms) {

		if ((text !=null ) || (text !="")){
			try{
				text = URLDecoder.decode(text, "UTF-8");
				text = deAccent(text);

				text = text.trim(); // removes redundant white spaces
				text = text.replaceAll("[\\p{Punct}&&[^\\,]]", "");
				text = text.replaceAll("[0-9]+", "");

				text = text.toLowerCase();
				text = text.replaceAll("\\s{2,}", " ");
				text = text.replaceAll("\\,{2,}", ",");
				text = text.trim();

				for(String term:text.split(",")){
					if(!term.replaceAll(" ", "").matches("[0-9]+")&&!term.isEmpty()){
						terms.add(term.trim());
						for(String interm:term.split(" ")){
							if(!interm.matches("[0-9]+")){
								terms.add(interm);
							}
						}
					}
				}
			}catch(UnsupportedEncodingException exception){	
			}catch(IllegalArgumentException exception){}
		}
		return terms;
	}
}
