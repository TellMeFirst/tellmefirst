/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
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

package it.polito.tellmefirst.classify;

import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.lucene.SimpleSearcher;
import it.polito.tellmefirst.lucene.IndexesUtil;
import it.polito.tellmefirst.util.TMFUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import it.polito.tellmefirst.apimanager.ImageManager;
import it.polito.tellmefirst.classify.threads.ClassiThread;
import it.polito.tellmefirst.lodmanager.DBpediaManager;
import it.polito.tellmefirst.parsing.DOCparser;
import it.polito.tellmefirst.parsing.HTMLparser;
import it.polito.tellmefirst.parsing.PDFparser;
import it.polito.tellmefirst.parsing.TXTparser;
import it.polito.tellmefirst.lucene.LuceneManager;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class Classifier {

    LuceneManager contextLuceneManager;
    ImageManager imageManager;
    DBpediaManager dBpediaManager;
    SimpleSearcher searcher;
    static Log LOG = LogFactory.getLog(Classifier.class);

    public Classifier(String lang) {
        LOG.debug("[constructor] - BEGIN");
        try{
            imageManager = new ImageManager();
            if(lang.equals("it")){
                LOG.info("[Initializing italian Classifier...");
                searcher = IndexesUtil.ITALIAN_CORPUS_INDEX_SEARCHER;
                contextLuceneManager = searcher.getLuceneManager();
            }else{
                LOG.info("[Initializing english Classifier...");
                searcher = IndexesUtil.ENGLISH_CORPUS_INDEX_SEARCHER;
                contextLuceneManager = searcher.getLuceneManager();
            }
        }catch (Exception e){
            LOG.error("[constructor] - EXCEPTION: ", e);
        }
        LOG.debug("[constructor] - END");
    }

    public ArrayList<String[]> classify(String inputText, File file, String url, String fileName, int numOfTopics,
                                        String lang) throws TMFVisibleException {
        LOG.debug("[classify] - BEGIN");
        // check if DBpedia endpoints are up
        dBpediaManager = new DBpediaManager();
        if (!lang.equals("italian") && !dBpediaManager.isDBpediaEnglishUp()){
            //comment for local use
            throw new TMFVisibleException("DBpedia English service seems to be down, so TellMeFirs can't work " +
                    "properly. Please try later!");
        } else {
            if (lang.equals("italian") && !dBpediaManager.isDBpediaItalianUp()){
                //comment for local use
                throw new TMFVisibleException("DBpedia Italian service seems to be down, so TellMeFirs can't work" +
                        " properly. Please try later!");
            }
        }
        ArrayList<String[]> result;
        Text text;
        if (inputText != null && !inputText.equals("")){
            text = new Text(inputText);
            result = classifyText(text, numOfTopics, lang);
        } else if (url != null){
            HTMLparser parser = new HTMLparser();
            text = new Text(parser.htmlToTextGoose(url));
            result = classifyText(text, numOfTopics, lang);
        } else if(file != null){
             //text = chooseParser(file, fileName);
            text = new Text ("Test for file parser");
            result = classifyText(text, numOfTopics, lang);
        } else {
            throw new TMFVisibleException("No valid parameters in your request: both 'text' and 'url' and 'file'" +
                    " are null.");
        }
        LOG.debug("[classify] - END");
        return  result;
    }

    public ArrayList<String[]> classifyLongText(Text text, int numOfTopics, String lang) throws InterruptedException,
            IOException {
        LOG.debug("[classifyLongText] - BEGIN");
        ArrayList<String[]> result;
        //no prod
        LOG.debug("[classifyLongText] - We're using as analyzer: "+contextLuceneManager.getLuceneDefaultAnalyzer());
        String longText = text.getText();
        ArrayList<String> pieces = new ArrayList<String>();

        // split long text in smaller parts and call getContextQueryForKBasedDisambiguator() for each one
        int n = 0;
        while(TMFUtils.countWords(longText) > 1000){
            String firstPart = StringUtils.join(longText.split(" "), " ", 0, 1000);
            String secondPart =  StringUtils.join(longText.split(" "), " ", 1000, TMFUtils.countWords(longText));
            pieces.add(firstPart);
            //no prod
            LOG.debug("Piece n°"+n+" analyzing...");
            longText = secondPart;
            if(TMFUtils.countWords(longText) < 300){
                //no prod
                LOG.debug("Final piece contains "+TMFUtils.countWords(longText)+" words. Discarded, because < " +
                        "300 words.");
            }
            else if(TMFUtils.countWords(longText) < 1000){
                //no prod
                LOG.debug("Final piece contains "+TMFUtils.countWords(longText)+" words.");
                pieces.add(longText);
            }
            n++;
        }
        ArrayList<ScoreDoc> mergedHitList = new ArrayList<ScoreDoc>();
        ArrayList<ClassiThread> threadList = new ArrayList<ClassiThread>();
        for(String textPiece : pieces){
            ClassiThread thread = new ClassiThread(contextLuceneManager, searcher, textPiece);
            thread.start();
            threadList.add(thread);
        }
        for (ClassiThread thread : threadList){
            thread.join();
            ScoreDoc[] hits = thread.getHits();
            ArrayList<ScoreDoc> hitList = new ArrayList<ScoreDoc>();
            for(int b=0; b<numOfTopics; b++){
                hitList.add(hits[b]);
            }
            mergedHitList.addAll(hitList);
        }
        HashMap<Integer, Integer> scoreDocCount = new HashMap<Integer, Integer>();
        for (ScoreDoc scoreDoc : mergedHitList) {
            Integer count = scoreDocCount.get(scoreDoc.doc);
            scoreDocCount.put(scoreDoc.doc, (count == null) ? 1 : count + 1);
        }
        HashMap<Integer,Integer> sortedMap = TMFUtils.sortHashMapIntegers(scoreDocCount);
        LinkedHashMap<ScoreDoc, Integer> sortedMapWithScore = new LinkedHashMap<ScoreDoc, Integer>();
        for(int docnum : sortedMap.keySet()){
            Document doc = searcher.getFullDocument(docnum);
            boolean flag = true;
            for(ScoreDoc sdoc : mergedHitList){
                if(flag && sdoc.doc == docnum){
                    sortedMapWithScore.put(sdoc, sortedMap.get(docnum));
                    flag = false;
                }
            }
        }
        ArrayList<ScoreDoc> finalHitsList = sortByRank(sortedMapWithScore);
        ScoreDoc[] hits = new ScoreDoc[finalHitsList.size()];
        for (int i = 0 ; i<finalHitsList.size(); i++){
            hits[i] = finalHitsList.get(i);
        }
        result = classifyCore(hits, numOfTopics, lang);
        LOG.debug("[classifyLongText] - END");
        return result;
    }

    public ArrayList<String[]> classifyShortText(Text text, int numOfTopics, String lang) throws ParseException,
            IOException {
        LOG.debug("[classifyShortText] - BEGIN");
        ArrayList<String[]> result;
        //no prod
        LOG.debug("[classifyLongText] - We're using as analyzer: "+contextLuceneManager.getLuceneDefaultAnalyzer());
        Query query = contextLuceneManager.getQueryForContext(text);
        ScoreDoc[] hits = searcher.getHits(query);
        result = classifyCore(hits, numOfTopics, lang);
        LOG.debug("[classifyShortText] - END");
        return result;
    }

    public ArrayList<String[]> classifyText(Text text, int numOfTopics, String lang) throws TMFVisibleException {
        ArrayList<String[]> result;
        String textString = text.getText();
        int totalNumWords = TMFUtils.countWords(textString);
        //no prod
        LOG.debug("TOTAL WORDS: "+totalNumWords);
        if(totalNumWords>30000){
            throw new TMFVisibleException("This is just a demo. Try with a text containing less than 30.000 words!");
        }
        try{
            if(totalNumWords>1000){
                //no prod
                LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for long texts.");
                result = classifyLongText(text, numOfTopics, lang);
            }   else {
                //no prod
                LOG.debug("Text contains "+totalNumWords+" words. We'll use Classify for short texts.");
                result = classifyShortText(text, numOfTopics, lang);
            }
        }catch (Exception e){
            LOG.error("[classify] - EXCEPTION: ", e);
            throw new TMFVisibleException("Unable to extract topics from specified text.");
        }
        return result;
    }

    public void manageFileClassification(File file, String fileName) throws TMFVisibleException {
        Text text;
        if(fileName.endsWith(".pdf") || fileName.endsWith(".PDF")){
            PDFparser parser = new PDFparser();
            text = new Text(parser.pdfToText(file));
        } else if(fileName.endsWith(".doc") || fileName.endsWith(".DOC")){
            DOCparser parser = new DOCparser();
            text = new Text(parser.docToText(file));
        } else if(fileName.endsWith(".txt") || fileName.endsWith(".TXT")){
            TXTparser parser = new TXTparser();
            text = new Text(parser.txtToText(file));
        } else if(fileName.endsWith(".pub") || fileName.endsWith(".")){
            // ToDO EPub Parser implementation
            text = new Text("Something");
        }
        else {
            throw new TMFVisibleException("File extension not valid: only 'pdf', 'doc' and 'txt' allowed.");
        }
    }

    public ArrayList<String[]> classifyCore(ScoreDoc[] hits, int numOfTopics, String lang) throws IOException {
        LOG.debug("[classifyCore] - BEGIN");
        ArrayList<String[]> result = new ArrayList<String[]>();
        if (hits.length == 0) {
            LOG.error("No results given by Lucene query from Classify!!");
        } else {
            for (int i = 0; i < numOfTopics; i++) {
                String[] arrayOfFields = new String[6];
                Document doc = searcher.getFullDocument(hits[i].doc);
                String uri = "";
                String visLabel = "";
                String title = "";
                String mergedTypes = "";
                String image = "";
                if(lang.equals("italian")){
                    String italianUri = "http://it.dbpedia.org/resource/" + doc.getField("URI").stringValue();
                    // Italian flag, resource without a corresponding in English DBpedia
                    if(doc.getField("SAMEAS") == null){
                        uri = italianUri;
                        title = doc.getField("TITLE").stringValue();
                        visLabel = title.replaceAll("\\(.+?\\)", "").trim();
                        Field[] types = doc.getFields("TYPE");
                        StringBuilder typesString = new StringBuilder();
                        for (Field value : types) {
                            typesString.append(value.stringValue()+"#");
                        }
                        mergedTypes = typesString.toString();
                        if(doc.getField("IMAGE") != null){
                            String dirtyImage = doc.getField("IMAGE").stringValue();
                            // we scrape anyway, even when the image URL is in DBpedia, to have always the right size
                            String[] fileNameSplit = dirtyImage.replace(" ","_").split("/");
                            image = imageManager.scrapeDBpediaImageFromPage("http://it.wikipedia.org/wiki/File:"+
                                    fileNameSplit[fileNameSplit.length-1]);

                        }
                        // Italian flag, resource with a corresponding in English DBpedia
                    } else {
                        uri = doc.getField("SAMEAS").stringValue();
                        title = IndexesUtil.getTitle(uri, "en");
                        visLabel = doc.getField("TITLE").stringValue().replaceAll("\\(.+?\\)", "").trim();;
                        String dirtyImage = IndexesUtil.getImage(uri, "en");
                        // we scrape anyway, even when the image URL is in DBpedia, to have always the right size
                        String[] fileNameSplit = dirtyImage.replace(" ","_").split("/");
                        image = imageManager.scrapeDBpediaImageFromPage("http://en.wikipedia.org/wiki/File:"+
                                fileNameSplit[fileNameSplit.length-1]);
                        ArrayList<String> typesArray = IndexesUtil.getTypes(uri, "en");
                        StringBuilder typesString = new StringBuilder();
                        for (String type : typesArray) {
                            typesString.append(type+"#");
                        }
                        mergedTypes = typesString.toString();
                    }
                    // English flag
                }else{
                    uri= "http://dbpedia.org/resource/" + doc.getField("URI").stringValue();
                    title = doc.getField("TITLE").stringValue();
                    visLabel = title.replaceAll("\\(.+?\\)", "").trim();
                    if(doc.getField("IMAGE") != null){
                        String dirtyImage = doc.getField("IMAGE").stringValue();
                        // we scrape anyway, even when the image URL is in DBpedia, to have always the right size
                        String[] fileNameSplit = dirtyImage.replace(" ","_").split("/");
                        image = imageManager.scrapeDBpediaImageFromPage("http://en.wikipedia.org/wiki/File:"+
                                fileNameSplit[fileNameSplit.length-1]);
                    }
                    Field[] types = doc.getFields("TYPE");
                    StringBuilder typesString = new StringBuilder();
                    for (Field value : types) {
                        typesString.append(value.stringValue()+"#");
                    }
                    mergedTypes = typesString.toString();
                }
                String score = String.valueOf(hits[i].score);
                arrayOfFields[0] = uri;
                arrayOfFields[1] = visLabel;
                arrayOfFields[2] = title;
                arrayOfFields[3] = score;
                arrayOfFields[4] = mergedTypes;
                arrayOfFields[5] = image;
                result.add(arrayOfFields);
            }
        }
        LOG.debug("[classifyCore] - END");
        return result;
    }


    public ArrayList<ScoreDoc> sortByRank(LinkedHashMap<ScoreDoc, Integer> inputList){
        LOG.debug("[sortByRank] - BEGIN");
        ArrayList<ScoreDoc> result = new ArrayList<ScoreDoc>();
        LinkedMap apacheMap = new LinkedMap(inputList);
        for (int i=0; i<apacheMap.size()-1; i++){
            TreeMap<Float, ScoreDoc> treeMap = new TreeMap<Float, ScoreDoc>(Collections.reverseOrder());
            do{ i++;
                treeMap.put(((ScoreDoc)apacheMap.get(i-1)).score,(ScoreDoc)apacheMap.get(i-1) );
            }while (i<apacheMap.size() && apacheMap.getValue(i) == apacheMap.getValue(i-1));
            i--;
            for(Float score : treeMap.keySet()){
                result.add(treeMap.get(score));
            }
        }
        LOG.debug("[sortByRank] - END");
        return result;
    }
}

