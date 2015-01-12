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

package it.polito.tellmefirst.web.rest.interfaces;

import static it.polito.tellmefirst.util.TMFUtils.optional;
import static it.polito.tellmefirst.util.TMFUtils.uncheck;
import static it.polito.tellmefirst.util.TMFUtils.unchecked;
import static it.polito.tellmefirst.util.TMFUtils.isNotEmpty;
import static it.polito.tellmefirst.util.TMFUtils.isEmpty;
import static java.lang.Integer.parseInt;
import it.polito.tellmefirst.exception.TMFOutputException;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ImageInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ImageInterface.class);
    
    public String getJSON(String uri, String label) throws TMFOutputException {
        LOG.debug("[getJSON] - BEGIN");
        String result = produceJSON( enhancer.getImageFromMediaWiki2(label) );
        LOG.debug("[getJSON] - END");
        return result;
    }
    
    public static String produceJSON(final String imageURL){
    	return unchecked(() -> {
					LOG.debug("producingJSON");
					return new JSONObject().put("Result", new JSONObject().put("imageURL",imageURL)).toString();
    		   }, "Failure in producing JSON for getImage");
    }
    
//    public static JSONObject getImageFromWikiHTML(String label){
//    	//TODO label to be Url encoded
//		Elements links 				= getImgElementsFrom("http://it.m.wikipedia.org/wiki/", label);
//		if(!links.isEmpty()){
//			List<Element> filteredLinks = filterSmallImages(links);
//			LOG.debug(filteredLinks.size() + " not small img found in wiki en");
//			return getJSONObjectFromHTMLElements(filteredLinks);
//		} else {
//			//TODO label to be Url encoded
//	    	links 	= getImgElementsFrom("http://en.m.wikipedia.org/wiki/", label);
//	    	if(!links.isEmpty()){
//	    		List<Element> filteredLinks = filterSmallImages(links);
//	    		LOG.debug(filteredLinks.size() + " not small img found in wiki it");
//	    		return getJSONObjectFromHTMLElements(filteredLinks);	
//	    	} else {
//	    		return getEmptyJSONImg();
//	    	}
//		}
//    }
    
    @Deprecated
    public static JSONObject getImageFromWikiHTML(String label){
    	//TODO label to be Url encoded
		ImgResponse link = getFilteredImgFrom("http://it.m.wikipedia.org/wiki/", label);
		if(!link.getSrc().isEmpty()){
			LOG.debug("img found from it : "+link);
			return getJSONAnswer(link);
		} else {
			//TODO label to be Url encoded
	    	link 	= getFilteredImgFrom("http://en.m.wikipedia.org/wiki/", label);
	    	LOG.debug("img found from en : "+link);
			return getJSONAnswer(link);
		}
    }
    
    public static ImgResponse getFilteredImgFrom(String uri, String label){
    	return optional( ()-> getImgFromHTMLElement( filterSmallImages( getImgElementsFrom(uri, label) ).get(0) ), getEmptyImg() );
    }
    
    private static ImgResponse getImgFromHTMLElement(Element element){
    	return optional( ()-> new ImgResponse( 	 element.attr("src"),
    						  	   		parseInt(element.attr("width")),
    						  	   		parseInt(element.attr("height"))), getEmptyImg() );
    }
    
    private static ImgResponse getEmptyImg(){
    	return new ImgResponse("", 0, 0);
    }
    
    private static JSONObject getJSONAnswer(ImgResponse imgResponse){
    	return unchecked(() ->  new JSONObject().put("imageURL"	, imgResponse.getSrc())
							   					.put("width"	, imgResponse.getWidth())
							   					.put("height"	, imgResponse.getHeight()) );
    }
    
    private static List<Element> filterSmallImages(Elements links){
    	return links.stream().filter((node) -> getNodeWidth(node)  >40 
				   							&& getNodeHeight(node) >40 ).collect(Collectors.toList());
    }
    
    private static Integer getNodeWidth(Node node){
    	return optional(() -> parseInt(node.attr("width")), 0);
    }
    private static Integer getNodeHeight(Node node){
    	return optional(() -> parseInt(node.attr("height")), 0);
    }
    
    private static Elements getImgElementsFrom(String uri, String label){
    	return optional(()-> Jsoup.connect(uri+label).timeout(15*1000).get().select("img") , new Elements());
    }
    
    public static class ImgResponse {
    	private String src;
    	private Integer width;
    	private Integer height;
		public ImgResponse(String src, Integer width, Integer height) {
			this.src = src;
			this.width = width;
			this.height = height;
		}
		public String getSrc() {
			return src;
		}
		public void setSrc(String src) {
			this.src = src;
		}
		public Integer getWidth() {
			return width;
		}
		public void setWidth(Integer width) {
			this.width = width;
		}
		public Integer getHeight() {
			return height;
		}
		public void setHeight(Integer height) {
			this.height = height;
		}
		public String toString() {
			return "ImgResponse [src=" + src + ", width=" + width + ", height="
					+ height + "]";
		}
    }
    
}
