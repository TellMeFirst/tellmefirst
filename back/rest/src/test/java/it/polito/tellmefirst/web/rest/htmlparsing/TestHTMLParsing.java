package it.polito.tellmefirst.web.rest.htmlparsing;

import it.polito.tellmefirst.web.rest.interfaces.ImageInterface;

import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;

import static it.polito.tellmefirst.util.TMFUtils.*;
import static java.lang.Double.parseDouble;
import static org.junit.Assert.*;

public class TestHTMLParsing {

	static String labelTest = "Steven_Spielberg";
//	static String labelTest = "Forum_(programma_televisivo)";
//	static String labelTest = "Universal_Pictures";
	
	@BeforeClass
	public static void init(){
		System.setProperty("http.proxyHost", "lelapo.telecomitalia.local");
		System.setProperty("http.proxyPort", "8080");
	}
	
	@Test
	public void testParsing(){
		//KO
		//http://it.m.wikipedia.org/wiki/Steven_Spielberg
		//http://it.m.wikipedia.org/wiki/Universal_Pictures
		
		//OK
		//http://it.m.wikipedia.org/wiki/Telecom_Italia
		//http://it.m.wikipedia.org/wiki/Nicole_Kidman
		
		
		Document doc = uncheck( ()-> Jsoup.connect("http://it.m.wikipedia.org/wiki/"+labelTest).get(), null );
		Elements links = doc.select("img");
		System.out.println(links.get(0).attr("src"));
		System.out.println(links.get(0).attr("width"));
		System.out.println(links.get(0).attr("height"));
		
		Double width = parseDouble(links.get(0).attr("width"));
		Double height = parseDouble(links.get(0).attr("height"));
		System.out.println(width / height);
		
	}
	
	@Test
	public void testGetWikiImageFromHTML(){
		JSONObject imgResult = ImageInterface.getImageFromWikiHTML(labelTest);
		assertNotNull(imgResult.optString("imgURL"));
		assertTrue(imgResult.optInt("width") > 40);
		assertTrue(imgResult.optInt("height") > 40);
		System.out.println(imgResult.toString());
		System.out.println(imgResult.optString("imageURL"));
	}
}
