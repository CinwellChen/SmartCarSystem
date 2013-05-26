package com.guet.SmartCarSystem;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.guet.SmartCarMusic.MyAdapter;

public class MyFileManager extends ListActivity
{
	private List<String> items = null;
	private List<String> paths = null;
	private List<String> backPaths = null;
	private Map<Long,Boolean> currentLrcHash = new HashMap<Long, Boolean>();

	private String rootPath = "/";
	private String sdPath = "/mnt/sdcard";
	private String curPath = "/";
	private String backPath = "/";	//��¼��һ��Ŀ¼

	private String curFile = "";
	private String curFilePath = "";
	private TextView mPath;
	
	private final static String TAG = "Car";	
	
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";
	
	protected static final String RESULT_ITEM = "result_item";		//���ؽ�����ʶ
	protected static final String RESULT_STYPE = "result_stype";	//���ؽ�����ͱ�ʶ
	private static final String FILTER_STYPE = "filter_stype";		//�û��������ͱ�ʶ
	private static String USER_REQUEST_STYPE;
	private static String USER_FILTER_STYPE;	//�û�Ҫ������ļ�������,����������Ƶ������Ƶ
	
	
	private static Button btnLocalView;		//����Ŀ¼
	private static Button btnSdView;		//SD��Ŀ¼
	private static Button btnBackView;		//������һ��Ŀ¼
	private static Button btnBeginSelect;	//��ʼѡ�񿪹�
	private static Button buttonConfirm;	//ȷ��
	private static Button buttonCancle;		//ȡ��
	
	private static boolean selectBeginFlag = false;	//��ʼѡ���־
	private static boolean notDisplayTip = false;	//��ֹ����ȡ��ʱ����ʾ���޷�����.."

	MyAdapter adapter;
	Handler mHandler; 
	
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		//(1)����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.fileselect2);
		
		//����Ԫ��
		mPath = (TextView) findViewById(R.id.mPath);		
		buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonCancle = (Button) findViewById(R.id.buttonCancle);
		btnLocalView = (Button) findViewById(R.id.buttonLocalView);
		btnSdView = (Button) findViewById(R.id.buttonSdView);
		btnBackView = (Button) findViewById(R.id.buttonBackView);
		btnBeginSelect = (Button) findViewById(R.id.buttonBeginSelect);
		
		//�����¼�
		buttonConfirm.setOnClickListener( btnListener );
		buttonCancle.setOnClickListener( btnListener );
		btnLocalView.setOnClickListener( btnListener );
		btnSdView.setOnClickListener( btnListener );
		btnBackView.setOnClickListener( btnListener );
		btnBeginSelect.setOnClickListener( btnListener );				
		
		//��ȡ�ļ���ʾ��������������ʾ���ͱ�־
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		USER_REQUEST_STYPE = bundle.getString(REQUEST_STYPE);
		USER_FILTER_STYPE = bundle.getString(FILTER_STYPE);
		Log.d(TAG,"User_Request_Stype="+ USER_REQUEST_STYPE);
		Log.d(TAG,"User_Filter_Stype="+ USER_FILTER_STYPE);
		
