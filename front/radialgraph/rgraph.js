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

var labelType, useGradients, nativeTextSupport, animate;
var rgraph;
var numNodesVar = 6;
var subsetVar = 0;
var statusGraph = false;
var nextNodesButton;
var previousNodesButton;
var backButton;
var backText;
var labelStyle;
var repos = true;
var queryCounter;
var resizing = false;

(function() {
	var ua = navigator.userAgent, iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i), typeOfCanvas = typeof HTMLCanvasElement, nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'), textSupport = nativeCanvasSupport && ( typeof document.createElement('canvas').getContext('2d').fillText == 'function');
	//I'm setting this based on the fact that ExCanvas provides text support for IE
	//and that as of today iPhone/iPad current text support is lame
	labelType = (!nativeCanvasSupport || (textSupport && !iStuff)) ? 'Native' : 'HTML';
	nativeTextSupport = labelType == 'Native';
	useGradients = nativeCanvasSupport;
	animate = !(iStuff || !nativeCanvasSupport);
})();

function getURLParameter(name) {
	return decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]);
}

function initMetadata(metadata) {
	var startingNodes = {
		id : metadata.id,
		name : metadata.name,
		children : [],
	}
	for (var i = 0; i < numNodesVar; i++) {
		if (metadata.children[i] != undefined) {
			startingNodes.children.push(metadata.children[i]);
		}
	}
	return startingNodes;
}

function initRgraph(data) {
	$jit.RGraph.Plot.NodeTypes.implement({
		'icon1' : {
			'render' : function(node, canvas) {
				var pos = node.pos.getc(true);
				var ctx = canvas.getCtx();
				if (ctx) {
					var img = new Image();
					img.src = '../images/background_node.png';
					if (statusGraph == false) {
						img.onload = function() {
							ctx.drawImage(img, pos.x - 54, pos.y - 28);
							statusGraph = true;
						}
					} else if (statusGraph == true) {
						ctx.drawImage(img, pos.x - 54, pos.y - 28);
					}
				}
			},
			'contains' : function(node, pos) {
				var npos = node.pos.getc(true);
				dim = node.getData('dim');
				return this.nodeHelper.square.contains(npos, pos, dim);
			}
		},
	});

	rgraph = new $jit.RGraph({
		// Where append the visualization
		injectInto : 'infovis',
		levelDistance : 85,
		// Optional: create a background canvas and plot
		// concentric circles in it.
		background : {
			CanvasStyles : {
				strokeStyle : '#FFFFFF',
				//shadowBlur : 10,
				shadowColor : '#FFFFFF'
			}
		},
		// Set Edge and Node styles
		Node : {
			overridable : true,
			type : "icon1",
			//'color' : '#00FF00',
			'dim' : 0
		},
		Edge : {
			overridable : true,
			lineWidth : 0.8,
		},

		Navigation : {
			//enable : true,
			//panning: 'avoid nodes',
			//zooming : 20,
		},

		//Add the name of the node in the correponding label
		onCreateLabel : function(domElement, node) {
			domElement.innerHTML = node.name;
			domElement.onclick = function() {
				if (node.id == metadata.id) {
					if (node.id.indexOf("resource#") != -1) {
						log(node.id.indexOf("#"));
						var idSplitted = metadata.id.split("#");
						window.open(idSplitted[1]);
					} else
						window.open(metadata.id);
				} else {
					if (node.id.indexOf("resource#") != -1) {
						removeNodesForNewMetadata(rgraph, node.id);
					}
				}
			};
		},
	});
	rgraph.loadJSON(data);
	rgraph.refresh();
	nextNodesButton = document.getElementById("nextNodes");
	previousNodesButton = document.getElementById("previousNodes");
	backButton = document.getElementById("backButton");
	backText = document.getElementById("backText");

	nextNodesButton.onclick = function() {
		addNewNodes(rgraph, metadata.id, metadata, "nextNodes");
	}

	previousNodesButton.onclick = function() {
		addNewNodes(rgraph, metadata.id, metadata, "previousNodes");
	}
		
	nextNodesExist(metadata);
	previousNodesExist(metadata);
}

