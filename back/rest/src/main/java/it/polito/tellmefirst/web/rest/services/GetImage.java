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

import it.polito.tellmefirst.enhance.Enhancer;
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
        LOG.info("GetImage REST Service called for the label: "+ label);
        try {
            String response = imageInterface.getJSON(uri, label);
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
                    .header("TMF-error",e.getMessage()).build());
        }
    }
    
    @GET
    @Path("new")
    public Response getImg(@QueryParam("label") String label) {
        LOG.debug("[postJSON] - BEGIN");
        return ok(new Enhancer().getImageFromMediaWiki2(label));
    }


}


