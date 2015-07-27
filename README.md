# Multimedia Geolocator
Contains the storm implementation of the multimedia-geolocator module that has been developed within the framework of Reveal project. This implementation is based on the [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies) of WeST Institute of University of Koblenz.

## Installation requirements
* Clone and install [REST service](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet) following the instructions that can be found [here](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet/blob/master/Container_setup_guide).
* The module needs at least 6gb of RAM to load all the required data. Edit `storm.yaml` file where storm configurations are contained and add/modify *worker.childopts* to ` worker.childopts: "-Xmx6g -Djava.net.preferIPv4Stack=true"` 
* Download [utility folder](https://www.dropbox.com/sh/6v7fz50saldiq9g/AABfyc9Zxe1kE4k3Sf-xNJyDa?dl=0)([zip](https://www.dropbox.com/s/8lfktlt0cjse5n3/multi-geo-utils.zip?dl=0)) and move it into the [resources directory of the REST service](https://github.com/Institute-Web-Science-and-Technologies/reveal_restlet/tree/master/resources) in order to be available at runtime.

## Topology deployment
* Direct deployment of the topology
  * running `mvn clean install` in order to deploy topology to the storm cluster with name `multimedia-geolocator`.
* Deployment using [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies)
  * move folder [`multimedia-geolocator`]() into [westTopologies](https://github.com/Institute-Web-Science-and-Technologies/westTopologies) folder
  * list topology in the [parent POM](https://github.com/Institute-Web-Science-and-Technologies/westTopologies/blob/master/pom.xml) in the `modules` field. To do so add `<module>multimedia-geolocator</module>` in the respective field.

## Location Estimation
The module receives emitted tweets through RabbitMQ and adds a field named `certh:loc_set`, which includes the following information:
`itinno:item_id`: tweet id
`location`: the estimated location format *POINT(lat lon)*
`confidence`: the confidence of the estimation
`geonames:loc_id`: geoname item id close to the estimated location
`geonames:name`: city and country of geoname item
`evidence`: list of associated word and their contribution to the estimation

## Topology testing
Two alternatives are available as test of the topology. In both cases, a [query sample]() is emitted by the [`JsonSpout`](). Also, a valid path have to be filled in order to save the results logs.
* a [`TestCase`]() that is immediately runnable.
* a [test topology]() that needs some configurations
  * in [`JsonSpout`]() the full path of [samples]() have to be provided
  * in the [parent POM]() the declaration of the main class has to be edited from `gr/iti/mklab/topology/CEARTHTopologyRunner` to `gr/iti/mklab/topology/TestTopologyRunner`


## Contact for further details about the project</h3>
Giorgos Kordopatis-Zilos (georgekordopatis@iti.gr)<br>
Symeon Papadopoulos (papadop@iti.gr)
