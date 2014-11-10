package gr.iti.mklab.tools;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.data.ImageMetadata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class organizes the images of a given dataset in cells
 * @author gkordo
 *
 */
public class OrganizeImages extends AbstractOrganizer {

	public OrganizeImages(String file, int numOfGrids){
		
		this.mapOfCells = new HashMap<String,List<String>>();
		this.mapOfImageIDs = new HashMap<String,ImageMetadata>();

		EasyBufferedReader reader = new EasyBufferedReader(file);

		String input;

		long gStartTime = System.currentTimeMillis();

		while ((input= reader.readLine())!=null){
			
			updateMapOfCells(input,numOfGrids);
			updateMapOfImageIDs(input);
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("\nTotal Time For Organizing Tag To Cells: "+(stopTime-gStartTime)/1000+"s");
		System.out.println("\nTotal Number Of Cells: "+mapOfCells.size());
		reader.close();
	}

	public void updateMapOfCells(String input, int numOfGrids){

		for(int i=2;i<(numOfGrids+1);i++){
			BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(input.split("\\t")[6])).setScale(i, BigDecimal.ROUND_HALF_UP);
			BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(input.split("\\t")[7])).setScale(i, BigDecimal.ROUND_HALF_UP);

			String cellID = String.valueOf(tmpLonCenter)+"_"+String.valueOf(tmpLatCenter);
			String imageID = input.split("\t")[0];

			if(mapOfCells.containsKey(cellID)){
				List<String> tmpImageIDList = mapOfCells.get(cellID);
				tmpImageIDList.add(imageID);
				mapOfCells.put(cellID,tmpImageIDList);
			}else{
				List<String> tmpImageIDList = new ArrayList<String>();
				tmpImageIDList.add(imageID);
				mapOfCells.put(cellID,tmpImageIDList);
			}
		}
	}
	
	public List<ImageMetadata> organizeImagesForSimilaritySearth(String cellID){
		
		List<ImageMetadata> imagesForSS = new ArrayList<ImageMetadata>();
		
		for(int i=0;i<mapOfCells.get(cellID).size();i++){
			imagesForSS.add(mapOfImageIDs.get(mapOfCells.get(cellID).get(i)));
		}
		
		return imagesForSS;
	}
}	

