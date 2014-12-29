#Epub Classifier

In this section we give some details of the implemented code for classifying documents published according to the [Epub](http://en.wikipedia.org/wiki/EPUB) (Electronic Publication) standard.

To include the Epub classifier in the legacy version of [TellMeFirst](https://github.com/TellMeFirst/tellmefirst/tree/master) (TMF), we decide to apply [minor changes](https://github.com/TellMeFirst/tellmefirst/commit/fccede5c41f6dfd4a30dfbc47e66f1c9ad485e47) to the [ClassifyInterface](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/rest/src/main/java/it/polito/tellmefirst/web/rest/interfaces/ClassifyInterface.java) class, in order to simplify the merge of the code in other [forks of TellMeFirst](https://github.com/TellMeFirst/tellmefirst/network).

## Management of different classification policies

The new [Client](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/core/src/main/java/it/polito/tellmefirst/client/Client.java) class will manage different classification policies, according to the type of document, the length of text, and ad hoc choices for specific contexts and domains (a content provider of news would apply different choices respect to an educational institution). Currently, the Epub classifier is entirely wrapped in the Client. In the next developments the classification policy for Epub files will be defined in a different class, becoming a specific module of TMF.

Below you find the first implementation of the classify method to manage different policies:

``` java
    public ArrayList<String[]> classify(String inputText, File file, String url, String fileName,
                                        int numOfTopics, String lang) throws TMFVisibleException {

        LOG.debug("[classify] - BEGIN");

        ArrayList<String[]> results;
        if (file != null && fileName.endsWith("epub")) {
            LOG.info("Launch the Epub Classifier");
            results = classifyEpub(inputText, file, url, fileName, numOfTopics,lang);
        } else {
            LOG.info("Launch the usual Classifier");
            results = classifier.classify(inputText, file, url, fileName, numOfTopics,lang);
        }

        LOG.debug("[classify] - END");

        return results;
    }
```
## Text extraction and classification

The Epub extractor is based on the [Apache Tika](https://www.gutenberg.org/) toolkit and exploits the structure of the Epub document defined in the [Toc](http://www.idpf.org/accessibility/guidelines/content/nav/toc.php) (Table of Content) file. This implementation allow you to develop more advanced classification policies, compared to the simple classification of the whole text.

For these reasons, beside the usual classification system, we have also implemented a new REST API called */rest/classifyEpubChapters* that provides results of the classification process for each chapter (the top-level section defined in the Toc) of an Epub file.

### Request - POST
   http://tellmefirst.polito.it:2222/rest/classifyEpubChapters

|    HEADER    |                     DESCRIPTION                     |     VALID VALUES    |
|:------------:|:---------------------------------------------------:|:-------------------:|
|    Accept    |   The MIME type of thereturned data format (JSON)   |   application/json  |
| Content-Type | Used to encode the data forsubmission to the server | multipart/form-data |

### Request URI parameters

| PARAMETER |           DESCRIPTION          |                EXAMPLE                |
|:---------:|:------------------------------:|:-------------------------------------:|
|    file   |            File path           | @home/tellmefirst/epub/siddarhta.epub |
|  filename |            File name           |            “siddartha.epub”           |
| numTopics | Number of topics in the result |                   7                   |
|    lang   |      Language of the text      |         “english” or “italian”        |

### Request example with curl

   curl -F file={file_location} -F fileName=”{file_name}.epub” -F numTopics=3 -F lang=english http://tellmefirst.polito.it:2222/rest/classifyEpubChapters


Here you find part of the classifyEpubChapters response on the "Siddartha by Hermann Hesse" ebook, available on the Project Gutenberg website: [http://www.gutenberg.org/ebooks/2500](http://www.gutenberg.org/ebooks/2500). These results are obtained with [DBpedia 3.9](http://wiki.dbpedia.org/Downloads39?show_files=1). 

``` javascript
{
    "@service": "ClassifyEpubChapters",
    "Chapters": [
        {
            "@toc": "THE SON OF THE BRAHMAN",
            "Resources": [
                {
                    "@uri": "http://dbpedia.org/resource/Brahman",
                    "@label": "Brahman",
                    "@title": "Brahman",
                    "@score": "1.7632357",
                    "@mergedTypes": "",
                    "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Wassertropfen.jpg/800px-Wassertropfen.jpg"
                },
                {
                    "@uri": "http://dbpedia.org/resource/Moksha",
                    "@label": "Moksha",
                    "@title": "Moksha",
                    "@score": "1.1602994",
                    "@mergedTypes": "",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Upanishads",
                    "@label": "Upanishads",
                    "@title": "Upanishads",
                    "@score": "1.0465069",
                    "@mergedTypes": "",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Gautama_Buddha",
                    "@label": "Gautama Buddha",
                    "@title": "Gautama Buddha",
                    "@score": "1.001269",
                    "@mergedTypes": "DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person#Schema:Person#DBpedia:Agent#",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Nirvana",
                    "@label": "Nirvana",
                    "@title": "Nirvana",
                    "@score": "0.93027043",
                    "@mergedTypes": "",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Brahmin",
                    "@label": "Brahmin",
                    "@title": "Brahmin",
                    "@score": "0.8890299",
                    "@mergedTypes": "",
                    "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Om.svg/356px-Om.svg.png"
                },
                {
                    "@uri": "http://dbpedia.org/resource/%C4%80tman_(Hinduism)",
                    "@label": "Ātman",
                    "@title": "Ātman (Hinduism)",
                    "@score": "1.1427768",
                    "@mergedTypes": "",
                    "@image": ""
                }
            ]
        },
        {
            "@toc": "WITH THE SAMANAS",
            "Resources": [
                {
                    "@uri": "http://dbpedia.org/resource/Gautama_Buddha",
                    "@label": "Gautama Buddha",
                    "@title": "Gautama Buddha",
                    "@score": "0.8649204",
                    "@mergedTypes": "DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person#Schema:Person#DBpedia:Agent#",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Nirvana",
                    "@label": "Nirvana",
                    "@title": "Nirvana",
                    "@score": "0.7755967",
                    "@mergedTypes": "",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Brahman",
                    "@label": "Brahman",
                    "@title": "Brahman",
                    "@score": "1.038997",
                    "@mergedTypes": "",
                    "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Wassertropfen.jpg/800px-Wassertropfen.jpg"
                },
                {
                    "@uri": "http://dbpedia.org/resource/Shramana",
                    "@label": "Shramana",
                    "@title": "Shramana",
                    "@score": "1.013327",
                    "@mergedTypes": "",
                    "@image": ""
                },
                {
                    "@uri": "http://dbpedia.org/resource/Meditation",
                    "@label": "Meditation",
                    "@title": "Meditation",
                    "@score": "0.7660967",
                    "@mergedTypes": "",
                    "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Seated_Iron_Vairocana_Buddha_of_Borimsa_Temple%28%EC%9E%A5%ED%9D%A5_%EB%B3%B4%EB%A6%BC%EC%82%AC_%EC%B2%A0%EC%A1%B0%EB%B9%84%EB%A1%9C%EC%9E%90%EB%82%98%EB%B6%88%EC%A2%8C%EC%83%81%29.jpg/450px-Seated_Iron_Vairocana_Buddha_of_Borimsa_Temple%28%EC%9E%A5%ED%9D%A5_%EB%B3%B4%EB%A6%BC%EC%82%AC_%EC%B2%A0%EC%A1%B0%EB%B9%84%EB%A1%9C%EC%9E%90%EB%82%98%EB%B6%88%EC%A2%8C%EC%83%81%29.jpg"
                },
                {
                    "@uri": "http://dbpedia.org/resource/Buddhahood",
                    "@label": "Buddhahood",
                    "@title": "Buddhahood",
                    "@score": "1.1517866",
                    "@mergedTypes": "",
                    "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Mahayanabuddha.jpg/444px-Mahayanabuddha.jpg"
                },
                {
                    "@uri": "http://dbpedia.org/resource/Enlightenment_in_Buddhism",
                    "@label": "Enlightenment in Buddhism",
                    "@title": "Enlightenment in Buddhism",
                    "@score": "1.1482058",
                    "@mergedTypes": "",
                    "@image": ""
                }
            ]
        }
    ]
}
```
As anticipated in the previous example, to test the Epub Classifier we use ebooks from the [Project Gutenberg website](https://www.gutenberg.org/). Nevertheless, we notice that in some ebooks of this repository the Toc file is not well-structured, so it is difficult to perform a classification for each chapter (In these cases we advice to use the usual *classify()* REST API). For text extraction, we combine different techniques to make more robust the process of text classification. See the *parseEpub()* method implemented in the [Client](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/core/src/main/java/it/polito/tellmefirst/client/Client.java).

## Contributing
Pull requests, feature requests and bug reports are welcome!
