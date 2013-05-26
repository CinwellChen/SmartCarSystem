package com.guet.music.mp3info;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class MusicInfoServer 
{ 
	private static final String TAG = "Car";
	public SongInfo info = null; 
	private RandomAccessFile ran = null; 
	private File file = null; 
	
	//构造函数1
	public MusicInfoServer()
	{
		
	}

	//构造函数1
	public MusicInfoServer(String filePath ) throws FileNotFoundException
	{ 
		file = new File(filePath);
		ran = new RandomAccessFile(file, "r"); 		
		Log.d(TAG,"mp3解析文件(" + file.getName() + ")加载完成!"); 

	}	
	
	//对文件进行正式解析
	public void getMp3Info() throws IOException
	{
		byte[] buffer = new byte[128];

		ran.seek(ran.length() - 128); 
		ran.read(buffer); 
		
		info = new SongInfo(buffer);
		
		Log.d(TAG,"歌曲名:" + info.getSongName());
		Log.d(TAG,"年份:" + info.getYear() );
		Log.d(TAG,"歌手:" + info.getArtist() );
		Log.d(TAG,"专辑名:"+ info.getAlbum() );
		Log.d(TAG,"备注:" + info.getComment()); 
		
//		文件加载完成!
//		歌曲名:一眼万年
//		年份:2006
//		歌手:S.H.E
//		专辑名:天外飞仙 电视原声带
//		备注:http://www.yyrl.com
	}
} 