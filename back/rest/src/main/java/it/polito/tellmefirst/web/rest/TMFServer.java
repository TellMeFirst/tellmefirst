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

package it.polito.tellmefirst.web.rest;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.web.rest.enhance.Enhancer;
import it.polito.tellmefirst.web.rest.exception.TMFConfigurationException;
import it.polito.tellmefirst.web.rest.exception.TMFIndexesWarmUpException;
import it.polito.tellmefirst.lucene.IndexesUtil;
import it.polito.tellmefirst.util.TMFVariables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class TMFServer {

    static Log LOG = LogFactory.getLog(TMFServer.class);

    private static volatile Boolean running = true;
    protected static Enhancer enhancer;
    protected static Classifier italianClassifier;
    protected static Classifier englishClassifier;


    // TMF starting point. From rest directory, launch this command:
    // mvn exec:java -Dexec.mainClass="it.polito.temefirst.web.rest.TMFServer" -Dexec.args="<path_to_TMF_installation>/conf/server.properties"
    // or use the run.sh file in bin directory
    public static void main(String[] args) throws TMFConfigurationException, TMFIndexesWarmUpException,
            URISyntaxException, InterruptedException, IOException {
        LOG.debug("[main] - BEGIN");
        String configFileName = args[0];
        TMFVariables tmfVariables = new TMFVariables(configFileName);
        IndexesUtil.init();
        URI serverURI = new URI("http://localhost:2222/rest/"); // ### You have to choose where put this address!!!
        enhancer = new Enhancer();
        italianClassifier = new Classifier("it");
        englishClassifier = new Classifier("en");

        //The following is adapted from DBpedia Spotlight (https://github.com/dbpedia-spotlight/dbpedia-spotlight)
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core." +
                "PackagesResourceConfig");
        initParams.put("com.sun.jersey.config.property.packages", "it.polito.tellmefirst.web.rest.services");
        initParams.put("com.sun.jersey.config.property.WadlGeneratorConfig", "it.polito.tellmefirst.web.rest.wadl." +
                "ExternalUriWadlGeneratorConfig");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(serverURI, initParams);
        threadSelector.start();
        System.err.println("Server started in " + System.getProperty("user.dir") + " listening on " + serverURI);
        Thread warmUp = new Thread() {
            public void run() {
            }
        };
        warmUp.start();
        while(running) {
            Thread.sleep(100);
        }
        threadSelector.stopEndpoint();
        System.exit(0);
        LOG.debug("[main] - END");
    }


    public static Enhancer getEnhancer(){
        return enhancer;
    }


    public static Classifier getItalianClassifier(){
        return italianClassifier;
    }


    public static Classifier getEnglishClassifier(){
        return englishClassifier;
    }
}
