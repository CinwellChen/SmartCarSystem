package com.guet.movie.service;


//ע������ʹ��Moive��ϵͳ��ͬ��!!!
public class myMovie
{
	public String  name;		//�ļ���(����),��hao123(*.3gp��)
	public String  title;		//�������ƣ���"���ŷɼ�"
	public String  path;		//����·��������/mnt/sdcard/
	public String  time;		//������ʱ�䣬��λΪms	
	
	//ע��isDel isExistʹ��һ���ַ���ʶ������"T"����"F"
	
	//���캯��1
	public myMovie()
	{
		
	}
	
	//���캯��2
	public myMovie(String name, String title, String path, String time  )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;		
	}
	
	//���ú���
	public void Set(String name, String title, String path, String time )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;		
	}	

	//����toString���������Log.i(TAG,Movie.toString())
	@Override
	public String toString()
	{
		return "Music [name=" + name + ", title=" + title
		+ ", path=" + path + ", time=" + time  + "]";
	}

}
