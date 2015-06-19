GeoTag
======

Contains the implementation of algorithms that estimate the geographic location of media content based on their content and metadata. It includes the <a href="http://ceur-ws.org/Vol-1263/mediaeval2014_submission_44.pdf">participation</a> in the <a href="http://www.multimediaeval.org/mediaeval2014/placing2014/">MediaEval Placing Task 2014</a>. The project's paper can be found <a href="http://link.springer.com/chapter/10.1007/978-3-319-18455-5_2">here</a>.



<h2>Main Method</h2>

<h3>Baseline Approach</h3>
This is a tag-based method, in which a complex geographical-tag model is built from the tags, titles and the locations of the images of the training set, in order to estimate the location of each query image included in the test set. The baseline approach comprises three steps.

A. Filtering: remove all punctuation and symbols from the training and test data (e.g. “.%!&”), transform all characters to lower case and then remove from the training set all images with empty tags and title.

B. Grid Of Cells & Language Model: Divide the earth surface in cells with a side length of 0.01° for both latitude and longitude (approximately 1km near equator). Then for each such cell and for each tag, the tag-cell probabilities are calculated.

C. Assignment in Cells: For a query image, probability for every cell is computed summing up the contributions of individual tags and title words.



<h3>Extensions</h3>
Having the implementation, described above, as baseline, some extensions are applied.

1. Similarity Search: Determine the _k_ most similar training images (using Jaccard similarity on the corresponding sets of tags) within the identical cell, and use their center-of-gravity is used as the estimated location.

2. Internal Grid: Built language model using a finer grid (cell side of 0.001°)and make the assumption that: if the estimated cell of finer granularity falls inside the borders of the estimated cell of coarser granularity, then apply similarity search inside former cell. Otherwise, apply similarity search inside latter cell.

3. Spatial Entropy: Built a Gaussian weight function based on the values of the spatial tag entropy. The spatial tag entropy calculated using the Shannon entropy formula on the tag-cell probabilities.



<h2>Instructions</h2>

In order to make possible to run the project you have to set all necessary argument in the file <a href="https://github.com/socialsensor/multimedia-geotagging/blob/master/config.properties">config.properties</a>. 


_Input File Format_		
The dataset's records, that are given as training and test set, have to be in the following format.

			imageID  imageHashID  userID  title  tags  machineTags  lon  lat  description
				
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

	tag	  ent-rank_ent-value   cell1-lon_cell1-lat>cell1-prob   cell2-lon_cell2-lat>cell2-prob...
		
`tag`: the actual name of the tag.<br>
`ent-value`: the value of the tag's entropy.<br>
`ent-rank`: the rank of the tag based on the entropy.<br>
`cellx`: the x most probable cell.<br>
`cellx-lon_cellx-lat`: the longitude and latitude of center of the cellx, which is also used as cell's ID.<br>
`cellx-prob`: the probability of the cellx for the specific tag.

The file described above is given as input for the Language Model process. During this process, a folder named `resultsLM` is created and inside the folder a file named `resultsLM_scale(s).txt`. The raw of this file contains the IDs of the most probable cell for every query image. Every row corresponds to the test set image of the same row.

In conclusion, the file that is created by the Language Model is used for the final process of the algorithm, the Internal Grid and Similarity Search. The final results are saved in the file specified in the arguments, and the records in each row are the ID of the query image, the estimated latitude and the estimated longitude separated with the symbol `;`.



<h3>Contact for further details about the project</h3>

Giorgos Kordopatis-Zilos (georgekordopatis@iti.gr)<br>
Symeon Papadopoulos (papadop@iti.gr)