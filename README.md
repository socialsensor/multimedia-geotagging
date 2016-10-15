Multimedia Geotagging
======

This repository contains the implementation of algorithms that estimate the geographic location of multimedia items based on their textual content. The approach is described in <a href="http://ceur-ws.org/Vol-1436/Paper58.pdf">here</a> and <a href="http://link.springer.com/chapter/10.1007/978-3-319-18455-5_2">here</a>. It was submitted in <a href="http://www.multimediaeval.org/mediaeval2016/placing/">MediaEval Placing Task 2016</a>.



<h2>Main Method</h2>

The approach is a refined language model, including feature selection and weighting schemes and heuristic techniques that improves the accuracy in finer granularities. It is a text-based method, in which a complex geographical-tag model is built from the tags, titles and the locations of a massive amount of geotagged images that are included in a training set, in order to estimate the location of each query image included in a test set.

The main approach comprises two major processing steps, an offline and an online.

<h3>Offline Processing Step</h3>

* Pre-processing
	* apply URL decoding, lowercase transformation, tokenization
	* remove accents, punctuations and symbols (e.g. “.%!&”)
	* discard terms consisting of numerics or less than three characters

* Language Model
	* divide earth surface in rectangular cells with a side length of 0.01°
	* calculate term-cell probabilities based on the users that used the term inside the cell

* Feature selection
	* calculate locality score of every term in the dataset
	* locality is based on the term frequency and the neighbor users that have used it in the cell distribution
	* the final set of selected terms is formed from the terms with locality score greater than zero 

* Feature weighting using spatial entropy
	* calculate spatial entropy values of every term applying the Shannon entropy formula in the term-cell probabilities
	* spatial entropy weights derives from a Gaussian weight function over the spatial entropy of terms
	* locality weights derives from the relative position in the rank of terms based on their locality score
	* combine locality and spatial entropy weight to generate the final weights
	
<h3>Online Processing Step</h3>

* Language Model based estimation (prior-estimation)
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


_Input File_<br>
The imput files must be in the same format as <a href="https://webscope.sandbox.yahoo.com/catalog.php?datatype=i&did=67">YFCC100M dataset</a>.


_Output Files_<br>
At the end of the training process, the algorithm creates a folder named `TermCellProbs` and inside the folder another folder named `scale_(s)`, named appropriately based on the scale `s` of the language model's cells. The format of this file is the following.

	term	cell1-lon_cell1-lat>cell1-prob>cell1-users  cell2-lon_cell2-lat>cell2-prob>cell2-users...
		
`term`: the actual name of the term<br>
`cellx`: the x most probable cell.<br>
`cellx-lon_cellx-lat`: the longitude and latitude of center of the `cellx`, which is used as cell ID<br>
`cellx-prob`: the probability of the `cellx` for the specific tag<br>
`cellx-users`: the number of users that used the specific term in the `cellx`

The output of the feature weighting scheme is a folder with name `Weights` containing two files one for locality weight and one for spatial entropy weights, namely `locality_weights` and `spatial_entropy_weights`, respectively. Each row contains a term and its corresponding weight, separated with a tab.

The files that are described above are given as input in the Language Model estimation process. During this process, a folder named `resultsLM` and inside that folder two files named `resultsLM_scale(s)`are created, where are included the MLCs of the query images. Every row contains the imageID and the MLC (tab-separated) of the image that corresponds in the respective line in the test set. Also, a file named `resultsLM_scale(s)_conf_evid` is created in the same folder, containing the confidence and evidences that lead to estimated MLC, for every query image.

Having estimated the MLCs for both granularity grids, the files are fed to the Multiple Resolution Grids technique, which produce a file named `resultsLM_mg(cs)-(fs)`, where `(cs)` and `(fs)` stands for coarser and finer granularity grid, respectively. Every row of this file contains the image id, the MLC of the coarser language model and the result of the Multiple Resolution Grids technique, separated with a `>`.

In conclusion, the file that is created by the Multiple Resolution Grids technique is used for the final processes of the algorithm, Similarity Search. During this process, a folder named `resultSS` is created, containing the similarity values and the location of the images that containing in the MLG of every image in the test set. The final results are saved in the file specified in the arguments, and the records in each row are the ID of the query image, the real longitude and latitude, the estimated longitude and latitude, and they are tab-separated.

<h3>Evaluation Framework</h3>

This <a href="https://github.com/MKLab-ITI/multimedia-geotagging/tree/develop/src/main/java/gr/iti/mklab/mmcomms16">pacage</a> contains the implemetations of the sampling strategies described in the <a href="http://dl.acm.org/citation.cfm?doid=2983554.2983558">MMCommons 2016 paper</a>. In order to run the evaluation framework you have to set all necessary argument in <a href="https://github.com/MKLab-ITI/multimedia-geotagging/blob/master/eval.properties">configuration file</a>, following the instruction for every argument. To run the code, the <a href="https://github.com/MKLab-ITI/multimedia-geotagging/blob/master/src/test/java/gr/iti/mklab/main/Evaluation.java">Evaluation class</a> have to be executed.

Additionally, in this <a href="https://github.com/MKLab-ITI/multimedia-geotagging/blob/master/samples/">folder</a>, the <a href="https://github.com/MKLab-ITI/multimedia-geotagging/blob/master/samples/samples.zip">zip file</a> that contains the generated collections from the different sampling strategies and the <a href="https://github.com/MKLab-ITI/multimedia-geotagging/blob/master/samples/building_concepts.txt">file</a> of the building concepts can be found. Keep in mind that the geographical uniform sampling, the user uniform sampling and text diversity sampling generates different files in every code execution because they involve random selections and permutations.

<h3>Demo Version</h3>

There have been developed a <a href="https://github.com/socialsensor/multimedia-geotagging/tree/demo">demo version</a> and a <a href="https://github.com/socialsensor/multimedia-geotagging/tree/storm">storm module</a> of the approach.

<h3>Contact for further details about the project</h3>

Giorgos Kordopatis-Zilos (georgekordopatis@iti.gr)<br>
Symeon Papadopoulos (papadop@iti.gr)
