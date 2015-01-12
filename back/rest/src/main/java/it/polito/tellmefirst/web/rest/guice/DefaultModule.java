package it.polito.tellmefirst.web.rest.guice;

import it.polito.tellmefirst.web.rest.images.ImagePolicyDAO;
import it.polito.tellmefirst.web.rest.images.ImagePolicyDAOImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DefaultModule implements Module{

	@Override
	public void configure(Binder binder) {
		binder.bind(ImagePolicyDAO.class).to(ImagePolicyDAOImpl.class);
	}

}