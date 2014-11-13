/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2014 Giuseppe Futia, Alessio Melandri
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

package it.polito.tellmefirst.parsing;

import it.polito.tellmefirst.exception.TMFVisibleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.xml.sax.ContentHandler;

import java.io.*;
import java.util.*;

public class EPUBparser {

    static Log LOG = LogFactory.getLog(EPUBparser.class);

    public LinkedHashMap<String, String> parseEPUB(File file) throws TMFVisibleException {
        LOG.debug("[parseEPUB] - BEGIN");

        // Tester
        LinkedHashMap<String, String> tocs = new LinkedHashMap<String, String>();
        ContentHandler text = new BodyContentHandler();
        LinkContentHandler links = new LinkContentHandler();
        ContentHandler handler = new TeeContentHandler(links,text);
        Metadata metadata = new Metadata();
        EpubParser parser = new EpubParser();
        ParseContext context = new ParseContext();
        try {
            InputStream input = new FileInputStream(file);
            parser.parse(input,handler,metadata,context);
        }
        catch (Exception e) {
            LOG.error("[parseEPUB()] - EXCEPTION: ", e);
            throw new TMFVisibleException("Problem parsing the file: the epub you uploaded seems malformed.");
        }
        tocs.put("toc1",text.toString());
        //tocs.put("toc2",text.toString());
        //tocs.put("toc3",text.toString());
        LOG.debug("[parseEPUB] - END");
        return tocs;
    }

    public ArrayList<ScoreDoc> aggregateResults(LinkedHashMap<String, ScoreDoc[]> tocResults, int numOfTopics) throws IOException {
        LOG.debug("[aggregateResults] - BEGIN");
        ArrayList<ScoreDoc> mergedHitList = new ArrayList<ScoreDoc>();
        for(String key : tocResults.keySet()){
            ArrayList<ScoreDoc> hitList = new ArrayList<ScoreDoc>();
            for (int i = 0; i < numOfTopics; i++){
                hitList.add(tocResults.get(key)[i]);
            }
            mergedHitList.addAll(hitList);
        }

        LOG.debug("[aggregateResults] - END");
        return mergedHitList;
    }

}
