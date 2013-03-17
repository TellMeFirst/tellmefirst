package it.polito.tellmefirst.web.rest.wadl;

import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Resources;
import it.polito.tellmefirst.web.rest.wadl.ExternalUriWadlGeneratorConfig;

/**
 * This class reuses the code of DBpedia Spotlight: TODO: copia-incolla l'URL della pagina di Spotlight
 *
 *  @author Federico Cairo (via Pablo Mendes)
 */
public class ExternalUriWadlGenerator extends WadlGeneratorImpl {

    @Override
    public Resources createResources() {
        Resources resources = super.createResources();
        resources.setBase(ExternalUriWadlGeneratorConfig.externalEndpointUri);
        return resources;
    }

    @Override
    public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        // nothing
    }

}