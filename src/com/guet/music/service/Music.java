package com.guet.music.service;


public class Music
{
	public String  name;		//�ļ���(����),��hao123(*.mp3��)
	public String  title;		//�������ƣ���"������ɫ"
	public String  path;		//����·��������/mnt/sdcard/
	public String  time;		//������ʱ�䣬��λΪms
	public String  artist;		//������
	public String  album;		//ר����
	public String  isDel;		//�Ƿ�Ӳ����б���ɾ����ʶ
	public String  isExist;		//�Ƿ������ϵͳ�Ĵ洢������ʶ�ļ���û��Ч
	
	//ע��isDel isExistʹ��һ���ַ���ʶ������"T"����"F"
	
	//���캯��1
	public Music()
	{
		
	}
	
	//���캯��2
	public Music(String name, String title, String path, String time,
					String artist, String album, String isDel, String isExist )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;
		this.artist = artist;
		this.album = album;	
		this.isDel = isDel;
		this.isExist = isExist;
	}
	
	//���ú���
	public void Set(String name, String title, String path, String time,
					String artist, String album, String isDel, String isExist )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;
		this.artist = artist;
		this.album = album;	
		this.isDel = isDel;
		this.isExist = isExist;
	}	

	//����toString���������Log.i(TAG,Music.toString())
	@Override
	public String toString()
	{
		return "Music [name=" + name + ", title=" + title
		+ ", path=" + path + ", time=" + time + ", artist=" + artist
		+ ", album=" + album+ ", isDel=" + isDel+ ", isExist=" + isExist + "]";
	}

}
