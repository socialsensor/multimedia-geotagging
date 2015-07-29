package gr.iti.mklab.util;

/**
 * Pre-process of a query sentence removes redundant white space, punctuation and symbols that may exist inside it.
 * @author gkordo
 *
 */
public class TextUtil {

	public static String cleanText (String text){

		if (!text.isEmpty()){
			text = text.trim();
			text = text.replaceAll("[[^\\w0-9]+&&[^\\s]]", "");
			text = text.toLowerCase();
			text = text.replaceAll("\\s+", " ");
		}
		return text.trim();
	}

}
