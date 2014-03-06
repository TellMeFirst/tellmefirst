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

import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.util.Behaviour;
import it.polito.tellmefirst.util.Ret;
import it.polito.tellmefirst.web.rest.TMFListener;
import it.polito.tellmefirst.web.rest.services.Classify;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.helpers.AttributesImpl;

import static it.polito.tellmefirst.classify.Classifier.getOptionalFields;
import static it.polito.tellmefirst.util.TMFUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ClassifyInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ClassifyInterface.class);

    public String getJSON(String textStr, int numTopics, String lang, boolean wikihtml, String optionalFieldsComma) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        
        String result = produceJSON(callClassify(textStr, numTopics, lang, wikihtml));
        
        //post-process result JSON string in order to add optional fields when required.
        if(hasContent(optionalFieldsComma))
        	result = addOptionalFields(result, optionalFieldsComma);
        
        //no prod
        LOG.info("--------Result from Classify--------");
        LOG.debug("[getJSON] - END");
        return result;
    }
    
    public List<String[]> callClassify(final String textStr,final int numTopics,final String lang,final boolean wikihtml){
    	return unchecked(new Ret<List<String[]>>() {
    		public List<String[]> ret() throws Exception {
    			LOG.debug("[callClassify] - BEGIN");
    			Classifier classifier = (lang.equals("italian")) ? TMFListener.getItalianClassifier() : TMFListener.getEnglishClassifier();  
    			List<String[]> topics = classifier.classify(textStr, numTopics, lang, wikihtml);
    			LOG.debug("[callClassify] - END");
    			return topics;
    		}
		}, "Classify failed ");
    }

    public static String produceJSON(List<String[]> topics){
    	String result = "";
    	JSONObject classifyResult = new JSONObject();
    	try {
			classifyResult.put("service", "Classify");
			
			JSONArray resources = new JSONArray();
			for (String[] topic : topics) {
				JSONObject resource = new JSONObject();
				resource.put("uri"			,topic[0]);
				resource.put("label"		,topic[1]);
				resource.put("title"		,topic[2]);
				resource.put("score"		,topic[3]);
				resource.put("mergedTypes"  ,topic[4]);
				resource.put("image"		,topic[5]);
				resource.put("wikilink"		,topic[6]);
				resources.put(resource);
			}
			classifyResult.put("Resources", resources);
			result = classifyResult.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	return result;
    }
    
    private String addOptionalFields(final String result, final String optionalFieldsComma){
    	try{
			JSONObject classifyJSONResult = new JSONObject(result);
			JSONArray resourcesArray = classifyJSONResult.getJSONArray("Resources");
			JSONArray resultArray = new JSONArray();
			for (int i=0; i<resourcesArray.length() ;i++){
				LOG.debug("\n\n\nAdding optional fields: "+optionalFieldsComma+" for "+i+" object, where max length is "+resourcesArray.length()+" \n\n\n");
				resultArray.put(addOptionalFieldsInASingleObject(resourcesArray.getJSONObject(i), optionalFieldsComma));
			}
			classifyJSONResult.put("Resources", resultArray);
		    return classifyJSONResult.toString();
    	} catch (Exception e) {
    		LOG.error("Optional fields not added",e);
    		return result;
		}
    }
    
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
