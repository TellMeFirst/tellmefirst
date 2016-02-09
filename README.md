# TellMeFirst - A Knowledge Discovery Application

TellMeFirst is a tool for classifying and enriching textual documents via Linked Open Data.

TellMeFirst leverages NLP and Semantic Web technologies to extract main topics from texts in the form of DBpedia resources. Input texts are enhanced with new information and contents retrieved from the Web (images, videos, maps, news) concerning those topics.

The TellMeFirst project started in October 2011 thanks to a [Working Capital – Premio Nazionale Innovazione grant from Telecom Italia](http://www.workingcapital.telecomitalia.it/2011/10/tellmefirst/). It has been developed mostly within the [Nexa Center for Internet & Society at the Politecnico di Torino (DAUIN)](http://nexa.polito.it/tellmefirst).

The collaboration between the Nexa Center for Internet & Society and Telecom Italia (Joint Open Lab) for a further development of TellMeFirst has continued during 2013 and 2014 and it is continuing in 2015. As explained by the article entitled [Semantic Annotation and Classification in Practice](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=7077255), Telecom Italia has adopted TellMeFirst to add value to two services available to its users: [FriendTV](http://www.stv.telecomitalia.it/) and [Society](http://www.telecomitalia.com/tit/it/innovazione/i-luoghi-della-ricerca/jol-mobilab-torino/scheda-progetto-societyschool.html).

A demo of the software can be explored at: [http://tellmefirst.polito.it/](http://tellmefirst.polito.it/).

## Build from Source Code with Maven

Requirements:

* Java 1.8+
* Maven 3
* Git
* RAM 10 GiB

Since this repository contains submodules, to clone this you have to
run the following command:

```
git clone --recursive https://github.com/TellMeFirst/tellmefirst.git
```

Otherwise, if you have already cloned, you can fetch the submodules using
the following command:

```
git submodule update --init
```

Run install through Maven:

```
cd tellmefirst/back
mvn install
```

## How to create TellMeFirst Indexes

TellMeFirst exploits [Lucene](http://lucene.apache.org/core/) Indexes for its classification and enrichment system. To build the TellMeFirst Indexes you can install and use our [fork of the DBpedia Spotlight project](https://github.com/TellMeFirst/dbpedia-spotlight).

## Licenses
This program can be used under the terms of the GNU Affero General Public License 3.0. Part of the code uses LingPipe under the Royalty Free License, and FancyBox under Creative Commons Attribution-NonCommercial 3.0 Unported.

## Publications
* Cairo, F. [Un approccio basato su DBpedia per la sistematizzazione della conoscenza sul Web](http://porto.polito.it/2507077/). PhD thesis, 2013
* Futia, G., Cairo, F., Morando, F., & Leschiutta, L. [Exploiting Linked Open Data and Natural Language Processing for Classification of Political Speech](http://porto.polito.it/2540694/). *International Conference for E-Democracy and Open Government*. Krems, Austria, 21-23 May 2014
* Rocha, O.R.; Vagliano, I.; Figueroa, C.; Cairo, F.; Futia, G.; Licciardi, C. A.; Marengo, M.; Morando, F., [Semantic Annotation and Classification in Practice](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=7077255), *IT Professional* , vol.17, no.2, pp.33,39, Mar.-Apr. 2015

