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

package it.polito.tellmefirst.enhance;

import com.aliasi.spell.JaroWinklerDistance;
import it.polito.tellmefirst.apimanager.ImageManager;
import it.polito.tellmefirst.apimanager.VideoManager;
import it.polito.tellmefirst.lodmanager.DBpediaManager;
import it.polito.tellmefirst.lodmanager.NewYorkTimesLODManager;
import it.polito.tellmefirst.apimanager.RestManager;
import it.polito.tellmefirst.lucene.IndexesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class Enhancer {

    private RestManager restManager;
    private DBpediaManager dBpediaManager;
    private NewYorkTimesLODManager nytManager;
    private ImageManager imageManager;
    private VideoManager videoManager;
    private ArrayList<String> badWikiImages;
    private ArrayList<String> typesWhiteList;
    static Log LOG = LogFactory.getLog(Enhancer.class);
    public final static String DEFAULT_IMAGE = "http://tellmefirst.polito.it/images/default_img.jpg";


    public Enhancer() {
        LOG.debug("[constructor] - BEGIN");
        badWikiImages = createBadImagesList();
        typesWhiteList = createTypesWhiteList();
        restManager = new RestManager();
        dBpediaManager = new DBpediaManager();
        nytManager = new NewYorkTimesLODManager();
        imageManager = new ImageManager();
        videoManager = new VideoManager();
        LOG.debug("[constructor] - END");
    }

    // TODO: this recursive stuff is quite tortuous, try simplifying
    public String getImageFromMediaWiki(String uri, String label, ArrayList<String> oldResults) {
        LOG.debug("[getImageFromMediaWiki] - BEGIN");
        String result = "";
        try{
            String lang = (uri.startsWith("http://dbpedia")) ? "en" : "it";
            String dbpediaPrefix = (uri.startsWith("http://dbpedia")) ? "http://dbpedia.org/resource/" :
                    "http://it.dbpedia.org/resource/";
            String queryStart = "http://"+lang+".wikipedia.org/w/api.php?action=query&titles=";
            String filePageURL = "http://"+lang+".wikipedia.org/wiki/File:";

            String queryEnd = "&prop=images&format=xml";
            String query = queryStart + label.replace(" ", "+") + queryEnd;
            //no prod
            LOG.debug("Call to Wikimedia Commons service for the resource "+uri+": "+query);
            String xml = restManager.getStringFromAPI(query);
            Document doc = Jsoup.parse(xml);

            Elements elementsFound = doc.getElementsByTag("im");

            if(elementsFound == null || elementsFound.size() == 0){
                //no prod
                LOG.debug("No images at all from Wikimedia for the resource "+uri+". We'll search for its BOC.");
                ArrayList<String> bagOfConcepts = IndexesUtil.getBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                if (bagOfConcepts.size() == 0 ||(bagOfConcepts.size() == 1 && bagOfConcepts.get(0).equals(""))) {
                    //no prod
                    LOG.debug("No concepts retrieved from the BOC of "+uri+". We'll search for its residual BOC.");
                    bagOfConcepts = IndexesUtil.getResidualBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                }
                if(bagOfConcepts.size() == 0){
                    LOG.debug("No concepts in the residual BOC of: "+uri+". We'll return default image.");
                    LOG.debug("[getImageFromMediaWiki] - END");
                    return DEFAULT_IMAGE;
                }
                // this is for avoiding a circular search between related concepts
                else if(!oldResults.contains(bagOfConcepts.get(0))){
                    String conceptUri = bagOfConcepts.get(0);
                    // no prod
                    LOG.debug("Ok, we'll search an image for the concept "+conceptUri+" (related to the URI "+uri+")");
                    oldResults.add(conceptUri);
                    String conceptLabel = IndexesUtil.getTitle(conceptUri, lang);
                    // recursive use of getImageFromMediaWiki()
                    return getImageFromMediaWiki(dbpediaPrefix + conceptUri, conceptLabel, oldResults);
                } else{
                    // no prod
                    LOG.debug("We found again the concept "+bagOfConcepts.get(0)+". We'll return default image.");
                    LOG.debug("[getImageFromMediaWiki] - END");
                    return DEFAULT_IMAGE;
                }
            }  else {
                ArrayList<String> imagesFound = new ArrayList<String>();
                for (Element e : elementsFound){
                    if(!e.attr("title").contains(".ogg")){
                        imagesFound.add(e.attr("title").replace("File:",""));
                    }
                }
                if(imagesFound.size() == 0){
                    //no prod
                    LOG.debug("No good images from Wikimedia for the resource "+uri+". We'll search for its BOC.");
                    ArrayList<String> bagOfConcepts = IndexesUtil.getBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                    if (bagOfConcepts.size() == 0 ||(bagOfConcepts.size() == 1 && bagOfConcepts.get(0).equals(""))) {
                        //no prod
                        LOG.debug("No concepts retrieved from the BOC of "+uri+". We'll search for its residual BOC.");
                        bagOfConcepts = IndexesUtil.getResidualBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                    }
                    if(bagOfConcepts.size() == 0){
                        LOG.debug("No concepts in the residual BOC of: "+uri+". We'll return default image.");
                        LOG.debug("[getImageFromMediaWiki] - END");
                        return DEFAULT_IMAGE;
                    }
                    // this is for avoiding a circular search between related concepts
                    else if(!oldResults.contains(bagOfConcepts.get(0))){
                        String conceptUri = bagOfConcepts.get(0);
                        // no prod
                        LOG.debug("Ok, we'll search an image for the concept "+conceptUri+" (related to the URI "+uri+")");
                        oldResults.add(conceptUri);
                        String conceptLabel = IndexesUtil.getTitle(conceptUri, lang);
                        // recursive use of getImageFromMediaWiki()
                        return getImageFromMediaWiki(dbpediaPrefix + conceptUri, conceptLabel, oldResults);
                    }else {
                        // no prod
                        LOG.debug("We found again the concept "+bagOfConcepts.get(0)+"(related to the URI "+uri+"). " +
                                "We'll return default image.");
                        LOG.debug("[getImageFromMediaWiki] - END");
                        return DEFAULT_IMAGE;
                    }
                } else {
                    TreeMap<Double,String> sortedMap = new TreeMap<Double, String>();
                    JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
                    //no prod
                    LOG.debug("Images available from Wikimedia for the resource "+uri+": ");
                    for (String image : imagesFound){
                        sortedMap.put(jaroWinklerDistance.distance(label,image),image);
                        //no prod
                        LOG.debug("* IMAGE: "+image+" - DISTANCE: "+jaroWinklerDistance.distance(label,image));
                    }
                    while (sortedMap.size()!=0 &&
                            (sortedMap.firstEntry().getValue().endsWith(".png")
                                    || sortedMap.firstEntry().getValue().endsWith(".svg")
                                    || sortedMap.firstEntry().getValue().endsWith(".gif"))){
                        String imageName = sortedMap.firstEntry().getValue();
                        // this is a bit empirical, but to discard all .svg for preventing template images seems too prudent
                        if(badWikiImages.contains(imageName) || imageName.startsWith("Flag of ") ||
                                imageName.endsWith(" flag.svg")){
                            sortedMap.remove(sortedMap.firstKey());
                        }else{
                            String imagePageURL = filePageURL + imageName.replace(" ", "_");
                            int[] scrapedImageSize = imageManager.scrapeImageSizeFromPage(imagePageURL);
                            // we don't want smaller images
                            if(scrapedImageSize[0]< 150 && scrapedImageSize[1]< 150){
                                sortedMap.remove(sortedMap.firstKey());
                            } else break;
                        }
                    }
                    if(sortedMap.size()!=0){
                        String imageName = sortedMap.firstEntry().getValue();
                        String imagePageURL = filePageURL + imageName.replace(" ", "_");
                        //no prod
                        LOG.debug("Winning image for "+uri+": "+imagePageURL+". Now try scraping it.");
                        String scrapedImage = imageManager.scrapeImageFromPage(imagePageURL);
                        result = "http:" + scrapedImage;
                        LOG.debug("Final image for "+uri+": "+result);
                    }else {
                        //no prod
                        LOG.debug("After filtering, there are not good images for the resource "+uri+
                                ". We'll search for its BOC.");
                        ArrayList<String> bagOfConcepts = IndexesUtil.getBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                        if (bagOfConcepts.size() == 0 ||(bagOfConcepts.size() == 1 && bagOfConcepts.get(0).equals(""))) {
                            //no prod
                            LOG.debug("No concepts retrieved from the BOC of "+uri+
                                    ". We'll search for its residual BOC.");
                            bagOfConcepts = IndexesUtil.getResidualBagOfConcepts(uri.replace(dbpediaPrefix, ""), lang);
                        }
                        if(bagOfConcepts.size() == 0){
                            LOG.debug("No concepts in the residual BOC of: "+uri+". We'll return default image.");
                            LOG.debug("[getImageFromMediaWiki] - END");
                            return DEFAULT_IMAGE;
                        }
                        // this is for avoiding a circular search between related concepts
                        else if(!oldResults.contains(bagOfConcepts.get(0))){
                            String conceptUri = bagOfConcepts.get(0);
                            // no prod
                            LOG.debug("Ok, we'll search an image for the concept "+conceptUri+" (related to the URI "+uri+")");
                            oldResults.add(conceptUri);
                            String conceptLabel = IndexesUtil.getTitle(conceptUri, lang);
                            // recursive use of getImageFromMediaWiki()
                            return getImageFromMediaWiki(dbpediaPrefix + conceptUri, conceptLabel, oldResults);
                        }else {
                            // no prod
                            LOG.debug("We found again the concept "+bagOfConcepts.get(0)+"(related to the URI "+uri+
                                    "). We'll return default image.");
                            LOG.debug("[getImageFromMediaWiki] - END");
                            return DEFAULT_IMAGE;
                        }
                    }
                    LOG.debug("[getImageFromMediaWiki] - END");
                    return result;
                }
            }
        }catch (Exception e){
            LOG.error("[getImageFromMediaWiki] - EXCEPTION: ", e);
        } finally {
            LOG.debug("[getImageFromMediaWiki] - END");
        }
        return result;
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
        String nytUri = dBpediaManager.getNytUri(uri);
        if(nytUri.equals("")){
            result = "{\"offset\" : \"0\" , \"results\" : []  , \"total\" : 0}";
        } else {
            String search = nytManager.getSearchApiQuery(nytUri);
            result = restManager.getStringFromAPI(search);
        }
        LOG.debug("[getNewsFromNYT] - END");
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

    public String getTitleFromDBpedia(String uri, String lang) {
        LOG.debug("[getTitleFromDBpedia] - BEGIN");
        String result = "";
        if(lang.equals("italian") && uri.startsWith("http://dbpedia")){
            String itaUri = IndexesUtil.getSameAsFromEngToIta(uri);
            if(!itaUri.equals("")){
                result = IndexesUtil.getTitle(itaUri, "it");
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

    // if there's not a rule for template images in Wikipedia, this is the only way so far...
    private ArrayList<String> createBadImagesList(){
        ArrayList<String> badWords = new ArrayList<String>();
        badWords.add("Question book-new.svg");
        badWords.add("Wikibooks-logo-en-noslogan.svg");
        badWords.add("Wiktionary-logo-en.svg");
        badWords.add("Translation arrow.svg");
        badWords.add("Local-important.svg");
        badWords.add("Symbol list class.svg");
        badWords.add("Commons-logo.svg");
        badWords.add("Ambox globe content.svg");
        badWords.add("Monitor padlock.svg");
        badWords.add("Wikisource-logo.svg");
        badWords.add("Wiktionary_small.svg");
        // TODO: test this one!
        badWords.add("Nuvola mimetypes charnotfound.PNG");
        return badWords;
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
