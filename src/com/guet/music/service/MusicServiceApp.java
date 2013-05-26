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
	
	//创建数据库
	public MusicServiceApp(Context context) throws Throwable
	{
		this.context = context;
		dbOpenHelper = new DBOpenHelper(context);
		dbOpenHelper.getWritableDatabase();	// 第一次调用该方法就会创建数据库		
		musicService = new MusicService(this.context);
	}
	
	//********************************************************************
	
	//保存一个数据项
	public boolean save( Music music ) throws Throwable
	{
		boolean result;
			
		result = musicService.save(music);
		return result;		//成功返回TRUE，失败返回FALSE
	}

	//更新一个数据项
	public void upate( Music music ) throws Throwable
	{	
		musicService.update(music);
	}

	//删除一个数据项
	public void delete( String delName ) throws Throwable
	{
		if( delName != null )
		{
			musicService.delete(delName);
		} else {
			Log.w(TAG,"MusicServiceApp:Delete: delName=Null!");
		}
	}
	
	//删除所有数据项
	public void clear() throws Throwable
	{
		musicService.clear();
	}
	

	//查找一个数据项并返回
	public Music find(String findName ) throws Throwable
	{
		Music music = musicService.find(findName);		
		if( music != null )	//必须判断是否为NULL，否则打印会出错！
		{			
			return music;
		}
		
		return null;
	}
	
	//获取所有数据项
	public List<Music> getScrollData (Integer offset, Integer maxResult) throws Throwable
	{
		List<Music> musics = musicService.getScrollData(offset, maxResult);
		if( musics != null ) return musics;
		
		return null;
	}

	//获取所有数据项的总项数
	public int GetCount() throws Throwable
	{	
		return (int) musicService.getCount();		
	}
	
	//关闭数据库
	public void closeDb()
	{			
		musicService.close();	//关闭数据库
		dbOpenHelper.close();
	}	
}
