Multimedia Geotagging
======

Contains the implementation of algorithms that estimate the geographic location of multimedia items based on their textual content and metadata. It includes the <a href="http://ceur-ws.org/Vol-1263/mediaeval2014_submission_44.pdf">participation</a> in the <a href="http://www.multimediaeval.org/mediaeval2014/placing2014/">MediaEval Placing Task 2014</a>. The project's paper can be found <a href="http://link.springer.com/chapter/10.1007/978-3-319-18455-5_2">here</a>. The instructions for this approach can be found <a href="https://github.com/socialsensor/multimedia-geotagging/tree/develop#instructions">here</a>



<h2>Main Method</h2>

The approach is a refined language model, including feature selection and weighting schemes and heuristic techniques that improves the accuracy in finer granularities. It is a text-based method, in which a complex geographical-tag model is built from the tags, titles and the locations of a massive amount of geotagged images that are included in a training set, in order to estimate the location of each query image included in a test set.

The main approach comprises two major processing steps, an offline and an online. A pre-processing step fist applied in all images. All punctuation and symbols are removed (e.g. “.%!&”), all characters are transformed to lower case and then all images from the training set with empty tags and title are filtered.

<h3>Offline Processing Step</h3>

* Language Model
	* divide earth surface in rectangular cells with a side length of 0.01°
	* calculate tag-cell probabilities based on the users that used the tag inside the cell

* Feature selection
	* cross-validation scheme using the training set only
	* rank tags based on their accuracy for predicting the location of items in the withheld fold
	* select tags that surpass a predefined threshold

* Feature weighting using spatial entropy
	* calculate entropy values applying the Shannon entropy formula in the tag-cell probabilities
	* build a Gaussian weight function based on the values of the spatial tag entropy
	
<h3>Online Processing Step</h3>

* Language Model based estimation
	* the probability of each cell is calculated
	* Most Likely Cell (MLC) considered the cell with the highest probability and used to produce the estimation

* Multiple Resolution Grids
	* build different language models for multiple resolution grids (side length 0.01° and 0.001°)
	* estimate the MLC combining the result of the individual language models

* Similarity Search
	* determine the most similar training images within the MLC
	* their center-of-gravity is the final location estimation


<h2>Instructions</h2>

In order to make possible to run the project you have to set all necessary argument in <a href="https://github.com/socialsensor/multimedia-geotagging/blob/master/config.properties">configurations</a>, following the instruction for every argument. The default values may be used. 


_Input File_		
The dataset's records, that are fed to the algorithm as training and test set, have to be in the following format.

			imageID	imageHashID	userID	title	tags	machineTags	lon	lat	description
				
`imageID`: the ID of the image.<br>
`imageHashID`: the Hash ID of the image that was provided by the organizers. (optional)<br>
`userID`: the ID of the user that uploaded the image.<br>
`title`: image's title.<br>
`tags`: image's tags.<br>
`machineTags`: image's machine tags.<br>
`lon`: image's longitude.<br>
`lat`: image's latitude.<br>
`description`: image's description, if it is provided. 


_Output File Format_	
At the end of the training process, the algorithm creates a folder named `CellProbsForAllTags` and inside the folder a file named `cell_tag_prob_scale(s)_entropy.txt`, where the `s` is the value of the scale that was given as argument. The format of this file is the following.

	tag	  ent-value   cell1-lon_cell1-lat>cell1-prob   cell2-lon_cell2-lat>cell2-prob...
		
`tag`: the actual name of the tag.<br>
`ent-rank`: the rank of the tag based on the entropy.<br>
`cellx`: the x most probable cell.<br>
`cellx-lon_cellx-lat`: the longitude and latitude of center of the cellx, which is also used as cell's ID.<br>
`cellx-prob`: the probability of the cellx for the specific tag.

The file described above is given as input for the Language Model process. During this process, a folder named `resultsLM` is created and inside the folder a file named `resultsLM_scale(s).txt`. The raw of this file contains the IDs of the most probable cell for every query image. Every row corresponds to the test set image of the same row.

In conclusion, the file that is created by the Language Model is used for the final process of the algorithm, the Internal Grid and Similarity Search. The final results are saved in the file specified in the arguments, and the records in each row are the ID of the query image, the estimated latitude and the estimated longitude separated with the symbol `;`.



<h3>Contact for further details about the project</h3>

Giorgos Kordopatis-Zilos (georgekordopatis@iti.gr)<br>
Symeon Papadopoulos (papadop@iti.gr)