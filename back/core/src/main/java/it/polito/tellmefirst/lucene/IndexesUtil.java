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

import it.polito.tellmefirst.exception.TMFIndexesWarmUpException;
import it.polito.tellmefirst.util.TMFVariables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class IndexesUtil {

    static Log LOG = LogFactory.getLog(IndexesUtil.class);
    public static SimpleSearcher ITALIAN_CORPUS_INDEX_SEARCHER;
    public static SimpleSearcher ENGLISH_CORPUS_INDEX_SEARCHER;


    public IndexesUtil() throws TMFIndexesWarmUpException {
        LOG.debug("[constructor] - BEGIN");
        try{
            // build italian searcher
            Directory contextIndexDirIT = LuceneManager.pickDirectory(new File(TMFVariables.CORPUS_INDEX_IT));
            LOG.info("Corpus index used for italian: "+contextIndexDirIT);
            LuceneManager contextLuceneManagerIT = new LuceneManager(contextIndexDirIT);
            contextLuceneManagerIT.setLuceneDefaultAnalyzer(new ItalianAnalyzer(Version.LUCENE_36, TMFVariables.STOPWORDS_IT));
            ITALIAN_CORPUS_INDEX_SEARCHER = new SimpleSearcher(contextLuceneManagerIT);

            // build english searcher
            Directory contextIndexDirEN = LuceneManager.pickDirectory(new File(TMFVariables.CORPUS_INDEX_EN));
            LOG.info("Corpus index used for english: "+contextIndexDirEN);
            LuceneManager contextLuceneManagerEN = new LuceneManager(contextIndexDirEN);
            contextLuceneManagerEN.setLuceneDefaultAnalyzer(new EnglishAnalyzer(Version.LUCENE_36, TMFVariables.STOPWORDS_EN));
            ENGLISH_CORPUS_INDEX_SEARCHER = new SimpleSearcher(contextLuceneManagerIT);
        }catch (Exception e){
            //exceptions are not catched here, because we want to stop TMF server
            throw new TMFIndexesWarmUpException("Problem with setting up TMF indexes: ", e);
        }
        LOG.debug("[constructor] - END");
    }


    public static ArrayList<String> getBagOfConcepts(String uri, String lang) {
        LOG.debug("[getBagOfConcepts] - BEGIN");
        ArrayList<String> result = new ArrayList<String>();
        try{
            String KBPath = (lang.equals("it")) ? TMFVariables.KB_IT : TMFVariables.KB_EN;
            MMapDirectory directory = new MMapDirectory(new File(KBPath));
            IndexReader reader = IndexReader.open(directory, true);
            IndexSearcher is = new IndexSearcher(directory, true);
            Query q = new TermQuery(new Term("URI", uri));
            TopDocs hits = is.search(q, 1);
            is.close();
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = reader.document(docId);
                String wikilinksMerged = doc.getField("KB").stringValue();
                String[] wikiSplits = wikilinksMerged.split(" ");
                //no prod
                LOG.debug("Bag of concepts for the resource " + uri + ": ");
                for (String s : wikiSplits) {
                    result.add(s);
                    //no prod
                    LOG.debug("* "+s);
                }
            }
            reader.close();
        }catch (Exception e){
            LOG.error("[getBagOfConcepts] - EXCEPTION: ", e);
        }
        LOG.debug("[getBagOfConcepts] - END");
        return result;
    }


    public static ArrayList<String> getResidualBagOfConcepts(String uri, String lang) {
        LOG.debug("[getResidualBagOfConcepts] - BEGIN");
        ArrayList<String> result = new ArrayList<String>();
        try{
            String residualKBPath = (lang.equals("it")) ? TMFVariables.RESIDUAL_KB_IT : TMFVariables.RESIDUAL_KB_EN;
            MMapDirectory directory = new MMapDirectory(new File(residualKBPath));
            IndexReader reader = IndexReader.open(directory, true);
            IndexSearcher is = new IndexSearcher(directory,true);
            Query q = new TermQuery(new Term("URI", uri));
            TopDocs hits = is.search(q, 1);
            is.close();
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = reader.document(docId);
                String wikilinksMerged = doc.getField("KB").stringValue();
                String[] wikiSplits = wikilinksMerged.split(" ");
                //no prod
                LOG.debug("Residual bag of concepts for the resource " + uri + ": ");
                for (String s : wikiSplits) {
                    result.add(s);
                    //no prod
                    LOG.debug("* "+s);
                }
            }
            reader.close();
        }catch (Exception e){
            LOG.error("[getResidualBagOfConcepts] - EXCEPTION: ", e);
        }
        LOG.debug("[getResidualBagOfConcepts] - END");
        return result;
    }


    public static ArrayList<String> getTypes(String uri, String lang) {
        LOG.debug("[getTypes] - BEGIN");
        ArrayList<String> result = new ArrayList<String>();
        try{
            SimpleSearcher simpleSearcher = (lang.equals("it")) ? ITALIAN_CORPUS_INDEX_SEARCHER : ENGLISH_CORPUS_INDEX_SEARCHER;
            String cleanUri =  uri.replace("http://it.dbpedia.org/resource/","").replace("http://dbpedia.org/resource/","");
            Query q = new TermQuery(new Term("URI", cleanUri));
            TopDocs hits = simpleSearcher.getIndexSearcher().search(q, 1);
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = simpleSearcher.getFullDocument((docId));
                Field[] types = doc.getFields("TYPE");
                for (Field type : types) {
                    result.add(type.stringValue());
                }
            }
        }catch (Exception e){
            LOG.error("[getTypes] - EXCEPTION: ", e);
        }
        LOG.debug("[getTypes] - END");
        return result;
    }


    public static String getTitle(String uri, String lang) {
        LOG.debug("[getTitle] - BEGIN");
        String result = "";
        try{
            SimpleSearcher simpleSearcher = (lang.equals("it")) ? ITALIAN_CORPUS_INDEX_SEARCHER : ENGLISH_CORPUS_INDEX_SEARCHER;
            IndexSearcher indexSearcher = simpleSearcher.getIndexSearcher();
            String cleanUri =  uri.replace("http://it.dbpedia.org/resource/","").replace("http://dbpedia.org/resource/","");
            Query q = new TermQuery(new Term("URI", cleanUri));
            TopDocs hits = indexSearcher.search(q, 1);
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = simpleSearcher.getFullDocument(docId);
                if(doc.getField("TITLE").stringValue() != null){
                    result = doc.getField("TITLE").stringValue();
                }
            } else {
                LOG.error("[getTitle] - ERROR: No Title found for the resource "+uri+" !!");
            }
        }catch (Exception e){
            LOG.error("[getTitle] - EXCEPTION: ", e);
        }
        LOG.debug("[getTitle] - END");
        return result;
    }


    public static String getImage(String uri, String lang) {
        LOG.debug("[getImage] - BEGIN");
        String result = "";
        try{
            SimpleSearcher simpleSearcher = (lang.equals("it")) ? ITALIAN_CORPUS_INDEX_SEARCHER : ENGLISH_CORPUS_INDEX_SEARCHER;
            IndexSearcher indexSearcher = simpleSearcher.getIndexSearcher();
            String cleanUri =  uri.replace("http://it.dbpedia.org/resource/","").replace("http://dbpedia.org/resource/","");
            Query q = new TermQuery(new Term("URI", cleanUri));
            TopDocs hits = indexSearcher.search(q, 1);
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = simpleSearcher.getFullDocument(docId);
                if (doc.getField("IMAGE") != null){
                    result = doc.getField("IMAGE").stringValue();
                }
            }
        }catch (Exception e){
            LOG.error("[getImage] - EXCEPTION: ", e);
        }
        LOG.debug("[getImage] - END");
        return result;
    }


    public static String getSameAsFromEngToIta(String engUri) {
        LOG.debug("[getSameAsFromEngToIta] - BEGIN");
        String result = "";
        try{
            IndexSearcher indexSearcher = ITALIAN_CORPUS_INDEX_SEARCHER.getIndexSearcher();
            Query q = new TermQuery(new Term("SAMEAS", engUri));
            TopDocs hits = indexSearcher.search(q, 1);
            if (hits.totalHits != 0) {
                int docId = hits.scoreDocs[0].doc;
                org.apache.lucene.document.Document doc = ITALIAN_CORPUS_INDEX_SEARCHER.getFullDocument(docId);
                if (doc.getField("URI") != null){
                    result = doc.getField("URI").stringValue();
                }
            }
        } catch (Exception e){
            LOG.error("[getSameAsFromEngToIta] - EXCEPTION: ", e);
        }
        LOG.debug("[getSameAsFromEngToIta] - END");
        return result;
    }
}
