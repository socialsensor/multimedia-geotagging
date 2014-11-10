package gr.iti.mklab.method;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;
import gr.iti.mklab.util.MyHashMap;
import gr.iti.mklab.util.Progress;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the core of the algorithm. It is the implementation of the language model.
 * @author georgekordopatis
 *
 */
public class LanguageModel {

	protected Map<String,Map<String,Double>> tagsAndProbsInclusedInCells;
	protected Map<String,List<String>> cellsInclusedInTags;
	protected Map<String,String> entropyTags;
	protected List<Double> p;
	protected String file;
	
	// Constractor initializes the need maps
	public LanguageModel(String dir, String file){
		this.tagsAndProbsInclusedInCells = new HashMap<String,Map<String,Double>>();
		this.cellsInclusedInTags = new HashMap<String,List<String>>();
		this.entropyTags = new HashMap<String,String>();
		this.p = new ArrayList<Double>();
		this.file = dir+"CellProbsForAllTags/"+file;

		organizeMapOfCellsTags();
	}

	/**
	 * Calculate the probability of every cell based on the given tags of the query image
	 * @param imageTags : the tags and title of an query image
	 * @return the most probable cell
	 */
	public String calculateLanguageModel(List<String> imageTags) {

		Map<String, Double[]> cellMap = calculateCellsProbForImageTags(imageTags);

		if(cellMap.isEmpty()){
			cellMap = calculateCellsProbForImageTags(imageTags);
		}

		String identicalCell = findIdenticalCell(cellMap);

		return identicalCell;
	}

	/**
	 * Calculate the probability of every cell based on the given tags of the query image
	 * @param cellMap : map with the cell probabilities
	 * @return the most probable cell
	 */
	public String findIdenticalCell(Map<String, Double[]> cellMap) {

		Map<String, Double[]> cellsProbsSorted = MyHashMap.sortByValuesTable(cellMap);

		String identicalCell = null;
		if (!cellsProbsSorted.isEmpty())
			identicalCell = cellsProbsSorted.keySet().toArray()[0].toString();

		return identicalCell;
	}
	
	/**
	 * The function that perform the calculation of the language model on the given tag set
	 * @param imageTags : the tags and title of an query image
	 * @return 
	 */
	public Map<String, Double[]> calculateCellsProbForImageTags (List<String> imageTags) {

		Map<String,Double[]> cellMap = new HashMap<String,Double[]>();

		GaussianDistribution gd = new GaussianDistribution(p);

		String tag,cell;
		for(int i=0;i<imageTags.size();i++){
			tag = imageTags.get(i);
			if(cellsInclusedInTags.containsKey(tag)){
				
				double entropyValue= Double.valueOf(entropyTags.get(tag).split("_")[1]);
				for(int j=0;j<cellsInclusedInTags.get(tag).size();j++){
					cell = cellsInclusedInTags.get(tag).get(j);
					
					if(tagsAndProbsInclusedInCells.get(cell).containsKey(tag)){

						if(cellMap.containsKey(cell)){
							Double[] tmp = cellMap.get(cell);
							
							tmp[0] += (tagsAndProbsInclusedInCells.get(cell).get(tag))*gd.calculateGaussianDistribution(entropyValue);
							tmp[1] += gd.calculateGaussianDistribution(entropyValue);
							cellMap.put(cell,tmp);			
						}else{
							Double[] tmp = new Double[2];
							
							tmp[0] = (tagsAndProbsInclusedInCells.get(cell).get(tag))*gd.calculateGaussianDistribution(entropyValue);
							tmp[1] = gd.calculateGaussianDistribution(entropyValue);
							cellMap.put(cell,tmp);
						}
					}
				}

			}
		}
		return cellMap;
	}

	// Initialize the method
	public void organizeMapOfCellsTags(){

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input = reader.readLine();
		String tag;

		System.out.println("\nLoading Cells' Probabilities For All Tags");
		System.out.println("Progress");

		Progress prog = new Progress(System.currentTimeMillis(),373513,10,60);
		int count=0;

		// load tag-cell probabilities from the given file
		while ((input = reader.readLine())!=null){

			prog.showProgress(count, System.currentTimeMillis());

			count++;

			String[] inputLine = input.split("\\s");

			tag = inputLine[0];
			List<String> tmpCellList = new ArrayList<String>();
			for(int i=2;i<inputLine.length;i++){
				tmpCellList.add(inputLine[i].split(">")[0]);
			}

			entropyTags.put(tag, inputLine[1]);

			p.add(Double.valueOf(inputLine[1].split("_")[1]));

			cellsInclusedInTags.put(tag, tmpCellList);

			for(int i=2;i<inputLine.length;i++){
				String cellID = inputLine[i].split(">")[0];
				String cellProb = inputLine[i].split(">")[1];
				if(tagsAndProbsInclusedInCells.containsKey(cellID)){
					Map<String, Double> tmpTagList = tagsAndProbsInclusedInCells.get(cellID);
					tmpTagList.put(tag,Double.parseDouble(cellProb));
					tagsAndProbsInclusedInCells.put(cellID, tmpTagList);
				}else{
					Map<String,Double> tmpTagList = new HashMap<String,Double>();
					tmpTagList.put(tag,Double.parseDouble(cellProb));
					tagsAndProbsInclusedInCells.put(cellID, tmpTagList);
				}
			}
		}
		System.out.println("\nNumber Of Cells: " + tagsAndProbsInclusedInCells.size()+"\nNumber Of Tags: "+cellsInclusedInTags.size());
		System.out.println("Progress");
		reader.close();
	}

	/**
	 * Function that perform language model method for a file provided and in the determined scale
	 * @param dir
	 * @param testFile
	 * @param scale
	 */
	public void computeLanguageModel(String dir, String testFile, int scale){

		EasyBufferedReader reader = new EasyBufferedReader(dir+testFile);

		(new File(dir+"resultsLM")).mkdirs();

		EasyBufferedWriter writer = new EasyBufferedWriter(dir+"resultsLM/resultsLM_scale"+String.valueOf(scale)+".txt");

		String input;

		int count = 0;

		Progress prog = new Progress(System.currentTimeMillis(),510000,100,60);

		while ((input = reader.readLine())!=null){

			prog.showProgress(count, System.currentTimeMillis());
			count++;

			String[] tags = input.split("\t")[4].split(",");
			String[] title = input.split("\t")[3].split("\\+");

			List<String> tagsList = new ArrayList<String>();

			Collections.addAll(tagsList, tags);
			Collections.addAll(tagsList, title);

			String result = calculateLanguageModel(tagsList);
			if(result==null&&input.split("\t").length>8){

				String[] disc = input.split("\t")[8].split("\\+");

				tagsList = new ArrayList<String>();

				Collections.addAll(tagsList, disc);

				result = calculateLanguageModel(tagsList);
			}

			if(result!=null){
				writer.write(result);
				writer.newLine();
			}else{
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}
}
