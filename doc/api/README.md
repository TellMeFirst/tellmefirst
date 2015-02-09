TellMeFirst APIs
================

## About This Guide

This guide is intended for all those that want to develop applications using TellMeFirst APIs.

## Conventions

* In the GET requests we use as example, variable names are shown in braces { }. Do not type the
braces and brackets in the request.
* In response descriptions, attributes are indicated by an at sign (@).
* In response examples, an ellipsis (…) indicates information that is omitted for brevity.

## Classify

### Description
The classify service allows you to identify the main topics of a textual document. The input data
could be a URL of a specific Web page, a PDF or DOC document (max 3MB) or a plain text (min 50
words - max 20,000 words). The results are expressed in the form of DBpedia URIs.

### Request - POST

	http://tellmefirst.polito.it:2222/rest/classify

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |
| Content-Type | Used to encode the data for submission to the server| multipart/form-data |

#### Request URI parameters

| PARAMETER |           DESCRIPTION          |                         EXAMPLE                         |
|-----------|:------------------------------:|:-------------------------------------------------------:|
|    file   |            File path           |       @/home/tellmefirst/files/2011-isccpaper.pdf       |
|  fileName |            File name           |                   2011-iscc-paper.pdf                   |
|    url    |          Web page url          |                  http://bit.ly/1M66e9t                  |
|    text   |     Plain text to evaluate     | The final work of legendary director Stanley Kubrick... |
| numTopics | Number of topics in the result |                        7 (max 20)                       |
|    lang   |      Language of the text      |                  “english” or “italian”                 |
|           |                                |                                                         |

#### Request examples with curl

Example of the use of classify service for extracting topics from a PDF file.

'''
curl -F file=@/home/tellmefirst/files/2011-iscc-paper.pdf -F fileName="2011-iscc-paper.pdf" -
F numTopics=7 -F lang=english -F key="" http://tellmefirst.polito.it:2222/rest/classify
'''

Example of the use of classify service for extracting topics from a URL. 

'''
curl -F url=”http://www.theguardian.com/environment/2012/nov/05/letter-decarbonisationtarget-energy-bill”
-F numTopics=7 -F lang=english http://tellmefirst.polito.it:2222/rest/classify 
'''

Example of the use of classify service for extracting topics from a plain text.

'''
curl -F text="The final work of legendary director Stanley Kubrick, who died within a week of
completing the edit, is based upon a novel by Arthur Schnitzler." -F numTopics=7 -F
lang=english http://tellmefirst.polito.it:2222/rest/classify
'''

#### Response JSON elements

| JSON ELEMENT |                                                  DESCRIPTION                                                  |                                        EXAMPLE                                       |
|--------------|:-------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|
|   @service   |                             String that identify the results of the classify call                             |                               Default value: ”Classify”                              |
|   Resources  |                    Array of the results (its length isrelated to the “numTopics” parameter)                   |                                 [{result1},{result2}]                                |
|     @uri     |                                            DBpedia URI of the topic                                           |                      http://dbpedia.org/resource/Stanley_Kubrick                     |
|    @label    |                      DBpedia label of the topic (it’s related to the language parameter)                      |                                    Stanley Kubrick                                   |
|    @title    |          Wikipedia page title of the topic  (useful to invoke the Wikimedia API for text extraction)          |                                    Stanley Kubrick                                   |
|    @score    |                       Value of the topic in relation to the context of the text analyzed                      |                                       0.821541                                       |
| @mergedTypes |                            DBpedia classes useful for getVideo and getMap requests                            | DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person #Schema:Person#DBpedia:Agent |
|    @image    | Wikipedia image url of the topic  (for topics in which is not present,  you can exploit the getImage service) |                                 http://bit.ly/1zOQt0k                                |


