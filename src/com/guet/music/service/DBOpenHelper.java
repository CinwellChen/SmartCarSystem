package com.guet.music.service;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//"SQLiteOpenHelper"为抽象类，必须通过继续
public class DBOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASENAME = "smartCarMusic.db"; // 数据库名称
	private static final int DATABASEVERSION = 1;	// 数据库版本

	//构造函数
	public DBOpenHelper(Context context)
	{
		//创建数据库,指定名称和版本
		super(context, DATABASENAME, null, DATABASEVERSION);
	}
	
	//创建时被调用
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//创建数据库中的表"music", name为主键
		db.execSQL("CREATE TABLE music (name varchar(20) primary key," +
				"title varchar(20), path varchar(40)," +
				"time varchar(20), artist varchar(20), " +
				"album varchar(20), isDel varchar(1), " +
				"isExist varchar(1))");
		// 执行有更改的sql语句
	}

	//软件更新，如数据库版本更新时被调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		//db.execSQL("DROP TABLE IF EXISTS music");
		//onCreate(db);
	}
}
