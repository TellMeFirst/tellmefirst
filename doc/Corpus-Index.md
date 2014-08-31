The Lucene Documents of the Corpus Index is composed of the following Lucene Fields:
* **URI**: the DBpedia entity URI;
* **URI COUNT**: number of times that an entity appears as a [wikilink] (http://en.wikipedia.org/wiki/Wikipedia:Manual_of_Style/Linking) within the Wikipedia corpus;
* **TITLE**: title of the Wikipedia page identified by URI;
* **TYPE**: ontology resource type of the entity (each entity can have more than one TYPE);
* **IMAGE**: url of the image retrieved from the property [foaf:depiction](http://xmlns.com/foaf/0.1/depiction) of the DBpedia entity. For instance, the foaf:depiction value of [Giacomo Leopardi](http://dbpedia.org/page/Giacomo_Leopardi) is [http://upload.wikimedia.org/wikipedia/commons/c/c6/Leopardi,_Giacomo_%281798-1837%29_-_ritr._A_Ferrazzi,_Recanati,_casa_Leopardi.jpg](http://upload.wikimedia.org/wikipedia/commons/c/c6/Leopardi,_Giacomo_%281798-1837%29_-_ritr._A_Ferrazzi,_Recanati,_casa_Leopardi.jpg);
* **CONTEXT**: paragraph of Wikpedia that contains the specific entity in the form of wikilink. The Lucene Field URI COUNT defines the number of the CONTEXT within the Lucene Document.

In this section we describe how to build the Corpus Index.

## Download DBpedia and Wikipedia datasets
The first step is to download the following files:
* [DBpedia Labels](http://downloads.dbpedia.org/3.9/en/labels_en.nt.bz2): used to have a complete set of all DBpedia URIs.
* [DBpedia Redirects](http://downloads.dbpedia.org/3.9/en/redirects_en.nt.bz2): used to identify the original URI of a DBpedia entity, excluding the Wikipedia redirection pages.
* [DBpedia Disambiguation](http://downloads.dbpedia.org/3.9/en/disambiguations_en.nt.bz2): used to generate bad URIs.
* [DBpedia Ontology Types](http://downloads.dbpedia.org/3.9/en/instance_types_en.nt.bz2): used to create type-specific datasets useful for the enrichment stage of TellMeFirst.
* [DBpedia Interlanguage](http://downloads.dbpedia.org/3.9/it/interlanguage_links_it.nt.bz2) (required for the Italian Version): used in the enrichment stage. In order to retrieve new information from other Linked Data repositories, you need to use English URIs.

In addition to these datasets you need to download the Wikipedia pages dump. The paragraphs of Wikipedia pages are stored in the CONTEXT field of a Lucene Document and are used in the classification process. For building TellMeFirst indexes we choose the following versions of the Wikipedia dump.

* enwiki-20130805-pages-articles-multistream.xml.bz2 (English version);
* itwiki-20131205-pages-articles-multistream.xml.bz2 (Italian version).

The second step is to copy the downloaded files (the DBpedia datasets and the Wikipedia dumps) in the following directories, according to the specific language:

```
/data/tellmefirst/dbpedia/en/original
```
```
/data/tellmefirst/dbpedia/it/original
```

If you want to change the original path of these input datasets, you can modify the following configuration files: 

```
/conf/indexing.tmf.en.properties
```

```
/conf/indexing.tmf.it.properties
```

With this configuration files you can also set the path of intermediate outputs (necessary to produce the final Lucene index) and other configuration parameters, such as the type of Analyzer to use in the indexing phase.

## Scripts for building the Corpus Index

In order to build the Corpus Index you have to run the following scripts:

```
/bin/index.tmf.en.sh
```

```
/bin/index.tmf.it.sh
```

Each script contains a set of Maven commands for progressively building the Corpus Index of TellMeFirst. We recommend to run one command at a time, making sure that each job is successfully completed. However, with the development work carried out by the Nexa Center and using the datasets specified in this guide, the process should not present hitches during the intermediate stages.

Below is a brief description of Maven commands available in these script:

```
mvn exec:java -e -Dexec.mainClass="org.dbpedia.spotlight.lucene.index.external.utils.TMFRedirectCleaner" -Dexec.args=$INDEX_CONFIG_FILE
```

The English dataset of redirection pages has the following bug:

* <http://dbpedia.org/resource/B_with_stroke> <http://dbpedia.org/ontology/wikiPageRedirects> <http://dbpedia.org/resource/%C9%83> .

* <http://dbpedia.org/resource/%C9%83> <http://dbpedia.org/ontology/wikiPageRedirects> <http://dbpedia.org/resource/B_with_stroke > .

This bidirectional redirection implies a "loop" phase that prevents the extractor to identify the root URI. TMFRedirectCleaner class removes these entries from redirection dataset to avoid this loop (required for the English version).

```
mvn exec:java -e -Dexec.mainClass="org.dbpedia.spotlight.lucene.index.external.utils.TMFWikiDumpCleaner" -Dexec.args=$INDEX_CONFIG_FILE
```

The DBpedia Extraction Framework used in the 0.6 version of DBpedia Spotlight is not able to parse some Wikipedia namespaces, that identify different types of page created by the MediaWiki software. Methods implemented in TMFWikiDumpCleaner class correct this issue.

```
mvn scala:run -Dlauncher=ExtractCandidateMap "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE"
```

Combining the labels, the redirection pages, and the disambiguation pages datasets, ExtractCandidateMap class extracts the entities to be stored in the Lucene Index.

```
mvn exec:java -e -Dexec.mainClass="org.dbpedia.spotlight.lucene.index.external.utils.TMFUriDecoder" -Dexec.args=$INDEX_CONFIG_FILE
```

The extracted URIs from DBpedia datasets are represented using the percent-encoding, while the dump of Wikipedia contains URIs already encoded. For these reasons, it is necessary to encode URIs extracted from DBpedia, so that URIs of Wikipedia dump can be correctly mapped (required for the English version).

```
mvn scala:run -Dlauncher=ExtractOccsFromWikipedia "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/occs.tsv"
```

Mapping the DBpedia entities with the Wikipedia dump entities, you can extract the Wikipedia paragraphs associated with the DBpedia entity (to be saved in the Lucene Fields CONTEXT).

```
mvn scala:run -Dlauncher=IndexMergedOccurrences "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/occs.uriSorted.tsv"
```

Building of the intermediate Corpus Index, starting from the occurrences previously created, which contains the following Lucene Fields: URI, URI COUNT and CONTEXT.

```
mvn scala:run -Dlauncher=AddTypesToIndex "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/index"
```

Add the Lucene Field TYPE to the Corpus Index.

```
mvn scala:run -Dlauncher=AddTitlesToIndex "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/index-withTypes"
```

Add the Lucene Field TITLE to the Corpus Index.

```
mvn scala:run -Dlauncher=AddImagesToIndex "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/index-withTypesTitles"
```

Add the Lucene Field IMAGE to the Corpus Index.

```
mvn scala:run -Dlauncher=AddSameAsToIndex "-DjavaOpts.Xmx=$JAVA_XMX" "-DaddArgs=$INDEX_CONFIG_FILE|$DBPEDIA_WORKSPACE/output/index-withTypesTitlesImages"
```

Add the Lucene Field SAMEAS to the Corpus Index (required for the Italian version).

