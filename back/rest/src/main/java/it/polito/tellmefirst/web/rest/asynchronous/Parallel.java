package it.polito.tellmefirst.web.rest.asynchronous;

import it.polito.tellmefirst.util.PostProcess;
import it.polito.tellmefirst.util.Ret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import static it.polito.tellmefirst.util.TMFUtils.*;

public class Parallel {

	static ExecutorService es = Executors.newFixedThreadPool(14);
	
	public static <T> List<T> parallelListMap(final List<T> baseList, final PostProcess<T> proc){
		return unchecked( ()-> {
				List<Callable<T>> callableList = new ArrayList<Callable<T>>();
				for (T t : baseList)
					callableList.add(new PostProcessCallable<T>(proc, t));
				
				List<T> processResult = new ArrayList<T>();
				for(Future<T> future : es.invokeAll(callableList))
					processResult.add(future.get());
				
				return processResult;
			}
		, "failed concurrent post processing");
	}
	
	static class PostProcessCallable<T> implements Callable<T> {
		PostProcess<T> proc;
		T preProcess;
		public PostProcessCallable(PostProcess<T> proc, T preProcess) {
			this.preProcess = preProcess;
			this.proc = proc;
		}
		
		@Override
		public T call() {
			try {
				return proc.process(preProcess);
			} catch (Exception e) {
				Logger.getLogger(getClass()).error("post process not completed, returning original object",e);
				return preProcess;
			}
		}
		
	}
	
}
