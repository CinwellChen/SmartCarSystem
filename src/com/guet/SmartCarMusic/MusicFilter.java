package com.guet.SmartCarMusic;


import java.io.File;
import java.io.FilenameFilter;

public class MusicFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String filename) {		
		
		//mp3ÎÄ¼ş¹ıÂËÆ÷
		return (filename.endsWith(".mp3"));
	} 
}
