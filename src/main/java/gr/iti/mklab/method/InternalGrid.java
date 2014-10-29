package gr.iti.mklab.method;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.data.ImageMetadata;
import gr.iti.mklab.tools.OrganizeImages;
import gr.iti.mklab.util.CellsLocations;
import gr.iti.mklab.util.Progress;

import java.util.List;
import java.util.Map;


public class InternalGrid {

	private String dir, resultFile;
	private OrganizeImages images;
	private Map<String, Double[]> cellLocMap;
	private String corserGrid, finerGrid;

	public InternalGrid(String dir, String trainFile, String resultFile, String corserGrid, String finerGrid){
		this.dir = dir;
		this.resultFile = resultFile;
		this.images = new OrganizeImages(dir+trainFile,3);
		this.cellLocMap = CellsLocations.loadCellsLocations(dir+"CellsLocations/grid2Up.txt");
		this.corserGrid = corserGrid;
		this.finerGrid = finerGrid;
	}

	public void calculateInternalGridSimilaritySearch(int k, int a, double t){

		SimilaritySearch itemSS = new SimilaritySearch(k,a,t);

		EasyBufferedReader testReader = new EasyBufferedReader(dir+"yfcc100m_dataset/all_test.txt");

		EasyBufferedReader resultLMG2Reader = new EasyBufferedReader(dir+"resultsLM/"+corserGrid);

		EasyBufferedReader resultLMG3Reader = new EasyBufferedReader(dir+"resultsLM/"+finerGrid);

		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"results/"+resultFile+k+".txt");

		String inputRG2 = resultLMG2Reader.readLine();
		String inputRG3 = resultLMG3Reader.readLine();
		String inputT = testReader.readLine();

		int count = 0;

		Progress prog = new Progress(System.currentTimeMillis(),510000,10,60);

		System.out.println("\nCalculating Results\nProgress:");

		while (inputT!=null){

			prog.showProgress(count, System.currentTimeMillis());
			count++;

			String cellID = null;
			Double[] result = null;

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
				}else if (((result[0]==0.0&&result[1]==0.0))&&(cellID==inputRG2)){
					result = cellLocMap.get(cellID);
				}
			}
			if((result==null)||(result[0]==0.0&&result[1]==0.0)){
				result = new Double[2];
				result[0] = 40.75282028252674;
				result[1] = -73.98282136256299;
			}

			//System.out.println(result[1]+" "+result[0]+" "+count);

			writer.write(inputT.split("\t")[0]+";"+String.valueOf(result[0])+";"+String.valueOf(result[1]));
			writer.newLine();

			inputRG2 = resultLMG2Reader.readLine(); 
			inputRG3 = resultLMG3Reader.readLine(); 
			inputT = testReader.readLine();
		}

		writer.close();
		testReader.close();
		resultLMG2Reader.close();
		resultLMG3Reader.close();
	}

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
