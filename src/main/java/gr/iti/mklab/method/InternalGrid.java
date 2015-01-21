package gr.iti.mklab.method;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.tools.OrganizeImages;
import gr.iti.mklab.util.CellsLocations;
import gr.iti.mklab.util.Progress;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The implementation of the Internal Grid technique
 * @author georgekordopatis
 *
 */
public class InternalGrid {

	private String dir, resultFile;
	private OrganizeImages images;
	private Map<String, Double[]> cellLocMap;
	private String corserGrid, finerGrid;

	static Logger logger = Logger.getLogger("gr.iti.mklab.method.InternalGrid");
	
	/**
	 * Class constractor
	 * @param dir : directory of the project
	 * @param trainFile : train file name
	 * @param resultFile : name of the output file
	 * @param corserGrid : file with the estimated cells of the corser grid
	 * @param corserGrid : file with the estimated cells of the finer grid
	 */
	public InternalGrid(String dir, String trainFile, String resultFile, String corserGrid, String finerGrid){
		this.dir = dir;
		this.resultFile = resultFile;
		this.images = new OrganizeImages(dir+trainFile,3);
		this.cellLocMap = CellsLocations.loadCellsLocations(dir+"CellsLocations/grid2.txt");
		this.corserGrid = corserGrid;
		this.finerGrid = finerGrid;
	}
	
	
	//Method that perform the Internal Grid technique and takes the arguments for the similarity search 
	public void calculateInternalGridSimilaritySearch(String testFile,int k, int a, double t){

		SimilaritySearch itemSS = new SimilaritySearch(k,a,t);

		EasyBufferedReader testReader = new EasyBufferedReader(dir+"yfcc100m_dataset/" + testFile);

		EasyBufferedReader resultLMG2Reader = new EasyBufferedReader(dir+"resultsLM/"+corserGrid);

		EasyBufferedReader resultLMG3Reader = new EasyBufferedReader(dir+"resultsLM/"+finerGrid);

		(new File(dir+"results/")).mkdirs();
		
		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"results/"+resultFile+k+".txt");

		
		String inputRG2 = resultLMG2Reader.readLine();
		String inputRG3 = resultLMG3Reader.readLine();
		String inputT = testReader.readLine();

		
		int count = 0;
		Progress prog = new Progress(System.currentTimeMillis(),510000,10,60,"calculate");
		logger.info("calculating estimated location for every query image");
		
		while (inputT!=null){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;

			String cellID = null;
			Double[] result = null;

			//the final location estimation for every query image 
			if(!(inputRG2.isEmpty()&&inputRG3.isEmpty())){

				cellID = deterimCellId(inputRG2, inputRG3);

				List<ImageMetadata> imagesForSS = images.organizeImagesForSimilaritySearth(cellID);

				result = itemSS.computeSimilarity(inputT.split("\t")[4]+","+inputT.split("\t")[3], imagesForSS);

				if((result[0]==0.0&&result[1]==0.0)&&(cellID==inputRG3)&&(!inputRG2.isEmpty())){

					cellID = inputRG2;

					List<ImageMetadata> imagesForSS2 = images.organizeImagesForSimilaritySearth(cellID);

					result = itemSS.computeSimilarity(inputT.split("\t")[4]+","+inputT.split("\t")[3], imagesForSS2);

					if((result[0]==0.0&&result[1]==0.0)){
						result = cellLocMap.get(cellID);
					}
				//if there is no result during the process, the query image is assigned in the average location of the images contained in the estimated cell
				}else if (((result[0]==0.0&&result[1]==0.0))&&(cellID==inputRG2)){
					result = cellLocMap.get(cellID);
				}
			}
			
			//if there is no result during the process, the query image is assigned in the center of the post probable cell of a courser griding
			if((result==null)||(result[0]==0.0&&result[1]==0.0)){
				result = new Double[2];
				result[0] = 40.75282028252674;
				result[1] = -73.98282136256299;
			}

			writer.write(inputT.split("\t")[0]+";"+String.valueOf(result[0])+";"+String.valueOf(result[1]));
			writer.newLine();

			inputRG2 = resultLMG2Reader.readLine(); 
			inputRG3 = resultLMG3Reader.readLine(); 
			inputT = testReader.readLine();
		}
		
		logger.info("locations estimated for " + count + "images");
		
		writer.close();
		testReader.close();
		resultLMG2Reader.close();
		resultLMG3Reader.close();
	}

	/**
	 * Method that deterims the borders of the cell that similarity search will take place
	 * @param inputRG2 : estimated cell of the corser grid
	 * @param inputRG3 : estimated cell of the finer grid
	 */
	private String deterimCellId(String inputRG2, String inputRG3){

		String cellID = "";

		if (!inputRG2.isEmpty()){

			Double[] cellIDG2 = {Double.parseDouble(inputRG2.split("_")[0]),Double.parseDouble(inputRG2.split("_")[1])};

			cellID = inputRG2;
			if(!inputRG3.isEmpty()){

				Double[] cellIDG3 = {Double.parseDouble(inputRG3.split("_")[0]),Double.parseDouble(inputRG3.split("_")[1])};

				if((cellIDG3[0]>=(cellIDG2[0]-0.005))&&(cellIDG3[0]<=(cellIDG2[0]+0.005))&&(cellIDG3[1]>=(cellIDG2[1]-0.005))&&(cellIDG3[1]<=(cellIDG2[1]+0.005))){
					cellID = inputRG3;
				}
			}
		} else if(!inputRG3.isEmpty()){
			cellID = inputRG3;
		}

		return cellID;
	}

}
