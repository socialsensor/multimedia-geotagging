# Multimedia Geolocator
Contains the storm implementation of the multimedia-geolocator module that has been developed within the framework of *Reveal* project. This implementation is based on the [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies) of WeST Institute of University of Koblenz.

## Installation requirements
* The [REST service](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet) have to be installed, following the provided [instructions](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet/blob/master/Container_setup_guide).
* The module needs at least 6gb of RAM to load all the required data. Edit `storm.yaml` file where storm configurations are contained and add/modify *worker.childopts* to ` worker.childopts: "-Xmx6g -Djava.net.preferIPv4Stack=true"` 
* The [utility folder](https://www.dropbox.com/sh/6v7fz50saldiq9g/AABfyc9Zxe1kE4k3Sf-xNJyDa?dl=0) ([zip](https://www.dropbox.com/s/8lfktlt0cjse5n3/multi-geo-utils.zip?dl=0)) have to be downloaded and moved into the [resources directory of the REST service](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet/tree/master/resources) woth name `` in order to be available at runtime.

## Topology deployment
* Direct deployment of the topology
  * running `mvn clean install` in order to deploy topology to the storm cluster with name `multimedia-geolocator`.
* Deployment using [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies)
  * move folder [multimedia-geolocator](https://github.com/socialsensor/multimedia-geotagging/tree/storm/multimedia-geolocator) into [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies) folder
  * list topology in the [parent POM](https://github.com/Institute-Web-Science-and-Technologies/westTopologies/blob/master/pom.xml) in the modules field. To do so add `<module>multimedia-geolocator</module>` in the respective field.

## Location Estimation
The module receives emitted tweets through RabbitMQ and adds a field named `certh:loc_set` which includes the following information:
`itinno:item_id`: tweet id<br>
`location`: the estimated location format `POINT(lat lon)`<br>
`confidence`: the confidence of the estimation<br>
`geonames:loc_id`: geoname item id close to the estimated location<br>
`geonames:name`: city and country of geoname item<br>
`evidence`: list of associated word and their contribution to the estimation<br>

## Topology testing
Two alternatives are available for testing.
* [TestCase](https://github.com/socialsensor/multimedia-geotagging/blob/storm/multimedia-geolocator/src/test/main/gr/iti/mklab/test/GeolocatorTest.java) (immediately runnable)
* [Test topology](https://github.com/socialsensor/multimedia-geotagging/blob/storm/multimedia-geolocator/src/main/java/gr/iti/mklab/topology/TestTopologyRunner.java) (needs configurations)
  - in [JsonSpout](https://github.com/socialsensor/multimedia-geotagging/blob/storm/multimedia-geolocator/src/main/java/gr/iti/mklab/spouts/JsonSpout.java) the full path of [samples](https://github.com/socialsensor/multimedia-geotagging/tree/storm/multimedia-geolocator/samples) have to be provided
  - in the [parent POM](https://github.com/socialsensor/multimedia-geotagging/blob/storm/pom.xml) the declaration of the main class has to be edited from `gr/iti/mklab/topology/CERTHTopologyRunner` to `gr/iti/mklab/topology/TestTopologyRunner`


## Contact for further details about the project
Giorgos Kordopatis-Zilos (georgekordopatis@iti.gr)<br>
Symeon Papadopoulos (papadop@iti.gr)
