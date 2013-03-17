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

import io.Source
import scala.collection.JavaConversions._
import org.apache.commons.logging.LogFactory
import java.util.{LinkedHashSet, LinkedList}
import java.io.{InputStream, File}
import org.semanticweb.yars.nx.parser.NxParser
import collection.JavaConversions
import org.dbpedia.spotlight.model._

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 *
 * This file customizes a DBpedia Spotlight class and is intended to be used within DBpedia Spotlight.
 * Copy it in the package org.dbpedia.spotlight.lucene.index.
 * For info about how to build and run DBpedia Spotlight to build indexes see: https://github.com/dbpedia-spotlight/dbpedia-spotlight
 */
object DataLoader
{
    private val LOG = LogFactory.getLog(this.getClass)

    def getTypesMap(typeDictFile : File) : Map[String, List[OntologyType]] = {
        LOG.info("Loading types map...")
        if (!(typeDictFile.getName.toLowerCase endsWith ".tsv"))
            throw new IllegalArgumentException("types mapping only accepted in tsv format so far! can't parse "+typeDictFile)
        // CAUTION: this assumes that the most specific type is listed last
        var typesMap = Map[String,List[OntologyType]]()
        for (line <- Source.fromFile(typeDictFile, "UTF-8").getLines) {
            val elements = line.split("\t")
            val uri = new DBpediaResource(elements(0)).uri
            val t = Factory.OntologyType.fromURI(elements(1))
            val typesList : List[OntologyType] = typesMap.get(uri).getOrElse(List[OntologyType]()) ::: List(t)
            typesMap = typesMap.updated(uri, typesList)
        }
        LOG.info("Done.")
        typesMap
    }

    def getTypesMapFromTSV_java(input: InputStream) : java.util.Map[String,java.util.LinkedHashSet[OntologyType]] = {
        LOG.info("Loading types map...")
        var typesMap = Map[String,java.util.LinkedHashSet[OntologyType]]()
        var i = 0;
        for (line <- Source.fromInputStream(input, "UTF-8").getLines) {
            val elements = line.split("\t")
            val uri = new DBpediaResource(elements(0)).uri
            val typeUri = elements(1)
            if (!typeUri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Thing")) {
                val t = Factory.OntologyType.fromURI(typeUri)
                i = i + 1;
                val typesList : java.util.LinkedHashSet[OntologyType] = typesMap.getOrElse(uri,new LinkedHashSet[OntologyType]())
                typesList.add(t)
                t match {
                    case ft: FreebaseType => typesList.add(Factory.OntologyType.fromQName("Freebase:/"+ft.domain))
                    case _ => //nothing
                }
                typesMap = typesMap.updated(uri, typesList)
            }
        }
        LOG.info("Done. Loaded %d types for %d resources.".format(i,typesMap.size))
        typesMap
    }


    def getTypesMap_java(instanceTypesStream : InputStream) : java.util.Map[String,java.util.LinkedHashSet[OntologyType]] = {
        LOG.info("Loading types map...")
        var typesMap = Map[String,java.util.LinkedHashSet[OntologyType]]()
        var i = 0;
        // CAUTION: this assumes that the most specific type is listed last
        val parser = new NxParser(instanceTypesStream)
        while (parser.hasNext) {
            val triple = parser.next
            if(!triple(2).toString.endsWith("owl#Thing")) {
                i = i + 1;
                val resource = new DBpediaResource(triple(0).toString)
                val t = Factory.OntologyType.fromURI(triple(2).toString)
                val typesList : java.util.LinkedHashSet[OntologyType] = typesMap.get(resource.uri).getOrElse(new LinkedHashSet[OntologyType]())
                typesList.add(t)
                typesMap = typesMap.updated(resource.uri, typesList)
            }
        }
        LOG.info("Done. Loaded %d types.".format(i))
        typesMap
    }

  def getTitlesMap_java(titlesStream : InputStream) : java.util.Map[String,java.util.LinkedHashSet[String]] = {
    LOG.info("Loading titles map...")
    var titlesMap = Map[String,java.util.LinkedHashSet[String]]()
    var i = 0;
    val parser = new NxParser(titlesStream)
    while (parser.hasNext) {
      val triple = parser.next
      if(!triple(2).toString.endsWith("owl#Thing")) {
        i = i + 1;
        val resource = new DBpediaResource(triple(0).toString)
        val t = triple(2).toString
        val titleList : java.util.LinkedHashSet[String] = titlesMap.get(resource.uri).getOrElse(new LinkedHashSet[String]())
        titleList.add(t)
        titlesMap = titlesMap.updated(resource.uri, titleList)
      }
    }
    LOG.info("Done. Loaded %d titles.".format(i))
    titlesMap
  }

  def getImagesMap_java(imagesStream : InputStream) : java.util.Map[String,java.util.LinkedHashSet[String]] = {
    LOG.info("Loading images map...")
    var imagesMap = Map[String,java.util.LinkedHashSet[String]]()
    var i = 0;
    val parser = new NxParser(imagesStream)
    while (parser.hasNext) {
      val triple = parser.next
      if(triple(0).toString.startsWith("http://dbpedia.org") && triple(1).toString.startsWith("http://xmlns.com/foaf/0.1/depiction")) {
        i = i + 1;
        val resource = new DBpediaResource(triple(0).toString)
        val t = triple(2).toString
        val imageList : java.util.LinkedHashSet[String] = imagesMap.get(resource.uri).getOrElse(new LinkedHashSet[String]())
        imageList.add(t)
        imagesMap = imagesMap.updated(resource.uri, imageList)
      }
    }
    LOG.info("Done. Loaded %d images.".format(i))
    imagesMap
  }

  def getImagesMap_italian_java(imagesStream : InputStream) : java.util.Map[String,java.util.LinkedHashSet[String]] = {
    LOG.info("Loading italian images map...")
    var imagesMap = Map[String,java.util.LinkedHashSet[String]]()
    var i = 0;
    val parser = new NxParser(imagesStream)
    while (parser.hasNext) {
      val triple = parser.next
      if(triple(0).toString.startsWith("http://it.dbpedia.org") && triple(1).toString.startsWith("http://xmlns.com/foaf/0.1/depiction")) {
        i = i + 1;
        val resource = new DBpediaResource(triple(0).toString)
        val t = triple(2).toString
        val imageList : java.util.LinkedHashSet[String] = imagesMap.get(resource.uri).getOrElse(new LinkedHashSet[String]())
        imageList.add(t)
        imagesMap = imagesMap.updated(resource.uri, imageList)
      }
    }
    LOG.info("Done. Loaded %d images.".format(i))
    imagesMap
  }


  def getSameAsMap_java(sameAsStream : InputStream) : java.util.Map[String,java.util.LinkedHashSet[String]] = {
    LOG.info("Loading sameAs map...")
    var sameAsMap = Map[String,java.util.LinkedHashSet[String]]()
    var i = 0;
    val parser = new NxParser(sameAsStream)
    while (parser.hasNext) {
      val triple = parser.next
      if(triple(2).toString.startsWith("http://dbpedia.org")) {
        i = i + 1;
        val resource = new DBpediaResource(triple(0).toString)
        val t = triple(2).toString
        val sameAsList : java.util.LinkedHashSet[String] = sameAsMap.get(resource.uri).getOrElse(new LinkedHashSet[String]())
        sameAsList.add(t)
        sameAsMap = sameAsMap.updated(resource.uri, sameAsList)
      }
    }
    LOG.info("Done. Loaded %d sameAs.".format(i))
    sameAsMap
  }
    
}