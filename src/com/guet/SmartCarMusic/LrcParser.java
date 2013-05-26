package com.guet.SmartCarMusic;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

//������������LRC�ļ� ������������LRC�ļ�����һ��LrcInfo������ ���ҷ������LrcInfo����
public class LrcParser
{
	private static final String TAG = "Car";

	private LrcInfo lrcinfo = new LrcInfo();

	private long currentTime = 0;// �����ʱʱ��
	private String currentContent = null;// �����ʱ���
	private Map<Long, String> maps = new HashMap<Long, String>();// �û��������еĸ�ʺ�ʱ�����Ϣ���ӳ���ϵ��Map
	public Map<Long, String> mapsToLrc = new HashMap<Long, String>();// �û��������еĸ�ʺ�ʱ�����Ϣ���ӳ���ϵ��Map
	
	int lrcPosition=0;				//��ǰLRC����ʹ�õĸ���ʱ�䣬��λMS
	boolean shouldDisplay=false;	//LRC��ʾ�߳�����
	boolean shouldRun = true;		//LRC��ʾ�̼߳���
	private Boolean lrcFilecode=true;		//�����LRC�ļ�����falseΪGBK��trueΪUTF-8
											//LRC�ļ���������Ҫ��ʽ
		
	/**
	 * �����ļ�·������ȡ�ļ�������һ��������
	 * @param path   ·��
	 * @return ������
	 * @throws FileNotFoundException
	 */
	private InputStream readLrcFile(String path) throws FileNotFoundException
	{
		File f = new File(path);
		InputStream ins = new FileInputStream(f);
		
		return ins;
	}

	public LrcInfo parser(String path) throws Exception
	{
		InputStream in = readLrcFile(path);
		lrcinfo = parser(in);
		
		return lrcinfo;
	}

	/**
	 * ���������е���Ϣ����������һ��LrcInfo����
	 * @param inputStream������
	 * @return �����õ�LrcInfo����
	 * @throws IOException
	 */
	public LrcInfo parser(InputStream inputStream) throws IOException
	{
		String fileCode;
		
		// �����װ
		if( lrcFilecode == true )	//UTF-8����
		{
			fileCode="utf-8";
		}
		else {						//GBK����(����)
			fileCode="GBK";
		}
		
		InputStreamReader inr = new InputStreamReader(inputStream,fileCode);
		BufferedReader reader = new BufferedReader(inr);		
		
		// һ��һ�еĶ���ÿ��һ�У�����һ��
		String line = null;		
		while ((line = reader.readLine()) != null)
		{
			parserLine(line);
		}
		
		// ȫ�������������info
		lrcinfo.setInfos(maps);
		Log.i(TAG,"mapsToLrc׼���ã�");
		
		return lrcinfo;
	}

	/**
	 * * ����������ʽ����ÿ�о������ ���ڽ���������󣬽�������������Ϣ������LrcInfo������
	 * @param str
	 */
	private void parserLine(String str)
	{		
		// ȡ�ø�������Ϣ
		if (str.startsWith("[ti:"))
		{
			String title = str.substring(4, str.length() - 1);
			Log.i( TAG,"[ti:" + title + "]");
			lrcinfo.setTitle(title);
		}
		// ȡ�ø�����Ϣ
		else if (str.startsWith("[ar:"))
		{
			String singer = str.substring(4, str.length() - 1);
			Log.i( TAG,"[ar:" + singer + "]");
			lrcinfo.setSinger(singer);
		}
		// ȡ��ר����Ϣ
		else if (str.startsWith("[al:"))
		{
			String album = str.substring(4, str.length() - 1);
			Log.i( TAG,"[al:" + album + "]");
			lrcinfo.setAlbum(album);
		}
		else if (str.startsWith("[by:"))
		{
			String lrcMaker = str.substring(4, str.length() - 1);
			Log.i( TAG,"[by:" + lrcMaker + "]");
			lrcinfo.setLrcMaker(lrcMaker);
		}
		// ͨ������ȡ��ÿ������Ϣ
		else
		{
			// �����������
			String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
			// ����
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(str);

			// �������ƥ�����ִ�����²���
			while (matcher.find())
			{
				// �õ�ƥ�����������
				String msg = matcher.group();
				// �õ����ƥ���ʼ������
				int start = matcher.start();
				// �õ����ƥ�������������
				int end = matcher.end();

				// �õ����ƥ�����е�����
				int groupCount = matcher.groupCount();
				// �õ�ÿ����������
				for (int i = 0; i <= groupCount; i++)
				{
					String timeStr = matcher.group(i);
					if (i == 1)
					{
						// ���ڶ����е���������Ϊ��ǰ��һ��ʱ���
						currentTime = strToLong(timeStr);
					}
				}

				// �õ�ʱ���������
				String[] content = pattern.split(str);
				// �����������
				for (int i = 0; i < content.length; i++)
				{
					if (i == content.length - 1)
					{
						// ����������Ϊ��ǰ����
						currentContent = content[i];
					}
				}
				
				// ����ʱ�������ݵ�ӳ��
				maps.put(currentTime, currentContent);				
				long temp = currentTime/1000;
				mapsToLrc.put(temp, currentContent);
			}
		}
	}

	/**
	 * �������õ��ı�ʾʱ����ַ�ת��ΪLong��
	 * @param group�ַ���ʽ��ʱ���
	 * @return Long��ʽ��ʱ��
	 */
	private long strToLong(String timeStr)
	{
		// ��Ϊ������ַ�����ʱ���ʽΪXX:XX.XX,���ص�longҪ�����Ժ���Ϊ��λ
		// 1:ʹ�ã��ָ� 2��ʹ��.�ָ�
		String[] s = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		String[] ss = s[1].split("\\.");
		int sec = Integer.parseInt(ss[0]);
		int mill = Integer.parseInt(ss[1]);
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

	//����Ҫ������ļ�
	//������fileCodeΪ�ļ����룬�˴�����UTF-8ΪTure,GBK������ΪFalse!
	public boolean setFile (String lrcFilePath, Boolean fileCode )
	{	
		try
		{
			this.mapsToLrc.clear();	//���HASH����ֹ�ϴε�Ӱ�죡��
			this.lrcFilecode = fileCode;
			this.parser(lrcFilePath);
			Log.i(TAG,"public void SetFile:" + lrcFilePath + "ok!");
			return true;			//ת����ɣ�������ֵ
		
		} catch (Exception e)
		{
			Log.i(TAG,"public void SetFile:" + lrcFilePath + "error!");
			Log.i(TAG,e.toString());
			return false;
		}
	}	
	
	public String getMapsToLrc( long position )
	{
		String temp = mapsToLrc.get(position);
		return temp;
	}
}