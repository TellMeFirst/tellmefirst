/**
 * Copyright 2011 Pablo Mendes, Max Jakob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.polito.tellmefirst.web.rest;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.enhance.Enhancer;
import it.polito.tellmefirst.exception.TMFConfigurationException;
import it.polito.tellmefirst.exception.TMFIndexesWarmUpException;
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
    public static void main(String[] args) throws TMFConfigurationException, TMFIndexesWarmUpException, URISyntaxException, InterruptedException, IOException {
        LOG.debug("[main] - BEGIN");
        String configFileName = args[0];
        TMFVariables tmfVariables = new TMFVariables(configFileName);
        IndexesUtil indexesUtil = new IndexesUtil();
        URI serverURI = new URI(tmfVariables.getRestURL());
        enhancer = new Enhancer();
        italianClassifier = new Classifier("it");
        englishClassifier = new Classifier("en");

        //The following is adapted from DBpedia Spotlight (Pablo Mendes, Max Jacob)
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        initParams.put("com.sun.jersey.config.property.packages", "it.polito.tellmefirst.web.rest.services");
        initParams.put("com.sun.jersey.config.property.WadlGeneratorConfig", "it.polito.tellmefirst.web.rest.wadl.ExternalUriWadlGeneratorConfig");
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
