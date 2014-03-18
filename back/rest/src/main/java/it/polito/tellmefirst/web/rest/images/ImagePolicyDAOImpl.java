package it.polito.tellmefirst.web.rest.images;

import static it.polito.tellmefirst.util.TMFUtils.existsLink;

public class ImagePolicyDAOImpl implements ImagePolicyDAO {

	@Override
	public Boolean existImage(String url) {
		return existsLink(url);
	}

	@Override
	public Double getAspectRatio(String title) {
		// TODO Auto-generated method stub
		return null;
	}

}