		getFileDir(rootPath);	//��ȡ��Ŀ¼����ʼ��ʾ
	}
	
	// �����ť���������¼�
	private OnClickListener btnListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.buttonLocalView:	//������ͼ
				{
					getFileDir(rootPath);
					curFilePath = rootPath;
				} break;
				
				case R.id.buttonSdView:		//SD����ͼ
				{
					getFileDir(sdPath);
					curFilePath = sdPath;
				} break;
				
				case R.id.buttonBackView:	//������һ��Ŀ¼
				{
					getFileDir(backPath);
					curFilePath = backPath;
					
				} break;
				
				case R.id.buttonBeginSelect://��ʼѡ��
				{
					currentLrcHash.clear();	//���ѡ���ʶ�б�
					
					if( selectBeginFlag == false )
					{
						btnBeginSelect.setText("����ȡ��");
						selectBeginFlag = true;
												
						//��ֹ������ť
						btnLocalView.setEnabled(false);
						btnSdView.setEnabled(false);
						btnBackView.setEnabled(false);
					}
					else
					{
						btnBeginSelect.setText("����ѡ��");
						selectBeginFlag = false;
						
						//ʹ��������ť
						btnLocalView.setEnabled(true);
						btnSdView.setEnabled(true);
						btnBackView.setEnabled(true);
						
						//��ֹȡ��ʱ����ʾ���޷����и�Ŀ¼��...
						notDisplayTip = true;
						
						//����ˢ��LISTVIEW						
						//����1
						getFileDir(curFilePath);
						
						//����2
						//adapter.notifyDataSetInvalidated();
						notDisplayTip = false;
					}
				} break;
				
				case R.id.buttonConfirm:	//ȷ��
				{
					//��ѡ����ļ����ļ�����LIST��ʽ���ͻ�ȥ
					if( selectBeginFlag == true )
					{
						List<String> selectItems = new ArrayList<String>();
						
						//�ռ���ѡ��������
						for( int position = 0; position< paths.size(); position++ )
						{
							if( adapter.itemsSelectTag.get(position) == (long)position )
							{
								String pathTemp = paths.get(position);
								if( pathTemp != null )
								{
									Log.d(TAG,"[" + position + "]=" + pathTemp );
									selectItems.add(pathTemp);
								}
							}							
						}
						
						//���������
						Intent data = new Intent(MyFileManager.this,SmartCarMusic.class);
						Bundle bundle = new Bundle();	
						bundle.putStringArrayList(RESULT_ITEM, (ArrayList<String>)selectItems);					
						bundle.putString(RESULT_STYPE, USER_REQUEST_STYPE);
						data.putExtras(bundle);						
						setResult(RESULT_OK, data);					
						
						finish();	//��������					
					}
				} break;
				
				case R.id.buttonCancle:		//ȡ��
				{
					finish();
				} break;
	
				default:
					break;
			}
		}
	};

	//��ȡĳһĿ¼�ṹ
	private void getFileDir(String filePath)
	{		
		Log.i(TAG,"filePath=" + filePath);
		
		File ff = new File(filePath);
		File[] files = ff.listFiles();
		//listFiles�ܹ���ȡ��ǰ�ļ����µ������ļ����ļ���
		
		if( files == null )
		{
			Log.d(TAG, "����Ϊ�գ�");
			if( notDisplayTip == false)
			{
				displayTip("�޷������Ŀ¼...");
			}
		}
		else
		{
			mPath.setText(filePath);			
			//��ʾ��ǰĿ¼·��
			
			items = new ArrayList<String>();	//�ļ��б�
			paths = new ArrayList<String>();	//·���б�
			backPaths = new ArrayList<String>();	//���ڷ����ϼ�·���б�
			
			//��Ϊ��Ŀ¼������ʾ��ҳ��������Ŀ
			//selectBeginFlag���ڷ�ֹˢ��VIEWʱ���ı䵱ǰbackPath
			if ( !filePath.equals(rootPath) && selectBeginFlag == false )
			{			
				//��������жϣ��Ƿ�Ϊ��Ŀ¼��������һ�д�����쳣��
				//java.lang.NullPointerException
				backPath = ff.getParent();
			}
						
			int size = files.length;			
			
			//��ȡ�������ж��ǣ���ӡ��ļ�����������ӡ��ļ��С�
			boolean fileFlag = USER_REQUEST_STYPE.equals("files");
			
			for ( int i = 0; i < size; i++ )
			{
				File file = files[i];
				
				//����MP3����Ƶ�ļ�
				if( file.isFile() && fileFlag )
				{
					String fileStype = getMIMEType(file);
					//Log.d(TAG,"fileStype=" + fileStype);
					
					//********************************************
					//�Բ�ͬ�û������ļ����͹��ˣ���������Ƶ����Ƶ�����
					//if( fileStype == "audio")
					if( fileStype.equals(USER_FILTER_STYPE) )
					{
						items.add(file.getName());
						paths.add(file.getPath());
						
						if( selectBeginFlag == false )
						{
							backPaths.add(file.getPath());
						}
						
					}
				}
				else if( file.isDirectory() ) 
				{
					items.add(file.getName());
					paths.add(file.getPath());
					
					if( selectBeginFlag == false )
					{
						backPaths.add(file.getPath());
					}
				}				
			}
			
			//������ʾ������
			adapter = new MyAdapter(this,items,paths);			
			setListAdapter(adapter);
			
			//��ʼ����ʾ��
			adapter.itemsSelectTag  = new ArrayList<Long>();
			for( int j=0; j <items.size(); j++)
			{
				adapter.itemsSelectTag.add(j, (long)(-1));
			}
		}	
	}
	
	@Override
	//����ѡ�У�˫������
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		File file = new File(paths.get(position));
		
		//�ж��Ƿ��ڡ���ѡ��ģʽ
		if( selectBeginFlag == false )
		{
			//���ݴ���					
			if (file.isDirectory())
			{
				curPath = paths.get(position);				
				curFilePath = paths.get(position);
				getFileDir(curFilePath);
			} 
			else {
				// ���Դ��ļ�	
				String curFileString = items.get(position);						 
				curFile = curPath + "/" + curFileString;
			}
		} else 
		{			
			if( adapter.itemsSelectTag.get(position) != (long)position )
			{
				boolean fileFlag = USER_REQUEST_STYPE.equals("files");
				
				if( fileFlag && file.isFile())
				{
					//��ֹѡ���ļ�ʱ��ѡ���ļ���
					adapter.itemsSelectTag.set(position, (long)position );
				}else if( fileFlag && file.isDirectory() ){
					displayTip("��ǰΪ�ļ�ѡ��ģʽ������ѡ���ļ���...");
				}

				if( !fileFlag && file.isDirectory() )
				{
					//��ֹѡ���ļ�ʱ��ѡ���ļ���
					adapter.itemsSelectTag.set(position, (long)position );
				} 
			}
			else {
				adapter.itemsSelectTag.set(position, (long)(-1) );
			}			
					
			//�¾�ǳ���Ҫ��ˢ����ʾ
			adapter.notifyDataSetInvalidated();
			//notifyDataSetInvalidated()�����ػ�ؼ�����ԭ����ʼ״̬��
			//notifyDataSetChanged()���ػ浱ǰ�ɼ�����			
		}
	}
	
	//��ʾ��ʾ��Ϣ
	private void displayTip( String tipStr )
	{
		if( tipStr != null )
			Toast.makeText(this,tipStr,Toast.LENGTH_SHORT).show();
	}

	//��ȡ�ļ�������
	private String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();
//		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
//				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		if( end.equals("mp3") )	//��Android2.2ϵͳֻ��mp3���������˴�ֻ��ʾmp3�ļ�
		{
			type = "audio";
		} 
		else if (end.equals("3gp") || end.equals("mp4")|| end.equals("rm")
				|| end.equals("rmvb")|| end.equals("avi")|| end.equals("wmv") )		
		{
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp"))
		{
			type = "image";
		} else
		{
			type = "*";
		}
		
		return type;
	}
}