package it.polito.tellmefirst.guice;

import it.polito.tellmefirst.dao.WikiDAO;
import it.polito.tellmefirst.dao.mock.MockWikiDAO;

import com.google.inject.Binder;
import com.google.inject.Module;

public class UnitTestModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(WikiDAO.class).to(MockWikiDAO.class);
	}

}
