package com.guet.music.service;


public class Music
{
	public String  name;		//文件名(主键),如hao123(*.mp3略)
	public String  title;		//歌曲名称，如"荷塘月色"
	public String  path;		//歌曲路径名，如/mnt/sdcard/
	public String  time;		//歌曲总时间，单位为ms
	public String  artist;		//艺术家
	public String  album;		//专辑名
	public String  isDel;		//是否从播放列表中删除标识
	public String  isExist;		//是否存在于系统的存储器，标识文件有没有效
	
	//注：isDel isExist使用一个字符标识，即真"T"，假"F"
	
	//构造函数1
	public Music()
	{
		
	}
	
	//构造函数2
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
	
	//设置函数
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

	//重载toString，方便输出Log.i(TAG,Music.toString())
	@Override
	public String toString()
	{
		return "Music [name=" + name + ", title=" + title
		+ ", path=" + path + ", time=" + time + ", artist=" + artist
		+ ", album=" + album+ ", isDel=" + isDel+ ", isExist=" + isExist + "]";
	}

}
