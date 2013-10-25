package it.polito.tellmefirst.web.rest;

import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.enhance.Enhancer;
import it.polito.tellmefirst.lucene.IndexesUtil;
import it.polito.tellmefirst.util.TMFVariables;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class TMFListener implements ServletContextListener {

	static Log LOG = LogFactory.getLog(TMFListener.class);

	protected static Enhancer enhancer;
	protected static Classifier italianClassifier;
	protected static Classifier englishClassifier;
	static Cache cacheTmf = null;

	public static Enhancer getEnhancer() {
		return enhancer;
	}

	public static Classifier getItalianClassifier() {
		return italianClassifier;
	}

	public static Classifier getEnglishClassifier() { 
		return englishClassifier;
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOG.debug("[main] - BEGIN");
		
		String configFileName = sce.getServletContext().getInitParameter("conf_file_path");;
		final TMFVariables tmfVariables = new TMFVariables(configFileName);

		// Create a Cache and add it to the CacheManager, then use it.
		/*
		 * @param name the name of the cache. Note that "default" is a reserved
		 * name for the defaultCache.
		 * 
		 * @param maxElementsInMemory the maximum number of elements in memory,
		 * before they are evicted
		 * 
		 * @param overflowToDisk whether to use the disk store
		 * 
		 * @param eternal whether the elements in the cache are eternal, i.e.
		 * never expire
		 * 
		 * @param timeToLiveSeconds the default amount of time to live for an
		 * element from its creation date
		 * 
		 * @param timeToIdleSeconds the default amount of time to live for an
		 * element from its last accessed or modified date
		 */

		if (CacheManager.getInstance().getCache("tmf") == null) {
			cacheTmf = new Cache("tmf", 5000, false, false,
					TMFVariables.CACHE_TTL, 0);
			CacheManager.getInstance().addCache(cacheTmf);
		}
		IndexesUtil.indexesUtil();

		enhancer = new Enhancer();
		italianClassifier = new Classifier("it");
		englishClassifier = new Classifier("en");

//		URI serverURI = optional(new Ret<URI>() {
//		public URI ret() throws Exception {
//			return new URI(tmfVariables.getRestURL());
//		}
//	}, "URI not retrieved");
		
		// The following is adapted from DBpedia Spotlight
		// (https://github.com/dbpedia-spotlight/dbpedia-spotlight)
//		final Map<String, String> initParams = new HashMap<String, String>();
//		initParams.put("com.sun.jersey.config.property.resourceConfigClass",
//				"com.sun.jersey.api.core.PackagesResourceConfig");
//		initParams.put("com.sun.jersey.config.property.packages",
//				"it.polito.tellmefirst.web.rest.services");
//		initParams.put("com.sun.jersey.config.property.WadlGeneratorConfig",
//				"it.polito.tellmefirst.web.rest.wadl.ExternalUriWadlGeneratorConfig");
//		SelectorThread threadSelector = GrizzlyWebContainerFactory.create(
//				serverURI, initParams);
//		threadSelector.start();
//		System.err.println("Server started in " + System.getProperty("user.dir")
//						+ " listening on " + serverURI);
//		Thread warmUp = new Thread() {
//			public void run() {
//			}
//		};
//		warmUp.start();
//		while (running) {
//			Thread.sleep(100);
//		}
//		threadSelector.stopEndpoint();
//		System.exit(0);
		LOG.debug("[main] - END");
	}

}
