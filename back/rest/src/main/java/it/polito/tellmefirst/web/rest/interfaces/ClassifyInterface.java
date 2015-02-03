/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.web.rest.exception.TMFOutputException;
import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.web.rest.exception.TMFVisibleException;
import it.polito.tellmefirst.web.rest.TMFServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        String xml = getXML(text, numTopics, lang);
        result = xml2json(xml);
        //no prod
        LOG.info("--------Result from Classify--------");
        LOG.info(result);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String text, int numTopics, String lang) throws TMFVisibleException, TMFOutputException, InterruptedException, ParseException, IOException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        Classifier classifier = (lang.equals("italian")) ? TMFServer.getItalianClassifier() : TMFServer.getEnglishClassifier();
        ArrayList<String[]> topics = classifier.classify(text, numTopics, lang);
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
