#Epub Classifier

In this section we give some details of the implemented code for classifying documents published according to the [Epub](http://en.wikipedia.org/wiki/EPUB) (Electronic Publication) standard.

To include the Epub classifier in the legacy version of [TellMeFirst](https://github.com/TellMeFirst/tellmefirst/tree/master) (TMF), we decide to apply [minor changes](https://github.com/TellMeFirst/tellmefirst/commit/fccede5c41f6dfd4a30dfbc47e66f1c9ad485e47) to the [ClassifyInterface](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/rest/src/main/java/it/polito/tellmefirst/web/rest/interfaces/ClassifyInterface.java) class, in order to simplify the merge of the code in other [forks of TellMeFirst](https://github.com/TellMeFirst/tellmefirst/network).

## Management of different classification policies

The new [Client](https://github.com/TellMeFirst/tellmefirst/blob/ebook-telecom/back/core/src/main/java/it/polito/tellmefirst/client/Client.java) class will manage different classification policies, according to the type of document, the length of text, and ad hoc choices for specific contexts and domains (a content provider of news would apply different choices respect to an educational institution). Currently, the Epub classifier is entirely wrapped in the Client. In the next developments the classification policy for Epub files will be defined in a different class.

Below you find the first implementation of the classify method to manage different policies:

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

## Text extraction and classification

The Epub extractor is based on the [Apache Tika](https://www.gutenberg.org/) toolkit and exploits the structure of the Epub document defined in the [Toc](http://www.idpf.org/accessibility/guidelines/content/nav/toc.php) (Table of Content) file. This implementation allow you to develop more advanced classification policies, compared to the simple classification of the whole text.

For these reasons, beside the usual classification system we have also implemented a new REST API called **/rest/classifyEPubChapters** that provides results of the classification process for each chapter (the top-level section defined in the Toc) of an Epub file.

Here you find part of the classifyEpubChapters response on the "Siddartha by Hermann Hesse" ebook. It is available on the Project Gutenberg website: [http://www.gutenberg.org/ebooks/2500](http://www.gutenberg.org/ebooks/2500). These results are obtained with [DBpedia 3.9](http://wiki.dbpedia.org/Downloads39?show_files=1). 

{
  "@service": "ClassifyEpubChapters",
  "Chapters":   [
        {
      "@toc": "THE SON OF THE BRAHMAN",
      "Resources":       [
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
      "Resources":       [
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
    },
        {
      "@toc": "GOTAMA",
      "Resources":       [
                {
          "@uri": "http://dbpedia.org/resource/Gautama_Buddha",
          "@label": "Gautama Buddha",
          "@title": "Gautama Buddha",
          "@score": "1.5224682",
          "@mergedTypes": "DBpedia:Person#DBpedia:Http://xmlns.com/foaf/0.1/Person#Schema:Person#DBpedia:Agent#",
          "@image": ""
        },
                {
          "@uri": "http://dbpedia.org/resource/Sangha",
          "@label": "Sangha",
          "@title": "Sangha",
          "@score": "1.2267755",
          "@mergedTypes": "",
          "@image": ""
        },
                {
          "@uri": "http://dbpedia.org/resource/Buddhahood",
          "@label": "Buddhahood",
          "@title": "Buddhahood",
          "@score": "1.1338243",
          "@mergedTypes": "",
          "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Mahayanabuddha.jpg/444px-Mahayanabuddha.jpg"
        },
                {
          "@uri": "http://dbpedia.org/resource/Nirvana",
          "@label": "Nirvana",
          "@title": "Nirvana",
          "@score": "1.045",
          "@mergedTypes": "",
          "@image": ""
        },
                {
          "@uri": "http://dbpedia.org/resource/Bodhi",
          "@label": "Bodhi",
          "@title": "Bodhi",
          "@score": "1.2666217",
          "@mergedTypes": "",
          "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Buddha_Meditating_Under_the_Bodhi_Tree%2C_800_C.E.jpg/444px-Buddha_Meditating_Under_the_Bodhi_Tree%2C_800_C.E.jpg"
        },
                {
          "@uri": "http://dbpedia.org/resource/Four_Noble_Truths",
          "@label": "Four Noble Truths",
          "@title": "Four Noble Truths",
          "@score": "1.2657106",
          "@mergedTypes": "",
          "@image": "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Gandharan_-_Expounding_the_Law_-_Walters_2551.jpg/477px-Gandharan_-_Expounding_the_Law_-_Walters_2551.jpg"
        },
                {
          "@uri": "http://dbpedia.org/resource/Enlightenment_in_Buddhism",
          "@label": "Enlightenment in Buddhism",
          "@title": "Enlightenment in Buddhism",
          "@score": "1.2248611",
          "@mergedTypes": "",
          "@image": ""
        }
      ]
    },
	
	*** TRUNCATION ***

As anticipated in the previous example, to test the Epub Classifier we use ebooks from the [Project Gutenberg website](https://www.gutenberg.org/). Nevertheless, we notice that in some ebooks of this repository the Toc file is not well-structured, so it is difficult to perform a classification for each chapter. For these reasons, we combine different techniques to make more robust the process of extraction and classification of text.

Below you find the method to extract text from Epub files:

    private HashMap<String, String> parseEpub(File file) throws IOException, TMFVisibleException {

        LOG.debug("[parseEpub] - BEGIN");

        ZipFile fi = new ZipFile(file);

        for (Enumeration e = fi.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            if (entry.getName().endsWith("ncx")) {
                InputStream tocMaybeDirty = fi.getInputStream(entry);
                Scanner scanner = new Scanner(tocMaybeDirty,"UTF-8").useDelimiter("\\A");
                String theString = scanner.hasNext() ? scanner.next() : "";
                tocMaybeDirty.close();
                scanner.close();

                String res = theString.replaceAll(">[\\s]*?<", "><");

                InputStream toc = new ByteArrayInputStream(res.getBytes(StandardCharsets.UTF_8));

                try {
                    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = dBuilder.parse(toc);
                    toc.close();

                    if (doc.hasChildNodes()) {
                        findNavMap(doc.getChildNodes());
                    }
                }
                catch (Exception ex) {
                    LOG.error("Unable to navigate the TOC");
                }

                removeEmptyTOC(epub);

                //search anchors in links and split
                Set set = epub.entrySet();
                Iterator i = set.iterator();
                while (i.hasNext()){
                    Map.Entry me = (Map.Entry)i.next();
                    if (me.getValue().toString().contains("#")){
                        String[] parts = me.getValue().toString().split("#");
                        String anchor = parts[1];
                        epub.put(me.getKey().toString(), anchor);
                    }
                }
            }
            if (entry.getName().endsWith("opf")) { //manage files because order is important
                InputStream content = fi.getInputStream(entry);

                Scanner scanner = new Scanner(content,"UTF-8").useDelimiter("\\A");
                String contentString = scanner.hasNext() ? scanner.next() : "";
                content.close();
                scanner.close();

                String filenameRegex = "href=\"(.*.htm(|l))\".*media-type=\"application/xhtml";
                Pattern pattern = Pattern.compile(filenameRegex);
                Matcher matcher = pattern.matcher(contentString);

                Integer count = 0;
                while (matcher.find()) {
                    files.put(count, matcher.group(1));
                    count++;
                }
            }
            if (entry.getName().endsWith("html") || entry.getName().endsWith("htm") || entry.getName().endsWith("xhtml")) {
                InputStream htmlFile = fi.getInputStream(entry);

                Scanner scanner = new Scanner(htmlFile,"UTF-8").useDelimiter("\\A");
                String htmlString = scanner.hasNext() ? scanner.next() : "";

                String regex1 = htmlString.replaceAll("^[^_]*?<body>", ""); //remove head
                String regex2 = regex1.replaceAll("</body>.*$", ""); //remove tail
                String htmlCleaned = regex2.replaceAll("<a.*?/>", ""); //anchor with one tag

                String[] bits = entry.getName().split("/");
                String fileName = bits[bits.length-1];

                htmls.put(fileName, htmlCleaned);
            }
        }
        fi.close();
        Integer i;
        for (i = 0; i < files.size(); i++) {
            stringBuilder.append("<p id=\"" + files.get(i) + "\"></p>"); // "anchor" also the heads of each files
            stringBuilder.append(htmls.get(files.get(i)));
        }
        String htmlAll = stringBuilder.toString();

        /* We have all needed files, start to split
           For each link -> made a chunk
           Start from the bottom */
        Metadata metadata = new Metadata();
        Parser parser = new HtmlParser();
        ListIterator<Map.Entry<String, String>> iter = new ArrayList<>(epub.entrySet()).listIterator(epub.size());

        while (iter.hasPrevious()) {
            Map.Entry<String, String> me = iter.previous();
            try {
                ContentHandler contenthandler = new BodyContentHandler(10*htmlAll.length());
                Scanner sc = new Scanner(htmlAll);
                sc.useDelimiter("id=\""+ me.getValue().toString() + "\">");
                htmlAll = sc.next();
                InputStream stream = new ByteArrayInputStream(sc.next().getBytes(StandardCharsets.UTF_8));
                parser.parse(stream, contenthandler, metadata, new ParseContext());
                epub.put(me.getKey().toString(), contenthandler.toString());

            } catch (Exception ex) {
                LOG.error("Unable to parse content for index: "+me.getKey()+", this chapter will be deleted");
                removeChapter(epub, me.getKey().toString());
            }
        }

         /* I remove the Project Gutenberg license chapter from the Map, because it is useless
           for the classification and it generates a Lucene Exception in case of the Italian language
           (the license text is always in English).

           You can use this method in order to remove each chapter that is useless for classifying
           your Epub document. */
        removeChapter(epub, "A Word from Project Gutenberg");
        removeChapterFromString(epub, "End of the Project Gutenberg EBook");
        removeEmptyItems(epub);

        //If the Epub file has a bad structure, I try to use the basic Epub extractor of Tika.
        if (epub.size() == 0) {
            LOG.info("The Epub file has a bad structure. Try to use the Tika extractor");
            epub.put("All text", autoParseAll(file));
        }

        removeEmptyItems(epub);

        if(epub.size() == 0) {
            LOG.error("Unable to extract text from this Epub");
            throw new TMFVisibleException("Unable to extract any text from this Epub.");
        }

        LOG.debug("[parseEpub] - END");

        return epub;
    }
  
