package gr.iti.mklab.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.Progress;
import gr.iti.mklab.util.TextUtil;

/**
 * Data manager
 * @author gkordo
 *
 */
public class DataManager {

	static Logger logger = Logger.getLogger("gr.iti.mklab.tools.DataManager");

	public static void createDataSet(String dir, String folder, String fileSet, String outFile, String hashFile, boolean filter){

		EasyBufferedReader reader = null;


		Set<String> idSet = meIDSet(dir + "/" + fileSet);

		Map<String,String> idMap = idMap(dir + "/" + hashFile, idSet);


		int count = 0, count2 = 0;

		logger.info("creating dataset");

		EasyBufferedWriter writer = new EasyBufferedWriter(dir + "/" + outFile);


		String[] list = new File(dir + "/" + folder).list();

		Progress prog = new Progress(System.currentTimeMillis(), 100000000, 10, 60, "creating", logger);

		for(String file:list){

			reader = new EasyBufferedReader(dir + "/" + folder + "/" +file);

			String line;

			while((line= reader.readLine()) != null){
				prog.showProgress(count2, System.currentTimeMillis());
				count2++;

				String [] feature = line.split("\t");

				if (idSet.contains(idMap.get(feature[0]))){

					if (!filter // filter parameter (if it is deactivated everything will be written in the file)
							|| (!feature[8].isEmpty()// there is no textual information in the metadata
									&& !feature[10].isEmpty() && !feature[11].isEmpty())){  // the location metadata is not provided

						writer.write(feature[0]+"\t"+idMap.get(feature[0])+"\t"+feature[1]
								+"\t"+feature[6]+"\t"+feature[8]+"\t"+feature[9]+"\t"+feature[10]
										+"\t"+feature[11]+"\t"+feature[7]);
						writer.newLine();
					}else{
						count ++;
					}
				}
			}
		}
		logger.info((idMap.size()-count) + " total images contained in the final dataset");
		logger.info(count + " images filtered");
		reader.close();
		writer.close();

	}

	// return the set of IDs of the images included in the MediaEval 2014 Placing Task dataset
	public static Set<String> meIDSet(String fileMEID){
		Set<String> idSet = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(fileMEID);

		String inputLine;

		logger.info("loading dataset's image hash IDs");

		long sTime = System.currentTimeMillis();

		while((inputLine = reader.readLine()) != null){
			idSet.add(inputLine.split("\t")[0]);
		}
		logger.info("dataset's image hash IDs loaded, in " + (System.currentTimeMillis()-sTime)/1000.0 + "s");
		logger.info("total size " + idSet.size() + " images");
		reader.close();

		return idSet;
	}

	// return a map the contains the hashed images' IDs
	public static Map<String,String> idMap(String fileID, Set<String> idSet){

		Map<String,String> idMap = new HashMap<String,String>();

		EasyBufferedReader reader = new EasyBufferedReader(fileID);

		String line;

		logger.info("loading images' IDs");

		long sTime = System.currentTimeMillis();

		while(((line = reader.readLine()) != null)){
			if(idSet.contains(line.split("\t")[1])){
				idMap.put(line.split("\t")[0],line.split("\t")[1]);
			}
		}

		logger.info("images' IDs loaded, in " + (System.currentTimeMillis()-sTime)/1000.0 + "s");
		logger.info("total size " + idMap.size() + " images");
		reader.close();

		return idMap;
	}

	// return a set contain the image IDs of the provided dataset
	public static Set<String> getSetOfImageIDs(String file){

		Set<String> usersIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;

		logger.info("images contained in file " + file);
		while ((input= reader.readLine())!=null){
			usersIncludedInFile.add(input.split("\\t")[0]);			
		}
		logger.info(usersIncludedInFile.size()+" total images included in file");
		reader.close();

		return usersIncludedInFile;
	}

	// return a set contain the individual tags of the provided dataset
	public static Set<String> getSetOfTags(String file){

		Set<String> tagsIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;
		String[] tags;

		logger.info("deterim the diffrent tags contained in file " + file);
		while ((input= reader.readLine())!=null){

			tags = TextUtil.parseImageText(input.split("\t")[4], input.split("\t")[3]).split(" ");

			Collections.addAll(tagsIncludedInFile, tags);

		}
		logger.info(tagsIncludedInFile.size()+" total tags included in file");
		reader.close();

		return tagsIncludedInFile;
	}

