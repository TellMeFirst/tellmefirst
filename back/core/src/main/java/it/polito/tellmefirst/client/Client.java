/*
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2014 Giuseppe Futia, Alessio Melandri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.client;

import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.classify.Text;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.lodmanager.DBpediaManager;
import it.polito.tellmefirst.util.TMFUtils;
import it.polito.tellmefirst.jaxrs.ClassifyOutput;
import static java.util.stream.Collectors.toList;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.*;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Map.Entry;

public class Client {

    /* The Client manages different classification policies, according to type of document, the length of text,
       and ad hoc choices for specific needs.

       Currently, the Epub classifier is implemented in the Client class in order to simplify the merge of its
       features in other TellMeFirst forks. In the future the classification policy for Epub files will be
       defined in a different class. */
    static Log LOG = LogFactory.getLog(Client.class);
    private HashMap<String, String> epub = new LinkedHashMap<>();
    private StringBuilder stringBuilder = new StringBuilder();
    private HashMap<Integer, String> files = new LinkedHashMap<>();
    private HashMap<String, String> htmls = new LinkedHashMap<>();
    private Classifier classifier;
    private DBpediaManager dBpediaManager;

    public Client(Classifier c){
        this.classifier = c;
    }

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

    private ArrayList<String[]> classifyEpub(String inputText, File file, String url, String fileName,
                                             int numOfTopics, String lang)
                                                               throws TMFVisibleException {

        LOG.debug("[classifyEpub] - BEGIN");

        dBpediaManager = new DBpediaManager();
        if (!lang.equals("english") && !dBpediaManager.isDBpediaEnglishUp()){
            throw new TMFVisibleException("DBpedia English service seems to be down, so TellMeFirst can't work " +
                    "properly. Please try later!");
        } else {
            if (lang.equals("italian") && !dBpediaManager.isDBpediaItalianUp()){
                throw new TMFVisibleException("DBpedia Italian service seems to be down, so TellMeFirst can't work" +
                        " properly. Please try later!");
            }
        }

        ArrayList<String[]> results;
        results = new ArrayList<>();

        HashMap <String, String> parserResults = new LinkedHashMap<>();
        try {
            parserResults = parseEpub(file);
        } catch (IOException e) {
            LOG.error("[classifyEpub] - EXCEPTION: ", e);
            throw new TMFVisibleException("The Epub parser cannot read the file.");
        }
        HashMap<String, List<ClassifyOutput>> classificationResults = new LinkedHashMap<>();
        Set set = parserResults.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            Text text = new Text((me.getValue().toString()));
            LOG.debug("* Title of the chapter");
            LOG.debug(me.getKey().toString());
            LOG.debug("* Text of the chapter");
            LOG.debug(me.getValue().toString().substring(0, 100));
            String textString = text.getText();
            int totalNumWords = TMFUtils.countWords(textString);
            LOG.debug("TOTAL WORDS: "+totalNumWords);
            try {
                if(totalNumWords>1000){
                    LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for long texts.");
                    ArrayList<String[]> chapterResults = classifier.classifyLongText(text, numOfTopics, lang);
                    classificationResults.put(me.getKey().toString(), jsonAdapter(chapterResults));
                } else {
                    LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for short texts.");
                    ArrayList<String[]> chapterResults = classifier.classifyShortText(text, numOfTopics, lang);
                    classificationResults.put(me.getKey().toString(), jsonAdapter(chapterResults));
                }
            }catch (Exception e){
                LOG.error("[classifyEpub] - EXCEPTION: ", e);
                throw new TMFVisibleException("Unable to extract topics from specified text.");
            }
        }

        ArrayList<ClassifyOutput> classifyOutputListSorted = sortResults(classificationResults);

        for (int k = 0 ; k < numOfTopics; k++) {
            String[] arrayOfFields = new String[6];
            arrayOfFields[0] = classifyOutputListSorted.get(k).getUri();
            arrayOfFields[1] = classifyOutputListSorted.get(k).getLabel();
            arrayOfFields[2] = classifyOutputListSorted.get(k).getTitle();
            arrayOfFields[3] = classifyOutputListSorted.get(k).getScore();
            arrayOfFields[4] = classifyOutputListSorted.get(k).getMergedTypes();
            arrayOfFields[5] = classifyOutputListSorted.get(k).getImage();
            results.add(arrayOfFields);
        }

        LOG.debug("[classifyEpub] - END");

        return results;
    }

    /**
     * Classify each chapter (the top-level section defined in the Toc file) of an Epub document.
     *
     * @param file the input file
     * @param fileName the input filename
     * @param numOfTopics number of topics to be returned
     * @param lang the language of the text to be classified ("italian" or "english")
     * @return A HashMap in which the key is a string with the title of the chapter and the value
     *         is a list of the results of the classification process
     */
    public HashMap <String, ArrayList<String[]>> classifyEPubChapters(File file, String fileName, int numOfTopics,
                                                                            String lang) throws TMFVisibleException, IOException {

        //The classfyEPubChapter method works when the Epub in case of a well-defined structure in the Toc file.
        //Otherwise you can use the usual classify method.

        LOG.debug("[classifyEPubChapters] - BEGIN");

        if(!(fileName.endsWith(".epub") || fileName.endsWith(".EPUB"))){
            throw new TMFVisibleException("File extension not valid: only 'epub' allowed.");
        }
        dBpediaManager = new DBpediaManager();
        if (!lang.equals("english") && !dBpediaManager.isDBpediaEnglishUp()){
            //comment for local use
            throw new TMFVisibleException("DBpedia English service seems to be down, so TellMeFirst can't work " +
                    "properly. Please try later!");
        } else {
            if (lang.equals("italian") && !dBpediaManager.isDBpediaItalianUp()){
                //comment for local use
                throw new TMFVisibleException("DBpedia Italian service seems to be down, so TellMeFirst can't work" +
                        " properly. Please try later!");
            }
        }
        HashMap <String, ArrayList<String[]>> results = new LinkedHashMap<>();
        HashMap<String, String> parserResults = new LinkedHashMap<String, String>();
        parserResults = parseEpub(file);
        Set set = parserResults.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            Text text = new Text((me.getValue().toString()));
            String textString = text.getText();
            int totalNumWords = TMFUtils.countWords(textString);
            LOG.debug("TOTAL WORDS: "+totalNumWords);
            try {
                if(totalNumWords>1000){
                    LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for long texts.");
                    ArrayList<String[]> classificationResults = classifier.classifyLongText(text, numOfTopics, lang);
                    results.put(me.getKey().toString(), classificationResults);
                } else {
                    LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for short texts.");
                    ArrayList<String[]> classificationResults = classifier.classifyShortText(text, numOfTopics, lang);
                    results.put(me.getKey().toString(), classificationResults);
                }
            }catch (Exception e){
                LOG.error("[classifyEpub] - EXCEPTION: ", e);
                throw new TMFVisibleException("Unable to extract topics from specified text.");
            }
        }

        LOG.debug("[classifyEPubChapters] - END");

        return results;
    }


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

        //If the Epub file has a bad structure, I try to use the epub extractor of Tika.
        if (epub.size() == 0) {
            LOG.info("The Epub file has a bad structure. Try to use the Tika extractor");
            epub.put("All text", autoParseAll(file));
        }

        /* I remove the Project Gutenberg license chapter from the Map, because it is useless
           for the classification and it generates a Lucene Exception in case of the Italian language
           (the license text is always in English).

           You can use this function in order to remove each chapter that is useless for classifying
           your Epub document. */
        removeChapter(epub, "A Word from Project Gutenberg");
        removeChapterFromString(epub, "End of the Project Gutenberg EBook");
        removeEmptyItems(epub);

        LOG.debug("[parseEpub] - END");
        return epub;
    }

    private String autoParseAll(File file) {

        InputStream is = null;
        String textBody = "";
        try {
            InputStream input = new FileInputStream(file);
            ContentHandler text = new BodyContentHandler(10*1024*1024);
            LinkContentHandler links = new LinkContentHandler();
            ContentHandler handler = new TeeContentHandler(links,text);
            Metadata metadata = new Metadata();
            EpubParser parser2 = new EpubParser();
            ParseContext context = new ParseContext();
            parser2.parse(input,handler,metadata,context);
            textBody = text.toString().replaceAll(">[\\s]*?<", "><");;
            LOG.debug("Body: " + textBody); //all text in one
        }
        catch (Exception el) {
            el.printStackTrace();
        }
        finally {
            if (is != null)
                IOUtils.closeQuietly(is);
        }
        return textBody;
    }


    private void findNavMap(NodeList nodeList) {

        LOG.debug("[findNavMap] - BEGIN");

        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                if (tempNode.getNodeName().equals("navMap")) {
                    getFirstChildElement(tempNode);
                    break;
                }
                if (tempNode.hasChildNodes()) {
                    findNavMap(tempNode.getChildNodes());
                }
            }
        }

        LOG.debug("[findNavMap] - END");
    }

    private void getFirstChildElement(Node navMap) {

        LOG.debug("[getFirstChildElement] - BEGIN");

        Node navPoint = navMap.getFirstChild();
        while (navPoint != null) {
            if (navPoint.getNodeType() == Node.ELEMENT_NODE) {
                Node navLabel = navPoint.getFirstChild();
                Node text = navLabel.getFirstChild();
                String title = text.getTextContent().toString();

                Node content = navLabel.getNextSibling();
                String link = content.getAttributes().getNamedItem("src").getNodeValue().toString();

                epub.put(title, link);
            }
            navPoint = navPoint.getNextSibling();
        }

        LOG.debug("[getFirstChildElement] - END");
    }

    private void removeEmptyItems(HashMap lhm) {

        LOG.debug("[removeEmptyItems] - BEGIN");

        Set set = lhm.entrySet();
        Iterator i = set.iterator();
        Pattern pattern = Pattern.compile("([a-zA-Z0-9]{1,}\\s){15,}"); //check if at least 15 words

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            Matcher matcher = pattern.matcher(me.getValue().toString());
            if (!matcher.find()) {
                i.remove();
            }
        }

        LOG.debug("[removeEmptyItems] - END");
    }

    private void removeEmptyTOC(HashMap lhm) {

        LOG.debug("[removeEmptyTOC] - BEGIN");

        Set set = lhm.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            if (me.getValue().toString().equals("")) {
                i.remove();
            }
        }

        LOG.debug("[removeEmptyTOC] - END");
    }

    private void removeChapter(HashMap lhm, String chapterTitle) {

        LOG.debug("[removeChapter] - BEGIN");

        Set set = lhm.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            if (me.getKey().toString().equals(chapterTitle)) {
                i.remove();
            }
        }

        LOG.debug("[removeChapter] - END");

    }

    private void removeChapterFromString(HashMap lhm, String uselessText) {

        LOG.debug("[removeChapter] - BEGIN");

        Set set = lhm.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            if (me.getValue().toString().contains(uselessText)) {
                i.remove();
            }
        }

        LOG.debug("[removeChapter] - END");

    }


    public ArrayList<ClassifyOutput> sortResults(HashMap<String, List<ClassifyOutput>> classifiedChapters) {

        LOG.debug("[sortOccurrences] - BEGIN");

        HashMap<String, Integer> classifyOutputOcc = new LinkedHashMap<>();
        Set set = classifiedChapters.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            List<ClassifyOutput> classifyOutputList = (List<ClassifyOutput>) me.getValue();

            for (ClassifyOutput classifyOutput: classifyOutputList) {
                if (classifyOutputOcc.get(classifyOutput.getUri()) == null) {
                    classifyOutputOcc.put(classifyOutput.getUri(), 1);
                }
                else {
                    Integer oldValue = classifyOutputOcc.get(classifyOutput.getUri());
                    classifyOutputOcc.put(classifyOutput.getUri(), oldValue + 1);
                }
            }
        }

        HashMap<String, Integer> sortedMapByOcc = sortMapByValues(classifyOutputOcc, false);
        HashMap<ClassifyOutput, Integer> sortedMapWithScore = createMapWithScore(sortedMapByOcc, classifiedChapters);
        ArrayList<ClassifyOutput> classifyOutputList = sortByRank(sortedMapWithScore);

        LOG.debug("[sortOccurrences] - END");
        return classifyOutputList;
    }

    private List<ClassifyOutput> jsonAdapter(List<String[]> list) {

        /* The TellMeFirst legacy instance of the Politecnico doesn't use the wikilink,
           so I comment this line "output.setWikilink(strings[6])", in order
           to avoid the following exception: java.lang.ArrayIndexOutOfBoundsException */
        return list.stream().map(strings -> {
            ClassifyOutput output = new ClassifyOutput();
            output.setUri(strings[0]);
            output.setLabel(strings[1]);
            output.setTitle(strings[2]);
            output.setScore(strings[3]);
            output.setMergedTypes(strings[4]);
            output.setImage(strings[5]);
            // output.setWikilink(strings[6]);
            return output;
        }).collect(toList());
    }

    private HashMap<String, Integer> sortMapByValues(Map<String, Integer> unsortMap, final boolean order) {

        LOG.debug("[sortMapByValues] - BEGIN");

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                               Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                }
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        LOG.debug("[sortMapByValues] - END");

        return sortedMap;
    }

    private HashMap<ClassifyOutput, Integer> createMapWithScore(HashMap<String, Integer> sortedMapByOcc,
                                                                      HashMap<String, List<ClassifyOutput>>
                                                                              classifiedChapters) {

        LOG.debug("[createMapWithScore] - BEGIN");

        HashMap<ClassifyOutput, Integer> sortedMapWithScore = new LinkedHashMap<>();
        ArrayList<ClassifyOutput> classifyOutputList = new ArrayList<>();

        for (Entry<String, List<ClassifyOutput>> chapterEntry :  classifiedChapters.entrySet()) {
            for (int i = 0; i < chapterEntry.getValue().size(); i++) {
                classifyOutputList.add(chapterEntry.getValue().get(i));
            }
        }

        for (Entry<String, Integer> sortedMapEntry : sortedMapByOcc.entrySet()) {
            boolean flag = true;
            for (int k = 0; k < classifyOutputList.size(); k++) {
                if(flag && sortedMapEntry.getKey() == classifyOutputList.get(k).getUri()) {
                    sortedMapWithScore.put(classifyOutputList.get(k), sortedMapEntry.getValue());
                    flag = false;
                }
            }
        }

        LOG.debug("[createMapWithScore] - END");

        return sortedMapWithScore;
    }

    public ArrayList<ClassifyOutput> sortByRank(HashMap<ClassifyOutput, Integer> inputList){

        LOG.debug("[sortByRank] - BEGIN");

        ArrayList<ClassifyOutput> result = new ArrayList<>();
        LinkedMap apacheMap = new LinkedMap(inputList);
        for (int i = 0; i< apacheMap.size()-1; i++){
            TreeMap<Float, ClassifyOutput> treeMap = new TreeMap<>(Collections.reverseOrder());
            do{ i++;
                treeMap.put(Float.valueOf(((ClassifyOutput)apacheMap.get(i-1)).getScore()),(ClassifyOutput)apacheMap.get(i-1) );
            }while (i<apacheMap.size() && apacheMap.getValue(i) == apacheMap.getValue(i-1));
            i--;
            for(Float score : treeMap.keySet()){
                result.add(treeMap.get(score));
            }
        }

        LOG.debug("[sortByRank] - END");
        return result;
    }

    private void printMap(Map<String, Integer> map) {

        for (Entry<String, Integer> entry : map.entrySet()) {
            LOG.info("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

    private void printClassifyOutputMap(Map<ClassifyOutput, Integer> map) {

        for (Entry<ClassifyOutput, Integer> entry : map.entrySet()) {
            LOG.info("Key : " + entry.getKey().getUri() + " Value : " + entry.getValue());
        }
    }

    private void printEpub(Map<String, String> map) {

        for (Entry<String, String> entry : map.entrySet()) {
            LOG.info("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }

    }

}
