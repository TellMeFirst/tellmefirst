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

package it.polito.tellmefirst.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import it.polito.tellmefirst.parsing.DOCparser;
import it.polito.tellmefirst.parsing.PDFparser;
import it.polito.tellmefirst.parsing.TMFTextParser;
import it.polito.tellmefirst.parsing.TXTparser;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class TMFUtils {

    static Log LOG = LogFactory.getLog(TMFUtils.class);

    public static Set<String> getStopWords(String stopwordsFilePath) {
        LOG.debug("[getStopWords] - BEGIN");
        ArrayList<String> stopWordsList = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(stopwordsFilePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stopWordsList.add(line.trim());
            }
            bufferedReader.close();
        } catch (Exception e) {
            LOG.error("Could not read stopwords file at location: " + stopwordsFilePath);
        }
        Set<String> stopwordsSet = new HashSet<String>(stopWordsList);
        LOG.debug("[getStopWords] - END");
        return stopwordsSet;
    }

    // take a look at: http://www.lampos.net/sort-hashmap
    public static LinkedHashMap sortHashMapIntegers(HashMap passedMap) {
        LOG.debug("[sortHashMapIntegers] - BEGIN");
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);
        LinkedHashMap sortedMap = new LinkedHashMap();
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();
                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((Integer) key, (Integer) val);
                    break;
                }
            }
        }
        LOG.debug("[sortHashMapIntegers] - END");
        return sortedMap;
    }


    public static int countWords(String in) {
        LOG.debug("[countWords] - BEGIN");
        String[] words = in.split(" ");
        LOG.debug("[countWords] - END");
        return words.length;
    }
    
    public static final Map<String, TMFTextParser> parseAssociation = new HashMap<String, TMFTextParser>();
    static {
    	parseAssociation.put("pdf",new PDFparser());
    	parseAssociation.put("doc",new DOCparser());
    	parseAssociation.put("txt",new TXTparser());
    }
    
    public static void optional(Behaviour b, String warning){
		try{
			b.behaviour();
		}catch(Exception e){
			LOG.warn(warning);
		}
	}
    
    public static void unchecked(Behaviour b, String warning){
		try{
			b.behaviour();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
    
    public static <T> T optional(Ret<T> ret, String warning){
    	T returnValue=null;
    	try{
			returnValue = ret.ret();
		}catch(Exception e){
			LOG.warn(warning, e);
		}
    	return returnValue;
    }
    
    public static <T> T unchecked(Ret<T> ret, String warning){
    	T returnValue=null;
    	try{
			returnValue = ret.ret();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
    	return returnValue;
    }
    
    public static Collection<String> filter(Collection<String> c, String ... patterns){
    	for (String pattern : patterns) {
			for (Iterator<String> iterator = c.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if(string.contains(pattern))
					iterator.remove();
			}
		}
    	return c;
    }
    
    /**
     * Remove the first "File:" and then replace whitespace characters with underscore '_'
     * @return the {@link String} used to calculate hash.
     */
    public static String processWikiFileLabelForHashComputation(String original){
    	return original.replaceFirst("File:", "").replaceAll(" ", "_");
    }
    
    public static String getWikiURL(final String file){
    	String wikiBase = "http://upload.wikimedia.org/wikipedia/commons";
    	String md5 = unchecked(new Ret<String>() {
			public String ret() throws Exception {
				final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				messageDigest.reset();
				messageDigest.update(file.getBytes(Charset.forName("UTF8")));
				final byte[] resultByte = messageDigest.digest();
				return new String(Hex.encodeHex(resultByte));
			}
		}, "Wiki img URL not found");
    	String finalURL = wikiBase+"/"+md5.charAt(0)+"/"+md5.charAt(0)+md5.charAt(1)+"/";
    	String encodedFileLabel = unchecked(new Ret<String>() {
			public String ret() throws Exception {
				return URLDecoder.decode(file, "UTF-8");
			}
		}, "Wiki img URL not found");
    	return finalURL+=encodedFileLabel;
    }
    
    public static String getFileExtension(String fileName){
    	String [] splat = fileName.split(".");
    	return splat[splat.length-1];
    }
    
    public static boolean hasContent (String string){
    	return string!=null && !string.isEmpty();
    }
    public static boolean hasNoContent (String string){
    	return !hasContent(string);
    }
    
    public static boolean existsLink(String url){
    	Client client = Client.create();
		WebResource webResource = client.resource(url);
		Integer status = webResource.head().getStatus();
		if(status==null) status = 0;
		return status==200;
    }
    
    public static boolean notExistsLink(String url){
    	return !existsLink(url);
    }
    
    public static List<JSONObject> jsonArrayToList(final JSONArray array){
    	return unchecked(new Ret<List<JSONObject>>() {
    		public List<JSONObject> ret() throws Exception {
    			List<JSONObject> result = new ArrayList<JSONObject>();
            	for (int i = 0; i < array.length(); i++)
        			result.add( array.getJSONObject(i) );
        		return result;
    		}
		}, "Not possible to retrieve a JSON Object list from JSONArray");
    }
    
}