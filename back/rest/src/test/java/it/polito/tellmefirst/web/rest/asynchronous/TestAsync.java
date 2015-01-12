package it.polito.tellmefirst.web.rest.asynchronous;

import static it.polito.tellmefirst.web.rest.asynchronous.Parallel.parallelListMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import it.polito.tellmefirst.util.PostProcess;
import it.polito.tellmefirst.web.rest.services.Classify;
import it.polito.tellmefirst.web.rest.services.Classify.ImagePolicy;

import java.util.List;

import org.junit.Test;

public class TestAsync {

	static long mainThread;
	
	@Test
	public void testAsync(){
		mainThread = Thread.currentThread().getId();

		List<Integer> completedResult = parallelListMap( asList(1,2,3,4,5), new PostProcess<Integer>() { public Integer process(Integer i) throws Exception {
			assertFalse( mainThread == Thread.currentThread().getId() );
			return i+2;
		}});
		
		assertEquals( asList(3,4,5,6,7) , completedResult );
	}
	
}
