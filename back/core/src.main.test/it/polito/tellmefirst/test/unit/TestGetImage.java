package it.polito.tellmefirst.test.unit;

import java.util.ArrayList;

import it.polito.tellmefirst.enhance.Enhancer;
import it.polito.tellmefirst.guice.DefaultModule;
import it.polito.tellmefirst.guice.GuiceEnv;
import it.polito.tellmefirst.guice.UnitTestModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static junit.framework.Assert.*;
import static it.polito.tellmefirst.enhance.Enhancer.DEFAULT_IMAGE;
import static it.polito.tellmefirst.dao.mock.MockWikiDAO.*;
import static it.polito.tellmefirst.util.TMFUtils.*;

@RunWith(JUnit4.class)
public class TestGetImage {

	static Enhancer en;
	
	@BeforeClass
	public static void init(){
		GuiceEnv.setModule(new UnitTestModule());
		en = new Enhancer();
	}
	
	@Before
	public void initial(){
		fileLabels = new ArrayList<String>();
		fileLabels.add("File:Beatles Platz Hamburg.JPG");
		fileLabels.add("File:Abbey Rd Studios.jpg");
		urlFromFileLabel = "http://upload.wikimedia.org/wikipedia/commons/0/0a/Beatles_Platz_Hamburg.JPG";
	}
	
	@Test
	public void testGetImage(){
		String url = en.getImageFromMediaWiki2("The_Beatles");
		assertEquals(urlFromFileLabel, url);
	}
	
	@Test
	public void testGetImageFailLabel(){
		fileLabels = new ArrayList<String>();
		String url = en.getImageFromMediaWiki2("The_Beatles");
		assertEquals(DEFAULT_IMAGE, url);
	}

	@Test
	public void testProcessFileLabelForHashComputing(){
		assertEquals("Beatles_Platz_Hamburg.JPG", processWikiFileLabelForHashComputation(fileLabels.get(0)));
	}
	
	@Test
	public void testGetWikiURL(){
		assertEquals(urlFromFileLabel, getWikiURL("Beatles_Platz_Hamburg.JPG"));
	}
	
	@AfterClass
	public static void after(){
		GuiceEnv.setModule(new DefaultModule());
	}
}
