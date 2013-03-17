package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.exception.TMFOutputException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class VideoInterface extends AbsResponseInterface{

    static Log LOG = LogFactory.getLog(VideoInterface.class);

    public String getJSON(String uri, String label) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(uri, label);
        result = xml2json(xml);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String uri, String label) throws TMFOutputException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        String videoURL = enhancer.getVideoFromYouTube(uri, label);
        result = produceXML(videoURL);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(String videoURL) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","videoURL","", videoURL);
            hd.startElement("","","Enhancement",null);
            hd.startElement("","","Result",atts);
            hd.endElement("", "", "Result");
            hd.endElement("","","Enhancement");
            hd.endDocument();
            xml = out.toString("utf-8");
        } catch (Exception e) {
            throw new TMFOutputException("Error creating XML output.", e);
        }
        LOG.debug("[produceXML] - END");
        return xml;
    }
}