function nextNodesExist(jsonObject) {
	if (jsonObject.children[((subsetVar + 1) * numNodesVar)] == undefined) {
		nextNodesButton.onclick = null;
		nextNodesButton.style.cssText = "filter: alpha(opacity=35); -khtml-opacity: 0.35;  -moz-opacity: 0.35; opacity: 0.35;";
	} else {
		nextNodesButton.onclick = function() {
			addNewNodes(rgraph, metadata.id, metadata, "nextNodes");
		};
		nextNodesButton.style.cssText = "filter: alpha(opacity=100); -khtml-opacity: 1;  -moz-opacity: 1; opacity: 1";
	}
}

function previousNodesExist(metadata) {
	if (subsetVar > 0) {
		previousNodesButton.onclick = function() {
			addNewNodes(rgraph, metadata.id, metadata, "previousNodes");
		}
		previousNodesButton.style.cssText = "filter: alpha(opacity=100); -khtml-opacity: 1;  -moz-opacity: 1; opacity: 1";
	} else {
		previousNodesButton.onclick = null;
		previousNodesButton.style.cssText = "filter: alpha(opacity=35); -khtml-opacity: 0.35;  -moz-opacity: 0.35; opacity: 0.35;";
	}
}

function setBackButton(addButton){
	if(addButton){
		backButton.style.cssText = "filter: alpha(opacity=100); -khtml-opacity: 1;  -moz-opacity: 1; opacity: 1;";
		backText.style.cssText = "filter: alpha(opacity=100); -khtml-opacity: 1;  -moz-opacity: 1; opacity: 1; color: black;";
		backText.innerHTML = origindata.name;
		backButton.onclick = function(){
			metadata = origindata;
			var startingNodes = initMetadata(metadata);
			rgraph.loadJSON(startingNodes);
			rgraph.refresh();
			nextNodesExist(metadata);
			previousNodesExist(metadata);
			setBackButton(false);
		}
		backText.onclick = function(){
			metadata = origindata;
			var startingNodes = initMetadata(metadata);
			rgraph.loadJSON(startingNodes);
			rgraph.refresh();
			nextNodesExist(metadata);
			previousNodesExist(metadata);
			setBackButton(false);
		}
	}
	else{
		backButton.style.cssText = "filter: alpha(opacity=0); -khtml-opacity: 0;  -moz-opacity: 0; opacity: 0;";
		backText.style.cssText = "filter: alpha(opacity=0); -khtml-opacity: 0;  -moz-opacity: 0; opacity: 0;";
	}
}

function addNewNodes(rgraph, rootNode, metadata, command) {
	previousNodesButton.disabled = true;
	nextNodesButton.disabled = true;
	if (command == "nextNodes")
		subsetVar++;
	else if (command == "previousNodes")
		subsetVar--;
	var n = rgraph.graph.getNode(rootNode);
	var subnodes = n.getSubnodes(1);
	var map = [];
	for (var i = 0; i < subnodes.length; i++) {
		map.push(subnodes[i].id);
	}
	removeNodes(rgraph, map, command);
}

function chooseNodesSubset(numNodes, subset, data) {
	var jsonData = {
		id : data.id,
		name : data.name,
		children : []
	}
	for (var i = subset * numNodes; i < numNodes * (subset + 1); i++) {
		if (data.children[i]) {
			jsonData.children.push({
				"id" : data.children[i].id,
				"name" : data.children[i].name,
				"data" : {
					"dim" : 0,
					"$type" : "none",
				},
				children : [{
					id : data.children[i].children[0].id,
					name : data.children[i].children[0].name,
				}]
			});
		}
	}
	return jsonData;
}

function setNodesSubset(jsonToModify) {
	var jsonToAdd = [{
		id : jsonToModify.id,
		adjacencies : []
	}];
	for (var i = 0; i < jsonToModify.children.length; i++) {
		jsonToAdd[0].adjacencies.push(jsonToModify.children[i].id);
	}
	for (var j = 0; j < jsonToModify.children.length; j++) {
		var edgeElement = {
			id : jsonToModify.children[j].id,
			name : jsonToModify.children[j].name,
			data : {
				"dim" : 0,
				"$type" : "none",
			},
			adjacencies : [jsonToModify.children[j].children[0].id],
		}
		jsonToAdd.push(edgeElement);
		var nodeElement = {
			id : jsonToModify.children[j].children[0].id,
			name : jsonToModify.children[j].children[0].name,
			adjacencies : [jsonToModify.children[j].id],
		}
		jsonToAdd.push(nodeElement);
	}
	return jsonToAdd;
}

