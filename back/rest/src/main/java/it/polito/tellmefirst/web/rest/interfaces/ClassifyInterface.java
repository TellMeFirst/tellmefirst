package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.web.rest.TMFServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ClassifyInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ClassifyInterface.class);

    public String getJSON(String text, File file, String url, String fileName, int numTopics, String lang) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(text, file, url, fileName, numTopics, lang);
        result = xml2json(xml);
        //no prod
        LOG.info("--------Result from Classify--------");
        LOG.info(result);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String text, File file, String url, String fileName, int numTopics, String lang) throws TMFVisibleException, TMFOutputException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        Classifier classifier = (lang.equals("italian")) ? TMFServer.getItalianClassifier() : TMFServer.getEnglishClassifier();
        ArrayList<String[]> topics = classifier.classify(text, file, url, fileName, numTopics, lang);
        result = produceXML(topics);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(ArrayList<String[]> topics) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","service","","Classify");
            hd.startElement("","","Classification",atts);
            int i=0;
            for (String[] topic : topics){
                if (i==0){
                    atts.clear();
                    hd.startElement("","","Resources",atts);
                }
                atts.addAttribute("","","uri","",topic[0]);
                atts.addAttribute("","","label","",topic[1]);
                atts.addAttribute("","","title","",topic[2]);
                atts.addAttribute("","","score","",topic[3]);
                atts.addAttribute("", "", "mergedTypes", "", topic[4]);
                atts.addAttribute("", "", "image", "", topic[5]);
                hd.startElement("","","Resource",atts);
                hd.endElement("","","Resource");
                i++;
            }
            if (i>0) hd.endElement("","","Resources");
            hd.endElement("","","Classification");
            hd.endDocument();
            xml = out.toString("utf-8");
            System.out.println(xml);
        } catch (Exception e) {
            throw new TMFOutputException("Error creating XML output.", e);
        }
        LOG.debug("[produceXML] - END");
        return xml;
    }
}
