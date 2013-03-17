package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.enhance.Enhancer;
import it.polito.tellmefirst.web.rest.TMFServer;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 *
 * Part of the logic of this class has been taken from DBpedia Spotlight
 */
public abstract class AbsResponseInterface {

    static Log LOG = LogFactory.getLog(AbsResponseInterface.class);
    protected Enhancer enhancer;

    public AbsResponseInterface(){
        enhancer = TMFServer.getEnhancer();
    }

    public String xml2json(String xmlDoc) throws TMFOutputException {
        LOG.debug("[xml2json] - BEGIN");
        String json;
        XMLSerializer xmlSerializer = new XMLSerializer();
        try {
            json = xmlSerializer.read(xmlDoc).toString(2);
        } catch (Exception e) {
            throw new TMFOutputException("Error converting XML to JSON.", e);
        }
        LOG.debug("[xml2json] - END");
        return json;
    }

    public TransformerHandler initXMLDoc(ByteArrayOutputStream out) throws TMFOutputException {
        LOG.debug("[initXMLDoc] - BEGIN");
        TransformerHandler hd;
        try{
            StreamResult streamResult = new StreamResult(out);
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            hd.setResult(streamResult);
            hd.startDocument();
        }catch (Exception e){
            throw new TMFOutputException("Error initializing XML.", e);
        }
        LOG.debug("[initXMLDoc] - END");
        return hd;
    }

}
