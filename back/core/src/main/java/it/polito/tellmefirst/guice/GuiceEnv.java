package it.polito.tellmefirst.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceEnv {

	private static Module guiceModule = new DefaultModule();
	private static Injector inj = Guice.createInjector(guiceModule);
	
	public static void setModule(Module module){
		guiceModule = module;
		inj = Guice.createInjector(guiceModule);
	}
	
	public static <T> T instance(Class<T> clazz){
		return inj.getInstance(clazz);
	}
	
}