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
	private String backPath = "/";	//记录上一级目录

	private String curFile = "";
	private String curFilePath = "";
	private TextView mPath;
	
	private final static String TAG = "Car";	
	
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";
	
	protected static final String RESULT_ITEM = "result_item";		//返回结果项标识
	protected static final String RESULT_STYPE = "result_stype";	//返回结果类型标识
	private static final String FILTER_STYPE = "filter_stype";		//用户过滤类型标识
	private static String USER_REQUEST_STYPE;
	private static String USER_FILTER_STYPE;	//用户要求过滤文件的类型,以区分是音频还是视频
	
	
	private static Button btnLocalView;		//本地目录
	private static Button btnSdView;		//SD卡目录
	private static Button btnBackView;		//返回上一层目录
	private static Button btnBeginSelect;	//开始选择开关
	private static Button buttonConfirm;	//确定
	private static Button buttonCancle;		//取消
	
	private static boolean selectBeginFlag = false;	//开始选择标志
	private static boolean notDisplayTip = false;	//防止点我取消时，显示“无法进入.."

	MyAdapter adapter;
	Handler mHandler; 
	
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		//(1)设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.fileselect2);
		
		//查找元件
		mPath = (TextView) findViewById(R.id.mPath);		
		buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonCancle = (Button) findViewById(R.id.buttonCancle);
		btnLocalView = (Button) findViewById(R.id.buttonLocalView);
		btnSdView = (Button) findViewById(R.id.buttonSdView);
		btnBackView = (Button) findViewById(R.id.buttonBackView);
		btnBeginSelect = (Button) findViewById(R.id.buttonBeginSelect);
		
		//设置事件
		buttonConfirm.setOnClickListener( btnListener );
		buttonCancle.setOnClickListener( btnListener );
		btnLocalView.setOnClickListener( btnListener );
		btnSdView.setOnClickListener( btnListener );
		btnBackView.setOnClickListener( btnListener );
		btnBeginSelect.setOnClickListener( btnListener );				
		
		//获取文件显示类型请求，设置显示类型标志
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		USER_REQUEST_STYPE = bundle.getString(REQUEST_STYPE);
		USER_FILTER_STYPE = bundle.getString(FILTER_STYPE);
		Log.d(TAG,"User_Request_Stype="+ USER_REQUEST_STYPE);
		Log.d(TAG,"User_Filter_Stype="+ USER_FILTER_STYPE);
		
		getFileDir(rootPath);	//获取根目录，开始显示
	}
	
	// 多个按钮单击倾听事件
	private OnClickListener btnListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.buttonLocalView:	//本地视图
				{
					getFileDir(rootPath);
					curFilePath = rootPath;
				} break;
				
				case R.id.buttonSdView:		//SD卡视图
				{
					getFileDir(sdPath);
					curFilePath = sdPath;
				} break;
				
				case R.id.buttonBackView:	//返回上一级目录
				{
					getFileDir(backPath);
					curFilePath = backPath;
					
				} break;
				
				case R.id.buttonBeginSelect://开始选择
				{
					currentLrcHash.clear();	//清空选择标识列表
					
					if( selectBeginFlag == false )
					{
						btnBeginSelect.setText("点我取消");
						selectBeginFlag = true;
												
						//禁止其它按钮
						btnLocalView.setEnabled(false);
						btnSdView.setEnabled(false);
						btnBackView.setEnabled(false);
					}
					else
					{
						btnBeginSelect.setText("点我选择");
						selectBeginFlag = false;
						
						//使能其它按钮
						btnLocalView.setEnabled(true);
						btnSdView.setEnabled(true);
						btnBackView.setEnabled(true);
						
						//防止取消时，显示“无法进行该目录”...
						notDisplayTip = true;
						
						//生新刷新LISTVIEW						
						//方法1
						getFileDir(curFilePath);
						
						//方法2
						//adapter.notifyDataSetInvalidated();
						notDisplayTip = false;
					}
				} break;
				
				case R.id.buttonConfirm:	//确认
				{
					//将选择的文件或文件夹以LIST形式传送回去
					if( selectBeginFlag == true )
					{
						List<String> selectItems = new ArrayList<String>();
						
						//收集点选的数据项
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
						
						//将数据项返回
						Intent data = new Intent(MyFileManager.this,SmartCarMusic.class);
						Bundle bundle = new Bundle();	
						bundle.putStringArrayList(RESULT_ITEM, (ArrayList<String>)selectItems);					
						bundle.putString(RESULT_STYPE, USER_REQUEST_STYPE);
						data.putExtras(bundle);						
						setResult(RESULT_OK, data);					
						
						finish();	//结束返回					
					}
				} break;
				
				case R.id.buttonCancle:		//取消
				{
					finish();
				} break;
	
				default:
					break;
			}
		}
	};

	//获取某一目录结构
	private void getFileDir(String filePath)
	{		
		Log.i(TAG,"filePath=" + filePath);
		
		File ff = new File(filePath);
		File[] files = ff.listFiles();
		//listFiles能够获取当前文件夹下的所有文件和文件夹
		
		if( files == null )
		{
			Log.d(TAG, "发现为空！");
			if( notDisplayTip == false)
			{
				displayTip("无法进入该目录...");
			}
		}
		else
		{
			mPath.setText(filePath);			
			//显示当前目录路径
			
			items = new ArrayList<String>();	//文件列表
			paths = new ArrayList<String>();	//路径列表
			backPaths = new ArrayList<String>();	//用于返回上级路径列表
			
			//不为根目录，则显示主页、返回项目
			//selectBeginFlag用于防止刷新VIEW时，改变当前backPath
			if ( !filePath.equals(rootPath) && selectBeginFlag == false )
			{			
				//必须加以判断，是否为根目录，否则下一行代码会异常！
				//java.lang.NullPointerException
				backPath = ff.getParent();
			}
						
			int size = files.length;			
			
			//获取用请求，判断是：添加“文件”，还是添加“文件夹”
			boolean fileFlag = USER_REQUEST_STYPE.equals("files");
			
			for ( int i = 0; i < size; i++ )
			{
				File file = files[i];
				
				//过滤MP3等音频文件
				if( file.isFile() && fileFlag )
				{
					String fileStype = getMIMEType(file);
					//Log.d(TAG,"fileStype=" + fileStype);
					
					//********************************************
					//对不同用户进行文件类型过滤，以区分音频、视频浏览器
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
			
			//更新显示适配器
			adapter = new MyAdapter(this,items,paths);			
			setListAdapter(adapter);
			
			//初始化显示项
			adapter.itemsSelectTag  = new ArrayList<Long>();
			for( int j=0; j <items.size(); j++)
			{
				adapter.itemsSelectTag.add(j, (long)(-1));
			}
		}	
	}
	
	@Override
	//单击选中，双击操作
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		File file = new File(paths.get(position));
		
		//判断是否处于“非选择”模式
		if( selectBeginFlag == false )
		{
			//数据处理					
			if (file.isDirectory())
			{
				curPath = paths.get(position);				
				curFilePath = paths.get(position);
				getFileDir(curFilePath);
			} 
			else {
				// 可以打开文件	
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
					//防止选择文件时，选择文件夹
					adapter.itemsSelectTag.set(position, (long)position );
				}else if( fileFlag && file.isDirectory() ){
					displayTip("当前为文件选择模式，不能选择文件夹...");
				}

				if( !fileFlag && file.isDirectory() )
				{
					//防止选择文件时，选择文件夹
					adapter.itemsSelectTag.set(position, (long)position );
				} 
			}
			else {
				adapter.itemsSelectTag.set(position, (long)(-1) );
			}			
					
			//下句非常重要，刷新显示
			adapter.notifyDataSetInvalidated();
			//notifyDataSetInvalidated()，会重绘控件（还原到初始状态）
			//notifyDataSetChanged()，重绘当前可见区域			
		}
	}
	
	//显示提示信息
	private void displayTip( String tipStr )
	{
		if( tipStr != null )
			Toast.makeText(this,tipStr,Toast.LENGTH_SHORT).show();
	}

	//获取文件的类型
	private String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();
//		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
//				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		if( end.equals("mp3") )	//因Android2.2系统只带mp3解码器，此处只显示mp3文件
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