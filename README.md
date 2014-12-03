# TellMeFirst

TellMeFirst is a tool for classifying and enriching textual documents via Linked Open Data.

TellMeFirst leverages NLP and Semantic Web technologies to extract main topics from texts in the form of DBpedia resources. Input texts are enhanced with new information and contents retrieved from the Web (images, videos, maps, news) concerning those topics.

The TellMeFirst project started in October 2011 thanks to a [Working Capital – Premio Nazionale Innovazione grant from Telecom Italia] (http://www.workingcapital.telecomitalia.it/2011/10/tellmefirst/). It has been developed mostly within the [Nexa Center for Internet & Society at the Politecnico di Torino (DAUIN)] (http://nexa.polito.it/tellmefirst).

A demo of the software can be explored at: [http://tellmefirst.polito.it/] (http://tellmefirst.polito.it/).

## Build from Source Code with Maven

Requirements:

* Java 1.6+
* Maven 2
* Git
* RAM 10 GiB

Checkout all code using the command:

```
https://github.com/TellMeFirst/tellmefirst.git
```

Run install through Maven:

```
cd tellmefirst
mvn install
```

## How to create TellMeFirst Indexes

TellMeFirst exploits [Lucene](http://lucene.apache.org/core/) Indexes for its classification and enrichment system. To build the TellMeFirst Indexes you can install and use our [fork of the DBpedia Spotlight project](https://github.com/TellMeFirst/dbpedia-spotlight/tree/tellmefirst).

## Licenses
This program can be used under the terms of the GNU Affero General Public License 3.0. Part of the code uses LingPipe under the Royalty Free License, and FancyBox under Creative Commons Attribution-NonCommercial 3.0 Unported.

## Publications
Futia, G., Cairo, F., Morando, F., & Leschiutta, L. [Exploiting Linked Open Data and Natural Language Processing for Classification of Political Speech] (http://porto.polito.it/2540694/). *International Conference for E-Democracy and Open Government*. Krems, Austria, 21-23 May 2014


