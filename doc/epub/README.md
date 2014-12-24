#Epub Classifier

In this section we describe the main features of the implemented code for classifying documents published according to the [Epub](http://en.wikipedia.org/wiki/EPUB) (Electronic Publication) standard.

To include the Epub classifier in the legacy version of [TellMeFirst](https://github.com/TellMeFirst/tellmefirst/tree/master) (TMF), we have decided to apply [minor changes](https://github.com/TellMeFirst/tellmefirst/commit/fccede5c41f6dfd4a30dfbc47e66f1c9ad485e47) as possible to the [ClassifyInterface](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/rest/src/main/java/it/polito/tellmefirst/web/rest/interfaces/ClassifyInterface.java) class, in order to simplify the merge of the code in other [forks of TellMeFirst](https://github.com/TellMeFirst/tellmefirst/network).

The new [Client](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/core/src/main/java/it/polito/tellmefirst/client/Client.java) class manages different classification policies, according to the type of document, the length of text, and ad hoc choices for specific contexts and domains (a content provider of news would apply different choices respect to an educational institution). Currently, the Epub classifier is entirely implemented in the Client class: in the future the classification policy for Epub files will be defined in a different class.

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

The Epub extractor is based on the [Apache Tika](http://tika.apache.org/) toolkit and exploits the structure of the Epub document shown in the [Toc](http://www.idpf.org/accessibility/guidelines/content/nav/toc.php) (Table of Content) file. This implementation allow you to develop more advanced classification policies, compared to the simple classification of the whole text.
