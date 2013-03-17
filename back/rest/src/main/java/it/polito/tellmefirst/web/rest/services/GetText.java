package it.polito.tellmefirst.web.rest.services;

import it.polito.tellmefirst.web.rest.interfaces.TextInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */

@Path("/getText")
@Consumes("text/plain")
public class GetText {

    static Log LOG = LogFactory.getLog(GetText.class);
    private static TextInterface textInterface = new TextInterface();

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSON(@QueryParam("uri") String uri,
                            @QueryParam("lang") String lang) {
        LOG.debug("[postJSON] - BEGIN");
        //no prod
        LOG.info("GetText REST Service called for the resource: "+ uri);
        try {
            String response = textInterface.getJSON(uri, lang);
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).header("TMF-error",e.getMessage()).build());
        }
    }
}

