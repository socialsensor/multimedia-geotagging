package gr.iti.mklab.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SetCreator {

	public SetCreator(String dir, String fileSet, String outFile, boolean filter){

		EasyBufferedReader reader = null;

		(new File(dir)).mkdirs();

		EasyBufferedWriter writer = new EasyBufferedWriter(dir + "/" + outFile + ".txt");
		
		Set<String> idSet = meIDSet (dir+fileSet);

		Map<String,String> idMap = idMap(dir+"yfcc100m_dataset/yfcc100m_hash", idSet);

		List<String> exportData = new ArrayList<String>();

		int count = 0, count2 = 0;
		
		Progress prog = new Progress(System.currentTimeMillis(),10000000,10,60);
		
		for(int i=0;i<10;i++){

			reader = new EasyBufferedReader(dir+"yfcc100m_dataset/yfcc100m_dataset-"+i);

			String inputLine;

			while((inputLine= reader.readLine()) != null){
				prog.showProgress(count2, System.currentTimeMillis());
				count2++;

				String [] input = inputLine.split("\t");
				String output = "";

				if (idSet.contains(idMap.get(input[0]))){

					input[8] = TextUtil.cleanTextTags(input[8]);
					
					String [] tags = input[8].split(",");
					Set<String> setTags = new HashSet<String>();
					
					for(int j=0;j<tags.length;j++){
						setTags.add(tags[j]);
						if(tags[j].split("\\+").length>1){
							for(int k=0;k<tags[j].split("\\+").length;k++){
								if(!setTags.contains(tags[j].split("\\+")[k])){
									input[8]+=(","+tags[j].split("\\+")[k]);
									setTags.add(tags[j].split("\\+")[k]);
								}
							}
						}
					}

					output = input[0]+"\t"+idMap.get(input[0])+"\t"+input[1]+"\t"+TextUtil.cleanTextTitle(input[6])+"\t"+input[8]+"\t"+input[9]+"\t"+input[10]+"\t"+input[11]+"\t"+TextUtil.cleanTextTitle(input[7]);

					if ((!input[8].isEmpty())||(!filter)){
						writer.write(output);
						writer.newLine();
					}else{
						count ++;
					}
				}
			}
		}
		System.out.println("\n"+exportData.size()+" "+count);
		reader.close();
		writer.close();

	}

	public Map<String,String> idMap(String fileID, Set<String> idSet){

		Map<String,String> idMap = new HashMap<String,String>();

		EasyBufferedReader reader = new EasyBufferedReader(fileID);

		String inputLine;
		String[] input = null;

		int count=0,count2=0;
		while(((inputLine = reader.readLine()) != null)&&(count<100000001)){
			if(count%10000000==0){
				System.out.print("*");
			}

			count++;
			input = inputLine.split("\t");
			if(idSet.contains(input[1])){
				count2++;
				idMap.put(input[0],input[1]);
			}
			inputLine = reader.readLine();

		}
		System.out.println("\nMap ID Loaded. Size : " + count2);
		reader.close();

		return idMap;
	}

	public Set<String> meIDSet(String fileMEID){
		Set<String> idSet = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(fileMEID);

		String inputLine;
		String[] input = null;


		while((inputLine = reader.readLine()) != null){

			input = inputLine.split("\t");
			idSet.add(input[0]);

			inputLine = reader.readLine();

		}
		System.out.println("ME ID Loaded. Size : " + idSet.size());
		reader.close();

		return idSet;
	}
}