``` javascript

	{
	    "@service": "Classify",
	    "Resources": [
	        {
	            "@uri": "http://dbpedia.org/resource/Stanley_Kubrick",
	            "@label": "Stanley Kubrick",
	            "@title": "Stanley Kubrick",
	            "@score": "0.821541",
	            "@mergedTypes": "DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person#Schema:Person#DBpedia:Agent#",
	            "@image": ""
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Arthur_Schnitzler",
	            "@label": "Arthur Schnitzler",
	            "@title": "Arthur Schnitzler",
	            "@score": "0.7634723",
	            "@mergedTypes": "DBpedia:Writer#DBpedia:Artist#DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person#Schema:Person#DBpedia:Agent#",
	            "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/8/86/Arthur_Schnitzler_1912.jpg/461pxArthur_Schnitzler_1912.jpg"
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Stanley_Kubrick's_Boxes",
	            "@label": "Stanley Kubrick's Boxes",
	            "@title": "Stanley Kubrick's Boxes",
	            "@score": "0.72381896",
	            "@mergedTypes": "DBpedia:Film#Schema:Movie#DBpedia:Work#Schema:CreativeWork#",
	            "@image": ""
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Alan_Conway",
	            "@label": "Alan Conway",
	            "@title": "Alan Conway",
	            "@score": "0.6253642",
	            "@mergedTypes": "",
	            "@image": ""
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Eyes_Wide_Shut",
	            "@label": "Eyes Wide Shut",
	            "@title": "Eyes Wide Shut",
	            "@score": "0.6202739",
	            "@mergedTypes": "DBpedia:Film#Schema:Movie#DBpedia:Work#Schema:CreativeWork#",
	            "@image": "http://upload.wikimedia.org/wikipedia/en/f/f2/Eyes_Wide_Shut.jpg"
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Stanley_Kubrick:_A_Life_in_Pictures",
	            "@label": "Stanley Kubrick: A Life in Pictures",
	            "@title": "Stanley Kubrick: A Life in Pictures",
	            "@score": "0.606057",
	            "@mergedTypes": "DBpedia:Film#Schema:Movie#DBpedia:Work#Schema:CreativeWork#",
	            "@image": "http://upload.wikimedia.org/wikipedia/en/9/90/Poster_of_the_movie_Stanley_Kubrick-_A_Life_in_Pictures.jpg"
	        },
	        {
	            "@uri": "http://dbpedia.org/resource/Dream_Story",
	            "@label": "Dream Story",
	            "@title": "Dream Story",
	            "@score": "0.6058782",
	            "@mergedTypes": "DBpedia:Book#Schema:Book#DBpedia:Http://purl.org/ontology/bibo/Book#DBpedia:WrittenWork#DBpedia:Work#Schema:CreativeWork#",
	            "@image": "http://upload.wikimedia.org/wikipedia/en/c/c7/ArthurSchnitzler_DreamStory.jpg"
	        }
	    ]
	}
```

## getImage

### Description

The getImage service allows you to find an image related to a specific DBpedia resource. It uses the
DBpedia URI and the title of the Wikipedia entry page. Usually it could be useful when there
@image field of classify service result is empty.

### Request - GET

	http://tellmefirst.polito.it:2222/rest/getImage?uri={uri}label={wikipedia+title}

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |

#### Request URI parameters

| PARAMETER |      DESCRIPTION      |                   EXAMPLE                   |
|:---------:|:---------------------:|:-------------------------------------------:|
|    uri    |      DBpedia URI      | http://dbpedia.org/resource/Stanley_Kubrick |
|   label   | Wikipedia entry title |               Stanley+Kubrick               |

#### Request Example

```
http://tellmefirst.polito.it:2222/rest/getImage?uri=http%3A%2F%2Fdbpedia.org%2Fresource%2FStanley_Kubrick&label=Stanley+Kubrick
```

### Response
Returns the standard HTTP status code of “200 – OK” and getImage results in the JSON format.

#### Response JSON elements

| JSON ELEMENT |    DESCRIPTION   |                   EXAMPLE                   |
|--------------|:----------------:|:-------------------------------------------:|
|   @imageURL  | url of the image | http://upload.wikimedia.org/wikipedia/{...} |

## getNews

### Description

