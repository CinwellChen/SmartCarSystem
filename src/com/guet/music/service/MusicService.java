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

	//���캯��
	public MusicService(Context context)
	{
		//ʵ����һ������
		this.dbOpenHelper = new DBOpenHelper(context);
	}

	//����һ�����ݶ���
	public boolean save(Music music)
	{
		// ���Ҫ�����ݽ��и��ģ��͵��ô˷����õ����ڲ������ݿ��ʵ��,�÷����Զ���д��ʽ�����ݿ�
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//ִ��SQL�������
		try
		{
			db.execSQL("insert into music (name,title,path,time,artist,album,isDel,isExist) values(?,?,?,?,?,?,?,?)", 
					new Object[]{ music.name, music.title, music.path, music.time, music.artist,
									music.album, music.isDel, music.isExist});
		} catch (Exception e)
		{			
			Log.e(TAG,e.toString());
			Log.e(TAG,"Music���ݿ��������: save error!");
			return false;
		} 
		
		return true;		
	}

	//����һ�����ݶ���
	public void update(Music music)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("update music set title=?, path=?, time=?, artist=?," +
				" album=?, isDel=?, isExist=? where name=?", 
				new Object[]{ music.title, music.path, music.time, music.artist,
								music.album, music.isDel, music.isExist, music.name});
	}

	//ɾ��һ�����ݶ���,���ݸ�������
	public void delete(String nameStr)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		//��������ID�Ž��в���ɾ��
		db.execSQL("delete from music where name=?", new Object[]{ nameStr });
	}
	
	//������music
	public void clear()
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		
		db.execSQL("delete from music");
	}

	//����һ�����ݶ���
	public Music find(String nameStr)
	{
		// ���ֻ�����ݽ��ж�ȡ������ʹ�ô˷���
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		//CursorΪָ����ҵ�����Ĺ�꣬������ڣ�
		Cursor cursor = db.rawQuery("select * from music where name=?",
				new String[]
				{ nameStr });
		
		//ͨ������ƶ�����һ�У��ж��Ƿ���ڲ��ҽ��
		if (cursor.moveToFirst())
		{
			//ͨ�������Ҷ���
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			String album = cursor.getString(cursor.getColumnIndex("album"));			
			String isDel = cursor.getString(cursor.getColumnIndex("isDel"));
			String isExist = cursor.getString(cursor.getColumnIndex("isExist"));
			
			//ʹ�û�ȡ�����ݣ�ʵ����һ��Person���󷵻�
			Music music = new Music(nameStr, title, path, time, artist, album, isDel, isExist);
			cursor.close();
			return music;
		}
		
		cursor.close();
		return null;
	}
	
	//���ҷ�ҳ
	public List<Music> getScrollData(Integer offset, Integer maxResult){
		List<Music> musics = new ArrayList<Music>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from music limit ?,?",
				new String[]{offset.toString(), maxResult.toString()});
		
		while(cursor.moveToNext()){
			
			//ͨ�������Ҷ���
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String path = cursor.getString(cursor.getColumnIndex("path"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String artist = cursor.getString(cursor.getColumnIndex("artist"));
			String album = cursor.getString(cursor.getColumnIndex("album"));			
			String isDel = cursor.getString(cursor.getColumnIndex("isDel"));
			String isExist = cursor.getString(cursor.getColumnIndex("isExist"));
			
			//ʹ�û�ȡ�����ݣ�ʵ����һ��Person���󷵻�
			Music music = new Music(name, title, path, time, artist, album, isDel, isExist);
		
			musics.add(music);
		}
		cursor.close();
		return musics;
	}		

	//��ȡ������
	public long getCount()
	{
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from music", null);
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
