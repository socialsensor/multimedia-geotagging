package gr.iti.mklab.tools;

import gr.iti.mklab.data.ImageMetadata;

import java.util.List;
import java.util.Map;

/**
 * Abstract class for organization of the images in cells
 * @author gkordo
 *
 */
public class AbstractOrganizer {

	protected Map<String, ImageMetadata> mapOfImageIDs;
	protected Map<String,List<String>> mapOfCells;

	protected void updateMapOfImageIDs(String input){

		String[] imageInfo = input.split("\t");

		ImageMetadata image = new ImageMetadata(imageInfo[0],imageInfo[2],imageInfo[4]+","+imageInfo[3].replace("\\+",","));
		
		image.setCoord(Double.parseDouble(imageInfo[6]), Double.parseDouble(imageInfo[7]));
		
		mapOfImageIDs.put(imageInfo[0],image);
	}

	public Map<String, List<String>> getMapOfCells () {
		return mapOfCells;
	}

	public Map<String, ImageMetadata> getMapOfImageIDs () {
		return mapOfImageIDs;
	}

}
