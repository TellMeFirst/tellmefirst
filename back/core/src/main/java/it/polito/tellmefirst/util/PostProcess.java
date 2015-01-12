package it.polito.tellmefirst.util;

public interface PostProcess<T> {

	public T process(T preProcess) throws Exception;
	
}
