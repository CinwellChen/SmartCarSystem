package com.guet.SmartCarMusic;


import java.io.File;
import java.io.FilenameFilter;

public class MusicFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String filename) {		
		
		//mp3�ļ�������
		return (filename.endsWith(".mp3"));
	} 
}
