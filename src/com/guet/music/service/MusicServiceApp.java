package com.guet.music.service;



import java.util.List;

import android.content.Context;
import android.util.Log;

public class MusicServiceApp
{
	private static final String TAG = "Car";
	private static Context context;
	private DBOpenHelper dbOpenHelper;
	private MusicService musicService;
	
	//�������ݿ�
	public MusicServiceApp(Context context) throws Throwable
	{
		this.context = context;
		dbOpenHelper = new DBOpenHelper(context);
		dbOpenHelper.getWritableDatabase();	// ��һ�ε��ø÷����ͻᴴ�����ݿ�		
		musicService = new MusicService(this.context);
	}
	
	//********************************************************************
	
	//����һ��������
	public boolean save( Music music ) throws Throwable
	{
		boolean result;
			
		result = musicService.save(music);
		return result;		//�ɹ�����TRUE��ʧ�ܷ���FALSE
	}

	//����һ��������
	public void upate( Music music ) throws Throwable
	{	
		musicService.update(music);
	}

	//ɾ��һ��������
	public void delete( String delName ) throws Throwable
	{
		if( delName != null )
		{
			musicService.delete(delName);
		} else {
			Log.w(TAG,"MusicServiceApp:Delete: delName=Null!");
		}
	}
	
	//ɾ������������
	public void clear() throws Throwable
	{
		musicService.clear();
	}
	

	//����һ�����������
	public Music find(String findName ) throws Throwable
	{
		Music music = musicService.find(findName);		
		if( music != null )	//�����ж��Ƿ�ΪNULL�������ӡ�����
		{			
			return music;
		}
		
		return null;
	}
	
	//��ȡ����������
	public List<Music> getScrollData (Integer offset, Integer maxResult) throws Throwable
	{
		List<Music> musics = musicService.getScrollData(offset, maxResult);
		if( musics != null ) return musics;
		
		return null;
	}

	//��ȡ�����������������
	public int GetCount() throws Throwable
	{	
		return (int) musicService.getCount();		
	}
	
	//�ر����ݿ�
	public void closeDb()
	{			
		musicService.close();	//�ر����ݿ�
		dbOpenHelper.close();
	}	
}
