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
 * @author Giuseppe Futia
 * 
 */

var TMF = {};

//For debugging
TMF.debug = false;
TMF.localUse = false;
TMF.timeLog = false;
TMF.metadataLog = true;

//For checking response times
TMF.startTime;
TMF.data;

TMF.displayedImages = [];
TMF.defaultImages = [];
for (var i=7; i>0; i--)
    TMF.defaultImages.push("default_images/default_img_0"+i+".jpg");
TMF.minImgWidth = 150;
TMF.minImgHeight = 150;
TMF.numOfLoadedImages = 0;
TMF.finishedLoadingImages = false;
//TMF.getVideoList = ["DBpedia:Actor","DBpedia:Activity","DBpedia:Band","DBpedia:Artist","DBpedia:Athlete","DBpedia:MusicalWork","Freebase:/celebrities","Freebase:/film/actor"];
//TMF.getVideoBlackList = ["DBpedia:Writer"];
TMF.getMapList = ["DBpedia:Place"];
TMF.getMapBlackList = [];

$('document').ready( function () {
	TMF.init();
});

TMF.init = function() { //Initialize functions, register event listeners, etc
	$("input:file, button").uniform();
	$("#uploadButton").click(TMF.handleFormSubmit);
	TMF.initLanguageFlags();
	$('input:radio').change(TMF.initLanguageFlags);
	$('#mainForm').ajaxForm({
		beforeSend: TMF.ajaxFormBeforeSend,
		success: TMF.ajaxFormSuccess,
		error: TMF.ajaxFormError,
		uploadProgress: TMF.ajaxFormUploadProgress
	});
	$('.loadingwrapper').css('rotateY',Math.PI);
	$('#accordion').accordion({
		change: TMF.onAccordionChange
	});
}

TMF.onAccordionChange = function(event, ui) {
	var oldHeaderID = ui.oldHeader.attr('id');
	if(oldHeaderID=="fileHeader") {
		$("#file").val("");
	} else if (oldHeaderID=="textHeader") {
		$("#text").val("");
	} else { //id=urlHeader
		$("#formUrl").val("");
	}
}

TMF.setLanguageService = function(){
	var defaultLang = "english";
	var service = $('input:radio:checked').val() == defaultLang ? "../ajax_it/rest/" : "../ajax_en/rest/";
	return service;
}

TMF.initLanguageFlags = function() {
	$('label img').css('box-shadow','none');
	$('input:radio:checked').each(function () {
		$('label[for='+$(this).val()+'] img').css('box-shadow','0 0 5px 5px white');
		var action = TMF.setLanguageService()+"classify";
		if(!TMF.localUse)
			$("#mainForm").attr("action", action);
	});
}

TMF.handleFormSubmit = function () {
	TMF.startTime = new Date().getTime();
	if($('#file').val()=="" && $('#text').val()=="" && $("#formUrl").val()=="") {
		//TMF.showError("Insert a text, an url or upload a file");
		return false;
	} else {
		if($('#file').val()=="") {
			//$('#file').val('C:\fakepath\fakefile.pdf');
		}
		$('#fileName').val($('#file').val().replace(/C:\\fakepath\\/i,''));
		var extension = $('#file').val().substr( ($('#file').val().lastIndexOf('.') +1) );
		if(extension == "epub") {
			var action = TMF.setLanguageService()+"classifyEpub";
			$("#mainForm").attr("action", action);
		}
		if($('#text').val()=="" && $('#formUrl').val()=="") {
			if ($("#file")[0].files[0].size>3145728) { //Filesize > 3 MB
				$(".formwrapper").animate({
					rotateY: "+="+Math.PI
				},500);
				$(".loadingwrapper").show();
				$("#mainForm, #instructionP").hide();
				TMF.showError("The file is too big. Try with one smaller than 3 MB");
				return false;
			}
			$('#text').remove();
			$('#formUrl').remove();
		} else {
			if ($('#text').val()=="") {
				$('#text').remove();
			} else {
				$('#formUrl').remove();
				//TMF.log("Word count: "+$('#text').val().split(" ").length);
				if ($('#text').val().split(" ").length>19999) {
					$(".formwrapper").animate({
						rotateY: "+="+Math.PI
					},500);
					$(".loadingwrapper").show();
					$("#mainForm, #instructionP").hide();
					TMF.showError('This is just a demo, try with less than 20000 words are allowed as input text');
					return false;
				}
			}
		}
		return true;
	}
}

