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

package it.polito.tellmefirst.web.rest.services;

import com.sun.jersey.multipart.FormDataParam;
import it.polito.tellmefirst.web.rest.exception.TMFVisibleException;
import it.polito.tellmefirst.web.rest.interfaces.ClassifyInterface;
import it.polito.tellmefirst.classify.Text;
import it.polito.tellmefirst.web.rest.parsing.DOCparser;
import it.polito.tellmefirst.web.rest.parsing.HTMLparser;
import it.polito.tellmefirst.web.rest.parsing.PDFparser;
import it.polito.tellmefirst.web.rest.parsing.TXTparser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;


@Path("/classify")
public class Classify {

    static Log LOG = LogFactory.getLog(Classify.class);
    private static ClassifyInterface classifyInterface = new ClassifyInterface();

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @POST
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJSON(@FormDataParam("text") String inputText,
                             @FormDataParam("file") File file,
                             @FormDataParam("url") String url,
                             @FormDataParam("fileName") String fileName,
                             @FormDataParam("numTopics") int numTopics,
                             @FormDataParam("lang") String lang) throws TMFVisibleException {
        LOG.debug("[postJSON] - BEGIN");
        LOG.info("Classify REST Service called.");

        // XXX: check on DBpedia is currently removed! Need to understand if it is still useful

        try {
            long startTime = System.currentTimeMillis();
            // retrieving input text/url/file as text string
            Text text;

            if (inputText != null && !inputText.equals("")){
                text = new Text(inputText);
            } else if (url != null){
                HTMLparser parser = new HTMLparser();
                text = new Text(parser.htmlToTextGoose(url));
            } else if(file != null){
                if(fileName.endsWith(".pdf") || fileName.endsWith(".PDF")){
                    PDFparser parser = new PDFparser();
                    text = new Text(parser.pdfToText(file));
                } else if(fileName.endsWith(".doc") || fileName.endsWith(".DOC")){
                    DOCparser parser = new DOCparser();
                    text = new Text(parser.docToText(file));
                } else if(fileName.endsWith(".txt") || fileName.endsWith(".TXT")){
                    TXTparser parser = new TXTparser();
                    text = new Text(parser.txtToText(file));
                } else {
                    throw new TMFVisibleException("File extension not valid: only 'pdf', 'doc' and 'txt' allowed.");
                }
            } else {
                throw new TMFVisibleException("No valid parameters in your request: both 'text' and 'url' and 'file'" +
                        " are null.");
            }

            String textString = text.getText();

            String response = classifyInterface.getJSON(textString, file, url, fileName, numTopics, lang);
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            //no prod
            LOG.info("########### Classification took "+duration+" seconds. ###########");
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
                    .header("TMF-error",e.getMessage()).build());
        }  
    }
}

