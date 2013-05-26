package com.guet.SmartCarMovie;


import java.io.File;
import java.io.FilenameFilter;

public class MovieFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String filename) {		
		
		//3gpÎÄ¼þ¹ýÂËÆ÷
		return (filename.endsWith(".3gp") 
				|| filename.endsWith(".mp4")
				|| filename.endsWith(".rm")
				|| filename.endsWith(".rmvb")
				|| filename.endsWith(".avi")
				|| filename.endsWith(".wmv"));
	} 
}
