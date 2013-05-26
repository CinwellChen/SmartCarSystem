package com.guet.movie.service;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//"SQLiteOpenHelper"Ϊ�����࣬����ͨ������
public class DBOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASENAME = "smartCarMovie.db"; // ���ݿ�����
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
		//�������ݿ��еı�"movie", nameΪ����
		db.execSQL( "CREATE TABLE movie ( name varchar(40) primary key," +
				"title varchar(40), path varchar(60), time varchar(20) )" );
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