function removeNodes(rgraph, nodes, command) {
	var animType = "fade:seq";
	var animFps = 25;
	repos = false;
	if (command == "nextNodes")
		nodes.reverse();
	if (nodes.length > 0) {
		rgraph.op.removeNode([nodes.pop(), nodes.pop()], {
			type : animType,
			duration : 1,
			fps : animFps,
			hideLabels : false,
			onComplete : function() {
				removeNodes(rgraph, nodes);
			}
		});
	} else {
		//log("Rimozione completata");
		var nodeSubset = chooseNodesSubset(numNodesVar, subsetVar, metadata);
		//log("L'oggetto che raccoglie il nuovo subset di nodi è")
		//log(nodeSubset);
		var newGraph = setNodesSubset(nodeSubset);
		//log("L'oggetto modificato è");
		//log(newGraph);
		addNodes(rgraph, metadata.id, newGraph);
	}
}

function addNodes(rgraph, root, graph) {
	var animType = "fade:seq";
	var animFps = 25;
	var animDuration = 300;
	repos = true;
	rgraph.op.sum(graph, {
		type : animType,
		duration : animDuration,
		//hideLabels : false,
		fps : animFps,
		onComplete : function() {
			if ($("body").css("padding") == "1px")
				$("body").css("padding", "0px");
			else
				$("body").css("padding", "1px");
			nextNodesExist(metadata);
			previousNodesExist(metadata);
		},
	});
}

function getMetadata(nodeName, nodeid, rgraph) {
	queryCounter = 0;
	log("getMetadata called for resource " + nodeName);
	var endPoint;
	var command;
	var language;
	var url = MDH.changeSpecialChars(nodeName);
	url = encodeURIComponent(url);
	if (nodeName.indexOf("http://dbpedia.org/resource") != -1) {
		endPoint = MDH.dbpediaProfile.endpoint;
		command = MDH.dbpediaProfile.command;
		language = "EN";
	} else if (nodeName.indexOf("http://it.dbpedia.org/resource") != -1) {
		endPoint = MDH.dbpediaItProfile.endpoint;
		command = MDH.dbpediaItProfile.command;
		language = "IT";
	} else
		return;
	var objectPropertiesQuery = MDH.setObjectPropertiesQuery(endPoint, command, url, language);
	var dataPropertiesQuery = MDH.setDataPropertiesQuery(endPoint, command, url, language);
	var subjectQuery = MDH.setSubjectQuery(endPoint, command, url);
	var isPrimaryTopicOfQuery = MDH.setIsPrimaryTopicOfQuery(endPoint, command, url);
	var isSubjectOfQuery = MDH.setIsSubjectOfQuery(endPoint, command, url, language);
	callSparqlQueries(objectPropertiesQuery, dataPropertiesQuery, subjectQuery, isPrimaryTopicOfQuery, isSubjectOfQuery, nodeid);
}

function callSparqlQueries(objectPropertiesQuery, dataPropertiesQuery, subjectQuery, isPrimaryTopicOfQuery, isSubjectOfQuery, nodeName) {
	queryCounter = 0;
	var propertiesObj = {
		objectProperties : "",
		dataProperties : "",
		subjectProperties : "",
		isPrimaryTopicOfProperties : "",
		isSubjectOfProperties : ""
	}
	callObjectProperties(objectPropertiesQuery, nodeName, propertiesObj);
	callDataProperties(dataPropertiesQuery, nodeName, propertiesObj);
	callSubjectProperties(subjectQuery, nodeName, propertiesObj);
	callIsPrimaryTopicOfQuery(isPrimaryTopicOfQuery, nodeName, propertiesObj);
	callIsSubjectOfQuery(isSubjectOfQuery, nodeName, propertiesObj);
}

function callObjectProperties(objectPropertiesQuery, nodeName, propertiesObj) {
	log("Il valore di queryCounter è " + queryCounter);
	$.ajax({
		url : objectPropertiesQuery,
		dataType : 'jsonp',
		jsonp : 'callback',
		success : function(data, textStatus, jqXHR) {
			propertiesObj.objectProperties = MDH.parseObjectProperties(data);
			queryCounter++;
			log("Il valore di queryCounter è " + queryCounter);
			checkForCreatingJson(nodeName, propertiesObj);
		},
		error : function() {
			log("Error with the objectPropertiesQuery")
		}
	});
}

