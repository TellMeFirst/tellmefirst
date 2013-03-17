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
import it.polito.tellmefirst.indexbuilding.corpusindexenricher.TMFIndexEnricher
import java.io.FileInputStream

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 *
 * This file customizes a DBpedia Spotlight class and is intended to be run within DBpedia Spotlight.
 * Copy it in the package org.dbpedia.spotlight.lucene.index.
 * For info about how to build and run DBpedia Spotlight to build indexes see: https://github.com/dbpedia-spotlight/dbpedia-spotlight
 */
object AddTitlesToIndex {

  //works with .tsv
  def loadTitles(instanceTypesFileName: String) = {
      DataLoader.getTitlesMap_java(new FileInputStream(instanceTypesFileName))
  }

  def main(args: Array[String]) {
    val indexingConfigFileName = args(0)
    val sourceIndexFileName = args(1)
    val config = new IndexingConfiguration(indexingConfigFileName)
    // add these properties in <dbpedia-spotlight installation>/conf/indexing.properties
    val targetIndexFileName = config.get("tellmefirst.index_with_titles")
    val titlesFileName = config.get("tellmefirst.titles")
    val titlesIndexer = new TMFIndexEnricher(sourceIndexFileName, targetIndexFileName, config)
    val titleMap = loadTitles(titlesFileName)
    titlesIndexer.enrichWithTitlesEnglish(titleMap)
    titlesIndexer.close
  }
}
