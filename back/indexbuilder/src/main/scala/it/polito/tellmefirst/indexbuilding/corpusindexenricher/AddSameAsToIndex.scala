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

package it.polito.tellmefirst.indexbuilding.corpusindexenricher

import org.dbpedia.spotlight.util._
import java.io.FileInputStream
import it.polito.tellmefirst.indexbuilding.corpusindexenricher.TMFIndexEnricher

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 *
 * This file customizes a DBpedia Spotlight class and is intended to be run within DBpedia Spotlight.
 * To run it, copy and paste in the package org.dbpedia.spotlight.lucene.index.
 * For info about how to build and run DBpedia Spotlight see: https://github.com/dbpedia-spotlight/dbpedia-spotlight
 */
object AddSameAsToIndex {

  // works with .tsv
  def loadSameAs(sameAsFileName: String) = {
      DataLoader.getSameAsMap_java(new FileInputStream(sameAsFileName))
  }

  def main(args: Array[String]) {
    val indexingConfigFileName = args(0)
    val sourceIndexFileName = args(1)
    val config = new IndexingConfiguration(indexingConfigFileName)
    // add these properties in <dbpedia-spotlight installation>/conf/indexing.properties
    val targetIndexFileName = config.get("tellmefirst.index_with_titles_images_and_sameAs")
    val sameAsFileName = config.get("tellmefirst.sameAs")
    val sameAsIndexer = new TMFIndexEnricher(sourceIndexFileName, targetIndexFileName, config)
    val sameAsMap = loadSameAs(sameAsFileName)
    sameAsIndexer.enrichWithSameAs(sameAsMap)
    sameAsIndexer.close
  }
}