function callDataProperties(dataPropertiesQuery, nodeName, propertiesObj) {
	$.ajax({
		url : dataPropertiesQuery,
		dataType : 'jsonp',
		jsonp : 'callback',
		success : function(data, textStatus, jqXHR) {
			propertiesObj.dataProperties = MDH.parseDataProperties(data);
			queryCounter++;
			checkForCreatingJson(nodeName, propertiesObj);
		},
		error : function() {
			log("Error with the objectPropertiesQuery")
		}
	});
}

function callSubjectProperties(subjectQuery, nodeName, propertiesObj) {
	$.ajax({
		url : subjectQuery,
		dataType : 'jsonp',
		jsonp : 'callback',
		success : function(data, textStatus, jqXHR) {
			propertiesObj.subjectProperties = MDH.parseSubjectProperties(data);
			queryCounter++;
			checkForCreatingJson(nodeName, propertiesObj);
		},
		error : function() {
			log("Error with the objectPropertiesQuery")
		}
	});
}

function callIsPrimaryTopicOfQuery(isPrimaryTopicOfQuery, nodeName, propertiesObj) {
	$.ajax({
		url : isPrimaryTopicOfQuery,
		dataType : 'jsonp',
		jsonp : 'callback',
		success : function(data, textStatus, jqXHR) {
			propertiesObj.isPrimaryTopicOfProperties = MDH.parseIsPrimaryTopicOf(data);
			queryCounter++;
			checkForCreatingJson(nodeName, propertiesObj);
		},
		error : function() {
			log("Error with the objectPropertiesQuery")
		}
	});
}

function callIsSubjectOfQuery(isSubjectOfQuery, nodeName, propertiesObj) {
	$.ajax({
		url : isSubjectOfQuery,
		dataType : 'jsonp',
		jsonp : 'callback',
		success : function(data, textStatus, jqXHR) {
			propertiesObj.isSubjectOfProperties = MDH.parseIsSubjectOf(data);
			log("Il risultato di callIsSubjectOfQuery è: ")
			log(propertiesObj.isSubjectOfProperties);
			queryCounter++;
			checkForCreatingJson(nodeName, propertiesObj);
		},
		error : function() {
			log("Error with the isSubjectOfQuery")
		}
	});
}

function checkForCreatingJson(nodeId, propertiesObj) {
	var queryNum = 5;
	if (queryCounter == queryNum) {
		var properties = propertiesObj.objectProperties.concat(propertiesObj.dataProperties, propertiesObj.subjectProperties, propertiesObj.isPrimaryTopicOfProperties, propertiesObj.isSubjectOfProperties);
		metadata = MDH.createRgraphJson(properties, nodeId, "name");
		log("The result of the queries is: ");
		log(metadata);
		subsetVar = 0;
		var nodeSubset = chooseNodesSubset(numNodesVar, subsetVar, metadata);
		var newGraph = setNodesSubset(nodeSubset);
		addNodes(rgraph, metadata.id, newGraph);
		log("The subset of nodes is ");
		log(newGraph);
		log("The id of the new root node is ");
		log(metadata.id);
		nextNodesExist(metadata);
		previousNodesExist(metadata);
		setTimeout(function(){
			//setBackButton(true);
		},200);
	}
}

function removeNodesForNewMetadata(rgraph, id) {
	var animType = "fade:seq";
	var animFps = 25;
	var nodesToRemove = new Array();
	var nextRootNode = rgraph.graph.getNode(id);
	var nextRootNodeParents = nextRootNode.getParents();
	rgraph.graph.eachNode(function(node) {
		if (node.id != id && node.id != metadata.id && node.id != nextRootNodeParents[0].id) {
			nodesToRemove.push(node.id);
		}
	});
	rgraph.op.removeNode(nodesToRemove, {
		type : animType,
		duration : 600,
		fps : animFps,
		hideLabels : false,
		onComplete : function() {
			rgraph.onClick(id, {
				hideLabels : false,
				duration : 600,
				onComplete : function() {
					var remainingNodes = new Array();
					rgraph.graph.eachNode(function(node) {
						if (node.id != id) {
							remainingNodes.push(node.id);
						}
					});
					rgraph.op.removeNode(remainingNodes, {
						type : animType,
						duration : 600,
						fps : animFps,
						hideLabels : false,
						onComplete : function() {
							var idSplitted = id.split("#");
							getMetadata(idSplitted[1], id, rgraph);
						}
					});
				}
			});
		}
	});
}