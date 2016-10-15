package gr.iti.mklab.mmcomms16;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

@SuppressWarnings("unchecked")
public class UserUniformSampling extends Sampling {

	private static Logger logger = Logger.getLogger(
			"gr.iti.mklab.eval.UserUniformSampling");

	public static Object sample(String testFile) throws Exception{

		logger.info("Sampling: User Uniform Strategy");
		
		UserUniformSampling sampling = new UserUniformSampling();

		return sampling.writeInFile(sampling.loadData(testFile));
	}

	protected Object loadData(String testFile) {

		Map<String, Set<String>> users =
				new HashMap<String, Set<String>>();

		EasyBufferedReader reader = 
				new EasyBufferedReader(testFile);
		String line;
		while((line = reader.readLine())!=null){
			String user = line.split("\t")[3];
			if(users.containsKey(user)){
				users.get(user).add(line.split("\t")[1]);
			}else{
				users.put(user, new HashSet<String>());
				users.get(user).add(line.split("\t")[1]);
			}
		}
		reader.close();
		logger.info(users.size() + " Users loaded");

		return users;
	}

	protected Object writeInFile(Object data) {

		Map<String, Set<String>> users =
				(Map<String, Set<String>>) data;

		Set<String> respond = new HashSet<String>();
		
		EasyBufferedWriter writer = new EasyBufferedWriter(
				"samples/user_uniform_sampling.txt");

		for(Entry<String, Set<String>> user:users.entrySet()){
			List<String> images =
					new ArrayList<String>(user.getValue());
			Collections.shuffle(images);

			respond.add(images.get(0));
			writer.write(images.get(0));
			writer.newLine();
		}
		writer.close();
		
		return respond;
	}
}
