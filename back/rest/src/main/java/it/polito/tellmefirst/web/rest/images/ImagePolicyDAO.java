package it.polito.tellmefirst.web.rest.images;

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
}
