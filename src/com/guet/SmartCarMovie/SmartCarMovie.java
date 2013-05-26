package com.guet.SmartCarMovie;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ViewFlipper;

import com.guet.SmartCarSystem.MyFileManager;
import com.guet.SmartCarSystem.R;
import com.guet.SmartCarSystem.SmartCarSystem;
import com.guet.movie.service.MovieServiceApp;
import com.guet.movie.service.myMovie;

//车载信息系统：2部分：视频播放器
public class SmartCarMovie extends Activity
{
	private MediaPlayer movieMediaPlayer;
	boolean movieIsPlay = false;
	boolean voiceOpen = true;
	boolean nextPlayFlag = false;
	private static String[] multiSelectitems1 = null;	// 选项数组
	private static String[] multiSelectitems2 = null;	// 选项数组
	private static String[] multiSelectitems3 = null;	// 选项数组
	private static String[] multiSelectitems4 = null;	// 选项数组
	
	private static final int listAddStype = 1;
	private static final int listDelStype = 2;
	private static final int listPlayStype = 3;
	private static final int listViewStype = 4;
	
	private ViewFlipper vfmovie;
	private Animation in_lefttoright;
	private Animation out_lefttoright;
	private Animation in_righttoleft;
	private Animation out_righttoleft;
	private static Button enterMovie;
	private static Button enterList;
	
	private static ImageView btnPlayStop; // 播放与暂停
	private static Button btnViewChoice; // 窗口选项(最小化、全屏、关闭)
	private static Button btnStypeChoice; // 模式选项(单视频播放、顺序、循环、随机播放)
	private static Button btnDelChoice; // 删除选项(当前播放、当前选择、全部)
	private static Button btnAddChoice; // 添加选项(文件、文件夹)
	
	private static ImageView btnReset; // 复位，停止
	private static ImageView fullScreenChange; // 全屏切换
	private static ImageButton btnVoice; // 声音
	private static SeekBar seekBarVoice; // 音量进度条
	private static SeekBar seekBarPlayProcess; // 视频进度条
	private static ListView movieListView;	//视频列表适配器
	private static TextView playTime; // 当前播放时间
	private static TextView durationTime; // 总播放时间
	private static TextView textMovieState;	//当前视频播放状态
	private static TextView textMoviePlayStype;	//视频播放模式
	
	protected static final int FILE_RESULT_CODE = 2;
	private Handler mHandler; // 让子线程刷新UI使用
	private SurfaceView surfaceView;	//视频播放
	private SurfaceView surfaceViewFull;

	private AudioManager audioManager; // 声音管理器
	private int soundVolume = 0; 	// 音量变量
	private int soundMaxVolume = 0; // 最大音量值
	private int soundSaveVolume = 0; // 静音时保存的音量值
	private int seekbarNum = 0; // 音量控制条值
	private int moviePosition; // 突发事件保存变量
	static int moviePlayType = 0; // 播放模式切换
	
	// 视频列表数据库，及其显示映射表
	private List<String> listNamesSave; // 视频列表名称
	private List<String> listTimesSave; // 视频列表时间

	MovieServiceApp myMovieListDb; // 音频数据库
	movieListAdapter mAdapter; // 显示适配器
	private int currentListItem; // 当前播放视频的索引
	private String currentListPath = null;	//当前播放文件全路径
	

	private int currentListItemSave; // 当前选择项
	private String selectItemNameSave; // 长击选中的选项名称
	private String clickItemNameSave; // 单击选中的选项名称

	protected static final String TAG = "Car";
	protected static final int MSG_PLAYTIME = 0;
	private final static int TIME = 6868;
	private final static int HIDE_CONTROLER = 1;
	
	movieMapToSeekBar movieThread1; // 子线程1
	
	// 添加文件时，是否播放视频
	boolean ThreadRunFlag = false; // 用于等待线程结束标识
	private boolean surfaceViewSavaFlag = false;	//用于记录恢复
	private boolean mediaPlayerIsPause = true;		//判断是否为暂停	
	
	protected static final String RESULT_ITEM = "result_item"; // 返回结果项标识
	protected static final String RESULT_STYPE = "result_stype"; // 返回结果类型标识
	protected static final String DISPLAY_AUDIO = "video";
	private static final String FILTER_STYPE = "filter_stype";		//用户过滤类型标识
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";	
	
	private View controlView = null;		//弹出控制栏
	private PopupWindow controlWndow = null;
		
	private boolean isControllerShow = false;	//是否显示控制器、全屏标识	
	private boolean isFullScreen = false;
	private boolean isFullBack = false;			//全屏返回标识
	private static int changeToFullPosition=0;	//全屏切换用于保存断点
	
	private View normalViewSave = null;
	private View fullViewSave = null;	
	private GestureDetector mGestureDetector = null;	//手势识别	

	
	//系统启动创建函数
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//缓存两个布局,初始化布局
		normalViewSave = getLayoutInflater().inflate(R.layout.moviehome2, null);
		fullViewSave = getLayoutInflater().inflate(R.layout.surfacefull, null);
		setContentView(normalViewSave);
		
		//"onResume()"后启动后调用,用于初始时显示弹出窗口
		/*Looper.myQueue().addIdleHandler(new IdleHandler()
		{
			@Override
			public boolean queueIdle() 
			{	
				if(controlView != null )	//控件窗口不为空，则显示
				{
					if( true )	//在AVD仿真器中显示
						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
					else		//在真实开发板中显示(7寸显示屏)
						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
			     
					putFuncationName("queueIdle");
		        	isControllerShow = true;
				}
				return false;  
			}
        });	*/
	
		initMovieComponent(); // 初始化GUI元件
		initMovieVariable(); // 初始化系统变量
		ReadSharedPreferences(); // 读取系统配置参数
		initMovieObject(); // 初始化系统一些保存的对象，将其从存储空间中读出
		initMovieListView(); // 初始化播放列表
		movieListViewSetListener();	//显示列表事件设置		
		
		// 通过getStream/MaxVolume获得当前音量大小,视频的最大音量大小,并设置seek进度条最大值
		soundVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundMaxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		seekBarVoice.setMax(soundMaxVolume);
		// 把当前音量的值设置给进度条
		seekBarVoice.setProgress(soundVolume);
	
		// 启动一个"Handler"用于接收消息
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case MSG_PLAYTIME:
					{
						// 获得当前播放的进度值
						int sekPosition = movieMediaPlayer.getCurrentPosition();
						//Log.d(TAG,"thread_positon=" + sekPosition);
						seekBarPlayProcess.setProgress(sekPosition);
						// 更新显示时间		
						playTime.setText( toTime(sekPosition));
					}
						break;
					
					//用于延长控制栏显示
					case HIDE_CONTROLER:
					{
						if( isFullScreen )	//用于全屏
						{
							hideController();
						}
					} break;
					
