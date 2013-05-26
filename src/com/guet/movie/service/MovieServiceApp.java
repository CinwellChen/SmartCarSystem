package com.guet.movie.service;


import java.util.List;

import android.content.Context;
import android.util.Log;

public class MovieServiceApp
{
	private static final String TAG = "Car";
	private static Context context;
	private DBOpenHelper dbOpenHelper;
	private MovieService movieService;
	
	//创建数据库
	public MovieServiceApp(Context context) throws Throwable
	{
		this.context = context;
		dbOpenHelper = new DBOpenHelper(context);
		dbOpenHelper.getWritableDatabase();	// 第一次调用该方法就会创建数据库		
		movieService = new MovieService(this.context);
	}
	
	//********************************************************************
	
	//保存一个数据项
	public boolean save( myMovie movie ) throws Throwable
	{
		boolean result;
			
		result = movieService.save(movie);
		return result;		//成功返回TRUE，失败返回FALSE
	}

	//更新一个数据项
	public void upate( myMovie movie ) throws Throwable
	{	
		movieService.update(movie);
	}

	//删除一个数据项
	public void delete( String delName ) throws Throwable
	{
		if( delName != null )
		{
			movieService.delete(delName);
		} else {
			Log.w(TAG,"MovieServiceApp:Delete: delName=Null!");
		}
	}
	
	//删除所有数据项
	public void clear() throws Throwable
	{
		movieService.clear();
	}
	

	//查找一个数据项并返回
	public myMovie find(String findName ) throws Throwable
	{
		myMovie movie = movieService.find(findName);		
		if( movie != null )	//必须判断是否为NULL，否则打印会出错！
		{			
			return movie;
		}
		
		return null;
	}
	
	//获取所有数据项
	public List<myMovie> getScrollData (Integer offset, Integer maxResult) throws Throwable
	{
		List<myMovie> movies = movieService.getScrollData(offset, maxResult);
		if( movies != null ) return movies;
		
		return null;
	}

	//获取所有数据项的总项数
	public int GetCount() throws Throwable
	{	
		return (int) movieService.getCount();		
	}
	
	//关闭数据库
	public void closeDb()
	{			
		movieService.close();	//关闭数据库
		dbOpenHelper.close();
	}	
}
