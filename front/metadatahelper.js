/*
 * TellMeFirst - A Knowledge Discovery Application.
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Giuseppe Futia
 * 
 */


var MDH = {}

MDH.dbpediaProfile = {};
MDH.dbpediaProfile.endpoint = 'http://dbpedia.org/sparql';
MDH.dbpediaProfile.command = '?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=';

MDH.dbpediaItProfile = {};
MDH.dbpediaItProfile.endpoint = 'http://it.dbpedia.org/sparql';
MDH.dbpediaItProfile.command = '?default-graph-uri=&query=';


MDH.uris = ["http://dbpedia.org/ontology/",
			"http://dbpedia.org/property/",
			"http://dbpedia.org/resource/",
			"http://purl.org/dc/terms/",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			"http://dbpedia.org/class/yago/",
			"http://xmlns.com/foaf/0.1/",
			];	

MDH.propertiesToIgnore = {"http://dbpedia.org/ontology/abstract": "1",
						  "http://dbpedia.org/ontology/wikiPageDisambiguates": "1",
						  "http://dbpedia.org/ontology/thumbnail": "1",
						  "http://dbpedia.org/property/alexa":"1",
						  "http://dbpedia.org/property/hasPhotoCollection": "1",
						  "http://dbpedia.org/property/name": "1",
				}

MDH.setObjectPropertiesQuery = function(endPoint, command, resource, language){
	var query = "SELECT ?subject ?relation ?relationLabel ?object ?objectLabel WHERE { "+
	             "<"+resource+"> ?relation ?object."+
                 //"<"+resource+"> <http://www.w3.org/2000/01/rdf-schema%23label> ?subjectLabel . "+
                 "?relation <http://www.w3.org/2000/01/rdf-schema%23label> ?relationLabel . "+
                 "?object <http://www.w3.org/2000/01/rdf-schema%23label> ?objectLabel . " + 
                 "FILTER (langMatches(lang(?relationLabel), \'"+language+"\')) . " + 
                 "FILTER (langMatches(lang(?objectLabel), \'"+language+"\')) . " + 
                  "}" 
	var sparqlQuery = endPoint+command+query+"&format=json";
	return sparqlQuery;
}

MDH.setDataPropertiesQuery = function(endPoint, command, resource, language){
	var query = "SELECT ?subject ?relation ?relationLabel ?dataObject WHERE {" +
				"<"+resource+"> ?relation ?dataObject ." +
				//"<"+resource+"> <http://www.w3.org/2000/01/rdf-schema%23label> ?subjectLabel ." +
				"?relation <http://www.w3.org/2000/01/rdf-schema%23label> ?relationLabel . " +
				"?relation <http://www.w3.org/1999/02/22-rdf-syntax-ns%23type> <http://www.w3.org/2002/07/owl%23DatatypeProperty>" +
				"FILTER (langMatches(lang(?relationLabel), \'"+language+"\')) . " +
				"FILTER (langMatches(lang(?dataObject), \'"+language+"\')) . " +
				"}"
	var sparqlQuery = endPoint+command+query+"&format=json";
	return sparqlQuery;			
}

MDH.setSubjectQuery = function(endPoint, command, resource){
	var query = "SELECT ?object ?objectLabel WHERE {" +
				"<"+resource+"> <http://purl.org/dc/terms/subject> ?object ." +
				"?object <http://www.w3.org/2000/01/rdf-schema%23label> ?objectLabel ."+
				"}"
	var sparqlQuery = endPoint+command+query+"&format=json";
	return sparqlQuery;			
}

MDH.setIsPrimaryTopicOfQuery = function(endPoint, command, resource){
	var query = "SELECT ?object WHERE {"+
				"<"+resource+"> <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> ?object ." +
				"}"
	var sparqlQuery = endPoint+command+query+"&format=json";
	return sparqlQuery;	
} 

