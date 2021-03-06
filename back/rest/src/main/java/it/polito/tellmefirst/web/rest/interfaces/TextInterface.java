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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class TextInterface extends AbsResponseInterface{

    static Log LOG = LogFactory.getLog(TextInterface.class);

    public String getJSON(String uri, String lang) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(uri, lang);
        result = xml2json(xml);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String uri, String lang) throws TMFOutputException, IOException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        String title = enhancer.getTitleFromDBpedia(uri, lang);
        result = produceXML(title);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(String title) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","title","", title);
            hd.startElement("","","Enhancement",null);
            hd.startElement("","","Result",atts);
            hd.endElement("","","Result");
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