					default:
						break;
				}

				super.handleMessage(msg);
			}
		};

		// 处理播放视频进度条线程
		movieThread1 = new movieMapToSeekBar();
		movieThread1.start();
		ThreadRunFlag = true;	

		// 视频视频进度条拖动
		seekBarPlayProcess.setOnSeekBarChangeListener( seekbarPlayListener );
		// 播放器完成事件监听
		movieMediaPlayer.setOnCompletionListener( completionPlayListener );
	
		//手势动作倾听事件
		gestureDetectorListener();
		putFuncationName("onCreate");
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		putFuncationName("onStart");
	}

	@Override
	protected void onRestart()
	{
		// 检查系列存在检查文件
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile.exists())
		{
			// 文件不存在，证明主页已关闭，表示其它子页不能返回
			finish(); // 退出
		}

		super.onRestart();
		putFuncationName("onRestart");
	}

	@Override
	protected void onStop()
	{
		putFuncationName("onStop");
		super.onStop();
	}

	// 当有电话有时,添加文件时会引发
	@Override
	protected void onPause()
	{
		// 当前播放器不工作、添加文件、返回主页 3种情况不执行停止
		if( currentListPath != null)
		{
			if (movieMediaPlayer.isPlaying() || mediaPlayerIsPause )					
			{			
				// 保存当前播放的位置
				movieMediaPlayer.pause();
				moviePosition = movieMediaPlayer.getCurrentPosition();
				seekBarPlayProcess.setProgress(moviePosition);
				Log.d(TAG, "moviePosition=" + moviePosition);
				//movieMediaPlayer.pause();			
				// movieIsPlay = false;
				surfaceViewSavaFlag = true;
			}
		}
		
		super.onPause();
		putFuncationName("onPause");
	}
	
	// 接完电话后,添加文件后会调用
	@Override
	protected void onResume()
	{		
		super.onResume();
		putFuncationName("onResume");
	}

	// 重新创建后，比如内存吃紧系统会将停止或暂停Activity杀死后再再恢复
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		Log.w(TAG, "onRestoreInstanceState()!");
	}

	// 在未知事件发生时，保存数据(系列化到系统硬盘)，如系统内存紧张，该ACTIVITY会被杀死
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		Log.w(TAG, "onSaveInstanceState()!");
	}
	
	// 初始化GUI控件
	private void initMovieComponent()
	{
		vfmovie = (ViewFlipper)findViewById(R.id.movievp);
		in_righttoleft = AnimationUtils.loadAnimation(this,R.anim.enter_righttoleft);
		out_righttoleft = AnimationUtils.loadAnimation(this,R.anim.out_righttoleft);
		in_lefttoright = AnimationUtils.loadAnimation(this,R.anim.enter_lefttoright);
		out_lefttoright = AnimationUtils.loadAnimation(this,R.anim.out_lefttoright);
		enterMovie = (Button)findViewById(R.id.entermovie);
		enterList = (Button)findViewById(R.id.enterlist);
		//控制栏、全屏ID查找
		controlView = getLayoutInflater().inflate(R.layout.control, null);
		controlWndow =   new PopupWindow(controlView, 
						LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);		

		// 1查找控制ID*************************************************		
		//主控制组
		btnReset = (ImageView) controlView.findViewById(R.id.ImageViewReset);
		btnPlayStop = (ImageView) controlView.findViewById(R.id.imageViewPlayStop);
		btnVoice = (ImageButton)findViewById(R.id.imageButtonVoice);
		fullScreenChange = (ImageView) controlView.findViewById(R.id.imageViewFullChange);
		
		//主控件组
		seekBarVoice = (SeekBar) findViewById(R.id.seekBarVoice);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		movieListView = (ListView) findViewById(R.id.listViewMovie);
		seekBarPlayProcess = (SeekBar) controlView.findViewById(R.id.seekBarPlay);
		
		//时间等显示组
		textMovieState = (TextView) controlView.findViewById(R.id.textMovieState);
		textMoviePlayStype = (TextView) controlView.findViewById(R.id.textMoviePlayStype);
		playTime = (TextView) controlView.findViewById(R.id.moviePlayTime);
		durationTime = (TextView)controlView. findViewById(R.id.movieDurationTime);
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);		
		//surfaceViewFull = (SurfaceView)fullView.findViewById(R.id.surfaceViewFull);		
		
		//选项组
		btnAddChoice = (Button) findViewById(R.id.movieBtnAddFiles1);
		btnAddChoice = (Button) findViewById(R.id.movieBtnAddFiles1);
		btnDelChoice = (Button) findViewById(R.id.movieBtnDelFiles1);
		btnStypeChoice = (Button) findViewById(R.id.movieBtnPlayStype1);
		btnViewChoice = (Button) findViewById(R.id.movieBtnViewStype1);
		
		/////////////
		enterMovie.setOnClickListener(movieClickListener);
		enterList.setOnClickListener(movieClickListener);
		// 2添加倾听事件*************************************************
		btnReset.setOnClickListener(movieClickListener);
		btnPlayStop.setOnClickListener(movieClickListener);
		btnVoice.setOnClickListener(movieClickListener);
		fullScreenChange.setOnClickListener(movieClickListener);
	
		//事件倾听事件		
		btnAddChoice.setOnClickListener(movieChoiceListener);
		btnDelChoice.setOnClickListener(movieChoiceListener);
		btnStypeChoice.setOnClickListener(movieChoiceListener);
		btnViewChoice.setOnClickListener(movieChoiceListener);

		// 添加视频文件
		seekBarVoice.setOnSeekBarChangeListener(voiceChangeListener);
	}

	// 初始化系统变量
	private void initMovieVariable()
	{
		// 初始化其它变量
		movieMediaPlayer = new MediaPlayer(); // 视频文件及播放器
		// currentListItem = 0; // 当前播放视频序号初始为0
		moviePlayType = 1; // 初始为顺序播放模式
		playTime.setText(""); 	// 播放时间和总时间显示初始化
		durationTime.setText("");
		seekBarPlayProcess.setEnabled(false); // 禁止拖动进度条
		btnReset.setEnabled(false); // 复位初始禁止
		textMoviePlayStype.setText("顺序播放");
		
		// 视频列实现
		listNamesSave = new ArrayList<String>();
		listTimesSave = new ArrayList<String>();
		
		multiSelectitems1 = getResources().getStringArray(R.array.addFileStype); 
		multiSelectitems2 = getResources().getStringArray(R.array.delFileStype); 
		multiSelectitems3 = getResources().getStringArray(R.array.moviePlayStype); 
		multiSelectitems4 = getResources().getStringArray(R.array.movieViewStype); 

		//视频显示设置
        surfaceView.getHolder().setFixedSize(320,240);//设置分辨率
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback()); 
	}

	// 初始化系统一些保存的对象，将其从存储空间中读出
	private void initMovieObject()
	{
		// **********************************************************
		// 1初始化视频列表数据库
		try
		{
			Log.d(TAG, "创建数据库:");
			myMovieListDb = new MovieServiceApp(this);
			Log.d(TAG, "  成功！");
			
			Log.d(TAG, "初始化存储器中的视频数据!");			
			initMovieDbData();			

		} catch (Throwable e1)
		{
			Log.e(TAG, e1.toString());
			Log.d(TAG, "  失败！");
		}
		
		// 2视频列表-名称"数据库"操作
		File nameFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListNames.db");
		if (!nameFile.exists()) // 若文件不存在，则创建
		{
			try
			{
				nameFile.createNewFile();
			} catch (IOException e)
			{
				Log.e(TAG, e.toString());
			}
		}

		try
		{
			ObjectInputStream nameOi = new ObjectInputStream(
					new FileInputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListNames.db"));
			listNamesSave = (List<String>) nameOi.readObject();
			nameOi.close();

		} catch (IOException e)
		{
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e)
		{
			Log.e(TAG, e.toString());
		}

		// **********************************************************
		// 3视频列表-时间"数据库"操作
		File timeFile = new File(""
				+ "/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListTimes.db");
		if (!timeFile.exists()) // 若文件不存在，则创建
		{
			try
			{
				timeFile.createNewFile();
			} catch (IOException e)
			{
				Log.e(TAG, e.toString());
			}
		}

		try
		{
			ObjectInputStream timeOi = new ObjectInputStream(
					new FileInputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListTimes.db"));
			listTimesSave = (List<String>) timeOi.readObject();
			timeOi.close();

		}  catch (IOException e)
		{
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e)
		{
			Log.e(TAG, e.toString());
		}
	}

	// 初始化视频数据库
	void initMovieDbData() throws Throwable
	{
		Cursor cursorTemp;

		// 获取外部存储器中所有视频文件
		cursorTemp = this.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				// 这个字符串数组表示要查询的列
				new String[]
				{ 
						MediaStore.Video.Media.DISPLAY_NAME,// 0
						// 视频文件名
						MediaStore.Video.Media.TITLE, // 1						
						// 视频的名称
						MediaStore.Audio.Media.DATA, // 2
						// 视频文件的路径
						MediaStore.Audio.Media.DURATION, // 3
						// 视频的总时间

				}, null, // 查询条件，相当于sql中的where语句
				null, // 查询条件中使用到的数据
				null // 查询结果的排序方式
				);

		// 将查找到的视频数据，添加到数据库中
		if (cursorTemp != null)
		{
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr;

			// 从Cursor中分离出数据送至ListView中显示
			for (int i = 0; i < cursorTemp.getCount(); i++)
			{
				cursorTemp.moveToPosition(i);

				// 先读取视频名称
				nameStr = cursorTemp.getString(0);

				// 进行视频文件过滤
				String movieFilter = nameStr.substring(nameStr.length() - 3);
				Log.d(TAG,movieFilter);
				if( movieFilter.equals("3gp") || movieFilter.equals("mp4") 
						|| movieFilter.equals(".rm")|| movieFilter.equals("mvb")
						|| movieFilter.equals("avi") || movieFilter.equals("wmv")
						||movieFilter.equals("MP4") )
				{
					
				}	
				else {
					Log.d(TAG, "系统对该文件: " + nameStr + "暂不支持!");
					continue;
				}
				Log.d(TAG, "视频扫描文件: " + nameStr );

				// 查看数据库是否存在
				myMovie movie1 = new myMovie();
				movie1 = myMovieListDb.find(nameStr);
				if (movie1 == null)
				{
					// 若不存在，则添加
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);					

					myMovie movie2 = new myMovie(); // 重新NEW一个对象，因为前面为NULL赋值
					movie2.Set( nameStr, titleStr, pathStr, timeStr );
					myMovieListDb.save(movie2);
					addCount++;
				}
			}
			cursorTemp.moveToFirst(); // 遍历完后复位
			cursorTemp.close(); // 关闭，释放资源

			if (addCount > 0)
			{
				Log.d(TAG, "初始化视频数据库：发现" + addCount + "个新视频文件...");
			} else
			{
				Log.d(TAG, "初始化视频数据库：未发现新视频文件...");
			}
		}		
	}
	// 初始化视频播放列表
	private void initMovieListView()
	{
		int position, listSize;

		// 新建适配器，更新显示
		mAdapter = new movieListAdapter(this, listNamesSave,
				listTimesSave);
		movieListView.setAdapter(mAdapter);

		// 刷新选项
		currentListItem = currentListItemSave;
		mAdapter.clickItemName = clickItemNameSave;
		mAdapter.selectItemName = selectItemNameSave;
		mAdapter.notifyDataSetInvalidated();

		listViewFlush(); // 刷新显示
	}
	
	// 系统配置参数读取
	void ReadSharedPreferences()
	{
		SharedPreferences user = getSharedPreferences("user_info_movie",
				Activity.MODE_PRIVATE);
	
		currentListItemSave = user.getInt("currentListItemSave", -1);
		selectItemNameSave = user.getString("selectItemNameSave", "");
		clickItemNameSave = user.getString("clickItemNameSave", "");
	}

	// 系统配置参数保存
	void WriteSharedPreferences()
	{
		SharedPreferences user = getSharedPreferences("user_info_movie",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = user.edit();
	
		// 获取参数
		currentListItemSave = currentListItem;
		selectItemNameSave = mAdapter.selectItemName;
		clickItemNameSave = mAdapter.clickItemName;
		// 保存
		editor.putInt("currentListItemSave", currentListItem);
		editor.putString("selectItemNameSave", selectItemNameSave);
		editor.putString("clickItemNameSave", clickItemNameSave);

		editor.commit();
	}

	// Acitity的Destroy事件
	@Override
	protected void onDestroy()
	{
		//弹出窗口处理
		if(controlWndow.isShowing())
		{
			controlWndow.dismiss();
		}
		
		//移除消息
		mHandler.removeMessages(MSG_PLAYTIME);
		mHandler.removeMessages(HIDE_CONTROLER);
		
		// 关闭music播放，时间显示线程
		movieThread1.shouldRun = false;
		while (ThreadRunFlag == true);

		// if( movieIsPlay == true )
		if (movieMediaPlayer.isPlaying())
		{ // 若正在播放
			movieMediaPlayer.stop(); // 停止
		}
		
		movieMediaPlayer.reset(); // 复位
		movieMediaPlayer.release(); // 释放占用设备		

		// 关闭数据库
		Log.d(TAG, "正在关闭数据库...");
		myMovieListDb.closeDb(); // 关闭数据库
	
		saveMusicObject(); // 保存一些系统对象
		WriteSharedPreferences(); // 保存系统配置参数
		System.gc(); // 明确释放内存

		super.onDestroy();
		putFuncationName("onDestroy");
	}

	// 保存系统一些对象
	private void saveMusicObject()
	{
		// *********************************************************
		// 1保存MovieList Name管理器对象
		try
		{
			ObjectOutputStream oos1 = new ObjectOutputStream(
					new FileOutputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListNames.db"));

			oos1.writeObject(listNamesSave);
			oos1.close();

		} catch (FileNotFoundException e)
		{
			Log.e(TAG, e.toString());
		} catch (IOException e)
		{
			Log.e(TAG, e.toString());
		}

		// *********************************************************
		// 2保存MovieList Time管理器对象
		try
		{
			ObjectOutputStream oos1 = new ObjectOutputStream(
					new FileOutputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListTimes.db"));

			oos1.writeObject(listTimesSave);
			oos1.close();

		} catch (FileNotFoundException e)
		{
			Log.e(TAG, e.toString());
		} catch (IOException e)
		{
			Log.e(TAG, e.toString());
		}		
	}

	// 重写onActivityResult,用于添加视频文件时返回数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// super.onActivityResult(requestCode, resultCode, data);
		if (FILE_RESULT_CODE == requestCode)
		{
			Bundle bundle = null;

			// 返回结果及返回类型变量声明
			List<String> returnSelectItems = new ArrayList<String>();
			List<String> returnFileItems = new ArrayList<String>();
			String resturnStype;

			if (data != null && (bundle = data.getExtras()) != null)
			{
				returnSelectItems = bundle.getStringArrayList(RESULT_ITEM);
				resturnStype = bundle.getString(RESULT_STYPE);
				int dataSize = returnSelectItems.size();

				if (dataSize > 0)
				{
					Log.d(TAG, "文件(夹)添加(结果如下): ");
					// 输入添加的文件或文件夹
					for (int i = 0; i < dataSize; i++)
					{
						Log.d(TAG, "[" + i + "]=" + returnSelectItems.get(i));
					}

					if (resturnStype.equals(DISPLAY_FILES))
					{
						// 若为文件添加，无须变换
						returnFileItems = returnSelectItems;
					} else if (resturnStype.equals(DISPLAY_FOLDERS))
					{
						// 若为文件夹添加，则须对文件夹进行扫描出文件
						// 传入文件夹列表，返回文件列表,若文件列表不大于0则返回NULL
						returnFileItems = fileScanFromFolders(returnSelectItems);

						if (returnFileItems == null)
						{
							Log.d(TAG, "文件(夹)扫描结果为空！");
							return;
						} else
						{
							Log.d(TAG, "文件(夹)扫描结果如下：");
							dataSize = returnFileItems.size();
							for (int j = 0; j < dataSize; j++)
							{
								Log.d(TAG, "[" + j + "]="
										+ returnFileItems.get(j));
							}
						}
					}

					// 调用文件(夹)添加函数
					try
					{
						doAddMovieFile(returnFileItems);
					} catch (Throwable e)
					{
						Log.d(TAG, "调用文件(夹)添加函数: 失败！");
						Log.d(TAG, e.toString());
					}
				} else
				{
					Log.d(TAG, "当前没有要扫描的文件夹或添加的文件！");
				}

			}
		}
	}
	
	
	//手势事件倾听
	private void gestureDetectorListener()
	{
		//手势识别
		mGestureDetector = new GestureDetector(new SimpleOnGestureListener()
		{
			@Override
			public boolean onDoubleTap(MotionEvent e)
			{				
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e)
			{
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e)
			{

			}	
        });
	}

	//显示列表事件设置
	private void movieListViewSetListener()
	{
		// 视频列表播放事件触发
		movieListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			// 当选择视频时，进行视频播放
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				currentListItem = position;
				mAdapter.selectItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();

				String currentMovieName = mAdapter.selectItemName;	
				Log.i(TAG, "currentMovieName=" + currentMovieName);
//				if(controlView != null )	//控件窗口不为空，则显示
//				{
//					if( true )	//在AVD仿真器中显示
//						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
//					else		//在真实开发板中显示(7寸显示屏)
//						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
//			     
//					putFuncationName("queueIdle");
//		        	isControllerShow = true;
//				}
//				// 调用视频播放处理程序
				animation1();
				showController();
				surfaceView.setVisibility(View.VISIBLE);
				playMovie(currentMovieName);
				return true; // 返回true Click不会被调用
			}
		});

		//单击事件显示
		movieListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// 单击显示更新
				mAdapter.clickItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();
			}
		});
	}
	
	//视频1显示回调函数
	private final class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder) 
		{
			movieThread1.shouldContinue = false;	//进度条显示处理
			
			if( surfaceViewSavaFlag == true && isFullScreen == false)
			{
				if ((moviePosition > 0) && (mAdapter.selectItemName != null))			
				{
					//Log.i(TAG, "moviePosition = " + moviePosition);			
					try
					{
						movieMediaPlayer.reset();//重置为初始状态
						
						/* 设置Video影片以SurfaceHolder播放 */
						movieMediaPlayer.setDisplay(surfaceView.getHolder());
						movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						movieMediaPlayer.setDataSource(currentListPath);
						movieMediaPlayer.prepare();//缓冲	
						movieMediaPlayer.start();//播放
						movieMediaPlayer.seekTo(moviePosition);				
						if( mediaPlayerIsPause )
						{
							movieMediaPlayer.pause();							
						}
	
					} catch (Exception e)
					{
						Log.e(TAG, e.toString());
					}				
				}
				
				surfaceViewSavaFlag = false;
			}
			
			//从全屏返回
			if( isFullBack == true && isFullScreen == true )
			{									
				try
				{	
					movieMediaPlayer.reset();
					movieMediaPlayer.setDisplay(surfaceView.getHolder());
					movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					
					if( movieIsPlay || mediaPlayerIsPause )	//若在显示或暂停，则返回时保持
					{
						movieMediaPlayer.setDataSource(currentListPath);
						movieMediaPlayer.prepare();			
						movieMediaPlayer.start();
						movieMediaPlayer.seekTo(changeToFullPosition);
						if( mediaPlayerIsPause )
						{
							movieMediaPlayer.pause();							
						}
					}else {
						//暂停
					}
					
					showController();				
					isFullBack = false;
					isFullScreen = false;
					fullScreenChange.setEnabled(true);
					Log.d(TAG,"全屏返回成功...");
					movieThread1.shouldContinue = true;	//进度条显示处理
				} 
				 catch (IOException e)
				{					
					e.printStackTrace();
					Log.e(TAG,"全屏返回失败!!!");
				}
			}
			
			movieThread1.shouldContinue = true;	//进度条显示处理	
			putFuncationName("surfaceCreated");
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height)
		{	
			putFuncationName("surfaceChanged");
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{	
			putFuncationName("surfaceDestroyed");
		}    	
    }
	
	//视频2显示回调函数
	private final class SurfaceCallbackFull implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder) 
		{
			if( surfaceViewSavaFlag == true )
			{
				if ((moviePosition > 0) && (mAdapter.selectItemName != null))			
				{
					try
					{
						movieMediaPlayer.reset();//重置为初始状态
						
						/* 设置Video影片以SurfaceHolder播放 */
						movieMediaPlayer.setDisplay(surfaceViewFull.getHolder());
						movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						movieMediaPlayer.setDataSource(currentListPath);
						movieMediaPlayer.prepare();//缓冲	
						movieMediaPlayer.start();//播放
						movieMediaPlayer.seekTo(moviePosition);				
						if( mediaPlayerIsPause )
						{
							movieMediaPlayer.pause();							
						}
	
					} catch (Exception e)
					{
						Log.e(TAG, e.toString());
					}				
				}
				
				surfaceViewSavaFlag = false;
			}
			else if( isFullScreen == true )
			{	
				//全屏切换
				try
				{
					movieMediaPlayer.reset();
					movieMediaPlayer.setDisplay(surfaceViewFull.getHolder());
					movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					movieMediaPlayer.setDataSource(currentListPath);
					movieMediaPlayer.prepare();			
					movieMediaPlayer.start();
					movieMediaPlayer.seekTo(changeToFullPosition);				
					
					hideController();
					
					Log.d(TAG, "全屏切换成功...");
					fullScreenChange.setEnabled(true);
					
				} 
				 catch (IOException e)
				{					
					e.printStackTrace();
					Log.d(TAG,"全屏切换失败!!!");
				}
			}
			
			putFuncationName("surfacefullCreated");
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height)
		{	
			putFuncationName("surfacefullChanged");
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{	
			putFuncationName("surfacefullDestroyed");
		}    	
    }	
		
	// 声音控制改变事件
	private OnSeekBarChangeListener voiceChangeListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{

		}

		// 数值变化事件
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser)
		{
			seekbarNum = seekBarVoice.getProgress();

			// 直接改变
			if (voiceOpen == true)
			{
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						seekbarNum, AudioManager.FLAG_PLAY_SOUND);
			} else
			{
				// 静音模式
				soundSaveVolume = seekbarNum; // 只保存不设置
			}

			soundVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (soundVolume == 0)
			{
				// 静音
				btnVoice.setImageResource(R.drawable.movievoiceclose);
			} else
			{
				// 非静音
				btnVoice.setImageResource(R.drawable.movievoiceopen);
			}
		}
	};
	
	// 多个按钮单击倾听事件
	private OnClickListener movieClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				switch (v.getId())
				{
				
				//全屏切换
				case R.id.imageViewFullChange:
				{
					changeToFullScreen();
				} break;
				
				case R.id.ImageViewReset: // 复位
				{
					movieMediaPlayer.stop(); // 停止
					movieIsPlay = false;
					seekBarPlayProcess.setProgress(0);
					if (seekBarPlayProcess.isEnabled())
					{
						seekBarPlayProcess.setEnabled(false); // 停止时禁止拖动进度条
					}
					playTime.setText("00:00:00");
					textMovieState.setText("停止");
					btnPlayStop.setImageResource(R.drawable.musicplaystart);
				}
					break;
					
				case R.id.imageViewPlayStop: // 播放或停止
				{
					if (movieMediaPlayer.isPlaying())
					{
						// 由"播放"到"暂停",更换为暂停图标
						movieMediaPlayer.pause();
						textMovieState.setText("暂停");
						mediaPlayerIsPause = true;
						btnPlayStop.setImageResource(R.drawable.musicplaystart);
					} else
					{
						if (listNamesSave.size() > 0)
						{
							if (movieIsPlay == false)
							{
								// 初次，要初始化数据源
								// 由"暂停"到"播放",更换为播放图标
								if (currentListItem == -1)
									currentListItem = 0; // 针对第一次启动

								String playName = listNamesSave.get(currentListItem);										
								
								if (playName != null)
								{
									playMovie(playName);
									currentMovieChange();
								}
							} else
							{
								movieMediaPlayer.start();						
								textMovieState.setText("播放中");
							}

							mediaPlayerIsPause = false;
							btnPlayStop.setImageResource(R.drawable.musicplaypause);
						}
					}

					if (!btnReset.isEnabled())
					{
						btnReset.setEnabled(true);
					}
				}
					break;

				case R.id.imageButtonVoice: // 静音控制开关
				{
					if (voiceOpen == true)
					{
						// 关闭声音,静音模式
						btnVoice.setImageResource(R.drawable.movievoiceclose);
						// 保存静音值,将视频音量设为0
						soundSaveVolume = audioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC);
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								0, AudioManager.FLAG_PLAY_SOUND);
						voiceOpen = false;
					} else
					{
						// 音量条为0，点击时不生效
						if (soundSaveVolume != 0)
						{
							// 打开声音,声音模式
							btnVoice
									.setImageResource(R.drawable.movievoiceopen);
							// 恢复静音值
							audioManager.setStreamVolume(
									AudioManager.STREAM_MUSIC, soundSaveVolume,
									AudioManager.FLAG_PLAY_SOUND);
							voiceOpen = true;
						}
					}
				}
					break;			
				case R.id.entermovie:
					animation1();
					showController();
					surfaceView.setVisibility(View.VISIBLE);
					break;
				case R.id.enterlist:
					animation2();
					controlWndow.dismiss();
					surfaceView.setVisibility(View.GONE);
				default:
					break;
				}
			} catch (Exception e)
			{
				// 输出异常信息
				Log.e(TAG, e.toString());
			}
		}

	};
	
	//视频播放完成倾听事件
	private OnCompletionListener completionPlayListener = new OnCompletionListener()
	{
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			Log.d(TAG,"触发了完成事件....");
			// 当前视频播放完成事件
			switch (moviePlayType)
			{
			case 1: // "顺序播放"模式
			{
				// 则转至下一首播放
				nextMovie();
			}
				break;

			case 2: // "单场循环"模式
			{
				currentListItem--;
				nextMovie();
			}
				break;

			case 3: // "列表循环"模式
			{
				nextMovie();
			}
				break;

			case 4: // "随机"模式
			{
				// 在List列表有效范围，产生一个随机视频,再进行播放
				if (listNamesSave.size() == 0)
				{
					// 没有视频
					nextMovie(); // 调用next以便停止
				} else if (listNamesSave.size() == 1)
				{
					// 只剩一首歌
					String playName = listNamesSave.get(0);
					if (playName != null)
					{
						playMovie(playName);
						currentMovieChange(); // 当前视频高亮
					}
				} else
				{
					// 两首视频以上
					int maxNumber = listNamesSave.size();
					int randomSongNumber;

					// 生成随机数(双重，防止重复率)，大小范围 0-maxNumber
					randomSongNumber = ((int) (Math.random() * maxNumber));
					do
					{
						randomSongNumber = ((int) (Math.random() * maxNumber));
					} while (randomSongNumber == currentListItem);

					Log.i(TAG, "randomSongNumber: " + randomSongNumber);
					if (randomSongNumber <= listNamesSave.size())
					{
						currentListItem = randomSongNumber;
						String playName = listNamesSave.get(currentListItem);
						if (playName != null)
						{
							playMovie(playName);
							currentMovieChange(); // 当前视频高亮
						}
					}
				}
			}
				break;

			default:
				break;

			}
		}
		
	};
	
	//视频播放进度条事件倾听
	private OnSeekBarChangeListener seekbarPlayListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			int position = seekBar.getProgress();
			movieMediaPlayer.seekTo(position);		
			movieThread1.shouldContinue = true;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			movieThread1.shouldContinue = false;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar,
				int progress, boolean fromUser)
		{
			if( fromUser == true )
			{
				// 判断是否在拖动，拖动时才更新
				// 防止平时两个更新
				if (movieThread1.shouldContinue == false)
				{
					// 动态显示当前拖动时间
					playTime.setText(toTime(progress));
				}
			}
		}
	};
	
	// 多个按钮单击倾听事件
	private OnClickListener movieChoiceListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				//1添加视频文件选项
				case R.id.movieBtnAddFiles1:
				{					
					showDialog(listAddStype); // 显示多选按钮对话框
				}					
					break;
				
				//2删除视频文件选项
				case R.id.movieBtnDelFiles1:
				{
					showDialog(listDelStype); // 显示多选按钮对话框
				}					
					break;
				
				//3视频模式播放选项
				case R.id.movieBtnPlayStype1:
				{
					showDialog(listPlayStype); // 显示多选按钮对话框
				}					
					break;
				
				//4窗口模式选项	
				case R.id.movieBtnViewStype1:
				{					
					showDialog(listViewStype); // 显示多选按钮对话框
				}					
					break;
	
				default:
					break;					
			}
			
			
		}		
	};
	
	
	// 重写onCreateDialog方法
	@Override
	protected Dialog onCreateDialog(final int id)
	{ 		
		Dialog dialog = null;
		String[] disItems = null;
		int width = 300;
		
		switch (id)
		{ 
			// 对id进行判断			
			case listAddStype:
			{
				disItems = multiSelectitems1;
			} break;
			
			case listDelStype:
			{
				disItems = multiSelectitems2;				
			} break;
			
			case listPlayStype:
			{
				disItems = multiSelectitems3;
				width = 400;
			} break;
			
			case listViewStype:
			{
				disItems = multiSelectitems4;
			} break;
			
			default: break;
		}
		
		if( disItems !=null )
		{
			Builder b = new AlertDialog.Builder(this); 	// 创建Builder对象
			b.setItems(disItems,  new DialogInterface.OnClickListener()
			{					
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Log.d(TAG,"你当前选择的是: "+ which);
					//displayTip("你当前选择的是: "+ which);
					
					switch (id)
					{
						//添加文件
						case listAddStype:
						{
							switch (which)
							{
								case 0:		//自动扫描
								{
									try
									{
										scanFilesFromExt();
									} catch (Throwable e)
									{										
										Log.e(TAG,"自动扫描添加文件失败");
										Log.e(TAG,e.toString());
									}
								} break;
								
								case 1:		//添加目录
								{
									doAddFilesView(DISPLAY_FOLDERS);
								} break;
								
								case 2:		//添加文件
								{
									doAddFilesView(DISPLAY_FILES);
								} break;
	
								default:
									break;
							}
						} break;
						
						//删除文件
						case listDelStype:
						{
							switch (which)
							{
								case 0:		//清空当前列表
								{
									doDelAllMovieFile(); 									
								} break;
								
								case 1:		//删除选中文件
								{
									doDelMusicFile();	
								} break;
	
								default:
									break;
							}
						} break;
						
						//播放模式
						case listPlayStype:
						{
							switch (which)
							{
								case 2: //单曲循环
									moviePlayType = 2;
									textMoviePlayStype.setText("单部循环");
									break;
	
								case 1: //列表循环
									moviePlayType = 3;
									textMoviePlayStype.setText("列表循环");
									break;
	
								case 0: //随机播放
									moviePlayType = 4;
									textMoviePlayStype.setText("随机播放");
									break;
	
								case 3: //顺序播放
									moviePlayType = 1; // 复位
									textMoviePlayStype.setText("顺序播放");
									break;
	
								default:
									break;
							}
						} break;
						
						//窗口设置
						case listViewStype:
						{
							switch (which)
							{
								case 0:		//退出关闭
								{
									doExitWork();
								} break;
								
								case 1:		//返回主页
								{
									returnHome();
								} break;
								
								case 2:		//全屏播放
								{									
									changeToFullScreen();
								} break;
	
								default:
									break;
							}
						} break;
						
						default: break;
					}
				}

				
				
			});
	
			//创建对弹出选项，并设置其显示宽高、坐标
			dialog = b.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			//dialog.getWindow().setLayout(200, 300);
			
//			if( false )	//在AVD中
//				dialog.getWindow().setLayout(200, width);
//			else 		//在开发板中
//				dialog.getWindow().setLayout(200, 450);
//			
//			LayoutParams a = dialog.getWindow().getAttributes();
//			
//			if( false )	//在AVD中
//			{
//				a.x = 135;
//				a.y = -200;
//			}else {		//在开发板中
//				a.x = 250;
//				a.y = -200;
//			}
//			dialog.getWindow().setAttributes(a);
			
		}
		
		return dialog; // 返回Dialog方法
	}
	
	//切换至全屏显示
	private void changeToFullScreen()
	{	
		//全屏显示
		if( (isFullScreen == false) && movieMediaPlayer.isPlaying() )
		{
			fullScreenChange.setEnabled(false);
			
			//停止保存断点，等待切换
			changeToFullPosition = movieMediaPlayer.getCurrentPosition();
			movieMediaPlayer.stop();	//停止
			
			setContentView(fullViewSave);
			
			if( surfaceViewFull == null )
			{
				surfaceViewFull = (SurfaceView)findViewById(R.id.surfaceViewFull);
				
				//设置单击事件
				surfaceViewFull.setOnClickListener( new OnClickListener()
				{				
					@Override
					public void onClick(View v)
					{				
						if( isControllerShow )
						{
							hideController();						
							isControllerShow = false;
						}
						else{
							showController();
							isControllerShow = true;
							hideControllerDelay();
						}
					}
				});
				
				//配置				
				surfaceViewFull.getHolder().setFixedSize(800,480);//设置分辨率
			    surfaceViewFull.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		        surfaceViewFull.getHolder().addCallback(new SurfaceCallbackFull());
			} 
			
			else 
			{
				Log.d(TAG, "surfaceViewFull已存在! 不要重复创建!");
			}
		  	Log.d(TAG,"全屏显示...");
		  	fullScreenChange.setImageResource(R.drawable.unfullscreennormal);
		  	isFullScreen = true;
		  	
		}else if( isFullScreen == true )
		{
			fullScreenChange.setEnabled(false);
			
			//停止保存断点，等待切换
			changeToFullPosition = movieMediaPlayer.getCurrentPosition();
			movieMediaPlayer.stop();	//停止
		
			setContentView(normalViewSave);
		  	Log.d(TAG,"标准显示...");
		  	fullScreenChange.setImageResource(R.drawable.fullscreennormal);
		  	isFullBack = true;
		}
	}
	
	//延时关闭
	private void hideControllerDelay()
	{
		mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	//显示控制栏
	private void showController()
	{	
		if( true )	//在AVD仿真器中显示
			controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
		else		//在真实开发板中显示(7寸显示屏)
			controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
       
       isControllerShow = true;
	}
	
	//延时隐藏控制栏
	private void cancelDelayHide()
	{
		mHandler.removeMessages(HIDE_CONTROLER);
	}
	
	//隐藏控制栏
	private void hideController()
	{	
		controlWndow.dismiss();			
		isControllerShow = false;
	}

	// 刷新listView当前的显示
	private void listViewFlush()
	{
		int positionView = listNamesSave.indexOf(mAdapter.selectItemName);

		if (positionView >= 0)
		{
			// 获取当前显示最前、最后一个项数
			int lastPosition = movieListView.getLastVisiblePosition();
			int firstPositon = movieListView.getFirstVisiblePosition();

			// 判断是否在视图内
			if ((positionView > lastPosition) || (positionView < firstPositon))
			{
				// 不是，则将选项置为当前视图
				movieListView.setSelection(positionView);
			}
		}
	}		

	// 播放视频
	void playMovie(String movieName)
	{
		if (movieName == null)
			return; // 防止视频名称为空

		// 根据视频名称，从数据库中提取数据
		myMovie movieTemp = new myMovie();
		try
		{
			movieTemp = myMovieListDb.find(movieName);
			if (movieTemp == null)
			{
				Log.d(TAG, "在数据库中无法查找该视频：" + movieName + "!");
				Log.d(TAG, "无法定位该视频文件...");
				return;
			}
		} catch (Throwable e)
		{
			Log.d(TAG, e.toString());
			Log.d(TAG, "在数据库中查找视频 -" + movieName + ": 异常！");			
			return;
		}
		
		// 2准备视频视频数据
		try
		{
			currentListPath = movieTemp.path;
			
			movieMediaPlayer.reset();
			//非全屏模式
			if( isFullScreen == false )
			{	
				movieMediaPlayer.setDisplay(surfaceView.getHolder());
			}else 
			{
				//全屏模式
				movieMediaPlayer.setDisplay(surfaceViewFull.getHolder());
			}
			movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			movieMediaPlayer.setDataSource(movieTemp.path);
			movieMediaPlayer.prepare();			
			movieMediaPlayer.start();
			
			// 开启复位
			if (!btnReset.isEnabled())	btnReset.setEnabled(true);			
			
		} catch (IOException e)
		{			
			Log.d(TAG,"播放视频文件：" + movieName + "出错!");
			Log.d(TAG,e.toString());
		}

		// 由“播放”切换到“停止”按钮
		textMovieState.setText("播放中");
		btnPlayStop.setImageResource(R.drawable.musicplaypause);
		movieIsPlay = true;
		mediaPlayerIsPause = false;
		
		if (!seekBarPlayProcess.isEnabled())
		{
			seekBarPlayProcess.setEnabled(true); // 播放时使能拖动进度条
		}

		// 更新进度条
		int currentMoviePosition = movieMediaPlayer.getDuration();
		seekBarPlayProcess.setMax(currentMoviePosition);	//bar最小单位为ms			
		durationTime.setText( "/ " + toTime(currentMoviePosition) );		
	}

	// 下一个视频
	void nextMovie()
	{
		// 判断视频列表是否已为空
		if (listNamesSave.size() == 0)
		{
			doNoMovieWork();
			return;
		}

		String playName;

		if (++currentListItem >= listNamesSave.size())
		{
			currentListItem = 0;

			// 若为"列表循环"模式,或者在播放最后一首时，点击下一首,则继续播放
			if (moviePlayType == 3 || nextPlayFlag == true)
			{
				nextPlayFlag = false;
				playName = listNamesSave.get(currentListItem);
				if (playName != null)
				{
					playMovie(playName);
				}

			} else
			{
				doNoMovieWork();
				if (moviePlayType == 1)
				{
					// 若为顺序播放，在只有一首歌时，取消最后一首高选
					mAdapter.selectItemName = "";
					mAdapter.notifyDataSetInvalidated();
				}
			}

		} else
		{
			playName = listNamesSave.get(currentListItem);
			if (playName != null)
			{
				playMovie(playName);
			}
		}

		currentMovieChange(); // 当前视频高亮
	}

	// 根据当前视频改变，高亮
	private void currentMovieChange()
	{
		// 视频根据"currentListItem"列表刷新,高亮
		if (currentListItem < listNamesSave.size() && currentListItem >= 0)
		{
			mAdapter.selectItemName = listNamesSave.get(currentListItem);
			mAdapter.notifyDataSetInvalidated();

			listViewFlush(); // 刷新显示
		}
	}	

	// 从文件夹列表中，查找文件，并返回文件列表
	private List<String> fileScanFromFolders(List<String> Folders)
	{
		int i;
		List<String> movieTempList = new ArrayList<String>();

		for (i = 0; i < Folders.size(); i++)
		{
			String dirStr = Folders.get(i); // 依次获取每个子目录
			if (dirStr != null)
			{
				File subDir = new File(dirStr);
				Log.d(TAG, "下面要扫描的文件夹为：" + subDir.getAbsolutePath());

				// 检查系统关键目录，不应扫描,还有很多，后面处理！！
				// 最好要明确哪些目录可以扫描的！
				if (dirStr.equals("/mnt/secure") || dirStr.equals("/root")
						|| dirStr.equals("/data"))

				{
					Log.d(TAG, "系统关键目录，略过扫描！");
					continue;
				}

				if (subDir.listFiles(new MovieFilter()).length > 0)
				{
					for (File file : subDir.listFiles(new MovieFilter()))
					{
						movieTempList.add(file.getName());
					}
				}
			}
		}

		// 若有文件则返回列表，否则返回NULL
		if (movieTempList.size() > 0)
			return movieTempList;
		else
			return null;
	}

	// 从外部存储器SD卡中扫描视频文件
	void scanFilesFromExt() throws Throwable
	{
		Cursor cursorTemp;

		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		// 获取外部存储器中所有视频文件
		cursorTemp = this.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				// 这个字符串数组表示要查询的列
				new String[]
				{ 
						MediaStore.Video.Media.DISPLAY_NAME,// 0
						// 视频文件名
						MediaStore.Video.Media.TITLE, // 1						
						// 视频的名称
						MediaStore.Audio.Media.DATA, // 2
						// 视频文件的路径
						MediaStore.Audio.Media.DURATION, // 3
						// 视频的总时间

				}, null, // 查询条件，相当于sql中的where语句
				null, // 查询条件中使用到的数据
				null // 查询结果的排序方式
				);

		// 将查找到的视频数据，添加到数据库中
		if (cursorTemp != null)
		{
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr;

			// 从Cursor中分离出数据送至ListView中显示
			for (int i = 0; i < cursorTemp.getCount(); i++)
			{
				cursorTemp.moveToPosition(i);

				// 先读取视频文件名
				nameStr = cursorTemp.getString(0);

				// 进行视频文件过滤
				String movieFilter = nameStr.substring(nameStr.length() - 3);
				if( movieFilter.equals("3gp") || movieFilter.equals("mp4") 
						|| movieFilter.equals(".rm")|| movieFilter.equals("mvb")
						|| movieFilter.equals("avi") || movieFilter.equals("wmv") )
				{

					
				}					
				else {
					Log.d(TAG, "系统对该文件: " + nameStr + "暂不支持!");
					continue;
				}			
				Log.d(TAG, "视频扫描文件: " + nameStr );

				// 查看数据库是否存在
				myMovie movie1 = new myMovie();
				movie1 = myMovieListDb.find(nameStr);
				if (movie1 == null)
				{
					// 若不存在，则添加
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					
					myMovie moive2 = new myMovie(); // 重新NEW一个对象，因为前面为NULL赋值

					//更新数据库
					moive2.Set(nameStr, titleStr, pathStr, timeStr );
					myMovieListDb.save(moive2);

				} else
				{
					// 无须更新数据库数据

					// 检查当前视频播放列表是否存在，不存在，则在尾部添加
					if (listNamesSave.contains(nameStr) == false)
					{
						addCount++;
						timeStr = movie1.time; // 从数据库中获取时间

						// 更新视频列表
						listNamesSave.add(nameStr);
						listTimesSave.add(timeStr);						

					} else
					{
						Log.d(TAG, "该文件，存在于当前播放列表，无须添加!");
					}
				}
			}
			cursorTemp.moveToFirst(); // 遍历完后复位
			cursorTemp.close(); // 关闭，释放资源

			// 返回用户添加结果
			if (addCount > 0)
			{
				// 新建适配器，更新显示
				mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
				movieListView.setAdapter(mAdapter);

				// 保持之前的显示
				mAdapter.clickItemName = clickItemNameSave;
				mAdapter.selectItemName = selectItemNameSave;
				// mAdapter.notifyDataSetInvalidated();
				mAdapter.notifyDataSetChanged();

				displayTip("成功添加" + addCount + "个的新视频文件...");
			} else
			{
				displayTip("没有要添加的视频...");
			}
		} else
		{
			Log.d(TAG, "无法获取Android内部的视频数据库");
		}
		
	}
	
	//添加文件
	private void doAddFilesView( String viewStype )
	{		 
		Intent intent = new Intent(SmartCarMovie.this,
				MyFileManager.class);
		// New一个Bundle对象，并将要传递的数据传入,告诉文件浏览，要求显示是音频文件
		Bundle bundle = new Bundle();
		bundle.putString(REQUEST_STYPE, viewStype);
		bundle.putString(FILTER_STYPE,DISPLAY_AUDIO);
		intent.putExtras(bundle);

		// 有返回结果
		startActivityForResult(intent, FILE_RESULT_CODE);
	}

	// 添加视频文件(含文件夹)
	void doAddMovieFile(List<String> dataItem) throws Throwable
	{
		int i, size, addCount = 0, tempInt;
		String addFileName, addFilePath;
		String nameStr,titleStr, pathStr, timeStr;

		MediaPlayer tempPlayer = new MediaPlayer(); // 用于获取视频播放长度
		myMovie movieTemp = new myMovie();

		// 获取旧的选项和单击项
		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		size = dataItem.size(); // 获取添加项大小

		Log.d(TAG, "以下为要添加的文件：");

		// 依次检查每一项文件
		for (i = 0; i < size; i++)
		{
			addFilePath = dataItem.get(i); // 先得到的是完全的名称，如/mnt/sdcard/x.3gp

			// 进行文件名全称中分离文件名
			File fileTemp = new File(addFilePath);
			addFileName = fileTemp.getName();
			Log.d(TAG, "");
			Log.d(TAG, "[" + i + "]=" + addFileName);

			// 1先从数据库中查找是否存在
			myMovie movie1 = new myMovie();
			movie1 = myMovieListDb.find(addFileName);
			Log.d(TAG, "1.在数据库中查找: ");

			if (movie1 != null)
			{
				Log.d(TAG, "  成功...");
				Log.d(TAG, "2.无须更新后台数据库...");

				// 直接在数据库中取参数
				nameStr = addFileName; // 1文件名
				timeStr = movie1.time;
			} else
			{
				// 若数据库中不存在，则对该视频数据进行分析，添加到数据库中
				Log.d(TAG, "  失败...");
				Log.d(TAG, "2.正在对该视频文件进行解析...");
				
				nameStr = addFileName;
				titleStr = "某知";
				pathStr = addFilePath;	//完整路径名

				// 获取视频的长度
				tempPlayer.reset();
				tempPlayer.setDataSource(addFilePath);
				tempPlayer.prepare();
				// tempPlayer.stop();

				tempInt = tempPlayer.getDuration();
				timeStr = toTime(tempInt);
				// Log.d(TAG,"解码获得其播放时间为：" + timeStr);

				// 构造一个movie数据项
				movieTemp.Set(nameStr, titleStr, pathStr, timeStr );
				Log.d(TAG, "music: " + movieTemp.toString());

				Log.d(TAG, "3.正在添加入数据库...");
				if (myMovieListDb.save(movieTemp))
				{
					Log.d(TAG, "  成功！");
				} else
				{
					Log.d(TAG, "  失败！");
				}
			}

			// 检查当前视频播放列表是否存在，不存在，则在尾部添加
			if (listNamesSave.contains(addFileName) == false)
			{
				addCount++;

				// 更新视频列表
				listNamesSave.add(addFileName);
				listTimesSave.add(timeStr);

			} else
			{
				Log.d(TAG, "该文件，存在于当前播放列表，无须添加!");
			}
		}

		tempPlayer.release(); // 释放硬件

		// 返回用户添加结果
		if (addCount > 0)
		{
			// 新建适配器，更新显示
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			// 保持之前的显示
			mAdapter.clickItemName = clickItemNameSave;
			mAdapter.selectItemName = selectItemNameSave;
			// mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();

			displayTip("成功添加" + addCount + "个新视频文件...");
		} else
		{
			displayTip("没有要添加的新视频文件...");
		}

	}

	// 调用删除所有视频列表函数
	private void doDelAllMovieFile()
	{
		if (listNamesSave.size() > 0)
		{
			listNamesSave.clear(); // 清空列表			
			listTimesSave.clear();

			// currentListItem = -1;
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			mAdapter.selectItemName = "";
			mAdapter.clickItemName = "";
			mAdapter.notifyDataSetInvalidated();
		} else
		{
			displayTip("当前列表为空!无须清空!");
		}
	}

	// 删除某一项视频文件
	void doDelMusicFile()
	{
		String musicNamePlay = "", delFileName, musicNameNext = "";

		if (mAdapter.clickItemName.length() > 0)
		{
			// 获取要删除的视频文件
			delFileName = mAdapter.clickItemName; 
			Log.d(TAG, "当前要删除的文件名称为：" + delFileName);

			// 保存当前播放项
			musicNamePlay = mAdapter.selectItemName;
			if (musicNamePlay.equals(delFileName))
			{
				Log.d(TAG, "当前删除视频为当前选择视频!");
				musicNamePlay = "";
			}

			// 根据名称，查找相应的位置
			int location = listNamesSave.indexOf(delFileName);
			Log.d(TAG, "索引位置为：" + location);
			if (location >= 0)
			{
				// 查找到后删除相应记录
				listNamesSave.remove(location);
				listTimesSave.remove(location);

				// 判断当前删除的是否为当前播放
				if (mAdapter.clickItemName.equals(mAdapter.selectItemName))
				{
					// 将当前播放视频删除
					mAdapter.selectItemName = ""; // 取消视频高亮
				}

				// 将单击选项移至下一首，要判断是否是最后一首
				if (listNamesSave.size() > 0)
				{
					// 当前删除是最后一首
					if (location == listNamesSave.size())
					{
						// 移至第一首
						musicNameNext = listNamesSave.get(0);

					} else if (location < listNamesSave.size())
					{
						// 移至下一首
						musicNameNext = listNamesSave.get(location);
					}

				} else
				{
					musicNameNext = "";
				}

			} else
			{
				musicNameNext = ""; // 查找不到，置空处理
			}

			// 新建适配器，更新显示
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			// 刷新或保持显示
			// 当前视频删除后,单击选项指向下一首
			mAdapter.clickItemName = musicNameNext;
			mAdapter.selectItemName = musicNamePlay;
			mAdapter.notifyDataSetInvalidated();
			// mAdapter.notifyDataSetChanged();

			// 将当前视图转到下一首单击的视频
			int positionView = listNamesSave.indexOf(mAdapter.clickItemName);
			if (positionView >= 0)
			{
				// 获取当前显示最前、最后一个项数
				int lastPosition = movieListView.getLastVisiblePosition();
				int firstPositon = movieListView.getFirstVisiblePosition();

				// 判断是否在视图内
				if ((positionView > lastPosition)
						|| (positionView < firstPositon))
				{
					// 不是，则将选项置为当前视图
					movieListView.setSelection(positionView);
				}
			}

		} else
		{
			displayTip("当前没有选中的文件...");
		}
	}
	
	// 执行没有视频播放工作
	private void doNoMovieWork()
	{
		// 判断是否仍在播放
		if (movieMediaPlayer.isPlaying())
		{
			movieMediaPlayer.stop();
		}

		seekBarPlayProcess.setProgress(0);
		if (seekBarPlayProcess.isEnabled())
		{
			seekBarPlayProcess.setEnabled(false); // 停止时禁止拖动进度条
		}
		
		playTime.setText("");		 // 播放时间和总时间显示刷新
		durationTime.setText("");
		textMovieState.setText("停止");
		btnPlayStop.setImageResource(R.drawable.musicplaystart);
		movieIsPlay = false;
		currentListItem = -1;
	}

	// 时间字符化
	public String toTime(int time)
	{
		//time-->ms
		
		time /= 1000;				//time-->秒
		int minute = time / 60;		//tims/60-->分
		int hour = minute / 60;		//小时
		int second = time % 60;		//秒
		
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	// 显示提示信息
	private void displayTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// 输出当前函数名称
	private void putFuncationName(String name)
	{
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}

	// 进度条处理
	class movieMapToSeekBar extends Thread
	{
		int position;
		boolean shouldContinue;
		boolean shouldRun = true;

		public movieMapToSeekBar()
		{
			shouldContinue = true;
		}

		@Override
		public void run()
		{
			// super.run();
			while (shouldRun)
			{
				if (movieMediaPlayer.isPlaying() && shouldContinue)
				{				
					songPlayTimeUpdate(); // 更新进度条
				}

				try
				{
					ThreadRunFlag = true;
					Thread.sleep(1000);
					//Thread.sleep(500);
				} catch (InterruptedException e)
				{
					Log.e(TAG, e.toString());
				}
			}

			ThreadRunFlag = false; // 线程结束标识
		}
	}

	// 视频播放时间更新
	private void songPlayTimeUpdate()
	{		
		Message m = new Message();
		m.what = MSG_PLAYTIME;						
		SmartCarMovie.this.mHandler.sendMessage(m);		
	}
	
	// 返回方法
	private void returnHome()
	{
		// 调用"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarMovie.this, SmartCarSystem.class);
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		
		putFuncationName("returnHome");
	}

	// 执行程序退出工作
	private void doExitWork()
	{
		SmartCarMovie.this.finish();
		Log.d(TAG, "doExitWork()");
	}
	protected void animation2() {
		// TODO Auto-generated method stub
		vfmovie.setInAnimation(in_lefttoright);
		vfmovie.setOutAnimation(out_lefttoright);
		vfmovie.showPrevious();
	}
	protected void animation1() {
		// TODO Auto-generated method stub
		vfmovie.setInAnimation(in_righttoleft);
		vfmovie.setOutAnimation(out_righttoleft);
		vfmovie.showNext();
	}
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO Auto-generated method stub
//		if(event.getAction()==MotionEvent.ACTION_DOWN)
//		{ startX=event.getX();}
//		else  if(event.getAction()==MotionEvent.ACTION_UP)
//		      {
//			         endX=event.getX();
//		                if(endX<startX) {animation1();showController();controlWndow.dismiss();surfaceView.setVisibility(View.VISIBLE);}
//		             else if(endX>startX) {animation2();controlWndow.dismiss();surfaceView.setVisibility(View.GONE);}
//		     
//              }		           		           
//		return super.onTouchEvent(event);
//	}

}
