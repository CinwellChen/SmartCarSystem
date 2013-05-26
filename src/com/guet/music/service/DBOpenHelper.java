package com.guet.music.service;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//"SQLiteOpenHelper"Ϊ�����࣬����ͨ������
public class DBOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASENAME = "smartCarMusic.db"; // ���ݿ�����
	private static final int DATABASEVERSION = 1;	// ���ݿ�汾

	//���캯��
	public DBOpenHelper(Context context)
	{
		//�������ݿ�,ָ�����ƺͰ汾
		super(context, DATABASENAME, null, DATABASEVERSION);
	}
	
	//����ʱ������
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//�������ݿ��еı�"music", nameΪ����
		db.execSQL("CREATE TABLE music (name varchar(20) primary key," +
				"title varchar(20), path varchar(40)," +
				"time varchar(20), artist varchar(20), " +
				"album varchar(20), isDel varchar(1), " +
				"isExist varchar(1))");
		// ִ���и��ĵ�sql���
	}

	//������£������ݿ�汾����ʱ������
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		//db.execSQL("DROP TABLE IF EXISTS music");
		//onCreate(db);
	}
}
