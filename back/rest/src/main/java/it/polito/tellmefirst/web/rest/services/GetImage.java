
package it.polito.tellmefirst.web.rest.services;

import it.polito.tellmefirst.web.rest.interfaces.ImageInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/getImage")
@Consumes("text/plain")
public class GetImage {

    static Log LOG = LogFactory.getLog(GetImage.class);
    private static ImageInterface imageInterface = new ImageInterface();

    
    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSON(@QueryParam("uri") String uri,
                            @QueryParam("label") String label) {
        LOG.debug("[postJSON] - BEGIN");
        //no prod
        LOG.info("GetImage REST Service called for the resource: "+ uri);
        try {
            String response = imageInterface.getJSON(uri, label);
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).header("TMF-error",e.getMessage()).build());
        }
    }


}


