package it.polito.tellmefirst.web.rest.images;

import it.polito.tellmefirst.web.rest.interfaces.ImageInterface.ImgResponse;
import static it.polito.tellmefirst.util.TMFUtils.optional;

public interface ImagePolicyDAO {
	
	public Boolean existImage(String url);
	
	/**
	 * Returns the aspect ratio from wikpedia to the relative title
	 * In case of missing image, or missing size information, this method returns 0.
	 * 
	 * @param title The Wikipedia title that refers to a particular Wikipedia entry.
	 * @return The aspect ratio from wikpedia to the relative title. In case of missing image, or missing size information, this method returns 0.
	 */
	public Double  getAspectRatio(String title);
	
	
	/**
	 * Get the image + size parsing the mobile wikpedia page.
	 * 
	 * @param title
	 * @return The {@link ImgResponse} structure with raw link (without protocol!) and size. 
	 * 		   Empty string "" for link and 0 for size are returned in case of img not found. 
	 */
	public ImgResponse getMobileWikiImg(String title);

	default public Double getRatioFromImgResponse(ImgResponse ir) {
		return optional(()-> ( 
									(double) ir.getWidth() / ir.getHeight()
			   ), 0.0);
	}
	
}