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

	//构造函数
	public MovieService(Context context)
	{
		//实例化一个对象
		this.dbOpenHelper = new DBOpenHelper(context);
	}

	//保存一个数据对象
	public boolean save(myMovie movie)
	{
		// 如果要对数据进行更改，就调用此方法得到用于操作数据库的实例,该方法以读和写方式打开数据库
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//执行SQL插入语句
		try
		{
			db.execSQL("insert into movie (name,title,path,time) values(?,?,?,?)", 
					new Object[]{ movie.name, movie.title, movie.path, movie.time });
		} catch (Exception e)
		{			
			Log.e(TAG,e.toString());
			Log.e(TAG,"Movie数据库操作错误: save error!");
			return false;
		} 
		
		return true;		
	}

	//更新一个数据对象
	public void update(myMovie movie)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("update movie set title=?, path=?, time=? where name=?", 
				new Object[]{ movie.title, movie.path, movie.time, movie.name});
	}

	//删除一个数据对象,根据歌曲名称
	public void delete(String nameStr)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//根据主键ID号进行查找删除
		db.execSQL("delete from movie where name=?", new Object[]{ nameStr });
	}
	
	//清除表格music
	public void clear()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("delete from movie");
	}

	//查找一个数据对象
	public myMovie find(String nameStr)
	{
		// 如果只对数据进行读取，建议使用此方法
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		//Cursor为指向查找到对象的光标，如果存在！
		Cursor cursor = db.rawQuery("select * from movie where name=?",
				new String[]
				{ nameStr });
		
		//通过光标移动至第一行，判断是否存在查找结果
		if (cursor.moveToFirst())
		{
			//通过光标查找对象
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			
			//使用获取的数据，实例化一个Person对象返回
			myMovie movie = new myMovie(nameStr, title, path, time );
			cursor.close();
			return movie;
		}
		
		cursor.close();
		return null;
	}
	
	//查找分页
	public List<myMovie> getScrollData(Integer offset, Integer maxResult){
		List<myMovie> movies = new ArrayList<myMovie>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from movie limit ?,?",
				new String[]{offset.toString(), maxResult.toString()});
		
		while(cursor.moveToNext())
		{
			
			//通过光标查找对象
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			
			//使用获取的数据，实例化一个Person对象返回
			myMovie movie = new myMovie( name, title, path, time );
		
			movies.add(movie);
		}
		cursor.close();
		return movies;
	}		

	//获取总项数
	public long getCount()
	{
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from movie", null);
		cursor.moveToFirst();
		long result = cursor.getLong(0);
		cursor.close();
		
		return result;
	}
	
	//关闭数据库
	public void close()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.close();		//关闭数据库
		dbOpenHelper.close();
	}
}
