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

package it.polito.tellmefirst.apimanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
// XXX Reimplementare utilizzando il Jersey client.
public class RestManager {

    static Log LOG = LogFactory.getLog(RestManager.class);

    public String getStringFromAPI(String urlStr) {
        LOG.debug("[getStringFromAPI] - BEGIN - url="+urlStr);
        String result = "";
        try{
        	
        	LOG.debug("[getStringFromAPI] - http.proxyHost="+System.getProperty("http.proxyHost"));
        	LOG.debug("[getStringFromAPI] - http.proxyPort="+System.getProperty("http.proxyPort")); 
        	LOG.debug("[getStringFromAPI] - https.proxyHost="+System.getProperty("https.proxyHost"));
        	LOG.debug("[getStringFromAPI] - https.proxyPort="+System.getProperty("https.proxyPort"));  	
        	
        	LOG.debug("[getStringFromAPI] - http.nonProxyHosts="+System.getProperty("http.nonProxyHosts"));
        
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setConnectTimeout(15000); //set timeout to 15 seconds
            conn.setReadTimeout(15000);   //set timeout to 15 seconds
            
            if (conn.getResponseCode() != 200) {
                throw new IOException(conn.getResponseMessage());
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
            result = sb.toString();
        }catch (Exception e){
            LOG.error("[getStringFromAPI] - EXCEPTION for url="+urlStr, e);
        }
        LOG.debug("[getStringFromAPI] - END");
        return result;
    }
}