The getNews service allows you to find the New York Times (http://www.nytimes.com/) news related to a specific topic,
using the DBpedia URI.

### Request - GET

	http://tellmefirst.polito.it:2222/rest/getNews?uri={uri}

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |	

#### Request URI parameters

| PARAMETER |      DESCRIPTION      |                   EXAMPLE                   |
|:---------:|:---------------------:|:-------------------------------------------:|
|    uri    |      DBpedia URI      | http://dbpedia.org/resource/Stanley_Kubrick |

#### Request example

	http://tellmefirst.polito.it:2222/rest/getNews?uri=http://dbpedia.org/resource/Stanley_Kubrick

#### Response

Returns the standard HTTP status code of “200 – OK” and getNews results in the JSON format.
For more details about the results provided by the New York Times API you can see: 
http://developer.nytimes.com/docs

## getText

### Description
The getText service allows you to identify the title of the Wikipedia of a DBPedia resource page
from which you can retrieve the text using Wikimedia API (http://www.mediawiki.org/wiki/API).

NOTE: getText service works only for the Italian language.

### Request - GET

	http://tellmefirst.polito.it:2222/rest/getText?uri={uri}&lang={text language}

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |

#### Request URI Parameters

| PARAMETER |      DESCRIPTION      |                   EXAMPLE                   |
|:---------:|:---------------------:|:-------------------------------------------:|
|    uri    |      DBpedia URI      | http://dbpedia.org/resource/Stanley_Kubrick |
|   lang    |  Language of the text |                   italian                   |	

#### Request example

	http://tellmefirst.polito.it:2222/rest/getText?uri=http://dbpedia.org/resource/Stanley_Kubrick&lang=italian

### Response

Returns the standard HTTP status code of “200 – OK” and getText results in the JSON format.

#### Response JSON elements

| JSON ELEMENT |       DESCRIPTION     |                   EXAMPLE                   |
|--------------|:---------------------:|:-------------------------------------------:|
|    @title    | Wikipedia entry title | Italia (http://it.wikipedia.org/wiki/Italia)|

## getVideo

### Description
The getVideo service allows you to retrieve the first video on YouTube related to a specific DBpedia
URI. For retrieving the correct result, getVideo exploit the DBpedia and the Freebase classes of the
DBpedia entity.

### Request - GET

	http://tellmefirst.polito.it:2222/rest/getVideo?uri={uri}&label={label+of+uri}&type={DBpedia or Freebase class}

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |

#### Request URI Parameters

| PARAMETER |        DESCRIPTION        |                   EXAMPLE                   |
|:---------:|:-------------------------:|:-------------------------------------------:|
|    uri    |        DBpedia URI        | http://dbpedia.org/resource/Stanley_Kubrick |
|   label   |         TMF label         |               Stanley+Kubrick               |
|    type   | DBpedia or Freebase class |             DBpedia:Person#{...}            |

#### Request example with curl

http://tellmefirst.polito.it:2222/rest/getVideo?uri=http%3A%2F%2Fdbpedia.org%2Fresource%2FStanley_Kubrick&label=Stanley+Kubrick&key=

### Response
Returns the standard HTTP status code of “200 – OK” and getText results in the JSON format.

#### Response JSON elements

| JSON ELEMENT |        DESCRIPTION       |           EXAMPLE           |
|--------------|:------------------------:|:---------------------------:|
|   @videoURL  | url of the YouTube video | http://youtu.be/yyt6aFI_sfA |

### getMap

#### Description
The getMap service allows you to retrieve the latitude and longitude of a DBpedia resource, only if
it's an instance of the DBpedia place class.

#### Request - GET

	http://tellmefirst.polito.it:2222/rest/getMap?uri={uri}&type=DBpedia:Place

#### Request Headers

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |

#### Request URI Parameters

| PARAMETER |     DESCRIPTION     |              EXAMPLE              |
|:---------:|:-------------------:|:---------------------------------:|
|    uri    |     DBpedia URI     | http://dbpedia.org/resource/Turin |
|    type   | DBpedia place class |           DBpedia:Place           |

#### Request example with curl
	http://tellmefirst.polito.it:2222/rest/getMap?uri=http://dbpedia.org/resource/Turin&type=DBpedia:Place

### Response
Returns the standard HTTP status code of “200 – OK” and getText results in the JSON format.

#### Response JSON elements

| JSON ELEMENT |      DESCRIPTION     | EXAMPLE |
|--------------|:--------------------:|:-------:|
|      lat     |  Latitude coordinate | 45.0667 |
|     long     | Longitude coordinate |   7.7   |