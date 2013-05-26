package com.guet.music.service;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MusicService
{
	private static final String TAG = "Car";
	private DBOpenHelper dbOpenHelper;

	//构造函数
	public MusicService(Context context)
	{
		//实例化一个对象
		this.dbOpenHelper = new DBOpenHelper(context);
	}

	//保存一个数据对象
	public boolean save(Music music)
	{
		// 如果要对数据进行更改，就调用此方法得到用于操作数据库的实例,该方法以读和写方式打开数据库
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//执行SQL插入语句
		try
		{
			db.execSQL("insert into music (name,title,path,time,artist,album,isDel,isExist) values(?,?,?,?,?,?,?,?)", 
					new Object[]{ music.name, music.title, music.path, music.time, music.artist,
									music.album, music.isDel, music.isExist});
		} catch (Exception e)
		{			
			Log.e(TAG,e.toString());
			Log.e(TAG,"Music数据库操作错误: save error!");
			return false;
		} 
		
		return true;		
	}

	//更新一个数据对象
	public void update(Music music)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("update music set title=?, path=?, time=?, artist=?," +
				" album=?, isDel=?, isExist=? where name=?", 
				new Object[]{ music.title, music.path, music.time, music.artist,
								music.album, music.isDel, music.isExist, music.name});
	}

	//删除一个数据对象,根据歌曲名称
	public void delete(String nameStr)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//根据主键ID号进行查找删除
		db.execSQL("delete from music where name=?", new Object[]{ nameStr });
	}
	
	//清除表格music
	public void clear()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("delete from music");
	}

	//查找一个数据对象
	public Music find(String nameStr)
	{
		// 如果只对数据进行读取，建议使用此方法
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		//Cursor为指向查找到对象的光标，如果存在！
		Cursor cursor = db.rawQuery("select * from music where name=?",
				new String[]
				{ nameStr });
		
		//通过光标移动至第一行，判断是否存在查找结果
		if (cursor.moveToFirst())
		{
			//通过光标查找对象
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			String album = cursor.getString(cursor.getColumnIndex("album"));			
			String isDel = cursor.getString(cursor.getColumnIndex("isDel"));
			String isExist = cursor.getString(cursor.getColumnIndex("isExist"));
			
			//使用获取的数据，实例化一个Person对象返回
			Music music = new Music(nameStr, title, path, time, artist, album, isDel, isExist);
			cursor.close();
			return music;
		}
		
		cursor.close();
		return null;
	}
	
	//查找分页
	public List<Music> getScrollData(Integer offset, Integer maxResult){
		List<Music> musics = new ArrayList<Music>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from music limit ?,?",
				new String[]{offset.toString(), maxResult.toString()});
		
		while(cursor.moveToNext()){
			
			//通过光标查找对象
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			String album = cursor.getString(cursor.getColumnIndex("album"));			
			String isDel = cursor.getString(cursor.getColumnIndex("isDel"));
			String isExist = cursor.getString(cursor.getColumnIndex("isExist"));
			
			//使用获取的数据，实例化一个Person对象返回
			Music music = new Music(name, title, path, time, artist, album, isDel, isExist);
		
			musics.add(music);
		}
		cursor.close();
		return musics;
	}		

	//获取总项数
	public long getCount()
	{
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from music", null);
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
