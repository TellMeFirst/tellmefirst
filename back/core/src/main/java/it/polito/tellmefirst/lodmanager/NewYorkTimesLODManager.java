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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class NewYorkTimesLODManager {

    static Log LOG = LogFactory.getLog(NewYorkTimesLODManager.class);
    final static String API_KEY = "bea5a5b7e21571786fe78f224ac6aca6:5:61245549";

    public String getSearchApiQuery(String uri){
        LOG.debug("[getSearchApiQuery] - BEGIN");
        String result = "";
        try{
            Model model = ModelFactory.createDefaultModel();
            model.read(uri);

            String queryString = "select ?search where {<" +
                    uri+"> <http://data.nytimes.com/elements/search_api_query> ?search ." +
                    "}" ;
            Query query = QueryFactory.create(queryString) ;
            QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

            ResultSet resultSet = qexec.execSelect() ;
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                Literal search= row.getLiteral("search");
                result = search.getString() + "&api-key=" + API_KEY;
            }
        }catch (Exception e){
            LOG.error("[getSearchApiQuery] - EXCEPTION: ", e);
        }
        LOG.debug("[getSearchApiQuery] - END");
        return result;
    }

    // just for testing
    public static void main(String[] args){
        NewYorkTimesLODManager nytlod = new NewYorkTimesLODManager();
        String s = nytlod.getSearchApiQuery("http://data.nytimes.com/86043020378633512412");
        System.out.println("Search Query: " + s);
    }
}
