package it.polito.tellmefirst.web.rest.wadl;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Resources;

import java.util.List;

/**
 * This class adapts the code of DBpedia Spotlight: TODO: copia-incolla l'URL della pagina di Spotlight
 *
 *  @author Federico Cairo (via Pablo Mendes)
 */

public class ExternalUriWadlGeneratorConfig extends WadlGeneratorConfig {

    public static String externalEndpointUri = "http://tellmefirst.polito.it/rest";

    public static void setUri(String uri) {
        externalEndpointUri = uri;
    }

    @Override
    public List<WadlGeneratorDescription> configure() {
        return generator(ExternalUriWadlGenerator.class).descriptions();
    }

}
