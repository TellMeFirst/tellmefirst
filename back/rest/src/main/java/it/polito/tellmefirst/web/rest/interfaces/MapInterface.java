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
public class MapInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(MapInterface.class);

    public String getJSON(String uri) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(uri);
        result = xml2json(xml);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String uri) throws TMFOutputException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        String [] coordinates = enhancer.getCoordinatesFromDBpedia(uri);
        result = produceXML(coordinates);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(String [] coordinates) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        String res1 = "";
        String res2 ="";
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            if(coordinates[0] != null){
                res1 = coordinates[0];
            }
            if(coordinates[1] != null){
                res2 = coordinates[1];
            }
            atts.addAttribute("","","lat","", res1);
            atts.addAttribute("","","long","", res2);
            hd.startElement("","","Enhancement",null);
            hd.startElement("","","Map",atts);
            hd.endElement("", "", "Map");
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
