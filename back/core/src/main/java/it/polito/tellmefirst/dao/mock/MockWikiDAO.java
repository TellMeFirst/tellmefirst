package it.polito.tellmefirst.dao.mock;

import java.util.ArrayList;
import java.util.List;

import it.polito.tellmefirst.dao.WikiDAO;

public class MockWikiDAO implements WikiDAO {

	public static List<String> fileLabels = new ArrayList<String>();
	public static String fileLabel = "File:Beatles Platz Hamburg.JPG";
	public static String urlFromFileLabel = "http://upload.wikimedia.org/wikipedia/commons/0/0a/Beatles_Platz_Hamburg.JPG";
	public static boolean exists=true;
	
	static{
		fileLabels.add(fileLabel);
	}
	
	@Override
	public List<String> getFileLabels(String seatchText) {
		return fileLabels;
	}

	@Override
	public String getURLFromFileLabel(String fileLabel) {
		return urlFromFileLabel;
	}

	@Override
	public Boolean existsImage(String url) {
		return exists;
	}

}