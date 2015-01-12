package it.polito.tellmefirst.web.rest.images;

import it.polito.tellmefirst.web.rest.guice.GuiceEnv;
import it.polito.tellmefirst.web.rest.guice.UnitTestModule;
import it.polito.tellmefirst.web.rest.interfaces.ImageInterface.ImgResponse;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestClassifyWikiParse {

	@BeforeClass
	public static void init(){
		GuiceEnv.setModule(new UnitTestModule());
	}
	
	@Test
	public void testClassifyWikiParse(){
		//TODO test by implementing DAO interface around classifier
	}
	
	@Test
	public void testCalculateRatio(){
		ImagePolicyDAO imgDAO = GuiceEnv.instance(ImagePolicyDAO.class);
		
		System.out.println(imgDAO.getRatioFromImgResponse(new ImgResponse("", 200, 90)));
		
	}
	
}
