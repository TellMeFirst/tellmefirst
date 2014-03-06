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

import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.util.Ret;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static it.polito.tellmefirst.util.TMFUtils.unchecked;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ImageInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ImageInterface.class);

    public String getJSON(String uri, String label) throws TMFOutputException {
        LOG.debug("[getJSON] - BEGIN");
        String result = produceJSON( enhancer.getImageFromMediaWiki2(label) );
        LOG.debug("[getJSON] - END");
        return result;
    }
    
    public static String produceJSON(final String imageURL){
    	return unchecked(new Ret<String>() {
			public String ret() throws Exception {
				LOG.debug("producingJSON");
		    	return new JSONObject().put("Result", new JSONObject().put("imageURL",imageURL)).toString();
			}
		}, "Failure in producing JSON for getImage");
    }
}
