package it.polito.tellmefirst.web.rest.services;

import com.sun.jersey.multipart.FormDataParam;
import it.polito.tellmefirst.web.rest.interfaces.ClassifyInterface;
import it.polito.tellmefirst.web.rest.interfaces.EPubChaptersInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;


@Path("/classifyEpubChapters")
public class ClassifyEpubChapters {

    static Log LOG = LogFactory.getLog(Classify.class);
    private static EPubChaptersInterface ePubChaptersInterface = new EPubChaptersInterface();

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @POST
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJSON(@FormDataParam("file") File file,
                             @FormDataParam("fileName") String fileName,
                             @FormDataParam("numTopics") int numTopics,
                             @FormDataParam("lang") String lang) {
        LOG.debug("[postJSON] - BEGIN");
        LOG.info("Classify Epub chapters REST Service called.");
        try {
            long startTime = System.currentTimeMillis();
            String response = ePubChaptersInterface.getJSON(file, fileName, numTopics, lang);
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
