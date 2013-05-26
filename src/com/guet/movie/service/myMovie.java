package com.guet.movie.service;


//注：不能使用Moive与系统类同名!!!
public class myMovie
{
	public String  name;		//文件名(主键),如hao123(*.3gp略)
	public String  title;		//歌曲名称，如"龙门飞甲"
	public String  path;		//歌曲路径名，如/mnt/sdcard/
	public String  time;		//歌曲总时间，单位为ms	
	
	//注：isDel isExist使用一个字符标识，即真"T"，假"F"
	
	//构造函数1
	public myMovie()
	{
		
	}
	
	//构造函数2
	public myMovie(String name, String title, String path, String time  )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;		
	}
	
	//设置函数
	public void Set(String name, String title, String path, String time )
	{	
		this.name = name;
		this.title = title;
		this.path = path;
		this.time = time;		
	}	

	//重载toString，方便输出Log.i(TAG,Movie.toString())
	@Override
	public String toString()
	{
		return "Music [name=" + name + ", title=" + title
		+ ", path=" + path + ", time=" + time  + "]";
	}

}
