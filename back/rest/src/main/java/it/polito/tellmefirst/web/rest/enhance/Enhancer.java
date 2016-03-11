/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 - 2015 Federico Cairo, Giuseppe Futia, Federico Benedetto, Alessio Melandri
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

package it.polito.tellmefirst.web.rest.enhance;

import it.polito.tellmefirst.enhancer.BBCEnhancer;
import it.polito.tellmefirst.enhancer.NYTimesEnhancer;
import it.polito.tellmefirst.lucene.KBIndexSearcher;
import it.polito.tellmefirst.web.rest.apimanager.*;
import it.polito.tellmefirst.web.rest.lodmanager.*;
import it.polito.tellmefirst.apimanager.NYTimesSearcher;
import it.polito.tellmefirst.lucene.SimpleSearcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Enhancer {

    private RestManager restManager;
    private DBpediaManager dBpediaManager;
    private NYTimesSearcher nyTimesSearcher;
    private VideoManager videoManager;
    private ArrayList<String> typesWhiteList;
    private SimpleSearcher italianSearcher;
    private SimpleSearcher englishSearcher;
    private KBIndexSearcher kbItalianSearcher;
    private KBIndexSearcher kbEnglishSearcher;
    static Log LOG = LogFactory.getLog(Enhancer.class);
    public final static String DEFAULT_IMAGE = "http://tellmefirst.polito.it/images/default_img.jpg";

    /**
     * Instantiate the enhancer taking as input all indexes involved at the enrichment phase.
     * @param is Italian Corpus Index Searcher.
     * @param es English Corpus Index Searcher.
     * @param kbIt Italian Knowledge Base Index Searcher
     * @param kbEn English Knowledge Base Index Searcher
     *
     * To improve when you create an enhancement git module. In the future, if we find better
     * solutions to get images, we will remove Knowledge Base Indexes to get entities related
     * to a specific URI.
     *
     * We also should improve how to manage language.
     *
     * @since 3.0.0.0.
     */
    public Enhancer(SimpleSearcher is, SimpleSearcher es, KBIndexSearcher kbIt, KBIndexSearcher kbEn) {
        LOG.debug("[constructor] - BEGIN");
        typesWhiteList = createTypesWhiteList();
        restManager = new RestManager();
        dBpediaManager = new DBpediaManager();
        nyTimesSearcher = new NYTimesSearcher();
        videoManager = new VideoManager();
        italianSearcher = is;
        englishSearcher = es;
        kbItalianSearcher = kbIt;
        kbEnglishSearcher = kbEn;
        LOG.debug("[constructor] - END");
    }

    public String getImageFromMediaWiki(String uri, String label) {
        LOG.debug("[getImageFromMediaWiki] - BEGIN");
        String result = "";
        String imageFileName = "";
        try {
            String lang = (uri.startsWith("http://dbpedia")) ? "en" : "it";

            String filePageURL = "https://"+lang+".wikipedia.org/wiki/Special:Redirect/file/";
            String commonsFilePageURL = "https://commons.wikimedia.org/wiki/Special:Redirect/file/";

            String queryStart = "https://"+lang+".wikipedia.org/w/api.php?action=query&prop=pageimages&titles=";
            String queryEnd = "&format=xml";
            String query = queryStart + label.replace(" ", "+") + queryEnd;

            LOG.debug("Call to Wikimedia Commons service for the resource "+uri+": "+query);
            String xml = restManager.getStringFromAPI(query);
            Document doc = Jsoup.parse(xml);
            Elements elementsFound = doc.getElementsByTag("page");
            imageFileName = elementsFound.attr("pageimage");

            if(imageFileName == "") {
                LOG.debug("No images at all from Wikipedia page "+uri+". We'll search on Wikidata.");

                String findQidStart = "https://wikidata.org/w/api.php?action=wbgetentities&format=xml&sites="+lang+"wiki&titles=";
                String findQidEnd = "&props=info&format=xml";
                String findQid = findQidStart + label.replace(" ", "+") + findQidEnd;

                LOG.debug("Call to Wikimedia Commons service for the resource "+uri+": "+findQid);
                xml = restManager.getStringFromAPI(findQid);
                doc = Jsoup.parse(xml);
                elementsFound = doc.getElementsByTag("entity");
                String Qid = elementsFound.attr("title");

                //XXX weak API but is the state of art; waiting for a better one https://phabricator.wikimedia.org/T95026
                findQidStart = "https://www.wikidata.org/w/api.php?action=query&prop=images&titles=";
                findQidEnd = "&format=xml";
                findQid = findQidStart + Qid + findQidEnd;

                LOG.debug("Call to Wikimedia Commons service for the resource "+uri+": "+findQid);
                xml = restManager.getStringFromAPI(findQid);
                doc = Jsoup.parse(xml);
                elementsFound = doc.getElementsByTag("im");
                imageFileName = elementsFound.attr("title").replace("File:","");

                if(imageFileName == "") {
                    LOG.debug("[getImageFromMediaWiki] - END");
                    return DEFAULT_IMAGE;
                } else {
                    LOG.debug("[getImageFromMediaWiki] - END");
                    return commonsFilePageURL + imageFileName;
                }
            } else {
                LOG.debug("[getImageFromMediaWiki] - END");
                return filePageURL + imageFileName;
            }
        } catch (Exception e) {
            LOG.error("[getImageFromMediaWiki] - EXCEPTION: ", e);
        }
        return DEFAULT_IMAGE;
    }

    public String[] getCoordinatesFromDBpedia(String uri){
        LOG.debug("[getCoordinatesFromDBpedia] - BEGIN");
        String[] result = dBpediaManager.getCoordinatesForAPlace(uri);
        LOG.debug("[getCoordinatesFromDBpedia] - END");
        return result;
    }

    public String getNewsFromNYT(String uri) {
        LOG.debug("[getNewsFromNYT] - BEGIN");
        String result;
        NYTimesEnhancer nyTimesEnhancer = new NYTimesEnhancer();
        result = nyTimesEnhancer.getNewsFromNYTimes(uri);
        if(result.equals("")) {
            result = "{\"offset\" : \"0\" , \"results\" : []  , \"total\" : 0}";
        }
        LOG.debug("[getNewsFromNYT] - END");
        return result;
    }

    public String getNewsFromBBC(String uri) throws IOException {
        LOG.debug("[getNewsFromBBC] - BEGIN");
        String result;
        BBCEnhancer bbcEnhancer = new BBCEnhancer();
        String API_KEY = bbcEnhancer.getPropValues();
        String URL = bbcEnhancer.createURL(uri, API_KEY);
        result = bbcEnhancer.getResultFromAPI(URL, "application/json");
        LOG.debug("[getNewsFromBBC] - END");
        return result;
    }

    public String getVideoFromYouTube(String uri, String label){
        LOG.debug("[getVideoFromYouTube] - BEGIN");
        String result = "";
        String resultFromApi = "";
        String mergedTypes;
        boolean interestingType = false;
        try{
            //this method works only with english resources, but it's ok because resources contained only in DBpedia Italiana
            // are tricky when you try retrieving a video from Youtube
            ArrayList<String> typesArray = dBpediaManager.getTypes(uri);
            StringBuilder sb = new StringBuilder();
            for(String type : typesArray){
                sb.append(type);
            }
            mergedTypes = sb.toString();
            for(String goodType : typesWhiteList){
                if(mergedTypes.contains(goodType)){
                    interestingType = true;
                }
            }
            if(interestingType){
                if(mergedTypes.contains("http://dbpedia.org/ontology/MusicalWork") && uri.startsWith("http://dbpedia")){
                    String artist = dBpediaManager.getArtistFromEnglishDBpedia(uri);
                    resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                            +artist.replace(" ", "+")+"+"+label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                }else if(mergedTypes.contains("http://dbpedia.org/ontology/MusicalWork") && uri.startsWith("http://it.dbpedia")){
                    String artist = dBpediaManager.getArtistFromItalianDBpedia(uri);
                    resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                            +artist.replace(" ", "+")+"+"+label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                }else if(mergedTypes.contains("http://dbpedia.org/ontology/Athlete") && uri.startsWith("http://dbpedia")){
                    if(!dBpediaManager.getNytUri(uri).equals("")){
                        resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                                +label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                    }
                }
                else if(mergedTypes.contains("http://dbpedia.org/ontology/Band")){
                    if(uri.startsWith("http://dbpedia")){
                        resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                                +"band+"+label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                    } else {
                        resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                                +"gruppo+"+label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                    }
                }
                else{
                    resultFromApi = restManager.getStringFromAPI("https://gdata.youtube.com/feeds/api/videos?q="
                            +label.replace(" ","+")+"&start-index=1&max-results=1&v=2&hd=true");
                }
                result = "http://youtu.be/"+videoManager.extractVideoIdFromResult(resultFromApi);
            }

        }catch (Exception e){
            LOG.error("[getVideoFromYouTube] - EXCEPTION: ", e);
        }
        LOG.debug("[getVideoFromYouTube] - END");
        return result;
    }

    public String getTitleFromDBpedia(String uri, String lang) throws IOException {
        LOG.debug("[getTitleFromDBpedia] - BEGIN");
        String result = "";
        if(lang.equals("italian") && uri.startsWith("http://dbpedia")){
            String itaUri = italianSearcher.getSameAsFromEngToIta(uri);
            if(!itaUri.equals("")){
                result = italianSearcher.getTitle(itaUri);
            }
        }
        LOG.debug("[getTitleFromDBpedia] - END");
        return result;
    }

    public String getAbstractFromDBpedia(String uri, String lang)  {
        LOG.debug("[getAbstractFromDBpedia] - BEGIN");
        String result;
        result = dBpediaManager.getAbstract(uri, lang);
        // call twice to prevent DBpedia endpoint malfunctions
        if (result.equals("")){
            result = dBpediaManager.getAbstract(uri, lang);
        }
        LOG.debug("[getAbstractFromDBpedia] - END");
        return result;
    }

    private ArrayList<String> createTypesWhiteList(){
        ArrayList<String> types = new ArrayList<String>();
        types.add("http://dbpedia.org/ontology/Actor");
        types.add("http://dbpedia.org/ontology/Activity");
        types.add("http://dbpedia.org/ontology/Band");
        types.add("http://dbpedia.org/ontology/Artist");
        types.add("http://dbpedia.org/ontology/Athlete");
        types.add("http://dbpedia.org/ontology/MusicalWork");
        types.add("http://dbpedia.org/ontology/Politician");
        types.add("http://umbel.org/umbel/rc/Actor");
        types.add("http://umbel.org/umbel/rc/Artist");
        types.add("http://umbel.org/umbel/rc/Politician");
        return types;
    }
}
