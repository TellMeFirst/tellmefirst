package it.polito.tellmefirst.web.rest.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class NewsInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(NewsInterface.class);

    public String getJSON(String uri) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result = enhancer.getNewsFromNYT(uri);
        LOG.debug("[getJSON] - END");
        return result;
    }
}
