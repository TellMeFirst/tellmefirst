package it.polito.tellmefirst.web.rest.images;

import it.polito.tellmefirst.web.rest.interfaces.ImageInterface.ImgResponse;

public class ImagePolicyDAOMock implements ImagePolicyDAO{

	public static boolean existsImage = true;
	public static double ratio = 1.0;
	
	public static ImgResponse imgResp = new ImgResponse("//upload.wikimedia.org/wikipedia/it/thumb/b/bd/Forummediaset.jpg/285px-Forummediaset.jpg", 200, 90);
	
	@Override
	public Boolean existImage(String url) {
		return existsImage;
	}

	@Override
	public Double getAspectRatio(String title) {
		return ratio;
	}

	@Override
	public ImgResponse getMobileWikiImg(String title) {
		return imgResp;
	}
	
}