MDH.setIsSubjectOfQuery = function(endPoint, command, resource, language){
	var query = "SELECT ?subject ?subjectLabel WHERE {" +
				"?subject <http://purl.org/dc/terms/subject> <"+resource+"> ." +	
				"?subject <http://www.w3.org/2000/01/rdf-schema%23label> ?subjectLabel . " +
				"FILTER (langMatches(lang(?subjectLabel), \'"+language+"\')) . " +
				"}"
	
	var sparqlQuery = endPoint+command+query+"&format=json";
	return sparqlQuery;	
}

MDH.parseObjectProperties = function(data){
	var properties = new Array();
	for(var j = 0; j<data.results.bindings.length; j++){
		if(!MDH.ignoreProperty(data.results.bindings[j].relation.value,MDH.propertiesToIgnore)){
			var relation = data.results.bindings[j].relation.value;
			var relationLabel = data.results.bindings[j].relationLabel.value;
			var object = data.results.bindings[j].object.value;
			var objectLabel = data.results.bindings[j].objectLabel.value;
			properties.push(new MDH.metadataObject(relation, relationLabel, object, objectLabel));
		}
	}
	return properties;	
}

MDH.parseDataProperties = function(data){
	var properties = new Array();
	for(var j = 0; j<data.results.bindings.length; j++){
		if(!MDH.ignoreProperty(data.results.bindings[j].relation.value,MDH.propertiesToIgnore)){
			var relation = data.results.bindings[j].relation.value;
			var relationLabel = data.results.bindings[j].relationLabel.value;
			var object = undefined;
			var objectLabel = data.results.bindings[j].dataObject.value;
			properties.push(new MDH.metadataObject(relation, relationLabel, object, objectLabel));
		}
	}
	return properties;
}

MDH.parseSubjectProperties = function(data){
	var properties = new Array();
	for(var j = 0; j<data.results.bindings.length; j++){
		properties.push(new MDH.metadataObject("http://purl.org/dc/terms/subject", "subject", data.results.bindings[j].object.value, data.results.bindings[j].objectLabel.value));
	}
	return properties;	
}

MDH.parseIsPrimaryTopicOf = function(data){
	var properties = new Array();
	for(var j = 0; j<data.results.bindings.length; j++){
		var relation = "http://xmlns.com/foaf/0.1/isPrimaryTopicOf"
		var relationLabel = "primary<br>Topic Of";
		var object = data.results.bindings[j].object.value;
		var objectLabel = (data.results.bindings[j].object.value)//.substring(0,7)+"...";
		objectLabel = [objectLabel.slice(0, 14), " ", objectLabel.slice(14)].join('');
		objectLabel = [objectLabel.slice(0, 30), " ", objectLabel.slice(30)].join('');
		if(objectLabel.length>39)
			objectLabel = objectLabel.substring(0,39)+"...";
		properties.push(new MDH.metadataObject(relation, relationLabel, object, objectLabel));
	}
	return properties;
}

MDH.parseIsSubjectOf = function (data){
	var properties = new Array();
	for(var j = 0; j<data.results.bindings.length; j++){
		properties.push(new MDH.metadataObject("http://purl.org/dc/terms/subject", "is subject of", data.results.bindings[j].subject.value, data.results.bindings[j].subjectLabel.value));
	}
	return properties;
}

MDH.metadataObject = function(relation,relationLabel,object,objectLabel){
	this.relation = relation;
	this.relationLabel = relationLabel;
	this.object = object;
	this.objectLabel = objectLabel;
}

MDH.changeSpecialChars = function(url){
	if(url.indexOf("%28")>-1)
		url = url.replace("%28","(");
	if(url.indexOf("%29")>-1)
		url = url.replace("%29",")");
	if(url.indexOf("%27")>-1)
		url = url.replace("%27","'");
	return url;
}
				
MDH.ignoreProperty = function(property,propertiesToIgnore){
	if(propertiesToIgnore[property]!=undefined)
		return true;
	else return false;
}

MDH.objectExists = function(propertiesObj,object){
	for (var i in propertiesObj){
		if(propertiesObj[i]==object)
			return true;	
	}
	return false;
}

