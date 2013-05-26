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
	
	//�������ݿ�
	public MovieServiceApp(Context context) throws Throwable
	{
		this.context = context;
		dbOpenHelper = new DBOpenHelper(context);
		dbOpenHelper.getWritableDatabase();	// ��һ�ε��ø÷����ͻᴴ�����ݿ�		
		movieService = new MovieService(this.context);
	}
	
	//********************************************************************
	
	//����һ��������
	public boolean save( myMovie movie ) throws Throwable
	{
		boolean result;
			
		result = movieService.save(movie);
		return result;		//�ɹ�����TRUE��ʧ�ܷ���FALSE
	}

	//����һ��������
	public void upate( myMovie movie ) throws Throwable
	{	
		movieService.update(movie);
	}

	//ɾ��һ��������
	public void delete( String delName ) throws Throwable
	{
		if( delName != null )
		{
			movieService.delete(delName);
		} else {
			Log.w(TAG,"MovieServiceApp:Delete: delName=Null!");
		}
	}
	
	//ɾ������������
	public void clear() throws Throwable
	{
		movieService.clear();
	}
	

	//����һ�����������
	public myMovie find(String findName ) throws Throwable
	{
		myMovie movie = movieService.find(findName);		
		if( movie != null )	//�����ж��Ƿ�ΪNULL�������ӡ�����
		{			
			return movie;
		}
		
		return null;
	}
	
	//��ȡ����������
	public List<myMovie> getScrollData (Integer offset, Integer maxResult) throws Throwable
	{
		List<myMovie> movies = movieService.getScrollData(offset, maxResult);
		if( movies != null ) return movies;
		
		return null;
	}

	//��ȡ�����������������
	public int GetCount() throws Throwable
	{	
		return (int) movieService.getCount();		
	}
	
	//�ر����ݿ�
	public void closeDb()
	{			
		movieService.close();	//�ر����ݿ�
		dbOpenHelper.close();
	}	
}
