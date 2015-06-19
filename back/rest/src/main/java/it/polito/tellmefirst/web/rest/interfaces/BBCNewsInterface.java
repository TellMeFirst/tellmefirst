package it.polito.tellmefirst.web.rest.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BBCNewsInterface extends AbsResponseInterface {
    static Log LOG = LogFactory.getLog(BBCNewsInterface.class);

    public String getJSON(String uri) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result = enhancer.getNewsFromBBC(uri);
        LOG.debug("[getJSON] - END");
        return result;
    }
}
