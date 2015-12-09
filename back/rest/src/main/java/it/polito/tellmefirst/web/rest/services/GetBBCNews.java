/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2015 Giuseppe Futia
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

import it.polito.tellmefirst.web.rest.interfaces.BBCNewsInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/getBBCNews")
@Consumes("text/plain")
public class GetBBCNews {

    static Log LOG = LogFactory.getLog(GetBBCNews.class);
    private static BBCNewsInterface bbcNewsInterface = new BBCNewsInterface();

    private Response ok(String response) {
        return Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSON(@QueryParam("uri") String uri) {
        LOG.debug("[postJSON] - BEGIN");
        //no prod
        LOG.info("GetBBCNews REST Service called for the resource: "+ uri);
        try {
            String response = bbcNewsInterface.getJSON(uri);
            LOG.debug("[postJSON] - END");
            return ok(response);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
                    .header("TMF-error",e.getMessage()).build());
        }
    }

}
