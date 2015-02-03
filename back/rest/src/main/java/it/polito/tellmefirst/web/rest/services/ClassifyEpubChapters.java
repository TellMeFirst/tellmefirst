/*
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2014 Giuseppe Futia, Alessio Melandri
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
import it.polito.tellmefirst.web.rest.interfaces.EpubChaptersInterface;
import it.polito.tellmefirst.web.rest.lodmanager.DBpediaManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;


@Path("/classifyEpubChapters")
public class ClassifyEpubChapters {

    static Log LOG = LogFactory.getLog(Classify.class);
    private static EpubChaptersInterface ePubChaptersInterface = new EpubChaptersInterface();
    private DBpediaManager dBpediaManager;

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @POST
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJSON(@FormDataParam("file") File file,
                             @FormDataParam("fileName") String fileName,
                             @FormDataParam("url") String url,
                             @FormDataParam("numTopics") int numTopics,
                             @FormDataParam("lang") String lang) {
        LOG.debug("[postJSON] - BEGIN");
        LOG.info("Classify Epub chapters REST Service called.");

        dBpediaManager = new DBpediaManager();
        if (!lang.equals("english") && !dBpediaManager.isDBpediaEnglishUp()){
            throw new TMFVisibleException("DBpedia English service seems to be down, so TellMeFirst can't work " +
                    "properly. Please try later!");
        } else {
            if (lang.equals("italian") && !dBpediaManager.isDBpediaItalianUp()){
                throw new TMFVisibleException("DBpedia Italian service seems to be down, so TellMeFirst can't work" +
                        " properly. Please try later!");
            }
        }

        try {
            long startTime = System.currentTimeMillis();
            String response = ePubChaptersInterface.getJSON(file, fileName, url, numTopics, lang);
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            //no prod
            LOG.info("########### Classification Epub chapters "+duration+" seconds. ###########");
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
                    .header("TMF-error",e.getMessage()).build());
        }
    }
}
