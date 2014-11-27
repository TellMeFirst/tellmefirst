package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.web.rest.TMFServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EPubChaptersInterface extends AbsResponseInterface {
    static Log LOG = LogFactory.getLog(ClassifyInterface.class);

    public String getJSON(File file, String fileName, int numTopics, String lang) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(file, fileName, numTopics, lang);
        result = xml2json(xml);
        LOG.info("--------Result from Classify--------");
        LOG.info(result);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(File file, String fileName, int numTopics, String lang) throws TMFVisibleException, TMFOutputException, IOException, InterruptedException {
        LOG.debug("[getXML] - BEGIN");
        String result;

        Classifier classifier = (lang.equals("italian")) ? TMFServer.getItalianClassifier() : TMFServer.getEnglishClassifier();
        LinkedHashMap<String, ArrayList<String[]>> chapters = classifier.classifyEPubChapters(file, fileName, numTopics, lang);
        result = produceXML(chapters);
        LOG.debug("[getXML] - END");

        return result;
    }

    private String produceXML(LinkedHashMap<String, ArrayList<String[]>> chapters) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","service","","ClassifyEpubChapters");
            hd.startElement("","","Classification",atts);
            int i = 0;
            Set set = chapters.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry)iterator.next();
                atts.clear();
                if (i == 0) {
                    hd.startElement("","","Chapters",atts);
                }
                atts.addAttribute("","","toc","",me.getKey().toString());
                hd.startElement("","","Chapter",atts);
                atts.clear();
                ArrayList<String[]> topics = (ArrayList<String[]>) me.getValue();
                int j = 0;
                for (String[] topic : topics) {
                    atts.clear();
                    if (j == 0){
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
                    j++;
                }
                if (j>0) hd.endElement("","","Resources");
                hd.endElement("","","Chapter");
                i++;
            }
            if (i>0) hd.endElement("","","Chapters");
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
