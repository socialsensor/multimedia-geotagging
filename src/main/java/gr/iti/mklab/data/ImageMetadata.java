package gr.iti.mklab.data;

import java.util.Set;

/**
 * The class that contains the metadata of an image.
 * @author gkordo
 * 
 */
public class ImageMetadata{

	private String imageID;
	private String predictedCell,coarserCell;
	private String  userID;
	private Set<String> tags;

	/**
	 * Constructor using the metadata provided by the dataset file
	 * @param id : image ID
	 * @param userID : user ID
	 * @param tags : image tags
	 */
	public ImageMetadata (String id, String userID,  Set<String> tags) {
		this.imageID = id;
		this.userID = userID;
		this.tags = tags;
	}

	public String getId () {
		return imageID;
	}

	public String getUserId () {
		return userID;
	}

	public Set<String> getTags () {
		return tags;
	}

	public void setPredictedCell (String cell){
		this.predictedCell = cell;
	}
	
	public void setCoarserCell (String cell){
		this.coarserCell = cell;
	}

	public String getCell () {
		return predictedCell;
	}
	
	public String getCoarserCell () {
		return coarserCell;
	}
}