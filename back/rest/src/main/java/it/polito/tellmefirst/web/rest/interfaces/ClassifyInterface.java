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

import static it.polito.tellmefirst.classify.Classifier.getOptionalFields;
import static it.polito.tellmefirst.util.TMFUtils.hasContent;
import static it.polito.tellmefirst.util.TMFUtils.unchecked;
import static it.polito.tellmefirst.web.rest.asynchronous.Parallel.parallelListMap;
import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.util.PostProcess;
import it.polito.tellmefirst.util.Ret;
import it.polito.tellmefirst.web.rest.TMFListener;
import it.polito.tellmefirst.web.rest.guice.GuiceEnv;
import it.polito.tellmefirst.web.rest.images.ImagePolicyDAO;
import it.polito.tellmefirst.web.rest.images.ImagePolicyDAOImpl;
import it.polito.tellmefirst.web.rest.interfaces.ImageInterface.ImgResponse;
import it.polito.tellmefirst.web.rest.services.Classify.ImagePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import scala.actors.threadpool.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ClassifyInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ClassifyInterface.class);

    public static ImagePolicyDAO imgDAO = GuiceEnv.instance(ImagePolicyDAO.class);
    
    public String getJSON(String textStr, int numTopics, String lang, boolean wikihtml, final String optionalFieldsComma,final ImagePolicy policy) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        
        List<JSONObject> baseJsonResult = getJSONObjectList( callClassify(textStr, numTopics, lang, wikihtml) ); 
        List<JSONObject> completedResult = parallelListMap( baseJsonResult, (entry)->{
        	String image = entry.getString("image");
        	Double ratio = 0.0;
        	switch(policy){
        		case CHECK:
        			image = fixBrokenLink(image);
        			break;
        		case RATIO:
        			ratio = imgDAO.getAspectRatio( getTitleFromWikiLink( entry.getString("wikilink") ) );
        			image = (ratio > 0.0 && hasContent(image) )? image: "";
                	entry.put("ratio", ratio);
        			break;
        		case WIKIPARSE:
        			ImgResponse imgResp = imgDAO.getMobileWikiImg( getTitleFromWikiLink( entry.getString("wikilink") ));
        			image = imgResp.getSrc().isEmpty()?"":"http:"+imgResp.getSrc();
        			entry.put("ratio", imgDAO.getRatioFromImgResponse(imgResp) );
        			break;
        		case BASIC:
        		default:
        			
        			break;
        	}
        	entry.put("image", image);
        	
            //post-process result JSON string in order to add optional fields when required.
        	if(hasContent(optionalFieldsComma))
        		entry = addOptionalFieldsInASingleObject(entry, optionalFieldsComma);
        	return entry;
		});
        
        //no prod
        LOG.info("--------Result from Classify--------");
        LOG.debug("[getJSON] - END");
        return produceJSON(completedResult);
    }
    
    public String getTitleFromWikiLink(String wikilink){
    	return wikilink.substring(wikilink.lastIndexOf('/') + 1);
    }
    
    public List<String[]> callClassify(final String textStr,final int numTopics,final String lang,final boolean wikihtml){
    	return unchecked(()-> {
    			LOG.debug("[callClassify] - BEGIN");
    			Classifier classifier = (lang.equals("italian")) ? TMFListener.getItalianClassifier() : TMFListener.getEnglishClassifier();  
    			List<String[]> topics = classifier.classify(textStr, numTopics, lang, wikihtml);
    			LOG.debug("[callClassify] - END");
    			return topics;
		});
    }
    
    public String fixBrokenLink(String image){
		if(hasContent(image) && !imgDAO.existImage(image))
			return "";
    	return image;
    }

    public static List<JSONObject> getJSONObjectList(final List<String[]> topics){
    	List<JSONObject> resources = new ArrayList<JSONObject>();
    	topics.forEach((topic)->{
	    	resources.add(unchecked(()->{
		    	JSONObject resource = new JSONObject();
		    	resource.put("uri"			,topic[0]);
		    	resource.put("label"		,topic[1]);
		    	resource.put("title"		,topic[2]);
		    	resource.put("score"		,topic[3]);
		    	resource.put("mergedTypes"  ,topic[4]);
		    	resource.put("image"		,topic[5]);
		    	resource.put("wikilink"		,topic[6]);
		    	return resource;
	    	}));
    	});
    	return resources;
    }
    
    public static String produceJSON(final List<JSONObject> topics){
    	return unchecked(()-> {
    			JSONObject classifyResult = new JSONObject();
    			classifyResult.put("service", "Classify");
    			JSONArray resources = new JSONArray(topics);
    			classifyResult.put("Resources", resources);
    			return classifyResult.toString();
    	}, "JSON composition failed");
    }
    
//    private String addOptionalFields(final String result, final String optionalFieldsComma){
//    	try{
//			JSONObject classifyJSONResult = new JSONObject(result);
//			JSONArray resourcesArray = classifyJSONResult.getJSONArray("Resources");
//			JSONArray resultArray = new JSONArray();
//			for (int i=0; i<resourcesArray.length(); i++){
//				LOG.debug("\n\n\nAdding optional fields: "+optionalFieldsComma+" for "+i+" object, where max length is "+resourcesArray.length()+" \n\n\n");
//				resultArray.put(addOptionalFieldsInASingleObject(resourcesArray.getJSONObject(i), optionalFieldsComma));
//			}
//			classifyJSONResult.put("Resources", resultArray);
//		    return classifyJSONResult.toString();
//    	} catch (Exception e) {
//    		LOG.error("Optional fields not added",e);
//    		return result;
//		}
//    }
    
    private JSONObject addOptionalFieldsInASingleObject(JSONObject singleResult, final String optionalFieldsComma){
    	try{
		    String uri 	 = singleResult.getString("uri");
		    String label = singleResult.getString("label");
		    String [] optionalFields = optionalFieldsComma.split(",");
		    Map<String, String> optionalFieldsMap = getOptionalFields(uri, label, optionalFields);
		    for (Map.Entry<String, String> entry : optionalFieldsMap.entrySet())
		    	singleResult.put(entry.getKey(), entry.getValue());
		    return singleResult;
    	} catch (Exception e) {
    		LOG.error("Optional fields not added",e);
    		return singleResult;
		}
    }
    
}
