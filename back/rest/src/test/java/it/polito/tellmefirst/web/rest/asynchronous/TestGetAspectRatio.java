package it.polito.tellmefirst.web.rest.asynchronous;

import static org.junit.Assert.*;
import it.polito.tellmefirst.web.rest.images.ImagePolicyDAO;
import it.polito.tellmefirst.web.rest.images.ImagePolicyDAOImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGetAspectRatio {

	static ImagePolicyDAO imagePolicyDAO;
	static Log LOG = LogFactory.getLog(TestGetAspectRatio.class);
	
	static final String title1 = "The_Beatles";
	static final String title2 = "Maurizio_Crozza";
	static final String title3 = "Arthur_Schnitzler"; 
	
	@BeforeClass
	public static void init(){
		
		imagePolicyDAO = new ImagePolicyDAOImpl();
	}
	
	@Test
	public void testGetAspectRatio(){
		
		Double ratio = imagePolicyDAO.getAspectRatio(title1);
		assertNotNull(ratio);
		assertTrue(ratio.doubleValue()>0);
		LOG.debug("Ratio = "+ratio.doubleValue());
		
		ratio = imagePolicyDAO.getAspectRatio(title2);
		assertNotNull(ratio);
		assertTrue(ratio.doubleValue()>0);
		LOG.debug("Ratio = "+ratio.doubleValue());
		
		ratio = imagePolicyDAO.getAspectRatio(title3);
		assertNotNull(ratio);
		assertTrue(ratio.doubleValue()>0);
		LOG.debug("Ratio = "+ratio.doubleValue());
		
	}
	
}
