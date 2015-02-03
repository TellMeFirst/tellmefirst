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
import it.polito.tellmefirst.web.rest.interfaces.ClassifyInterface;
import it.polito.tellmefirst.web.rest.lodmanager.DBpediaManager;
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
    private DBpediaManager dBpediaManager;

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @POST
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJSON(@FormDataParam("text") String text,
                             @FormDataParam("file") File file,
                             @FormDataParam("url") String url,
                             @FormDataParam("fileName") String fileName,
                             @FormDataParam("numTopics") int numTopics,
                             @FormDataParam("lang") String lang) {
        LOG.debug("[postJSON] - BEGIN");
        LOG.info("Classify REST Service called.");

        // Check if DBpedia is up
        dBpediaManager = new DBpediaManager();
        if (!lang.equals("english") && !dBpediaManager.isDBpediaEnglishUp()){
            //comment for local use
            throw new TMFVisibleException("DBpedia English service seems to be down, so TellMeFirst can't work " +
                    "properly. Please try later!");
        } else {
            if (lang.equals("italian") && !dBpediaManager.isDBpediaItalianUp()){
                //comment for local use
                throw new TMFVisibleException("DBpedia Italian service seems to be down, so TellMeFirst can't work" +
                        " properly. Please try later!");
            }
        }

        try {
            long startTime = System.currentTimeMillis();
            String response = classifyInterface.getJSON(text, file, url, fileName, numTopics, lang);
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