	// return a set contain the different users in the provided dataset
	public static Set<String> getSetOfUserID (String file){

		Set<String> usersIncludedInFile = new HashSet<String>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;

		logger.info("deterim the diffrent users contained in file " + file);
		while ((input= reader.readLine())!=null){
			usersIncludedInFile.add(input.split("\\t")[2]);			
		}
		logger.info(usersIncludedInFile.size()+" total users included in file");
		reader.close();

		return usersIncludedInFile;
	}

	// create a temporary file containing a specific file
	public static void createTempFile(String dir, String file) throws IOException {
		logger.info("create temporary file for file " + file);
		new File(dir+"temp/").mkdir();
		Files.copy(new File(dir+file).toPath(), new File(dir+"temp/"+file).toPath(),StandardCopyOption.REPLACE_EXISTING);
	}

	// delete the temporary file
	public static void deleteTempFile(String dir) throws IOException {
		FileUtils.cleanDirectory(new File(dir+"/temp"));
		FileUtils.forceDelete(new File(dir+"temp"));
		logger.info("temporary file deleted");
	}

	public static void splitDataset(String dir, String file, int step) throws IOException{

		int count = 0, i = 0;
		(new File(dir+"/temp")).mkdirs();

		logger.info("creat temp files splitting "+file);

		EasyBufferedReader reader = new EasyBufferedReader(dir+"/"+file);
		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"/temp/"+file.replaceAll(".txt", "")+"-"+String.valueOf(i));

		String input;		
		while((input=reader.readLine())!=null){
			if(count%step==0){
				writer.close();
				writer = new EasyBufferedWriter(dir+"/temp/"+file.replaceAll(".txt", "")+"-"+String.valueOf(i));
				i++;
			}
			writer.write(input);
			writer.newLine();
			count++;
		}

		logger.info(i+" temp files created");
		writer.close();
		reader.close();
	}

	// separate file for Cross Validation
	public static void separateForCrossValidation(String dir, String file, int t) throws IOException{

		int count = 0, i = 0;

		(new File(dir+"/temp")).mkdirs();

		int step = DataManager.getSetOfImageIDs(dir + file).size()/t;

		logger.info("create temp files for cross-validation");

		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"/temp/crossval-"+String.valueOf(i));


		EasyBufferedReader reader = new EasyBufferedReader(dir + file);
		String input;
		while((input=reader.readLine()) != null){
			if(count%step == 0){
				writer.close();
				writer = new EasyBufferedWriter(dir+"/temp/crossval-"+String.valueOf(i));
				i++;
			}
			if (!input.split("\t")[6].isEmpty() && !input.split("\t")[7].isEmpty()){
				writer.write(input);
				writer.newLine();
				count++;
			}
		}
		reader.close();
		logger.info("temp files created");
		writer.close();
	}

	// merge the result of the Cross Validation files
	public static void mergeResults(String dir, String fileName, String trainFile, int count) throws IOException{

		Map<String,String> map = new HashMap<String,String>();

		String line;
		for (int i=0;i<count;i++){
			EasyBufferedReader reader = new EasyBufferedReader(dir+fileName+"-"+i);


			while((line=reader.readLine())!=null){
				if (line.split(";").length>1){
					map.put(line.split(";")[0], line.split(";")[1]);
				}
			}
			reader.close();
			FileUtils.forceDelete(new File(dir+fileName+"-"+i));
		}

		EasyBufferedWriter writer = new EasyBufferedWriter(dir+fileName);

		EasyBufferedReader reader = new EasyBufferedReader(trainFile);

		while((line=reader.readLine())!=null){
			writer.write(line.split("\t")[0]+";"+map.get(line.split("\t")[0]));
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	// delete temporary files
	public static String[] getListOfFiles(String dir) throws IOException{
		File f = new File(dir+"/temp");
		return f.list();
	}
}
