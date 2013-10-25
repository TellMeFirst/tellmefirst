package it.polito.tellmefirst.dao;

import static it.polito.tellmefirst.guice.GuiceEnv.instance;

public class DAOFactory {
	public static <T> T getDAO(Class<T> dao){
		return instance(dao);
	}
}