TMF.ajaxFormSuccess= function(data, textStatus, jqXHR) {
	if(typeof data == "string")
		eval("data="+data);
	if(data) {
		TMF.data = data;
		TMF.splitMergedTypes();
		TMF.writeLabels(data.Resources, TMF.showGrid);
	}
	else {
		TMF.showError("Error in response from server");
	}
}

TMF.splitMergedTypes = function() {
	for (var i=0; i<7; i++) {
		var mergedTypes = TMF.data.Resources[i]["@mergedTypes"].replace(/#$/,"");
		TMF.data.Resources[i]["@mergedTypesString"] = TMF.data.Resources[i]["@mergedTypes"];
		TMF.data.Resources[i]["@mergedTypes"] = mergedTypes.split("#");
		TMF.log(mergedTypes);
	}
}

TMF.ajaxFormError = function(jqXHR, textStatus, errorThrown) {
	var errorString = jqXHR.getResponseHeader('TMF-error');
	if (errorString) {
		TMF.showError(errorString)
	} else {
		TMF.showError(errorThrown);
	}
}

TMF.imageLoadError = function(jqXHR, textStatus, errorThrown) {
	TMF.numOfLoadedImages++;
	TMF.showError(errorThrown);
	return;
}

TMF.ajaxFormBeforeSend = function(jqXHR, settings) {
	jqXHR.setRequestHeader("Accept","application/json");
	$(".formwrapper").animate({
		rotateY: "+="+Math.PI
	},500);
	$(".loadingwrapper").show();
	$("#mainForm, #instructionP").hide();
}

TMF.ajaxFormUploadProgress = function(event, position, total, percent) {
	TMF.log(percent)
}

TMF.showGrid = function() {
    $(window).unbind('resize',adjustHeight);
	$('#formcontainer').fadeOut(2000);
	$('#errorbar').fadeOut(2000);
	$('#gridcontainer').fadeIn(2000);	
	
    //$('body').css('-moz-transform','');
}

TMF.writeLabels = function(resources, callback) {
	callback();
	for (var i = 0; i < resources.length; i++) {
		$('#label'+(i+1)).html(/*'<a href="'+resources[i]['@uri']+'" target="_blank" id="dbpedialink"'+(i+1)+'>'+*/resources[i]['@label']/*+'</a>'*/);
		/*$('#label'+(i+1)).click(function(k){
			return function(event) {
				event.stopPropagation();
				if(TMF.finishedLoadingImages) {
					$('#dbpedialink'+k).click();
				}
			}
		}(i+1));*/
	}
	TMF.loadImages();
}

TMF.loadImages = function() {
	if (TMF.data) {
		for (var i = 0; i < TMF.data.Resources.length; i++) {
			if(TMF.data.Resources[i]["@image"]!="") {
				TMF.checkImageFromJson(i);
			}
			else {
				var action;
				if(TMF.localUse)
					action = '/json/getImage.json';
				else
					action = TMF.setLanguageService()+"getImage";
				jQuery.ajax(action, {
					type: 'GET',
					//data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@label']},
					data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@title']},
					success: TMF.imgPublishClosure(i+1),
					error: TMF.imageLoadError
				});
			}
		}
	} else {
		TMF.log("Error: called TMF.loadImages without data");
	}
}

TMF.checkImageFromJson = function(i){
	var img = new Image();
	img.addEventListener('load', function(){
		if(img.width>=100 && img.height>=100){
			TMF.publishImage(i+1, $("#content"+(i+1)), TMF.data.Resources[i]["@image"]);
		}
		else{
			var action;
			if(TMF.localUse)
				action = '/json/getImage.json';
			else
				action = TMF.setLanguageService()+"getImage";
			jQuery.ajax(action, {
				type: 'GET',
				//data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@label']},
				data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@title']},
				success: TMF.imgPublishClosure(i+1),
				error: TMF.imageLoadError
			});
		}
	});
	img.addEventListener('error', function(){
		var action;
		if(TMF.localUse)
			action = '/json/getImage.json';
		else
			action = TMF.setLanguageService()+"getImage";
		jQuery.ajax(action, {
		type: 'GET',
		//data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@label']},
		data: {uri: TMF.data.Resources[i]['@uri'], label:TMF.data.Resources[i]['@title']},
		success: TMF.imgPublishClosure(i+1),
		error: TMF.imageLoadError
		});
	});
	img.src = TMF.data.Resources[i]["@image"];
}

TMF.showError = function(msg) {
	TMF.log("******ERROR******");
	TMF.log(msg);
	TMF.log("******ERROR******");
	//$('body').append('<a href="#errorText" class="err" rel="err" title="Oops! Something went wrong!?!?"></a>');
	//$('body').append('');
	//TMF.initErrorFancyBox();
	
	$("#errormessage p").html(msg);
	$("#errormessage").show("slow");
}

TMF.log = function(msg) {
	if(TMF.debug) {
		console.log(msg);
	}
}

TMF.logTime = function(msg) {
	if(TMF.timeLog){
		console.log(msg)
	}	
}

TMF.logMetadata = function(msg){
	if(TMF.metadataLog){
		console.log(msg);
	}
}

TMF.imgPublishClosure = function(i) {
	return function(data, textStatus, jqXHR) {
		TMF.publishImage(i, $("#content"+i),data[0]["@imageURL"]);
	}
}

TMF.publishImage = function(i, box, url) {
	var img = new Image();
	img.addEventListener('load',function () {
		/*if (img.width>=TMF.minImgWidth || img.height>=TMF.minImgHeight) {
			if (((box.width()/img.width)*img.height)<box.height() ) {
				box.css('background-size','auto 100%');
			}
			if(TMF.displayedImages[url]) {
				url = TMF.defaultImage;
			} else {
				TMF.displayedImages[url] = 1;
			}
			box.css('background-image','url('+url+')');
			$('#imglink'+i).attr('href',url);
			$('#imglink'+i).attr('title',TMF.data.Resources[i-1]['@label']);
			TMF.loadedNewImage();*/
		if (((box.width()/img.width)*img.height)<box.height() ) {
			box.css('background-size','auto 100%');
		}
		if(TMF.displayedImages[url]) {
			url = "./"+TMF.defaultImages.pop();
		} else {
			/*TMF.getBetterImage(i,url);*/
			TMF.displayedImages[url] = 1;
		}
		// If there are any problems with the encoding of special characters you can visit: http://en.wikipedia.org/wiki/Percent-encoding
		if(url.indexOf("(")>-1)
			url = url.replace("(","%28");
		if(url.indexOf(")")>-1)
			url = url.replace(")","%29");
		if(url.indexOf("'")>-1)
			url = url.replace("'","%27");
		TMF.log("L'url dell'immagine è: "+url+" e si colloca nella posizione");
		box.css('background-image','url('+url+')');
		$('#imglink'+i).attr('href',url);
		$('#imglink'+i).attr('title',TMF.data.Resources[i-1]['@label']+' - Image');
		TMF.loadedNewImage();
	});
	img.src = url;
}

TMF.initFancyBox = function() {
	for(var i = 1; i<=7; i++){
		$(".group"+i).fancybox({
			nextMethod : 'resizeIn',
			nextSpeed : 400,
			prevMethod : 'resizeOut',
			prevSpeed : 400,
			scrolling : 'no',
			autoScale : true,
			autoSize: true,
			height: 'auto',
			afterShow: function() {
				var content = this.content;
				//TMF.log(content);
				$('.fancybox-wrap').each(function() {
					var logo_url = "";
					if (content=="video") {
						logo_url = "images/youtube_logo.png";
					} else if (content=="iconvis") {
						logo_url = "images/dbpedia_logo.png";
					} else if (content=="map") {
						logo_url = "images/map_logo.png";
					} else if (!content) {
						logo_url = "images/wikimedia_logo.png";
					} else if ($(content).attr('class')=='wikiText') {
						logo_url = "images/wikipedia_logo.png";
						//$(content).parent().attr("style","overflow:auto; overflow-x: hidden;");
                        $('.fancybox-inner').lionbars();
					} else if ($(content).attr('class')=='nyTimesNews') {
						logo_url = "images/nytimes_logo.png";
						//$(content).parent().attr("style","overflow:auto; overflow-x: hidden;");
                        $('.fancybox-inner').lionbars();
					}
					$(this).append('<div class="topleftlogo"><div class="inside" style="background-image:url('+logo_url+')"></div></div>');
				});
				if(this.index == 0)
					setTimeout(function(){$(".fancybox-prev span").css("visibility","hidden")},1);
				else if(this.index == this.group.length-1)
					setTimeout(function(){$(".fancybox-next span").css("visibility","hidden")},1);	
			},
			beforeLoad: function(){
				var content = this.content;
					if (content=="video"){
						this.scrolling = 'no';
						this.width=700;
						this.height=400;
					}
					else if (content=="iconvis"){
						this.scrolling = 'no';
						this.width=600;
						this.height=525;
					} else if (content=="map") {
						this.scrolling = 'no';
						this.width=700;
						this.height=460;
					}
			},
			
			helpers: {
				buttons	: {}
			}
		});
		$("#content"+i).on('click',function(k){
			return function() {
				$('.group'+k).eq(0).click();
			}
		}(i));
	}
}

TMF.initErrorFancyBox = function(){
	$(".err").fancybox({
		openEffect: 'elastic',
		openSpeed: 2000,
		openEasing: 'swing'
	});	
	$(".err").eq(0).click();
}

TMF.getBetterImage = function (i, badImageUrl) {
	TMF.log("called getBetterImage for image "+badImageUrl);
	if (TMF.data) {
		var action;
		if(localeUse)
			action = '/json/getBetterImage.json'
		else
			action = TMF.setLanguageService()+"getBetterImage";
		jQuery.ajax(action, {
			type: 'GET',
			data: {uri: TMF.data.Resources[i-1]["@uri"], label:TMF.data.Resources[i-1]['@label'], badImage: badImageUrl},
			success: function(data, textStatus, jqXHR) {
				TMF.publishBetterImage(i,data[0]['@imageURL']);
			},
			error: TMF.imageLoadError
		});
	} else {
		TMF.log('getBetterImage called, but no data available');
	}
}

TMF.publishBetterImage = function (i, url) {
	var img = new Image();
	var box = $('#content'+i);
	img.addEventListener('load',function () {
		if (((box.width()/img.width)*img.height)<box.height() ) {
			box.css('background-size','auto 100%');
		}
		if(TMF.displayedImages[url]) {
			url = TMF.defaultImage;
		} else {
			TMF.displayedImages[url] = 1;
		}
		box.css('background-image','url('+url+')');
		$('#imglink'+i).attr('href',url);
		$('#imglink'+i).attr('title',TMF.data.Resources[i-1]['@label']);
		TMF.loadedNewImage();
	});
	img.src = url;
}

TMF.loadedNewImage = function () {
	TMF.numOfLoadedImages++;
	TMF.log("Loaded images: "+TMF.numOfLoadedImages);
	if(TMF.numOfLoadedImages==7) {
		//Finished loading all the images!
		TMF.finishedLoadingImages = true;
		TMF.enhanceContents(); //Starts the enhance phase
		TMF.logTime("Ho finito di caricare le immagini");
		var endTime = new Date().getTime();
		TMF.logTime((endTime - TMF.startTime)/1000+" secondi");
	}
}

TMF.enhanceContents= function() {
	for(var i=0; i<7; i++) { //For each Resource
		//Get Text is called for every resource
		TMF.getText(i);
		//Get Metadata for every resource
		TMF.getMetadata(i); 
		//Get News is called for every resource
		TMF.getNews(i);
		//Get Video is called for resources contained in list and is not Writer
		//if(TMF.needsVideo(TMF.data.Resources[i]["@mergedTypes"])) {  S
		TMF.getVideo(i);
		//}
		//Get Map is called for resources contained in list
		if(TMF.needsMap(TMF.data.Resources[i]["@mergedTypes"])) {
			TMF.getMap(i);
		}
	}
}

TMF.needsVideo= function(mergedTypes) {
	for (type in mergedTypes) {
		if (jQuery.inArray(mergedTypes[type],TMF.getVideoBlackList)!=-1) {
			TMF.log("Resource is in Video Blacklist: "+mergedTypes[type]);
			return false;
		}
	}
	for (type in mergedTypes) {
		if(jQuery.inArray(mergedTypes[type],TMF.getVideoList)!=-1) {
			return true;
		}
	}
	return false;
}

TMF.needsMap = function(mergedTypes) {
	for (type in mergedTypes) {
		if (jQuery.inArray(mergedTypes[type],TMF.getMapBlackList)!=-1) {
			return false;
		}
	}
	for (type in mergedTypes) {
		if(jQuery.inArray(mergedTypes[type],TMF.getMapList)!=-1) {
			return true;
		}
	}
}

TMF.getText = function(i) {
	TMF.log("getText called for resource "+i)
	if(TMF.getDbpediaLanguage(TMF.data.Resources[i]["@uri"])=="it" && $("input[name=lang]:checked").val() == "italian") {
		//call directly wikipedia with "it"
		TMF.queryWikipedia(TMF.data.Resources[i]["@title"],"it",i);
	} else if (TMF.getDbpediaLanguage(TMF.data.Resources[i]["@uri"])=="en" && $("input[name=lang]:checked").val() == "english") {
		//call directly wikipedia with "en"
		TMF.queryWikipedia(TMF.data.Resources[i]["@title"],"en",i);
	} else if (TMF.getDbpediaLanguage(TMF.data.Resources[i]["@uri"])=="en" && $("input[name=lang]:checked").val() == "italian") {
		//TODO: call getText services
		TMF.getTextRemote(i);
	} else {
		TMF.log("getText in the last case, shouldn't be here!");
	}
}

TMF.getTextRemote = function(i) {
	//Call remote getText service
	TMF.log("Calling remote getText service for resource "+i);
	if (TMF.data) {
		var action;
		if(TMF.localUse)
			action = '/json/getText.json'
		else
			action = TMF.setLanguageService()+"getText";
		jQuery.ajax(action, {
			type: 'GET',
				data: {uri: TMF.data.Resources[i]['@uri'], lang:$("input[name=lang]:checked").val()}, //The lang should be italian
				success: function(data, textStatus, jqXHR) {
					var titleResp = data[0]["@title"];
					if (!titleResp) {
						TMF.getAbstract(i);
					} else {
						TMF.queryWikipedia(titleResp,"it",i);	
					}
				},
				error: TMF.showError
			});
	} else {
		TMF.log("Error: called TMF.getTextRemote without data");
	}
}

TMF.getDbpediaLanguage = function(url) {
	var re = /^http:\/\/it./;
	if (re.test(url)) {
		return "it";
	} else {
		return "en";
	}
}

TMF.queryWikipedia = function(title, lang, i) { //lang: it or en
	TMF.log("CALLING WIKISCRAP")
	var ws = getWikiscrap(title,lang);
	ws.init(function() {
		var textHtml = ws.toSimpleHTML();
		if (TMF.isTextAcceptable(textHtml))
			TMF.publishText(textHtml,i);
		else
			TMF.getAbstract(i);
	});
}

TMF.isTextAcceptable = function(text) {
	return /<p>.+<\/p>/m.test(text);
}

TMF.getAbstract = function(i) {
	TMF.log("Calling getAbstract service for resource "+i);
	if (TMF.data) {
		var action;
		if(TMF.localUse)
			action = '/json/getAbstract.json'
		else
			action = TMF.setLanguageService()+"getAbstract";
		jQuery.ajax(action, {
			type: 'GET',
				data: {uri: TMF.data.Resources[i]['@uri'], lang:$("input[name=lang]:checked").val()}, //The lang should be italian
				success: function(data, textStatus, jqXHR) {
					TMF.publishText(data[0]["@abstract"],i);
				},
				error: TMF.showError
			});
	} else {
		TMF.log("Error: called TMF.getAbstract without data");
	}
}

TMF.publishText = function(text,i) {
	TMF.initFancyBox();
	$('div#box'+(i+1)+' .fancyboxtext').append('<a href="#wikiText'+(i+1)+'" class="group'+(i+1)+'" rel="group'+(i+1)+'" title="'+TMF.data.Resources[i]["@label"]+' - Text"></a>');
	$('div#box'+(i+1)).append('<div id="wikiText'+(i+1)+'" class="wikiText" style="display:none;width:700px;color:black;padding-left: 25px; padding-right: 25px; padding-top: 10px; padding-bottom:10px; background-image:url(images/paper_texture.jpg);">'+text+'</div>');
	//TMF.getMetadata(i);
} 

TMF.getNews = function (i) {
	TMF.log("getNews called for resource "+i);
	var action;
	if(TMF.localUse)
		action = '/json/getNews.json';
	else
		action = TMF.setLanguageService()+"getNews";
	jQuery.ajax(action, {
		type: 'GET',
		data: {uri: TMF.data.Resources[i]["@uri"]},
		success: function(data,textStatus,jqXHR) {
			TMF.publishNews(data, i);
		},
		error: TMF.showError
	});
}

TMF.publishNews = function(data,i) {
	TMF.initFancyBox();
	var articles = data["results"][0]["article_list"]["results"];
	if(articles.length == 0) { //No results found
		TMF.log("publishNews: no news found (0 results)");
	} else { //At least 1 res has been found
		console.log(articles);
		var articlesDiv = $('<div id="news'+(i+1)+'" style="display:none;width:700px" class="nyTimesNews"></div>');
		if($("input[name=lang]:checked").val() == "italian")
			articlesDiv.append('<h1>Ultime notizie su '+TMF.data.Resources[i]["@label"]+'</h1>')
		else
			articlesDiv.append('<h1>Latest News About '+TMF.data.Resources[i]["@label"]+'</h1>')
		for(var k in articles) {
			var article = articles[k];
			var newsTable = $("<table><table>");
			newsTable.append('<tr><td class="nyTimesDate">'+article["date"].substr(0,4)+'/'+article["date"].substr(4,2)+'/'+article["date"].substr(6,2)+'</td><td><h2>'+article["title"]+'</h2><p>'+article["body"]+' <a href="'+article["url"]+'" target="_blank">[...]</a></p></td></tr>');
			articlesDiv.append(newsTable.prop("outerHTML"));
		}
		$('div#box'+(i+1)).append(articlesDiv.prop('outerHTML'));
		$('div#box'+(i+1)+' .fancyboxnews').append('<a href="#news'+(i+1)+'" class="group'+(i+1)+'" rel="group'+(i+1)+'" title="'+TMF.data.Resources[i]["@label"]+' - News"></a>');
	}
}

TMF.getVideo = function(i) {
	//TODO
	TMF.log("getVideo called for resource "+i);
	var action;
	if(TMF.localUse)
		action = '/json/getVideo.json';
	else
		action = TMF.setLanguageService()+"getVideo";
	jQuery.ajax(action, {
		 type: 'GET',
		 data: {uri: TMF.data.Resources[i]["@uri"],
		     label: TMF.data.Resources[i]["@label"],
		     //type: TMF.data.Resources[i]["@mergedTypesString"],
			 //lang: $('input:radio:checked').val()
	 }, 
	 success: function(data,textStatus,jqXHR) {
		  TMF.publishVideo(data[0]["@videoURL"],i)
	 },
	 error: TMF.showError
});
}

TMF.publishVideo = function(video,i){
	TMF.log(video);
	if(video){
		TMF.log('found video for resource '+i+": "+video);
		TMF.data.Resources[i]["@videoURL"] = video;
		$('#box'+(i+1)+' .fancyboxvideo').append('<a href="youtube/player.html?rand='+Math.random()+'&id='+i+'" class="group'+(i+1)+' fancybox.iframe video" content="video" rel="group'+(i+1)+'" title="'+TMF.data.Resources[i]["@label"]+' - Video"></a>');
	} else {
		 TMF.log("No video found for resource "+i);
	}
}

TMF.retrieveVideoUrl = function(i) {
	TMF.log("retrieveVideoURl called for resource: "+i);
	return TMF.data.Resources[parseInt(i)]["@videoURL"].replace(/http:\/\/youtu.be\//i,"");
}

TMF.getMap = function(i) {
	TMF.log("getMap called for resource "+i);
	var action;
	if(TMF.localUse)
		action = '/json/getMap.json'
	else
		action = TMF.setLanguageService()+"getMap";
	jQuery.ajax(action, {
		type: 'GET',
		data: {uri: TMF.data.Resources[i]["@uri"]},
		success: function(data, textStatus, jqXHR) {
			TMF.publishMap(data[0],i)
		},
		error: TMF.showError
	});
}

TMF.publishMap = function(latLong, i) {
	if(latLong["@lat"] && latLong["@long"]) {
		TMF.data.Resources[i]["@latLong"] = latLong;
		$('#box'+(i+1)+' .fancyboxmap').append('<a href="/leaflet/index.html?'+Math.random()+'&id='+i+'" class="group'+(i+1)+' fancybox.iframe video" content="map" rel="group'+(i+1)+'" title="'+TMF.data.Resources[i]["@label"]+' - Map"></a>');
	} else {
		TMF.log("No map found for resource "+i);
	}
}

TMF.retrieveCoordinates = function(i) {
	TMF.log("retrieveCoordinates called for resource "+i);
	return TMF.data.Resources[parseInt(i)]["@latLong"];
}

TMF.getMetadata = function(i) {
	var queryObj = {
		objectPropertiesQuery: "",
		dataPropertiesQuery: "",
		subjectQuery: "",
		isPrimaryTopicOfQuery: ""
	}
	
	var propertiesObj = {
		objectProperties: "",
		dataProperties: "",
		subjectProperties: "",
		isPrimaryTopicOfProperties: ""
	}
		
	var endPoint;
	var command;
	var language;
	var url = MDH.changeSpecialChars(TMF.data.Resources[i]["@uri"]);
	if(url.indexOf("http://dbpedia.org/resource")!= -1){
		url = encodeURIComponent(url);
		endPoint = MDH.dbpediaProfile.endpoint;
		command = MDH.dbpediaProfile.command;
		language = "EN";
	}else if (url.indexOf("http://it.dbpedia.org/resource")!= -1) {
		url = encodeURIComponent(url);
		endPoint = MDH.dbpediaItProfile.endpoint;
		command = MDH.dbpediaItProfile.command;
		language = "IT";
	}else return;	
	queryObj.objectPropertiesQuery = MDH.setObjectPropertiesQuery(endPoint, command, url , language);
	queryObj.dataPropertiesQuery = MDH.setDataPropertiesQuery(endPoint, command, url , language);
	queryObj.subjectQuery = MDH.setSubjectQuery(endPoint,command, url );
	queryObj.isPrimaryTopicOfQuery = MDH.setIsPrimaryTopicOfQuery(endPoint, command, url );	
	TMF.callObjectProperties(queryObj, propertiesObj, i);
}

TMF.callObjectProperties = function(queryObj, propertiesObj, i){
	$.ajax({
		url:queryObj.objectPropertiesQuery,
        dataType: 'jsonp',
        jsonp: 'callback',
        success: function(data, textStatus, jqXHR) {
        	propertiesObj.objectProperties = MDH.parseObjectProperties(data),
        	TMF.callDataProperties(queryObj, propertiesObj, i);
        },	
        error: TMF.showError
	});
}

TMF.callDataProperties = function(queryObj, propertiesObj, i){
	$.ajax({
		url:queryObj.dataPropertiesQuery,
        dataType: 'jsonp',
        jsonp: 'callback',
        success: function(data, textStatus, jqXHR) {
        	propertiesObj.dataProperties = MDH.parseDataProperties(data)
        	TMF.callSubjectProperties(queryObj, propertiesObj, i);
        },	
        error: TMF.showError
	});
}

TMF.callSubjectProperties = function(queryObj, propertiesObj, i){
	$.ajax({
		url:queryObj.subjectQuery,
        dataType: 'jsonp',
        jsonp: 'callback',
        success: function(data, textStatus, jqXHR) {
        	propertiesObj.subjectProperties = MDH.parseSubjectProperties(data);
        	TMF.callIsPrimaryTopicOfQuery(queryObj, propertiesObj, i);
        },
        error: TMF.showError
	});    	
}

TMF.callIsPrimaryTopicOfQuery = function(queryObj, propertiesObj, i){
	$.ajax({
		url:queryObj.isPrimaryTopicOfQuery,
        dataType: 'jsonp',
        jsonp: 'callback',
        success: function(data, textStatus, jqXHR) {
	 		propertiesObj.isPrimaryTopicOfProperties = MDH.parseIsPrimaryTopicOf(data);
	 		TMF.checkForCreatingJson(propertiesObj,i);
	 	},
        error: TMF.showError
	});    	
}

TMF.checkForCreatingJson = function(propertiesObj,i){
	var properties = propertiesObj.objectProperties.concat(propertiesObj.dataProperties, propertiesObj.subjectProperties, propertiesObj.isPrimaryTopicOfProperties);
	var jsonData = MDH.createRgraphJson(properties,TMF.data.Resources[i]["@uri"],TMF.data.Resources[i]["@label"]);	
	TMF.publishMetadata(jsonData,i);
}

TMF.publishMetadata = function(jsonData,i){
	if(jsonData){
		TMF.data.Resources[i]["@metadata"] = jsonData;
		$('#box'+(i+1)+' .fancyboxmetadata').append('<a href="radialgraph/index.html?rand='+Math.random()+'&id='+i+'" class="group'+(i+1)+' fancybox.iframe" content="iconvis" rel="group'+(i+1)+'" title="'+TMF.data.Resources[i]["@label"]+' - Metadata"></a>');
	} else {
		TMF.log("Error for metadata "+i);
	}
}

TMF.retrieveMetadata = function(i){
	TMF.log("retrieveMetada called for resource "+i);
	return TMF.data.Resources[parseInt(i)]["@metadata"];
}
