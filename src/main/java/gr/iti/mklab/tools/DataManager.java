package gr.iti.mklab.tools;

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

	static Logger logger = Logger.getLogger("gr.iti.mklab.tools.DataManager");

	// return a set contain the image IDs of the provided dataset
	public static Set<String> getSetOfImageIDs(String file){

		Set<String> usersIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;

		logger.info("images contained in file " + file);
		while ((input= reader.readLine())!=null){
			usersIncludedInFile.add(input.split("\t")[1]);			
		}
		logger.info(usersIncludedInFile.size()+" total images included in file");
		reader.close();

		return usersIncludedInFile;
	}

	// return a set contain the individual tags of the provided dataset
	public static Set<String> getSetOfTerms(String file){

		EasyBufferedReader reader = new EasyBufferedReader(file);
		Set<String> termsIncludedInFile = new HashSet<String>();
		
		String line;

		logger.info("deterim the diffrent tags contained in file " + file);
		while ((line= reader.readLine())!=null){

			Set<String> terms = new HashSet<String>();
			TextUtil.parse(line.split("\t")[10], terms);
			TextUtil.parse(line.split("\t")[8], terms);

			termsIncludedInFile.addAll(terms);

		}
		logger.info(termsIncludedInFile.size()+" total tags included in file");
		reader.close();

		return termsIncludedInFile;
	}

	// return a set contain the different users in the provided dataset
	public static Set<String> getSetOfUserID (String file){

		Set<String> usersIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;

		logger.info("deterim the diffrent users contained in file " + file);
		while ((input= reader.readLine())!=null){
			usersIncludedInFile.add(input.split("\t")[3]);			
		}
		logger.info(usersIncludedInFile.size()+" total users included in file");
		reader.close();

		return usersIncludedInFile;
	}
}
