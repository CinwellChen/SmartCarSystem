package com.guet.movie.service;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MovieService
{
	private static final String TAG = "Car";
	private DBOpenHelper dbOpenHelper;

	//���캯��
	public MovieService(Context context)
	{
		//ʵ����һ������
		this.dbOpenHelper = new DBOpenHelper(context);
	}

	//����һ�����ݶ���
	public boolean save(myMovie movie)
	{
		// ���Ҫ�����ݽ��и��ģ��͵��ô˷����õ����ڲ������ݿ��ʵ��,�÷����Զ���д��ʽ�����ݿ�
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//ִ��SQL�������
		try
		{
			db.execSQL("insert into movie (name,title,path,time) values(?,?,?,?)", 
					new Object[]{ movie.name, movie.title, movie.path, movie.time });
		} catch (Exception e)
		{			
			Log.e(TAG,e.toString());
			Log.e(TAG,"Movie���ݿ��������: save error!");
			return false;
		} 
		
		return true;		
	}

	//����һ�����ݶ���
	public void update(myMovie movie)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("update movie set title=?, path=?, time=? where name=?", 
				new Object[]{ movie.title, movie.path, movie.time, movie.name});
	}

	//ɾ��һ�����ݶ���,���ݸ�������
	public void delete(String nameStr)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//��������ID�Ž��в���ɾ��
		db.execSQL("delete from movie where name=?", new Object[]{ nameStr });
	}
	
	//������music
	public void clear()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("delete from movie");
	}

	//����һ�����ݶ���
	public myMovie find(String nameStr)
	{
		// ���ֻ�����ݽ��ж�ȡ������ʹ�ô˷���
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		//CursorΪָ����ҵ�����Ĺ�꣬������ڣ�
		Cursor cursor = db.rawQuery("select * from movie where name=?",
				new String[]
				{ nameStr });
		
		//ͨ������ƶ�����һ�У��ж��Ƿ���ڲ��ҽ��
		if (cursor.moveToFirst())
		{
			//ͨ�������Ҷ���
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			
			//ʹ�û�ȡ�����ݣ�ʵ����һ��Person���󷵻�
			myMovie movie = new myMovie(nameStr, title, path, time );
			cursor.close();
			return movie;
		}
		
		cursor.close();
		return null;
	}
	
	//���ҷ�ҳ
	public List<myMovie> getScrollData(Integer offset, Integer maxResult){
		List<myMovie> movies = new ArrayList<myMovie>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from movie limit ?,?",
				new String[]{offset.toString(), maxResult.toString()});
		
		while(cursor.moveToNext())
		{
			
			//ͨ�������Ҷ���
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			
			//ʹ�û�ȡ�����ݣ�ʵ����һ��Person���󷵻�
			myMovie movie = new myMovie( name, title, path, time );
		
			movies.add(movie);
		}
		cursor.close();
		return movies;
	}		

	//��ȡ������
	public long getCount()
	{
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from movie", null);
		cursor.moveToFirst();
		long result = cursor.getLong(0);
		cursor.close();
		
		return result;
	}
	
	//�ر����ݿ�
	public void close()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.close();		//�ر����ݿ�
		dbOpenHelper.close();
	}
}
