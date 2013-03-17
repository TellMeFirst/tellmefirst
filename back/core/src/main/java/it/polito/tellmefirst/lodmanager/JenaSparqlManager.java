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

package it.polito.tellmefirst.lodmanager;

import com.hp.hpl.jena.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 * Date: 08/03/12
 * Time: 18.47
 */
public class JenaSparqlManager {

    static Log LOG = LogFactory.getLog(JenaSparqlManager.class);

    public ResultSet executeRemoteQuery(String queryString, String endpoint){
        LOG.debug("[executeRemoteQuery] - BEGIN");
        Query query = QueryFactory.create(queryString);
        QueryExecution querex = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = querex.execSelect();
        //ResultSetFormatter.out(System.out, results);
        querex.close() ;
        LOG.debug("[executeRemoteQuery] - END");
        return results;
    }

    // just for testing
    public void executeRemoteQueryTest(){
        String ontologyService = "http://dbpedia.org/sparql";
        String queryString =
                "PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
                        "	PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"+
                        "	PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
                        "	PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
                        "	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                        "	PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                        "	PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
                        "		select ?url ?title\n"+
                        "		where {\n"+
                        "		?url rdfs:subClassOf %s.\n"+
                        "		?url dc:title ?title.\n"+
                        "		}\n";

        Query query = QueryFactory.create(queryString);
        QueryExecution querex = QueryExecutionFactory.sparqlService(ontologyService, query);
        ResultSet results = querex.execSelect();
        ResultSetFormatter.out(System.out, results);
        querex.close() ;
    }

    // just for testing
    public static void main(String[] args){
        JenaSparqlManager jsm = new JenaSparqlManager();
        jsm.executeRemoteQueryTest();

    }

}
