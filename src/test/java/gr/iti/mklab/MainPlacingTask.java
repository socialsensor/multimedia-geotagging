package gr.iti.mklab;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import gr.iti.mklab.method.InternalGrid;
import gr.iti.mklab.method.Entropy;
import gr.iti.mklab.method.LanguageModel;
import gr.iti.mklab.tools.ClusterTagsToCells;
import gr.iti.mklab.tools.DataManager;

/**
 * The main class that combines all the other class in order to implement the method.
 * For memory allocation issues the main method has been seperated in three steps, train, LM (language model) and IGSS (internal grid and similarity search).
 * @author gkordo
 *
 */
public class MainPlacingTask {

	public static void main(String[] args) throws FileNotFoundException, IOException{

		Properties properties = new Properties();

		System.out.println("Program Started");

		properties.load(new FileInputStream("config.properties"));
		String dir = properties.getProperty("dir");

		String trainFile = properties.getProperty("trainFile");
		String testFile = properties.getProperty("testFile");

		String fileTagCell = properties.getProperty("fileTagCell");
		
		String process = properties.getProperty("process");

		int scale = Integer.parseInt(properties.getProperty("scale"));

		int k = Integer.parseInt(properties.getProperty("k"));
		String corserGrid = properties.getProperty("corserGrid");
		String finerGrid = properties.getProperty("finerGrid");
		
		String resultFile = properties.getProperty("resultFile");


		if(process.equals("train")||process.equals("all")){
			Set<String> tagsInTestSet = DataManager.getSetOfDiffrentTags(dir+testFile);

			ClusterTagsToCells.createMapOfTagsInTrainSet(dir,tagsInTestSet,dir+trainFile,scale,true);

			Entropy.createEntropyFile(dir+"CellProbsForAllTags/cell_tag_prob_scale"+String.valueOf(scale)+".txt");
		}


		if(process.equals("LM")||process.equals("all")){
			LanguageModel lmItem = new LanguageModel(dir, fileTagCell);

			lmItem.computeLanguageModel(dir,testFile,scale);
		}


		if(process.equals("IGSS")||process.equals("all")){
			InternalGrid dgss = new InternalGrid(dir,trainFile,resultFile,corserGrid,finerGrid);

			for(int i=1;i<k;i++){
				dgss.calculateInternalGridSimilaritySearch(i,1,0.05);
			}
		}
		
		
		System.out.println("Program Finished");
	}
}
