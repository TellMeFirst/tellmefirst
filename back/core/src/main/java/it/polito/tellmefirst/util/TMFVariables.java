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

import it.polito.tellmefirst.exception.TMFConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * User: Federico Cairo
 */
public class TMFVariables {

    static Log LOG = LogFactory.getLog(TMFVariables.class);

    private String restURL;

    public static String CORPUS_INDEX_IT;
    public static String KB_IT;
    public static String RESIDUAL_KB_IT;
    public static Set<String> STOPWORDS_IT;

    public static String CORPUS_INDEX_EN;
    public static String KB_EN;
    public static String RESIDUAL_KB_EN;
    public static Set<String> STOPWORDS_EN;
    
    public static int CACHE_TTL = 0;
    
    public static String EXT_LOWER_DOC = "doc";
    public static String EXT_LOWER_PDF = "pdf";
    public static String EXT_LOWER_TXT = "txt";
    
    //Constants for query params in classify
    public static String ALTERNATIVE_IMAGE = "alternativeImage";
    public static String YOUTUBE_VIDEO	   = "youtubeVideo";


    public TMFVariables(String confFile) throws TMFConfigurationException {
        LOG.debug("[constructor] - BEGIN");
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(new File(confFile)));
            restURL = config.getProperty("rest.service.url", "").trim();

            CORPUS_INDEX_IT = config.getProperty("corpus.index.it", "").trim();
            KB_IT = config.getProperty("kb.it", "").trim();
            RESIDUAL_KB_IT = config.getProperty("residualkb.it", "").trim();
            STOPWORDS_IT = TMFUtils.getStopWords(config.getProperty("stopWords.it", "").trim());

            CORPUS_INDEX_EN = config.getProperty("corpus.index.en", "").trim();
            KB_EN = config.getProperty("kb.en", "").trim();
            RESIDUAL_KB_EN = config.getProperty("residualkb.en", "").trim();
            STOPWORDS_EN = TMFUtils.getStopWords(config.getProperty("stopWords.en", "").trim());
            
            String cacheTTL = config.getProperty("cache.TTL");
            if(cacheTTL!=null)
            	CACHE_TTL = new Integer(cacheTTL);
            
        } catch (IOException e) {
            //exceptions are not catched here, because we want to stop TMF server
            throw new TMFConfigurationException("Problem with configuring initial parameters: ", e);
        }
        LOG.debug("[constructor] - END");
    }


    public String getRestURL() {
        return restURL;
    }
}
