package it.polito.tellmefirst.dao;

import java.util.List;

public interface WikiDAO {
	
	public List<String> getFileLabels(String searchText);
	public String getURLFromFileLabel(String fileLabel);
	public Boolean existsImage(String url);
	
}