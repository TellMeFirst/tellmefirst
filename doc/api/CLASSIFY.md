TellMeFirst APIs
================

## About This Guide

This guide is intended for users that want to develop applications using TellMeFirst Classify APIs.

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
|:---------:|:------------------------------:|:-------------------------------------------------------:|
|    file   |            File path           |                      ./test.pdf                         |
|  fileName |            File name           |                        test.pdf                         |
|    url    |          Web page url          |                  http://bit.ly/1M66e9t                  |
|    text   |     Plain text to evaluate     | The final work of legendary director Stanley Kubrick... |
| numTopics | Number of topics in the result |                        7 (max 20)                       |
|    lang   |      Language of the text      |                  “english” or “italian”                 |
|           |                                |                                                         |

#### Request examples with curl

Example of the use of classify service for extracting topics from a PDF file.

	curl -F file=@/home/tellmefirst/files/2011-iscc-paper.pdf -F fileName="2011-iscc-paper.pdf" -F numTopics=7 -F lang=english http://tellmefirst.polito.it:2222/rest/classify

Example of the use of classify service for extracting topics from a URL. 

	curl -F url="http://www.theguardian.com/environment/2012/nov/05/letter-decarbonisationtarget-energy-bill" -F numTopics=7 -F lang=english http://tellmefirst.polito.it:2222/rest/classify 

Example of the use of classify service for extracting topics from a plain text.

	curl -F text="The final work of legendary director Stanley Kubrick, who died within a week of completing the edit, is based upon a novel by Arthur Schnitzler." -F numTopics=7 -F lang=english http://tellmefirst.polito.it:2222/rest/classify

#### Response JSON elements

| JSON ELEMENT |                                                  DESCRIPTION                                                  |                                        EXAMPLE                                       |
|:------------:|:-------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|
|   @service   |                             String that identify the results of the classify call                             |                               Default value: ”Classify”                              |
|   Resources  |                    Array of the results (its length isrelated to the “numTopics” parameter)                   |                                 [{result1},{result2}]                                |
|     @uri     |                                            DBpedia URI of the topic                                           |                      http://dbpedia.org/resource/Stanley_Kubrick                     |
|    @label    |                      DBpedia label of the topic (it’s related to the language parameter)                      |                                    Stanley Kubrick                                   |
|    @title    |          Wikipedia page title of the topic  (useful to invoke the Wikimedia API for text extraction)          |                                    Stanley Kubrick                                   |
|    @score    |                       Value of the topic in relation to the context of the text analyzed                      |                                       0.821541                                       |
| @mergedTypes |                            DBpedia classes useful for getVideo and getMap requests                            | DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person #Schema:Person#DBpedia:Agent |
|    @image    |                           Wikipedia image url of the topic  (not always avaiable)                             |                                 http://bit.ly/1zOQt0k                                |

#### jQuery implementation example

``` javascript

	var sampleText = "The final work of legendary director Stanley Kubrick, who died \
	within a week of completing the edit, is based upon a novel by Arthur Schnitzler. \
	Tom Cruise and Nicole Kidman play William and Alice Harford, a physician and a \	
	gallery manager who are wealthy, successful, and travel in a sophisticated social circle."

	var dataForClassify = new FormData();
	dataForClassify.append('text', sampleText)
	dataForClassify.append('numTopics',7)
	dataForClassify.append('lang','english')

	$.ajax({
	    url: 'http://tellmefirst.polito.it:2222/rest/classify',
	    data: dataForClassify,
	    cache: false,
	    contentType: false,
	    processData: false,
	    type: 'POST',
	    success: function(data){
	        console.log(data);
	    }
	});

```

#### Node.js implementation example

``` javascript

	function classify (url) {
	    var data = new FormData();
	    data.append('url', url)
	    data.append('numTopics', 7)
	    data.append('lang','english')

	    var options = {
	        host: 'tellmefirst.polito.it',
	        port: 2222,
	        path: '/rest/classify',
	        method: 'POST',
	        headers: data.getHeaders()
	    };

	    var req = http.request(options, function (res) {
	        res.setEncoding('utf8');
	        res.on('data', function (chunk) {
	            try {
	                console.info(JSON.parse(chunk)['Resources']);
	            } catch (e) {
	                // TODO
	            }	
	        });
	    });
	    data.pipe(req);
	}

```

#### Example of JSON result

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
