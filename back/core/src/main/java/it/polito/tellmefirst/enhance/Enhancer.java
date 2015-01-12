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

import static it.polito.tellmefirst.dao.DAOFactory.getDAO;
import static it.polito.tellmefirst.util.TMFUtils.filter;
import static it.polito.tellmefirst.util.TMFUtils.getWikiURL;
import static it.polito.tellmefirst.util.TMFUtils.processWikiFileLabelForHashComputation;
import static it.polito.tellmefirst.util.TMFVariables.DEFAULT_IMAGE;
import static org.apache.commons.lang.StringUtils.isEmpty;
import it.polito.tellmefirst.apimanager.ImageManager;
import it.polito.tellmefirst.apimanager.RestManager;
import it.polito.tellmefirst.apimanager.VideoManager;
import it.polito.tellmefirst.dao.WikiDAO;
import it.polito.tellmefirst.lodmanager.DBpediaManager;
import it.polito.tellmefirst.lodmanager.NewYorkTimesLODManager;
import it.polito.tellmefirst.lucene.IndexesUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aliasi.spell.JaroWinklerDistance;

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
    
    //XXX should be on REST module
    public String getImageFromMediaWiki2(String searchText){
    	LOG.debug("Looking images for label => "+searchText);
    	WikiDAO wikidao = getDAO(WikiDAO.class);
    	List<String> fileLabel = wikidao.getFileLabels(searchText);
    	filter(fileLabel, ".ogg", "Flag of ",".svg", ".gif");
    	String imgLabel = mostSimilar(fileLabel, searchText);
    	LOG.debug("imgLabelWinner - "+imgLabel);
    	if(isEmpty(imgLabel))
    		return DEFAULT_IMAGE;
    	String url = getWikiURL(processWikiFileLabelForHashComputation(imgLabel));
    	LOG.debug("hash computed URL => "+ url);
    	if(!wikidao.existsImage(url)){
    		url = url.replace("/commons/", "/en/");
    		if(!wikidao.existsImage(url))
    			return DEFAULT_IMAGE;
    	}
    	return url;
    }
    
    public String mostSimilar(List<String> images, String label){
    	Map<Double,String> sortedMap = new TreeMap<Double, String>();
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        for (String image : images){
            sortedMap.put(jaroWinklerDistance.distance(label,image),image);
            LOG.debug("* IMAGE: "+image+" - DISTANCE: "+jaroWinklerDistance.distance(label,image));
        }
        Iterator<String> it = sortedMap.values().iterator();
        return (it.hasNext())?it.next():"";
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
        badWords.add("Gnome-mime-sound-openclipart.svg");
        badWords.add("File:Cscr-featured.svg");
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
