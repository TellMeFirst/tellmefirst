# TellMeFirst

TellMeFirst is a tool for classifying and enriching textual documents via Linked Open Data.

TellMeFirst exploits [Lucene](http://lucene.apache.org/core/) Indexes for its classification and enrichment system. To build the TellMeFirst Indexes you can install and use our [fork of the DBpedia Spotlight project](https://github.com/TellMeFirst/dbpedia-spotlight/tree/tellmefirst).

This program can be used under the terms of the GNU Affero General Public License 3.0. Part of the code uses LingPipe under the Royalty Free License, and FancyBox under Creative Commons Attribution-NonCommercial 3.0 Unported.

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