MDH.uriToString = function(uris,uriToCheck){
	for (var i = 0; i<uris.length; i++){
		//TMF.log(uris[i]);
		if(uriToCheck.indexOf(uris[i]) != -1){
			uriToCheck = uriToCheck.replace(uris[i],"");
			return uriToCheck;
		}
	}
	return uriToCheck;
}

MDH.createRgraphJson = function(queryResult,idNode,nameNode){
    var idCount = 0;
    var jsonData = {
    	id: idNode,
    	name: "<span class='clickable'>"+nameNode+"</span>",
    	children: []
    }
    
    for(var j=0; j<queryResult.length; j++){
    	if(queryResult[j].object == undefined){ //For managing the visualization of Data Properties
    		jsonData.children.push({
    			"id": "#"+queryResult[j].relation+"#"+j,
    			"name": "<span style='color:black;'>"+queryResult[j].relationLabel+"</span>", //"<a href='"+queryResult[j].relation+"' target='_blank' style='color:black;'>"+queryResult[j].relationLabel+"</a>",
    			"data":{
    				"dim": 0,
    				"$type": "none",
    			},
    			children: [{
    				"id": "#"+queryResult[j].objectLabel+"#"+j,
    				"name": queryResult[j].objectLabel,
    			}]
    		});
    	} else if(queryResult[j].relationLabel == "subject"){ //For managing the visualization of Subject Properties
    		jsonData.children.push({
    			"id": "#"+queryResult[j].relation+"#"+j,
    			"name": "<span style='color:black;'>subject</span>", //"<a href='"+queryResult[j].relation+"' target='_blank' style='color:black;'>subject</a>",
    			"data":{
    				"dim": 0,
    				"$type": "none",	
    			},
    			children: [{
    				"id": "resource#"+queryResult[j].object+"#"+j,
    				"name": "<span class='clickable'>"+queryResult[j].objectLabel+"</span>", //"<a href='"+queryResult[j].object+"' target='_blank'>"+queryResult[j].objectLabel+"</a>"
    			}]
    		});
    	} else if(queryResult[j].relation=="http://xmlns.com/foaf/0.1/isPrimaryTopicOf"){ //For managing visualization of isPrimaryTopicOf
    		jsonData.children.push({
    			"id": "#"+queryResult[j].relation+"#"+j,
    			"name": "<span style='color:black;'>"+queryResult[j].relationLabel+"</span>", //"<a href='"+queryResult[j].relation+"' target='_blank' style='color:black;'>"+queryResult[j].relationLabel+"</a>",
    			"data":{
    				"dim": 0,
    				"$type": "none",	
    			},
    			children: [{
    				"id": "#"+queryResult[j].object+"#"+j,
    				"name": "<a href='"+queryResult[j].object+"' target='_blank'>"+queryResult[j].objectLabel+"</a>",
    			}]
    		});	
    	}
    	else{ //For managing the visualization of all other Properties
    		jsonData.children.push({
    			"id": "#"+queryResult[j].relation+"#"+j,
    			"name": "<span style='color:black;'>"+queryResult[j].relationLabel+"</span>", //"<a href='"+queryResult[j].relation+"' target='_blank' style='color:black;'>"+queryResult[j].relationLabel+"</a>",
    			"data":{
    				"dim": 0,
    				"$type": "none",	
    			},
    			children: [{
    				"id": "resource#"+queryResult[j].object+"#"+j,
    				"name": "<span class='clickable'>"+queryResult[j].objectLabel+"</span>",//"<a href='"+queryResult[j].object+"' target='_blank'>"+queryResult[j].objectLabel+"</a>",
    			}]
    		});			
    	}
    }
    return jsonData; 
}

/**var wrapLabel = function(str, width, brk, cut ){
    brk = brk || '\n';
    width = width || 75;
    cut = cut || false;
 
    if (!str) { return str; }
 
    var regex = '.{1,' +width+ '}(\\s|$)' + (cut ? '|.{' +width+ '}|.+$' : '|\\S+?(\\s|$)');
 
    return str.match( RegExp(regex, 'g') ).join( brk );
}**/
