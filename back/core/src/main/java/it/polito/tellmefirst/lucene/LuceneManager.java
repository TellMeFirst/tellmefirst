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

package it.polito.tellmefirst.lucene;

import it.polito.tellmefirst.classify.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class LuceneManager {

    static Log LOG = LogFactory.getLog(LuceneManager.class);
    private Analyzer luceneDefaultAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
    private Directory luceneCorpusIndexDirectory;
    // this is customized for TMF GUI
    private int limitForQueryResult = 7;


    public LuceneManager(Directory directory) throws IOException {
        LOG.debug("[constructor] - BEGIN");
        luceneCorpusIndexDirectory = directory;
        LOG.debug("[constructor] - BEGIN");
    }

    // this method is customized from DBpedia Spotlight
    public static Directory pickDirectory(File indexDir) throws IOException {
        LOG.debug("[pickDirectory] - BEGIN");
        Directory directory;
        if (System.getProperty("os.name").equals("Linux") && System.getProperty("os.arch").contains("64")) {
            directory = new MMapDirectory(indexDir);
        } else if (System.getProperty("os.name").equals("Linux") ) {
            directory = new NIOFSDirectory(indexDir);
        } else {
            directory = FSDirectory.open(indexDir);
        }
        LOG.debug("[pickDirectory] - END");
        return directory;
    }

    public Analyzer getLuceneDefaultAnalyzer() {
        return luceneDefaultAnalyzer;
    }

    public void setLuceneDefaultAnalyzer(Analyzer analyzer) {
        luceneDefaultAnalyzer = analyzer;
    }

    public int getLimitForQueryResult() {
        return limitForQueryResult;
    }

    public Directory getLuceneCorpusIndexDirectory() {
        return luceneCorpusIndexDirectory;
    }

    public Query getQueryForContext(Text context) throws ParseException {
        LOG.debug("[getQueryForContext] - BEGIN");
        Query result;
        QueryParser parser = new QueryParser(Version.LUCENE_36, "CONTEXT", this.getLuceneDefaultAnalyzer());
        //no prod
        LOG.debug("Analyzer used here: "+getLuceneDefaultAnalyzer());
        //escape special characters
        String queryText = context.getText().replaceAll("[\\+\\-\\|!\\(\\)\\{\\}\\[\\]\\^~\\*\\?\"\\\\:&]", " ");
        queryText = QueryParser.escape(queryText);
        result = parser.parse(queryText);
        LOG.debug("Main query from Classify: "+result);
        LOG.debug("[getQueryForContext] - END");
        return result;
    }

}
