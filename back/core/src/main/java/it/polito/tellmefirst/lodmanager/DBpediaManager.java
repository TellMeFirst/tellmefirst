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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class DBpediaManager {

    static Log LOG = LogFactory.getLog(DBpediaManager.class);
    public static final String DBPEDIA_ENGLISH_ENDPOINT = "http://dbpedia.org/sparql";
    public static final String DBPEDIA_ITALIAN_ENDPOINT = "http://it.dbpedia.org/sparql";

    public String[] getCoordinatesForAPlace(String uri){
        LOG.debug("[getCoordinatesForAPlace] - BEGIN");
        String[] result = new String[2];
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = uri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("select ?lat ?long where {<" +
                    cleanUri+"> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . <" +
                    cleanUri+"> <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long ." +
                    "}", DBPEDIA_ENGLISH_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                Literal lat= row.getLiteral("lat");
                Literal longit= row.getLiteral("long");
                result[0] = lat.getString();
                result[1] = longit.getString();
            }
        }catch (Exception e){
            LOG.error("[getCoordinatesForAPlace] - EXCEPTION: ", e);
        }
        LOG.debug("[getCoordinatesForAPlace] - END");
        return result;
    }

    // this method returns an empty string when a sameAs between DBpedia and NYT is not available
    public String getNytUri(String dbpediaUri){
        LOG.debug("[getNytUri] - BEGIN");
        String result = "";
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = dbpediaUri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("select ?sameas where {" +
                    "?sameas <http://www.w3.org/2002/07/owl#sameAs> <" + cleanUri + "> ." +
                    "}", DBPEDIA_ENGLISH_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                String uri = row.get("sameas").toString();
                if(uri.contains("data.nytimes.com")){
                    LOG.debug("[getNytUri] - END");
                    return uri;
                }
            }
        }catch (Exception e){
            LOG.error("[getNytUri] - EXCEPTION: ", e);
        }
        LOG.debug("[getNytUri] - END");
        return result;
    }

    // never used so far
    public String getLabelFromEnglishDBpedia (String uri){
        String result = "";
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?label " +
                    "WHERE {<" +
                    uri+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label ." +
                    "FILTER (langMatches(lang(?label), \"EN\")) ." +
                    "}", DBPEDIA_ENGLISH_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                String label = row.get("label").toString();
                result = label.replace("\"","").replace("@en","");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> getTypes (String uri){
        LOG.debug("[getTypes] - BEGIN");
        ArrayList<String> result = new ArrayList<String>();
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = uri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?types " +
                    "WHERE {<" +
                    cleanUri+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?types ." +
                    "}", DBPEDIA_ENGLISH_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                String type = row.get("types").toString();
                result.add(type);
            }
        }catch (Exception e){
            LOG.error("[getTypes] - EXCEPTION: ", e);
        }
        LOG.debug("[getTypes] - END");
        return result;
    }

    public String getArtistFromEnglishDBpedia(String uri){
        LOG.debug("[getArtistFromEnglishDBpedia] - BEGIN");
        String result = "";
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = uri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?artistLabel WHERE {<" +
                    cleanUri+"> <http://dbpedia.org/property/artist> ?artist ." +
                    "?artist <http://www.w3.org/2000/01/rdf-schema#label> ?artistLabel ." +
                    "FILTER (langMatches(lang(?artistLabel), \"EN\"))" +
                    "}", DBPEDIA_ENGLISH_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                result = row.get("artistLabel").toString().replace("\"","").replace("@en","");
            }
        }catch (Exception e){
            LOG.error("[getArtistFromEnglishDBpedia] - EXCEPTION: ", e);
        }
        LOG.debug("[getArtistFromEnglishDBpedia] - END");
        return result;
    }

    public String getArtistFromItalianDBpedia(String uri){
        LOG.debug("[getArtistFromItalianDBpedia] - BEGIN");
        String result = "";
        try{
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = uri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?artistLabel WHERE {<" +
                    cleanUri+"> <http://it.dbpedia.org/property/artista> ?artistLabel ." +
                    "FILTER (langMatches(lang(?artistLabel), \"IT\"))" +
                    "}", DBPEDIA_ITALIAN_ENDPOINT);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                result = row.get("artistLabel").toString().replace("\"", "").replace("@it","");
            }
        }catch (Exception e){
            LOG.error("[getArtistFromItalianDBpedia] - EXCEPTION: ", e);
        }
        LOG.debug("[getArtistFromItalianDBpedia] - END");
        return result;
    }


    public String getAbstract(String uri, String lang){
        LOG.debug("[getAbstract] - BEGIN");
        String result = "";
        try{
            String endpoint = "";
            String language = "" ;
            if(lang.equals("italian")){
                language = "IT";
                endpoint = (uri.startsWith("http://it")) ? DBPEDIA_ITALIAN_ENDPOINT : DBPEDIA_ENGLISH_ENDPOINT;
            }   else {
                language = "EN";
                endpoint = DBPEDIA_ENGLISH_ENDPOINT;
            }
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            String cleanUri = uri.replace("%28","(").replace("%29", ")").replace("%27", "'");
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?abstract WHERE {<" +
                    cleanUri+"> <http://dbpedia.org/ontology/abstract> ?abstract ." +
                    "FILTER (langMatches(lang(?abstract), \""+language+"\"))" +
                    "}", endpoint);
            while (resultSet.hasNext()) {
                QuerySolution row= resultSet.next();
                result = row.get("abstract").toString().replace("@it","").replace("@en","");
            }
        }catch (Exception e){
            LOG.error("[getAbstract] - EXCEPTION: ", e);
        }
        LOG.debug("[getAbstract] - END");
        return result;
    }


    public boolean isDBpediaEnglishUp(){
        LOG.debug("[isDBpediaEnglishUp] - BEGIN");
        try {
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?s WHERE { ?s ?p ?o . } LIMIT 1", DBPEDIA_ENGLISH_ENDPOINT);
            if (!resultSet.hasNext()) {
                LOG.debug("[isDBpediaEnglishUp] - END");
                return false;
            }
        } catch (Exception e) {
            LOG.debug("[isDBpediaEnglishUp] - END");
            return false;
        }
        LOG.debug("[isDBpediaEnglishUp] - END");
        return true;
    }

    public boolean isDBpediaItalianUp(){
        LOG.debug("[isDBpediaItalianUp] - BEGIN");
        try {
            JenaSparqlManager jenaSparqlManager = new JenaSparqlManager();
            ResultSet resultSet = jenaSparqlManager.executeRemoteQuery("SELECT ?s WHERE { ?s ?p ?o . } LIMIT 1", DBPEDIA_ITALIAN_ENDPOINT);
            if (!resultSet.hasNext()) {
                LOG.debug("[isDBpediaItalianUp] - END");
                return false;
            }
        } catch (Exception e) {
            LOG.debug("[isDBpediaItalianUp] - END");
            return false;
        }
        LOG.debug("[isDBpediaItalianUp] - END");
        return true;
    }

    // just for testing
    public static void main(String[] args){
        DBpediaManager bpm = new DBpediaManager();
//        float[] coordinate = bpm.getCoordinatesForAPlace("http://dbpedia.org/resource/Italy");
//        System.out.println("Lat: "+coordinate[0]);
//        System.out.println("Long: "+coordinate[1]);
        //String s = bpm.getNytUri("http://dbpedia.org/resource/Meteorology");
//        ArrayList<String> types = bpm.getTypes("http://dbpedia.org/resource/Italy");
//        for(String s : types){
//            System.out.println(s);
//        }
        //String artist = bpm.getArtistFromItalianDBpedia("http://it.dbpedia.org/resource/Una_mattina");
        String abstracto = bpm.getAbstract("http://dbpedia.org/resource/Kevin_Costner","italian");
        System.out.println("Abstract: "+abstracto);
    }
}
