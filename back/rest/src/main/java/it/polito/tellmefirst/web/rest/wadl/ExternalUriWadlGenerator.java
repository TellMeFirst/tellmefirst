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

package it.polito.tellmefirst.web.rest.wadl;

import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Resources;
import it.polito.tellmefirst.web.rest.wadl.ExternalUriWadlGeneratorConfig;

/**
 * This class reuses code from DBpedia Spotlight (https://github.com/dbpedia-spotlight/dbpedia-spotlight)
 *
 *  @author Federico Cairo
 */
public class ExternalUriWadlGenerator extends WadlGeneratorImpl {

    @Override
    public Resources createResources() {
        Resources resources = super.createResources();
        resources.setBase(ExternalUriWadlGeneratorConfig.externalEndpointUri);
        return resources;
    }

    @Override
    public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        // nothing
    }

}