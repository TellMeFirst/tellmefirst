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
 * @author Federico Benedetto
 * 
 */


var getWikiscrap = function(wikipedia_title, language) {
	var entryPoint = language ? "http://"+language+".wikipedia.org/w/api.php?redirects&callback=?":"http://en.wikipedia.org/w/api.php?redirects&callback=?";
	var title = wikipedia_title;
	var pageHTML = "";
	var pageDOM;
	var cleanData;
	var onLoaded; //callback function to be called when finished loading Wikipedia page DOM
	var debug = false;

	/*
	PRIVATE METHODS
	*/
	var retrievePage = function() {
		$.getJSON(entryPoint, {
			"action": "parse",
			"page": title,
			"format": "json"
		},jsonpCallback)
	}

	var jsonpCallback = function(obj) {
		//TODO: manage errors
		var clean_data = removeImgTags(obj["parse"]["text"]["*"]);
		clean_data = removeATags(clean_data);
		clean_data = removeSupTags(clean_data);
		cleanData = removeMiscTags(clean_data);
		pageDOM = $("<div>").html(cleanData);
		log(pageDOM);
		$('table',pageDOM).remove(); //Remove tables
		$('div.references,\
		 	div.reflist,\
		 	div.seealso,\
		 	h2:has(span#Voci_correlate) + ul, \
		 	h2:has(span#Collegamenti_esterni) + ul, \
		 	div#interProject + ul, \
		 	div#interProject',pageDOM).remove(); //Remove references
		$('div.thumb',pageDOM).remove() // Remove thumbs
		$('.editsection',pageDOM).remove() //Remove editSections
		onLoaded();
	}

	var log = function(msg) {
		if(debug) {
			console.log(msg)
		}
	}

	var removeImgTags = function(string) {
		var re = /<img.*?\/>/g;
		var result = string.replace(re,"");
		return result;
	}

	var removeATags = function(string) {
		var result = "";
		result =  string.replace(/<a .*?>/g,"");
		result = result.replace(/<\/a>/g,"")
		return result;
	}

	var removeSupTags = function(string) {
		var result = "";
		result = string.replace(/<sup.*?\/sup>/g,"");
		return result;
	}

	var removeMiscTags = function(string) {
		var result="";
		result = string.replace(/<b>Notes<\/b>|<b>Citations<\/b>|<b>Bibliography<\/b>/g,"");
		//result = result.replace(/<span.*?>/g,"");
		//result = result.replace(/<\/span>/g,"");
		return result;
	}

	/*
	PUBLIC METHODS
	*/
	var init = function(callback) {
		onLoaded = callback;
		retrievePage();
	}

	var toSimpleHTML = function() {
		var tempDiv = $('<div>');
		tempDiv.append("<h1>"+title+"</h1>");
		var firstDiv = $('p:first', pageDOM);
		tempDiv.append($(firstDiv.prop('outerHTML')));
		var nextDivs = firstDiv.nextUntil("h1, h2, h3, h4");
		nextDivs.each(function() {
			tempDiv.append($(this).prop('outerHTML'));
		});
		$(".mw-headline",pageDOM).each(function (i) {
			var tagName = $(this).parent().get(0).tagName;
			tempDiv.append($("<"+tagName+">").html($(this).html()+" "));
			var siblings = $(this).parent().nextUntil("h1, h2, h3, h4","p,ul,ol");
			log(siblings);
			siblings.each(function() {
				tempDiv.append($(this));
			});
			if (siblings.length==0) {
				if(!$(this).parent().next().get(0) || $(this).parent().next().get(0).tagName==$(this).parent().get(0).tagName)
					$(':last',tempDiv).remove();
			}
		});
		//Remove empty paragraphs
		log(tempDiv.html());
		return tempDiv.html();
	}

	var toRawText = function() {
		var rawText = "";
		if(pageDOM) {
			$("p, ul, ol",pageDOM).each(function () {
				rawText+=($(this).html());
			});
			return rawText;
		} else {
			log("toRawText error: pageDOM not yet loaded");
		}
	};

	return { //RETURNS ALL THE PUBLIC METHODS AND VARIABLES
		init: init,
		toSimpleHTML: toSimpleHTML,
		toRawText: toRawText
	}
};