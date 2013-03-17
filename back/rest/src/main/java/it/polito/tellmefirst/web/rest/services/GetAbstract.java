package it.polito.tellmefirst.web.rest.services;

import it.polito.tellmefirst.web.rest.interfaces.AbstractInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by IntelliJ IDEA.
 * User: federico
 * Date: 30/10/12
 * Time: 17.53
 * To change this template use File | Settings | File Templates.
 */

@Path("/getAbstract")
@Consumes("text/plain")
public class GetAbstract {

    static Log LOG = LogFactory.getLog(GetAbstract.class);
    private static AbstractInterface abstractInterface = new AbstractInterface();
    
    
    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSON(@QueryParam("uri") String uri,
                            @QueryParam("lang") String lang){
        LOG.debug("[postJSON] - BEGIN");
        //no prod
        LOG.info("GetAbstract REST Service called for the resource: "+ uri);
        try {
            String response = abstractInterface.getJSON(uri, lang);
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).header("TMF-error",e.getMessage()).build());
        }
    }
}
