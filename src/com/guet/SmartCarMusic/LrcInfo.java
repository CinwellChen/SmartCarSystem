package com.guet.SmartCarMusic;

import java.util.HashMap;
import java.util.Map;

//������װ�����Ϣ����
public class LrcInfo {
   
	private static final String TAG = "Car";
	private String title;	//������
	private String singer;	//�ݳ���
	private String album;	//ר��	
	private String lrcMaker;	//��ʱ���
	private HashMap<Long,String> infos;//��������Ϣ��ʱ���һһ��Ӧ��Map
   	
	//����Ϊgetter()  setter()	
	public void setAlbum(String album2)
	{		
		this.album = album2;
	}

	public void setSinger(String singer2)
	{		
		this.singer = singer2;
	}

	public void setTitle(String title2)
	{		
		this.title = title2;	
	}

	public void setInfos(Map<Long, String> maps)
	{		
		this.infos = (HashMap<Long, String>)(maps);
	}

	public void setLrcMaker(String lrcMaker2)
	{		
		this.lrcMaker = lrcMaker2;
	}
}
