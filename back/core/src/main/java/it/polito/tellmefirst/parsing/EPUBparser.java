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

package it.polito.tellmefirst.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.ScoreDoc;
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

public class EPUBparser {

    static Log LOG = LogFactory.getLog(EPUBparser.class);
    private LinkedHashMap<String, String> epub = new LinkedHashMap<String, String>();
    private StringBuilder stringBuilder = new StringBuilder();
    private LinkedHashMap<Integer, String> files = new LinkedHashMap<Integer, String>();
    private LinkedHashMap<String, String> htmls = new LinkedHashMap<String, String>();

    public LinkedHashMap<String, String> parseEPUB(File file) throws IOException {
        LOG.debug("[parseEPUB] - BEGIN");

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
                //LOG.info("Show list in order");
                //checkMap(files); //show files list in order
            }
            if (entry.getName().endsWith("html") || entry.getName().endsWith("htm") || entry.getName().endsWith("xhtml")) {
                InputStream htmlFile = fi.getInputStream(entry);
                /*Scanner scanner = new Scanner(htmlFile,"UTF-8").useDelimiter("\\A");
                String htmlString = scanner.hasNext() ? scanner.next() : "";
                String[] bits = entry.getName().split("/");
                String fileName = bits[bits.length-1];*/

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
        for (i = 0; i<files.size(); i++) {
            stringBuilder.append("<p id=\"" + files.get(i) + "\"></p>"); // "anchor" also the heads of each files
            stringBuilder.append(htmls.get(files.get(i)));
        }
        String htmlAll = stringBuilder.toString();

        //We have all needed files, start to split
        //For each link -> made a chunk
        //Start from the bottom

        Metadata metadata = new Metadata();
        Parser parser = new HtmlParser();
        ListIterator<Map.Entry<String, String>> iter = new ArrayList<Map.Entry<String, String>>(epub.entrySet()).listIterator(epub.size());

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
            }
        }

        removeEmptyItems(epub);

        LOG.debug("[parseEPUB] - END");
        return epub;
    }

    public String autoParseAll(File file) {
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
            textBody = text.toString();
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

    public ArrayList<ScoreDoc> aggregateChapterResults(LinkedHashMap<String, ScoreDoc[]> tocResults, int numOfTopics) throws IOException {
        LOG.debug("[aggregateChapterResults] - BEGIN");
        ArrayList<ScoreDoc> mergedHitList = new ArrayList<ScoreDoc>();
        for(String key : tocResults.keySet()){
            ArrayList<ScoreDoc> hitList = new ArrayList<ScoreDoc>();
            for (int i = 0; i < numOfTopics; i++){
                hitList.add(tocResults.get(key)[i]);
            }
            LOG.info(key);
            mergedHitList.addAll(hitList);
        }
        LOG.debug("[aggregateChapterResults] - END");
        return mergedHitList;
    }

    private void findNavMap(NodeList nodeList) {
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
    }

    public void getFirstChildElement(Node navMap) {
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
    }

    public void checkMap(LinkedHashMap lhm) {
        Set set = lhm.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            LOG.debug(me.getKey() + ": ");
            LOG.debug(me.getValue());
        }
    }

    public static void removeEmptyItems(LinkedHashMap lhm) {

        Set set = lhm.entrySet();
        Iterator i = set.iterator();
        Pattern pattern = Pattern.compile("\\s"); //check if at least one space

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            Matcher matcher = pattern.matcher(me.getValue().toString());
            if (!matcher.find()) {
                i.remove();
            }
        }
    }

    public static void removeEmptyTOC(LinkedHashMap lhm) {

        Set set = lhm.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            if (me.getValue().toString().equals("")) {
                i.remove();
            }
        }
    }
}

