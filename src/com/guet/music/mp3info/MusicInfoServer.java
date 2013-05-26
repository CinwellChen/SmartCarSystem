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
	
	//���캯��1
	public MusicInfoServer()
	{
		
	}

	//���캯��1
	public MusicInfoServer(String filePath ) throws FileNotFoundException
	{ 
		file = new File(filePath);
		ran = new RandomAccessFile(file, "r"); 		
		Log.d(TAG,"mp3�����ļ�(" + file.getName() + ")�������!"); 

	}	
	
	//���ļ�������ʽ����
	public void getMp3Info() throws IOException
	{
		byte[] buffer = new byte[128];

		ran.seek(ran.length() - 128); 
		ran.read(buffer); 
		
		info = new SongInfo(buffer);
		
		Log.d(TAG,"������:" + info.getSongName());
		Log.d(TAG,"���:" + info.getYear() );
		Log.d(TAG,"����:" + info.getArtist() );
		Log.d(TAG,"ר����:"+ info.getAlbum() );
		Log.d(TAG,"��ע:" + info.getComment()); 
		
//		�ļ��������!
//		������:һ������
//		���:2006
//		����:S.H.E
//		ר����:������� ����ԭ����
//		��ע:http://www.yyrl.com
	}
} 