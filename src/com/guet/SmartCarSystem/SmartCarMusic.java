package com.guet.SmartCarSystem;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.guet.SmartCarMusic.LrcParser;
import com.guet.SmartCarMusic.MusicFilter;
import com.guet.SmartCarMusic.musicListAdapter;
import com.guet.music.mp3info.MusicInfoServer;
import com.guet.music.service.Music;
import com.guet.music.service.MusicServiceApp;

//车载信息系统：1部分：音频播放器
public class SmartCarMusic extends Activity {
	// 系统启动参数
	private static String SDMUSIC_HAVE_READ = "F";

	private MediaPlayer musicMediaPlayer;
	boolean musicIsPlay = false;
	boolean voiceOpen = true;
	boolean nextPlayFlag = false;
	
	private ViewFlipper viewflipper;
	private Animation in_lefttoright;
	private Animation out_lefttoright;
	private Animation in_righttoleft;
	private Animation out_righttoleft;
	private float startX;
	private float endX;
	private static Button enterPlay;
	
	private static ImageView btnPlayStop;
	private static Button btnMusicClose; // 全部关闭
	private static Button btnMusicMin; // 最小化，返回主页
	private static ImageView btnHome; // 最小化，返回主页
	private static Button listshow;
	

	private static ImageView btnReset;
	private static ImageView btnBefore;
	private static ImageView btnNext;
	private static ImageButton btnVoice;
	private static SeekBar seekBarVoice;
	private static SeekBar seekBarPlayProcess;
	private static ListView musicListView;
	private static ImageView MusicPlayModel;
	private static TextView textSongPlayStype;
	private static TextView playTime; // 当前播放时间
	private static TextView durationTime; // 总播放时间
	private static TextView textMusicVoice;
	private static TextView textMusicState;
	private static TextView textSongTitle; // 歌曲名
	private static TextView textSongArtist; // 艺术家
	private static TextView textSongAlbum; // 专辑名
	private static TextView textSongLrc; // LRC歌词显示
	private static Button btnScanFiles; // 扫描文件
	private static Button btnDelFiles; // 删除文件

	// 添加音频文件部分
	private static Button btnAddFiles; // 添加音乐文件，一个或多个
	private static Button btnAddFolders; // 添加音乐文件夹，需要扫描
	protected static final int FILE_RESULT_CODE = 1;

	private Handler mHandler; // 让子线程刷新UI使用

	// 获取当前歌曲总时间
	int currentSongPosition = 0;
	int currentSongMinute = 0;
	int currentSongSecond = 0;

	private AudioManager audioManager; // 声音管理器
	private int soundVolume = 0; // 音量变量
	private int soundMaxVolume = 0; // 最大音量值
	private int soundSaveVolume = 0; // 静音时保存的音量值
	private int seekbarNum = 0; // 音量控制条值
	private int musicPosition; // 突发事件保存变量
	static int musicPlayType = 0; // 音乐播放模式切换
	static int randomNumberBase = 0; // 随机数产生辅助数1

	private Map<String, Map<Long, String>> mapsLrcManager = new HashMap<String, Map<Long, String>>();
	// String:歌词名,HashMap为对应LRC歌词的Hash对象表
	private Map<Long, String> currentLrcHash = new HashMap<Long, String>();

	// 歌曲列表数据库，及其显示映射表
	private List<String> listNumbersSave;// 音频列表序号
	private List<String> listNamesSave; // 音频列表名称
	private List<String> listTimesSave; // 音频列表时间

	MusicServiceApp myMusicListDb; // 音频数据库
	musicListAdapter mAdapter; // 显示适配器
	private int currentListItem; // 当前播放歌曲的索引

	private int currentListItemSave; // 当前选择项
	private String selectItemNameSave; // 长击选中的选项名称
	private String clickItemNameSave; // 单击选中的选项名称

	protected static final String TAG = "Car";
	protected static final int MSG_PLAYTIME = 0;
	private static final int MSG_LRCPOS = 1;

	songMapToSeekBar songThread1; // 子线程1
	LrcParser songLrcParser; // LRC歌词处理类1
	long songLrcPosition = 0;
	long songLrci = 1;
	boolean findLrcFlag = false;
	boolean addFilesFlag = false; // 添加文件时，用于在onPause(),onResume()中识别
	// 添加文件时，是否播放音乐
	boolean returnHomeFlag = false; // 返回主页标识，即最小化
	boolean ThreadRunFlag = false; // 用于等待线程结束标识

	protected static final String RESULT_ITEM = "result_item"; // 返回结果项标识
	protected static final String RESULT_STYPE = "result_stype"; // 返回结果类型标识
	private static final String FILTER_STYPE = "filter_stype"; // 用户过滤类型标识
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";
	protected static final String MUSIC_PATH = "/mnt/sdcard/";
	protected static final String DISPLAY_AUDIO = "audio";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.musichome);

		initMusicComponent(); // 初始化GUI元件t
		initMusicVariable(); // 初始化系统变量
		ReadSharedPreferences(); // 读取系统配置参数
		initMusicObject(); // 初始化系统一些保存的对象，将其从存储空间中读出
		initMusicListView(); // 初始化播放列表

		// 音乐列表播放事件触发
		musicListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			// 当选择音乐时，进行音乐播放
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentListItem = position;
				mAdapter.selectItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();

				String currentSongName = mAdapter.selectItemName + ".mp3";
				Log.i(TAG, "currentSongName=" + currentSongName);

				// 调用音乐播放处理程序
				
				animation1();
				playMusic(currentSongName);
				
				return true;
				// 返回true Click不会被调用
			}
		});

		musicListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 单击显示更新
				mAdapter.clickItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();
			}
		});

		// 音乐播放模式设置事件
		MusicPlayModel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (musicPlayType) {
				case 1: // 由顺序播放-->单曲循环
					MusicPlayModel.setImageResource(R.drawable.musicplaytype2);
					musicPlayType = 2;
					textSongPlayStype.setText("单曲循环");

					break;

				case 2: // 由单曲循环-->列表循环
					MusicPlayModel.setImageResource(R.drawable.musicplaytype3);
					musicPlayType = 3;
					textSongPlayStype.setText("列表循环");
					break;

				case 3: // 由列表循环-->随机播放
					MusicPlayModel.setImageResource(R.drawable.musicplaytype4);
					musicPlayType = 4;
					textSongPlayStype.setText("随机播放");
					break;

				case 4: // 由随机播放-->顺序播放
					MusicPlayModel.setImageResource(R.drawable.musicplaytype1);
					musicPlayType = 1; // 复位
					textSongPlayStype.setText("顺序播放");
					break;

				default:
					break;
				}
			}
		});

		// 通过getStream/MaxVolume获得当前音量大小,音乐的最大音量大小,并设置seek进度条最大值
		soundVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundMaxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		seekBarVoice.setMax(soundMaxVolume);

		// 把当前音量的值设置给进度条
		seekBarVoice.setProgress(soundVolume);
		textMusicVoice.setText("音量(" + soundVolume + ")");

		// 启动一个"Handler"用于接收当前歌曲播放时间变化
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_PLAYTIME: {
					// 更新显示时间
					playTime.setText(msg.arg1 + ":" + msg.arg2);
				}
					break;

				case MSG_LRCPOS: {
					songLrci = ((long) msg.arg1);

					if (currentLrcHash != null) // 使用前判断是否为NULL
					{
						// 从当前歌曲HashMap取歌曲显示
						String temp = currentLrcHash.get(songLrci);
						if (temp != null) {
							textSongLrc.setText(temp);
						}
					}
				}
					break;

				default:
					break;
				}

				super.handleMessage(msg);
			}
		};

		// 处理播放音乐进度条线程
		songThread1 = new songMapToSeekBar();
		songThread1.start();
		ThreadRunFlag = true;

		// 音乐歌曲进度条拖动
		seekBarPlayProcess
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						int position = seekBar.getProgress();
						musicMediaPlayer.seekTo(position);
						songThread1.shouldContinue = true;
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						songThread1.shouldContinue = false;
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// 判断是否在拖动，拖动时才更新
						// 防止平时两个更新
						if (songThread1.shouldContinue == false) {
							songPlayTimeUpdate(progress); // 动态显示当前拖动时间
						}
					}
				});

		// 播放器完成事件监听
		musicMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// 当前歌曲播放完成事件
				switch (musicPlayType) {
				case 1: // "顺序播放"模式
				{
					// 则转至下一首播放
					nextMusic();
				}
					break;

				case 2: // "单曲循环"模式
				{
					currentListItem--;
					nextMusic();
				}
					break;

				case 3: // "列表循环"模式
				{
					nextMusic();
				}
					break;

				case 4: // "随机"模式
				{
					// 在List列表有效范围，产生一个随机歌曲,再进行播放
					if (listNamesSave.size() == 0) {
						// 没有歌曲
						nextMusic(); // 调用next以便停止
					} else if (listNamesSave.size() == 1) {
						// 只剩一首歌
						String playName = listNamesSave.get(0) + ".mp3";
						if (playName != null) {
							playMusic(playName);
							currentSongChange(); // 当前歌曲高亮
							
						}
					} else {
						// 两首歌曲以上
						int maxNumber = listNamesSave.size();
						int randomSongNumber;

						// 生成随机数(双重，防止重复率)，大小范围 0-maxNumber
						randomSongNumber = ((int) (Math.random() * maxNumber));
						do {
							randomSongNumber = ((int) (Math.random() * maxNumber));
						} while (randomSongNumber == currentListItem);

						Log.i(TAG, "randomSongNumber: " + randomSongNumber);
						if (randomSongNumber <= listNamesSave.size()) {
							currentListItem = randomSongNumber;
							String playName = listNamesSave
									.get(currentListItem) + ".mp3";
							if (playName != null) {
								playMusic(playName);
								currentSongChange(); // 当前歌曲高亮
							}
						}
					}
				}
					break;

				default:
					break;

				}
			}
		});

		putFuncationName("onCreate");
	}

	 void animation1() {
		// TODO Auto-generated method stub
		viewflipper.setInAnimation(in_righttoleft);
		viewflipper.setOutAnimation(out_righttoleft);
		viewflipper.showNext();
	}

	// 初始化GUI控件
	private void initMusicComponent() {
		// 查找控制ID
		
		btnMusicClose = (Button) findViewById(R.id.buttonMusicClose);
		btnHome = (ImageView) findViewById(R.id.ImageViewHome);
		btnMusicMin = (Button) findViewById(R.id.buttonMUsicMin);
		btnReset = (ImageView) findViewById(R.id.ImageViewReset);
		btnBefore = (ImageView) findViewById(R.id.ImageViewBefore);
		btnPlayStop = (ImageView) findViewById(R.id.imageViewPlayStop);
		btnNext = (ImageView) findViewById(R.id.ImageViewNext);
		btnVoice = (ImageButton) findViewById(R.id.imageButtonVoice);
		seekBarVoice = (SeekBar) findViewById(R.id.seekBarVoice);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		musicListView = (ListView) findViewById(R.id.listViewSong);
		MusicPlayModel = (ImageView) findViewById(R.id.imageViewMusicPlayModel);
		textSongPlayStype = (TextView) findViewById(R.id.textSongPlayStype);
		seekBarPlayProcess = (SeekBar) findViewById(R.id.seekBarPlay);
		playTime = (TextView) findViewById(R.id.playTime);
		durationTime = (TextView) findViewById(R.id.durationTime);
		textMusicVoice = (TextView) findViewById(R.id.textMusicVoice);
		textMusicState = (TextView) findViewById(R.id.textMusicState);
		textSongTitle = (TextView) findViewById(R.id.textView1);
		textSongArtist = (TextView) findViewById(R.id.textView2);
		textSongAlbum = (TextView) findViewById(R.id.textView3);
		textSongLrc = (TextView) findViewById(R.id.textView4);
		btnAddFiles = (Button) findViewById(R.id.musicBtnAddFiles);
		btnAddFolders = (Button) findViewById(R.id.musicBtnAddFolders);
		btnScanFiles = (Button) findViewById(R.id.musicBtnScanFiles);
		btnDelFiles = (Button) findViewById(R.id.musicBtnDelFiles);
		

		// 添加倾听事件
		btnMusicClose.setOnClickListener(musicListener);
		btnMusicMin.setOnClickListener(musicListener);
		btnHome.setOnClickListener(musicListener);
		btnReset.setOnClickListener(musicListener);
		btnBefore.setOnClickListener(musicListener);
		btnPlayStop.setOnClickListener(musicListener);
		btnNext.setOnClickListener(musicListener);
		btnVoice.setOnClickListener(musicListener);
		//////////////
		viewflipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		listshow =(Button) findViewById(R.id.list);
		enterPlay = (Button) findViewById(R.id.enterplay);
		in_righttoleft = AnimationUtils.loadAnimation(this,R.anim.enter_righttoleft);
		out_righttoleft = AnimationUtils.loadAnimation(this,R.anim.out_righttoleft);
		in_lefttoright = AnimationUtils.loadAnimation(this,R.anim.enter_lefttoright);
		out_lefttoright = AnimationUtils.loadAnimation(this,R.anim.out_lefttoright);
		// 添加音乐文件
		btnAddFiles.setOnClickListener(musicListener);
		btnAddFolders.setOnClickListener(musicListener);
		btnScanFiles.setOnClickListener(musicListener);
		btnDelFiles.setOnClickListener(musicListener); // 单击删除单文件
		btnDelFiles.setOnLongClickListener(musicLongListener); // 长按全部删除
		seekBarVoice.setOnSeekBarChangeListener(voiceChangeListener);
		listshow.setOnClickListener(musicListener);
		enterPlay.setOnClickListener(musicListener);
	}

	// 初始化系统变量
	private void initMusicVariable() {
		// 初始化其它变量
		musicMediaPlayer = new MediaPlayer(); // 音乐文件及播放器
		// currentListItem = 0; // 当前播放歌曲序号初始为0
		musicPlayType = 1; // 初始为顺序播放模式
		textSongPlayStype.setText("顺序播放");
		playTime.setText("00:00"); // 播放时间和总时间显示初始化
		durationTime.setText("");
		textSongLrc.setText(""); // LRC歌词清空
		seekBarPlayProcess.setEnabled(false); // 禁止拖动进度条
		btnReset.setEnabled(false); // 复位初始禁止
		songLrcParser = new LrcParser(); // 新建类

		// 歌曲列实现
		listNumbersSave = new ArrayList<String>();
		listNamesSave = new ArrayList<String>();
		listTimesSave = new ArrayList<String>();
	}

	// 初始化系统一些保存的对象，将其从存储空间中读出
	private void initMusicObject() {
		// **********************************************************
		// 1初始化歌曲列表数据库
		try {
			Log.d(TAG, "创建数据库:");
			myMusicListDb = new MusicServiceApp(this);
			Log.d(TAG, "  成功！");

			// 检查系统是否第1次启动是否已扫描过SD卡等存储器中的音频数据
			if (SDMUSIC_HAVE_READ.equals("T")) {
				// 已经读取过，无需配置
				Log.d(TAG, "系统已初始化存储器中的音频数据!");
			} else {
				// 未读取过，初始读取,方便以后“扫描”和“添加”音频数据
				Log.d(TAG, "系统未初始化存储器中的音频数据!");

				// 扫描SD卡中的数据,注：此操作仅在系统第一次启动才启动
				initMusicDbData();
				SDMUSIC_HAVE_READ = "T";
			}

		} catch (Throwable e1) {
			Log.e(TAG, e1.toString());
			Log.d(TAG, "  失败！");
		}

		// **********************************************************
		// 2歌词HASH数据库操作
		File lrcFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMusic-Lrc.db");
		if (!lrcFile.exists()) // 若文件不存在，则创建
		{
			try {
				lrcFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}

		try {
			ObjectInputStream lrcOi = new ObjectInputStream(
					new FileInputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-Lrc.db"));
			// 上一句会异常第一次启动时会出现：但不影响程序结果！
			// ERROR/Car(28063): java.io.EOFException

			// 将LRC HASH管理器对象读出来
			mapsLrcManager = (Map<String, Map<Long, String>>) lrcOi
					.readObject();
			lrcOi.close(); // 关闭

		} catch (StreamCorruptedException e) {
			Log.e(TAG, e.toString());
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.toString());
		}

		// **********************************************************
		// 3歌曲列表-名称"数据库"操作
		File nameFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListNames.db");
		if (!nameFile.exists()) // 若文件不存在，则创建
		{
			try {
				nameFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}

		try {
			ObjectInputStream nameOi = new ObjectInputStream(
					new FileInputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListNames.db"));
			listNamesSave = (List<String>) nameOi.readObject();
			nameOi.close();

		} catch (StreamCorruptedException e) {
			Log.e(TAG, e.toString());
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.toString());
		}

		// **********************************************************
		// 4歌曲列表-时间"数据库"操作
		File timeFile = new File(""
				+ "/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListTimes.db");
		if (!timeFile.exists()) // 若文件不存在，则创建
		{
			try {
				timeFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}

		try {
			ObjectInputStream timeOi = new ObjectInputStream(
					new FileInputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListTimes.db"));
			listTimesSave = (List<String>) timeOi.readObject();
			timeOi.close();

		} catch (StreamCorruptedException e) {
			Log.e(TAG, e.toString());
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.toString());
		}
	}

	// 初始化歌曲播放列表
	private void initMusicListView() {
		int position, listSize;

		// 根据需要初始化listNumbersSave
		listSize = listNamesSave.size();
		listNumbersSave.clear(); // 清空
		for (position = 1; position <= listSize; position++) {
			listNumbersSave.add(Integer.toString(position));
		}

		// 新建适配器，更新显示
		mAdapter = new musicListAdapter(this, listNumbersSave, listNamesSave,
				listTimesSave);
		musicListView.setAdapter(mAdapter);

		// 刷新选项
		currentListItem = currentListItemSave;
		mAdapter.clickItemName = clickItemNameSave;
		mAdapter.selectItemName = selectItemNameSave;
		mAdapter.notifyDataSetInvalidated();

		listViewFlush(); // 刷新显示
	}

	// 刷新listView当前的显示
	private void listViewFlush() {
		int positionView = listNamesSave.indexOf(mAdapter.selectItemName);

		if (positionView >= 0) {
			// 获取当前显示最前、最后一个项数
			int lastPosition = musicListView.getLastVisiblePosition();
			int firstPositon = musicListView.getFirstVisiblePosition();

			// 判断是否在视图内
			if ((positionView > lastPosition) || (positionView < firstPositon)) {
				// 不是，则将选项置为当前视图
				musicListView.setSelection(positionView);
			}
		}
	}

	// 系统配置参数读取
	void ReadSharedPreferences() {
		SharedPreferences user = getSharedPreferences("user_info_music",
				Activity.MODE_PRIVATE);
		SDMUSIC_HAVE_READ = user.getString("SDMUSIC_HAVE_READ", "");
		currentListItemSave = user.getInt("currentListItemSave", -1);
		selectItemNameSave = user.getString("selectItemNameSave", "");
		clickItemNameSave = user.getString("clickItemNameSave", "");
	}

	// 系统配置参数保存
	void WriteSharedPreferences() {
		SharedPreferences user = getSharedPreferences("user_info_music",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = user.edit();
		editor.putString("SDMUSIC_HAVE_READ", SDMUSIC_HAVE_READ);

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
	protected void onDestroy() {
		// 关闭music播放，LRC显示线程
		songThread1.shouldRun = false;
		while (ThreadRunFlag == true)
			;

		// 不能先释放"musicMediaPlayer"，要等上面的线程结束
		// 因为上述线程会调用到该对象，要该对象删除，会出错：
		// FATAL EXCEPTION: Thread-10
		// java.lang.IllegalStateException
		// at android.media.MediaPlayer.isPlaying(Native Method)
		// at
		// com.guet.SmartCarSystem.SmartCarMusic$songMapToSeekBar.run(SmartCarMusic.java:2092)

		// if( musicIsPlay == true )
		if (musicMediaPlayer.isPlaying()) { // 若正在播放
			musicMediaPlayer.stop(); // 停止
		}

		musicMediaPlayer.reset(); // 复位
		musicMediaPlayer.release(); // 释放占用设备

		// 关闭数据库
		Log.d(TAG, "正在关闭数据库...");
		myMusicListDb.closeDb(); // 关闭数据库

		saveMusicObject(); // 保存一些系统对象
		WriteSharedPreferences(); // 保存系统配置参数
		System.gc(); // 明确释放内存

		super.onDestroy();
		putFuncationName("onDestroy");
	}

	// 保存系统一些对象
	private void saveMusicObject() {
		// *********************************************************
		// 1保存MusicList Name管理器对象
		try {
			ObjectOutputStream oos1 = new ObjectOutputStream(
					new FileOutputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListNames.db"));

			oos1.writeObject(listNamesSave);
			oos1.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

		// *********************************************************
		// 2保存MusicList Time管理器对象
		try {
			ObjectOutputStream oos1 = new ObjectOutputStream(
					new FileOutputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListTimes.db"));

			oos1.writeObject(listTimesSave);
			oos1.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

		// *********************************************************
		// 2保存LRC HASH管理器对象
		try {
			ObjectOutputStream oos2 = new ObjectOutputStream(
					new FileOutputStream(
							"/mnt/sdcard/SmartCarSystem/SmartCarMusic-Lrc.db"));

			oos2.writeObject(mapsLrcManager);
			oos2.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

	// 重写onActivityResult,用于添加音频文件时返回数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		if (FILE_RESULT_CODE == requestCode) {
			Bundle bundle = null;

			// 返回结果及返回类型变量声明
			List<String> returnSelectItems = new ArrayList<String>();
			List<String> returnFileItems = new ArrayList<String>();
			String resturnStype;

			if (data != null && (bundle = data.getExtras()) != null) {
				returnSelectItems = bundle.getStringArrayList(RESULT_ITEM);
				resturnStype = bundle.getString(RESULT_STYPE);
				int dataSize = returnSelectItems.size();

				if (dataSize > 0) {
					Log.d(TAG, "文件(夹)添加(结果如下): ");
					// 输入添加的文件或文件夹
					for (int i = 0; i < dataSize; i++) {
						Log.d(TAG, "[" + i + "]=" + returnSelectItems.get(i));
					}

					if (resturnStype.equals(DISPLAY_FILES)) {
						// 若为文件添加，无须变换
						returnFileItems = returnSelectItems;
					} else if (resturnStype.equals(DISPLAY_FOLDERS)) {
						// 若为文件夹添加，则须对文件夹进行扫描出文件
						// 传入文件夹列表，返回文件列表,若文件列表不大于0则返回NULL
						returnFileItems = fileScanFromFolders(returnSelectItems);

						if (returnFileItems == null) {
							Log.d(TAG, "文件(夹)扫描结果为空！");
							return;
						} else {
							Log.d(TAG, "文件(夹)扫描结果如下：");
							dataSize = returnFileItems.size();
							for (int j = 0; j < dataSize; j++) {
								Log.d(TAG,
										"[" + j + "]=" + returnFileItems.get(j));
							}
						}
					}

					// 调用文件(夹)添加函数
					try {
						doAddMusicFile(returnFileItems);
					} catch (Throwable e) {
						Log.d(TAG, "调用文件(夹)添加函数: 失败！");
						Log.d(TAG, e.toString());
					}
				} else {
					Log.d(TAG, "当前没有要扫描的文件夹或添加的文件！");
				}

			}

			// 添加文件完成
			addFilesFlag = false;
		}
	}

	// 从文件夹列表中，查找文件，并返回文件列表
	private List<String> fileScanFromFolders(List<String> Folders) {
		int i;
		List<String> musicTempList = new ArrayList<String>();

		for (i = 0; i < Folders.size(); i++) {
			String dirStr = Folders.get(i); // 依次获取每个子目录
			if (dirStr != null) {
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

				if (subDir.listFiles(new MusicFilter()).length > 0) {
					for (File file : subDir.listFiles(new MusicFilter())) {
						musicTempList.add(file.getName());
					}
				}
			}
		}

		// 若有文件则返回列表，否则返回NULL
		if (musicTempList.size() > 0)
			return musicTempList;
		else
			return null;
	}

	// 当有电话有时,添加文件时会引发
	@Override
	protected void onPause() {
		// 当前播放器不工作、添加文件、返回主页 3种情况不执行停止
		if (musicMediaPlayer.isPlaying() && (addFilesFlag == false)
				&& returnHomeFlag == false) {
			// 保存当前播放的位置
			musicPosition = musicMediaPlayer.getCurrentPosition();
			Log.d(TAG, "musicPosition=" + musicPosition);
			musicMediaPlayer.pause();
			// musicIsPlay = false;
		}

		super.onPause();
		putFuncationName("onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		putFuncationName("onStart");
	}

	@Override
	protected void onRestart() {
		// 检查系列存在检查文件
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile.exists()) {
			// 文件不存在，证明主页已关闭，表示其它子页不能返回
			finish(); // 退出
		}

		super.onRestart();
		putFuncationName("onRestart");
	}

	@Override
	protected void onStop() {
		putFuncationName("onStop");
		super.onStop();
	}

	// 接完电话后,添加文件后会调用
	@Override
	protected void onResume() {
		if ((musicPosition > 0) && (mAdapter.selectItemName != null)
				&& (addFilesFlag == false)) {
			Log.i(TAG, "musicPosition = " + musicPosition);

			try {
				Log.d(TAG, "musicPosition=" + musicPosition);
				musicMediaPlayer.seekTo(musicPosition);
				musicMediaPlayer.start();
				// musicIsPlay = true;

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}

		super.onResume();
		putFuncationName("onResume");
	}

	// 重新创建后，比如内存吃紧系统会将停止或暂停Activity杀死后再再恢复
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// this.fileName = savedInstanceState.getString("fileName");
		// this.musicPosition =savedInstanceState.getInt("musicPosition");
		// super.onRestoreInstanceState(savedInstanceState);
		//
		Log.e(TAG, "onRestoreInstanceState()!");
	}

	// 在未知事件发生时，保存数据(系列化到系统硬盘)，如系统内存紧张，该ACTIVITY会被杀死
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// outState.putString("fileName", fileName);
		// outState.putInt("musicPosition", musicPosition);
		// super.onSaveInstanceState(outState);

		Log.e(TAG, "onSaveInstanceState()!");
	}

	// 声音控制改变事件
	private OnSeekBarChangeListener voiceChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		// 数值变化事件
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			seekbarNum = seekBarVoice.getProgress();

			// (2)直接改变
			if (voiceOpen == true) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						seekbarNum, AudioManager.FLAG_PLAY_SOUND);
			} else {
				// 静音模式
				soundSaveVolume = seekbarNum; // 只保存不设置
			}

			soundVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (soundVolume == 0) {
				// 静音
				textMusicVoice.setText("静音!");
				btnVoice.setImageResource(R.drawable.musicvoiceclose);
			} else {
				// 非静音
				textMusicVoice.setText("音量(" + soundVolume + ")");
				btnVoice.setImageResource(R.drawable.musicvoiceopen);
			}
		}
	};

	// 多个按钮长按倾听事件
	private OnLongClickListener musicLongListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			boolean result = false;

			switch (v.getId()) {
			case R.id.musicBtnDelFiles: {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SmartCarMusic.this);
				// builder.setIcon(R.drawable.jg); //设置图标
				builder.setIcon(R.drawable.carparnter);
				builder.setTitle("是否要清空当前播放列表？"); // 设置标题

				// 确认单击事件
				builder.setPositiveButton("是(Yes)",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d(TAG, "点击了对话框上的确定按钮");
								doDelAllMusicFile(); // 调用删除所有歌曲列表函数
							}

						});

				// 取消单击事件
				builder.setNegativeButton("否(No)",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d(TAG, "点击了对话框上的取消按钮");
							}
						});

				builder.create().show(); // 显示出来
				result = true; // 单击无效
			}
				break;

			default:
				break;
			}

			return result;
		}
	};

	// 多个按钮单击倾听事件
	private OnClickListener musicListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				switch (v.getId()) {
				case R.id.ImageViewReset: // 复位
				{
					musicMediaPlayer.stop(); // 停止
					musicIsPlay = false;
					seekBarPlayProcess.setProgress(0);
					if (seekBarPlayProcess.isEnabled()) {
						seekBarPlayProcess.setEnabled(false); // 停止时禁止拖动进度条
					}
					playTime.setText("00:00");
					textMusicState.setText("停止");
					btnPlayStop.setImageResource(R.drawable.musicplaystart);
				}
					break;

				case R.id.ImageViewBefore: // 上一首
				{
					lastMusic();
				}
					break;

				case R.id.ImageViewNext: // 下一首
				{
					nextPlayFlag = true;
					nextMusic();
				}
					break;

				case R.id.imageViewPlayStop: // 播放或停止
				{
					if (musicMediaPlayer.isPlaying()) {
						// 由"播放"到"暂停",更换为暂停图标
						musicMediaPlayer.pause();
						textMusicState.setText("暂停");
						btnPlayStop.setImageResource(R.drawable.musicplaystart);
					} else {
						if (listNamesSave.size() > 0) {
							if (musicIsPlay == false) {
								// 初次，要初始化数据源
								// 由"暂停"到"播放",更换为播放图标
								if (currentListItem == -1)
									currentListItem = 0; // 针对第一次启动

								String playName = listNamesSave
										.get(currentListItem) + ".mp3";
								if (playName != null) {
									playMusic(playName);
									currentSongChange();
								}
							} else {
								musicMediaPlayer.start();
								textMusicState.setText("播放中");
							}

							btnPlayStop
									.setImageResource(R.drawable.musicplaypause);
						}
					}

					if (!btnReset.isEnabled()) {
						btnReset.setEnabled(true);
					}
				}
					break;

				case R.id.buttonMusicClose: // 音乐主页关闭
				{
					doExitWork(); // 执行程行退出工作
				}
					break;

				case R.id.buttonMUsicMin: // 最小化
				case R.id.ImageViewHome: // 从音乐主页返回系统主页,不关闭音乐
				{
					returnHome();
				}
					break;

				case R.id.imageButtonVoice: // 静音控制开关
				{
					if (voiceOpen == true) {
						// 关闭声音,静音模式
						btnVoice.setImageResource(R.drawable.musicvoiceclose);
						textMusicVoice.setText("静音!");
						// 保存静音值,将音乐音量设为0
						soundSaveVolume = audioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC);
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								0, AudioManager.FLAG_PLAY_SOUND);

						voiceOpen = false;
					} else {
						// 音量条为0，点击时不生效
						if (soundSaveVolume != 0) {
							// 打开声音,声音模式
							btnVoice.setImageResource(R.drawable.musicvoiceopen);
							// 恢复静音值
							audioManager.setStreamVolume(
									AudioManager.STREAM_MUSIC, soundSaveVolume,
									AudioManager.FLAG_PLAY_SOUND);

							textMusicVoice.setText("音量(" + soundVolume + ")");
							voiceOpen = true;
						}
					}
				}
					break;

				case R.id.musicBtnAddFiles: // 添加音频文件
				{
					Intent intent = new Intent(SmartCarMusic.this,
							MyFileManager.class);
					// New一个Bundle对象，并将要传递的数据传入,告诉文件浏览，要求显示是音频文件
					Bundle bundle = new Bundle();
					bundle.putString(REQUEST_STYPE, DISPLAY_FILES);
					bundle.putString(FILTER_STYPE, DISPLAY_AUDIO);
					intent.putExtras(bundle);

					// 有返回结果
					startActivityForResult(intent, FILE_RESULT_CODE);

					// 添加文件启动
					addFilesFlag = true;
				}
					break;

				case R.id.musicBtnAddFolders: // 添加音频文件夹
				{

					Intent intent = new Intent(SmartCarMusic.this,
							MyFileManager.class);

					// New一个Bundle对象，并将要传递的数据传入,告诉文件浏览，要求显示是文件夹
					Bundle bundle = new Bundle();
					bundle.putString(REQUEST_STYPE, DISPLAY_FOLDERS);
					bundle.putString(FILTER_STYPE, DISPLAY_AUDIO);
					intent.putExtras(bundle);

					// 有返回结果
					startActivityForResult(intent, FILE_RESULT_CODE);

					// 添加文件启动
					addFilesFlag = true;
				}
					break;

				case R.id.musicBtnScanFiles: // 扫描方式添加音频文件，默认为SD卡
				{
					try {
						scanFilesFromExt();
					} catch (Throwable e) {
						Log.e(TAG, e.toString());
					}

					Log.d(TAG, "case R.id.musicBtnScanFiles:");
				}
					break;

				case R.id.musicBtnDelFiles: // 删除当前文件
				{
					doDelMusicFile();
				}
					break;
				case R.id.list:
				{
				     animation2();
				}
				break;
				case R.id.enterplay:
				{ animation1();}
				break;
				default:
					break;
				}
			} catch (Exception e) {
				// 输出异常信息
				Log.e(TAG, e.toString());
			}
		}

	};

	// 返回方法
	private void returnHome() {
		returnHomeFlag = true;

		// 调用"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarMusic.this, SmartCarSystem.class);

		// FLAG_ACTIVITY_REORDER_TO_FRONT标志，能防止重复实例化一个Activity
		// 进去后，会跳过"onCreate()",直接到"onRestart()"-->"onStart()"
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		startActivity(intent);

		// SmartCarMusic.this.finish(); //不能用finish，会触发onDestroy();
		putFuncationName("returnHome");
	}

	protected void animation2() {
		// TODO Auto-generated method stub
		viewflipper.setInAnimation(in_lefttoright);
		viewflipper.setOutAnimation(out_lefttoright);
		viewflipper.showPrevious();
	}

	// 执行程序退出工作
	private void doExitWork() {
		SmartCarMusic.this.finish();
		Log.d(TAG, "doExitWork()");
	}

	// 播放音乐
	void playMusic(String musicName) {
		String songTitleStr = null; // 3--歌曲名
		String songArtistStr = null; // 5--艺术家
		String songAlbumStr = null; // 6--专辑名
		String songFilePath = null; // 1--MP3路径名
		String songFileName = null; // 2--MP3文件名
		String lrcFilePath = null;

		if (musicName == null)
			return; // 防止歌曲名称为空

		try {
			// 根据歌曲名称，从数据库中提取数据
			Music musicTemp = new Music();
			try {
				musicTemp = myMusicListDb.find(musicName);
				if (musicTemp == null) {
					Log.d(TAG, "在数据库中无法查找该歌曲：" + musicName + "!");
					textSongLrc.setText("无法定位该歌曲文件...");
					return;
				}
			} catch (Throwable e) {
				Log.d(TAG, e.toString());
				Log.d(TAG, "在数据库中查找歌曲 -" + musicName + ": 异常！");
				textSongLrc.setText("无法定位该歌曲文件...");
				return;
			}

			// 执行至此，查找到music
			songLrci = 1;
			findLrcFlag = false;
			boolean songLrcOk = false;

			// 开启复位
			if (!btnReset.isEnabled()) {
				btnReset.setEnabled(true);
			}

			songFilePath = musicTemp.path; // 获取文件路径
			songFileName = musicTemp.name; // 获取文件名
			songTitleStr = musicTemp.title; // 获取歌曲名
			songArtistStr = musicTemp.artist; // 获取艺术家
			songAlbumStr = musicTemp.album; // 获取音乐ID

			textSongTitle.setText(songTitleStr);
			textSongArtist.setText("歌手：" + songArtistStr);
			textSongAlbum.setText("专辑：" + songAlbumStr);

			// (1)首先判断SD卡上是否有相应LRC HASH对象
			Log.i(TAG, "songFileName:" + songFileName + " find in SD DataLib!");

			currentLrcHash = (HashMap<Long, String>) mapsLrcManager
					.get(songFileName);
			// 此句很关键！必须加(HashMap<Long, String>)
			// (2)此句很关键！必须进行检测
			if (currentLrcHash != null && currentLrcHash.size() > 0) {
				// 在LRC HASH数据库中有相应备份
				// 直接取出来，赋给当前LRC 歌曲显示使用的HASH
				Log.i(TAG, "在SD卡查找到相应的HashMap!");

				// 使能歌词解析
				findLrcFlag = true;
			} else {
				// 去"mp3"
				lrcFilePath = songFilePath.substring(0,
						songFilePath.length() - 3);
				lrcFilePath = lrcFilePath + "lrc";
				File lrcFile = new File(lrcFilePath);

				if (!lrcFile.exists()) // 判断该LRC文件是否存在
				{
					// 显示无法加载LRC文件
					Log.i(TAG, "/////////////////无法查到的LRC，也无法在SD卡中找到相应的HashMap!");
					textSongLrc.setText("无法加载该文件的\"lrc\"歌词文件...");
				} else {
					textSongLrc.setText("正在加载该文件的\"lrc\"歌词文件...");

					// 判断歌词文件格式,否则易乱码
					if (isUft8File(lrcFilePath) == true) {
						// LRC文件编码为UTF8
						songLrcOk = songLrcParser.setFile(lrcFilePath, true);
					} else {
						// LRC文件编码为GBK
						songLrcOk = songLrcParser.setFile(lrcFilePath, false);
					}

					// 2进行LRC文件解析后，得到一个LRC MAPSHASH
					Map<Long, String> mapsLrcSon = new HashMap<Long, String>();

					do {
						Thread.sleep(10);

					} while (!songLrcOk); // 等待LRC解析完成!

					// 将得到的Hash进行保存
					mapsLrcSon = songLrcParser.mapsToLrc;

					Log.i(TAG, "songFileName:" + songFileName
							+ " put in SD DataLib!");
					mapsLrcManager.put(songFileName, mapsLrcSon);
					currentLrcHash = mapsLrcSon; // 将得到的Hash赋给当前LRC专用显示HASH

					// 使能歌词解析
					findLrcFlag = true;
					Log.i(TAG, "已从查找到LRC文件生成相应的HashMap!");
				}
			}

			// 2准备歌曲音频数据
			musicMediaPlayer.reset();
			musicMediaPlayer.setDataSource(songFilePath);
			musicMediaPlayer.prepare();
			musicMediaPlayer.start();

			// 由“播放”切换到“停止”按钮
			textMusicState.setText("播放中");
			btnPlayStop.setImageResource(R.drawable.musicplaypause);
			musicIsPlay = true;
			if (!seekBarPlayProcess.isEnabled()) {
				seekBarPlayProcess.setEnabled(true); // 播放时使能拖动进度条
			}

			// 更新进度条
			currentSongPosition = musicMediaPlayer.getDuration();
			seekBarPlayProcess.setMax(currentSongPosition);

			// 显示歌曲时间长度
			currentSongPosition = currentSongPosition / 1000; // 转化为秒
			currentSongMinute = currentSongPosition / 60;
			currentSongSecond = currentSongPosition % 60;
			durationTime.setText(currentSongMinute + ":" + currentSongSecond);

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	// 下一首
	void nextMusic() {
		// 判断歌曲列表是否已为空
		if (listNamesSave.size() == 0) {
			doNoMusicWork();
			return;
		}

		String playName;

		if (++currentListItem >= listNamesSave.size()) {
			currentListItem = 0;

			// 若为"列表循环"模式,或者在播放最后一首时，点击下一首,则继续播放
			if (musicPlayType == 3 || nextPlayFlag == true) {
				nextPlayFlag = false;
				playName = listNamesSave.get(currentListItem) + ".mp3";
				if (playName != null) {
					playMusic(playName);
				}

			} else {
				doNoMusicWork();
				if (musicPlayType == 1) {
					// 若为顺序播放，在只有一首歌时，取消最后一首高选
					mAdapter.selectItemName = "";
					mAdapter.notifyDataSetInvalidated();
				}
			}

		} else {
			playName = listNamesSave.get(currentListItem) + ".mp3";
			if (playName != null) {
				playMusic(playName);
			}
		}

		currentSongChange(); // 当前歌曲高亮
	}

	// 执行没有歌曲播放工作
	private void doNoMusicWork() {
		// 判断是否仍在播放
		if (musicMediaPlayer.isPlaying()) {
			musicMediaPlayer.stop();
		}

		seekBarPlayProcess.setProgress(0);
		if (seekBarPlayProcess.isEnabled()) {
			seekBarPlayProcess.setEnabled(false); // 停止时禁止拖动进度条
		}
		playTime.setText("00:00"); // 播放时间和总时间显示刷新
		durationTime.setText("");
		textMusicState.setText("停止");
		btnPlayStop.setImageResource(R.drawable.musicplaystart);
		musicIsPlay = false;
		currentListItem = -1;

		// return;
	}

	// 根据当前歌曲改变，高亮
	private void currentSongChange() {
		// 歌曲根据"currentListItem"列表刷新,高亮
		if (currentListItem < listNamesSave.size() && currentListItem >= 0) {
			mAdapter.selectItemName = listNamesSave.get(currentListItem);
			mAdapter.notifyDataSetInvalidated();

			listViewFlush(); // 刷新显示
		}
	}

	// 上一首
	void lastMusic() {
		if (listNamesSave.size() == 0) {
			doNoMusicWork();
			return;
		}

		String playName;

		if (currentListItem != 0) {
			if (--currentListItem >= 0) {
				playName = listNamesSave.get(currentListItem) + ".mp3";
				if (playName != null) {
					playMusic(playName);
				}
			} else {
				// 到最顶层歌曲
				currentListItem = listNamesSave.size();
			}
		} else {
			playName = listNamesSave.get(currentListItem) + ".mp3";
			if (playName != null) {
				playMusic(playName);
			}
		}

		currentSongChange(); // 当前歌曲高亮
	}

	// 初始化歌曲数据库
	void initMusicDbData() throws Throwable {
		Cursor cursorTemp;

		// 获取外部存储器中所有音频文件
		cursorTemp = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				// 这个字符串数组表示要查询的列
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,// 0
						// 音乐文件名
						MediaStore.Audio.Media.TITLE, // 1
						// 歌曲的名称
						MediaStore.Audio.Media.DATA, // 2
						// 音乐文件的路径
						MediaStore.Audio.Media.DURATION, // 3
						// 音乐的总时间
						MediaStore.Audio.Media.ARTIST, // 4
						// 艺术家
						MediaStore.Audio.Media.ALBUM // 5
				// 专辑名

				}, null, // 查询条件，相当于sql中的where语句
				null, // 查询条件中使用到的数据
				null // 查询结果的排序方式
				);

		// 将查找到的音频数据，添加到数据库中
		if (cursorTemp != null) {
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr, artistStr, albumStr;

			// 从Cursor中分离出数据送至ListView中显示
			for (int i = 0; i < cursorTemp.getCount(); i++) {
				cursorTemp.moveToPosition(i);

				// 先读取歌曲名称
				nameStr = cursorTemp.getString(0);

				// 进行mp3文件过滤
				String mp3Filter = nameStr.substring(nameStr.length() - 3);
				if (!mp3Filter.equals("mp3")) {
					Log.d(TAG, "系统对该文件: " + nameStr + "暂不支持!");
					continue;
				}

				// 查看数据库是否存在
				Music music1 = new Music();
				music1 = myMusicListDb.find(nameStr);
				if (music1 == null) {
					// 若不存在，则添加
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					artistStr = cursorTemp.getString(4);
					albumStr = cursorTemp.getString(5);

					Music music2 = new Music(); // 重新NEW一个对象，因为前面为NULL赋值
					music2.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
							albumStr, "T", "T");
					myMusicListDb.save(music2);
					addCount++;
				}
			}
			cursorTemp.moveToFirst(); // 遍历完后复位
			cursorTemp.close(); // 关闭，释放资源

			if (addCount > 0) {
				Log.d(TAG, "初始化歌曲数据库：发现" + addCount + "个音频文件...");
			} else {
				Log.d(TAG, "初始化歌曲数据库：未发现可用音频文件...");
			}
		}
	}

	// 从外部存储器SD卡中扫描音频文件
	void scanFilesFromExt() throws Throwable {
		Cursor cursorTemp;

		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		// 获取外部存储器中所有音频文件
		cursorTemp = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				// 这个字符串数组表示要查询的列
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,// 0
						// 音乐文件名
						MediaStore.Audio.Media.TITLE, // 1
						// 歌曲的名称
						MediaStore.Audio.Media.DATA, // 2
						// 音乐文件的路径
						MediaStore.Audio.Media.DURATION, // 3
						// 音乐的总时间
						MediaStore.Audio.Media.ARTIST, // 4
						// 艺术家
						MediaStore.Audio.Media.ALBUM // 5
				// 专辑名

				}, null, // 查询条件，相当于sql中的where语句
				null, // 查询条件中使用到的数据
				null // 查询结果的排序方式
				);

		// 将查找到的音频数据，添加到数据库中
		if (cursorTemp != null) {
			cursorTemp.moveToFirst();
			int temp, addCount = 0, itemNum;
			String nameStr, nameFind, titleStr, pathStr, timeStr, artistStr, albumStr;

			// 从Cursor中分离出数据送至ListView中显示
			for (int i = 0; i < cursorTemp.getCount(); i++) {
				cursorTemp.moveToPosition(i);

				// 先读取歌曲名称
				nameStr = cursorTemp.getString(0);

				// 进行mp3文件过滤
				String mp3Filter = nameStr.substring(nameStr.length() - 3);
				if (!mp3Filter.equals("mp3")) {
					Log.d(TAG, "系统对该文件: " + nameStr + "暂不支持!");
					continue;
				}

				// 查看数据库是否存在
				Music music1 = new Music();
				music1 = myMusicListDb.find(nameStr);
				if (music1 == null) {
					// 若不存在，则添加
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					artistStr = cursorTemp.getString(4);
					albumStr = cursorTemp.getString(5);

					Music music2 = new Music(); // 重新NEW一个对象，因为前面为NULL赋值

					// 为第一次初始化
					music2.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
							albumStr, "T", "T");
					myMusicListDb.save(music2);

				} else {
					// 无须更新数据库数据

					// 去除(.mp3)
					nameFind = nameStr.substring(0, nameStr.length() - 4);

					// 检查当前歌曲播放列表是否存在，不存在，则在尾部添加
					if (listNamesSave.contains(nameFind) == false) {
						addCount++;
						timeStr = music1.time; // 从数据库中获取时间

						// 更新歌曲列表
						listNamesSave.add(nameFind);
						listTimesSave.add(timeStr);

						// 添加歌曲，在尾部，序号递增即可！
						itemNum = listNumbersSave.size() + 1;
						listNumbersSave.add(Integer.toString(itemNum));

					} else {
						Log.d(TAG, "该文件，存在于当前播放列表，无须添加!");
					}
				}
			}
			cursorTemp.moveToFirst(); // 遍历完后复位
			cursorTemp.close(); // 关闭，释放资源

			// 返回用户添加结果
			if (addCount > 0) {
				// 新建适配器，更新显示
				mAdapter = new musicListAdapter(this, listNumbersSave,
						listNamesSave, listTimesSave);
				musicListView.setAdapter(mAdapter);

				// 保持之前的显示
				mAdapter.clickItemName = clickItemNameSave;
				mAdapter.selectItemName = selectItemNameSave;
				// mAdapter.notifyDataSetInvalidated();
				mAdapter.notifyDataSetChanged();

				displayTip("成功添加" + addCount + "首歌曲...");
			} else {
				displayTip("没有要添加的歌曲...");
			}
		} else {
			Log.d(TAG, "无法获取Android内部的音频数据库");
		}
	}

	// 添加音乐文件(含文件夹)
	void doAddMusicFile(List<String> dataItem) throws Throwable {
		int i, size, addCount = 0, tempInt;
		String addFileName, addFilePath, tempStr;
		String nameStr, nameFind, titleStr, pathStr, timeStr, artistStr, albumStr;

		MediaPlayer tempPlayer = new MediaPlayer(); // 用于获取MP3播放时间
		Music musicTemp = new Music();

		// 获取旧的选项和单击项
		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		size = dataItem.size(); // 获取添加项大小

		Log.d(TAG, "以下为要添加的文件：");

		// 依次检查每一项文件
		for (i = 0; i < size; i++) {
			addFilePath = dataItem.get(i); // 先得到的是完全的名称，如/mnt/sdcard/hao.mp3

			// 进行文件名全称中分离文件名
			File fileTemp = new File(addFilePath);
			addFileName = fileTemp.getName();
			Log.d(TAG, "");
			Log.d(TAG, "[" + i + "]=" + addFileName);

			// 1先从数据库中查找是否存在
			Music music1 = new Music();
			music1 = myMusicListDb.find(addFileName);
			Log.d(TAG, "1.在数据库中查找: ");

			if (music1 != null) {
				Log.d(TAG, "  成功...");
				Log.d(TAG, "2.无须更新后台数据库...");

				// 直接在数据库中取参数
				nameStr = addFileName; // 1文件名
				timeStr = music1.time;
			} else {
				// 若数据库中不存在，则对该音频数据进行分析，添加到数据库中
				Log.d(TAG, "  失败...");
				Log.d(TAG, "2.正在对该文件进行解析...");

				// New一个MP3信息解析类
				MusicInfoServer mp3Info = new MusicInfoServer(addFilePath);
				mp3Info.getMp3Info(); // 启动解析

				// 若不存在，则添加
				nameStr = addFileName; // 1文件名
				pathStr = addFilePath; // 3文件名，全路径

				tempStr = mp3Info.info.songName; // 2歌曲名(标题)
				if (tempStr != null) {
					titleStr = tempStr;
				} else {
					titleStr = "未知";
				}

				tempStr = mp3Info.info.artist; // 5艺术家
				if (tempStr != null) {
					artistStr = tempStr;
				} else {
					artistStr = "未知";
				}

				tempStr = mp3Info.info.album; // 6专辑
				if (tempStr != null) {
					albumStr = tempStr;
				} else {
					albumStr = "未知";
				}

				// 获取歌曲的长度
				tempPlayer.reset();
				tempPlayer.setDataSource(addFilePath);
				tempPlayer.prepare();
				// tempPlayer.stop();

				tempInt = tempPlayer.getDuration();
				timeStr = toTime(tempInt);
				// Log.d(TAG,"解码获得其播放时间为：" + timeStr);

				// 构造一个music数据项
				musicTemp.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
						albumStr, "F", "T");
				Log.d(TAG, "music: " + musicTemp.toString());

				Log.d(TAG, "3.正在添加入数据库...");
				if (myMusicListDb.save(musicTemp)) {
					Log.d(TAG, "  成功！");
				} else {
					Log.d(TAG, "  失败！");
				}
			}

			// 检查当前歌曲播放列表是否存在，不存在，则在尾部添加
			nameFind = nameStr.substring(0, nameStr.length() - 4);
			if (listNamesSave.contains(nameFind) == false) {
				addCount++;

				// 更新歌曲列表
				listNamesSave.add(nameFind);
				listTimesSave.add(timeStr);

				// 添加歌曲，在尾部，序号递增即可！
				int itemNum = listNumbersSave.size() + 1;
				listNumbersSave.add(Integer.toString(itemNum));

			} else {
				Log.d(TAG, "该文件，存在于当前播放列表，无须添加!");
			}
		}

		tempPlayer.release(); // 释放硬件

		// 返回用户添加结果
		if (addCount > 0) {
			// 新建适配器，更新显示
			mAdapter = new musicListAdapter(this, listNumbersSave,
					listNamesSave, listTimesSave);
			musicListView.setAdapter(mAdapter);

			// 保持之前的显示
			mAdapter.clickItemName = clickItemNameSave;
			mAdapter.selectItemName = selectItemNameSave;
			// mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();

			displayTip("成功添加" + addCount + "首歌曲...");
		} else {
			displayTip("没有要添加的歌曲...");
		}

	}

	// 调用删除所有歌曲列表函数
	private void doDelAllMusicFile() {
		if (listNamesSave.size() > 0) {
			listNamesSave.clear(); // 清空列表
			listNumbersSave.clear();
			listTimesSave.clear();

			// currentListItem = -1;
			mAdapter = new musicListAdapter(this, listNumbersSave,
					listNamesSave, listTimesSave);
			musicListView.setAdapter(mAdapter);

			mAdapter.selectItemName = "";
			mAdapter.clickItemName = "";
			mAdapter.notifyDataSetInvalidated();
		} else {
			displayTip("当前列表为空!无须清空!");
		}
	}

	// 删除某一项音乐文件
	void doDelMusicFile() {
		String musicNamePlay = "", delFileName, musicNameNext = "";

		if (mAdapter.clickItemName.length() > 0) {
			// 获取要删除的音频文件
			delFileName = mAdapter.clickItemName; // + ".mp3";
			Log.d(TAG, "当前要删除的文件名称为：" + delFileName + ".mp3");

			// 保存当前播放项
			musicNamePlay = mAdapter.selectItemName;
			if (musicNamePlay.equals(delFileName)) {
				Log.d(TAG, "当前删除歌曲为当前选择歌曲!");
				musicNamePlay = "";
			}

			// 根据名称，查找相应的位置
			int location = listNamesSave.indexOf(delFileName);
			Log.d(TAG, "索引位置为：" + location);
			if (location >= 0) {
				// 查找到后删除相应记录
				listNamesSave.remove(location);
				listTimesSave.remove(location);

				// 判断当前删除的是否为当前播放
				if (mAdapter.clickItemName.equals(mAdapter.selectItemName)) {
					// 将当前播放歌曲删除
					mAdapter.selectItemName = ""; // 取消歌曲高亮
				}

				// 将单击选项移至下一首，要判断是否是最后一首
				if (listNamesSave.size() > 0) {
					// 当前删除是最后一首
					if (location == listNamesSave.size()) {
						// 移至第一首
						musicNameNext = listNamesSave.get(0);

					} else if (location < listNamesSave.size()) {
						// 移至下一首
						musicNameNext = listNamesSave.get(location);
					}

				} else {
					musicNameNext = "";
				}

				// 更新序号列表
				int position, listSize;

				// 根据需要初始化listNumbersSave
				listSize = listNamesSave.size();
				listNumbersSave.clear(); // 清空
				for (position = 1; position <= listSize; position++) {
					listNumbersSave.add(Integer.toString(position));
				}

			} else {
				musicNameNext = ""; // 查找不到，置空处理
			}

			// 新建适配器，更新显示
			mAdapter = new musicListAdapter(this, listNumbersSave,
					listNamesSave, listTimesSave);
			musicListView.setAdapter(mAdapter);

			// 刷新或保持显示
			// 当前歌曲删除后,单击选项指向下一首
			mAdapter.clickItemName = musicNameNext;
			mAdapter.selectItemName = musicNamePlay;
			mAdapter.notifyDataSetInvalidated();
			// mAdapter.notifyDataSetChanged();

			// 将当前视图转到下一首单击的歌曲
			int positionView = listNamesSave.indexOf(mAdapter.clickItemName);
			if (positionView >= 0) {
				// 获取当前显示最前、最后一个项数
				int lastPosition = musicListView.getLastVisiblePosition();
				int firstPositon = musicListView.getFirstVisiblePosition();

				// 判断是否在视图内
				if ((positionView > lastPosition)
						|| (positionView < firstPositon)) {
					// 不是，则将选项置为当前视图
					musicListView.setSelection(positionView);
				}
			}

		} else {
			displayTip("当前没有选中的文件...");
		}
	}

	// 时间字符化
	public String toTime(int time) {
		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	// 进度条处理
	class songMapToSeekBar extends Thread {
		int position;
		boolean shouldContinue;
		boolean shouldRun = true;

		public songMapToSeekBar() {
			shouldContinue = true;
		}

		@Override
		public void run() {
			// super.run();
			while (shouldRun) {
				if (musicMediaPlayer.isPlaying() && shouldContinue) {
					// 获得当前播放的进度值
					seekBarPlayProcess.setProgress(musicMediaPlayer
							.getCurrentPosition());
					position = musicMediaPlayer.getCurrentPosition();
					songPlayTimeUpdate(position); // 更新进度条
				}

				try {
					ThreadRunFlag = true;
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e(TAG, e.toString());
				}
			}

			ThreadRunFlag = false; // 线程结束标识
		}
	}

	// 歌曲播放时间更新
	private void songPlayTimeUpdate(int value) {
		int songTimeMinute;
		int songTimeSecond;

		// 将ms-->s,分离分秒并显示
		value = value / 1000;
		songTimeMinute = value / 60; // 取得分
		songTimeSecond = value % 60; // 取得秒

		Message m = new Message();
		m.what = MSG_PLAYTIME;
		m.arg1 = songTimeMinute;
		m.arg2 = songTimeSecond;
		SmartCarMusic.this.mHandler.sendMessage(m);

		if (findLrcFlag == true) {
			Message m2 = new Message();
			m2.what = MSG_LRCPOS;
			m2.arg1 = value;
			SmartCarMusic.this.mHandler.sendMessage(m2);
		}
	}

	// 判断文件是否为UTF8格式
	private static boolean isUft8File(String fileName) {
		boolean result = false;

		java.io.File f = new java.io.File(fileName);

		try {
			java.io.InputStream ios = new java.io.FileInputStream(f);

			byte[] b = new byte[3];
			ios.read(b);
			ios.close();
			if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
				Log.i(TAG, f.getName() + ": 编码为UTF-8!");
				result = true;
			} else {
				Log.i(TAG, f.getName() + ": 可能是GBK!");
				result = false;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		return result;
	}

	// 显示提示信息
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// 输出当前函数名称
	private void putFuncationName(String name) {
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{ startX=event.getX();}
		else  if(event.getAction()==MotionEvent.ACTION_UP)
		      {
			         endX=event.getX();
		                if(endX<startX) {animation1();}
		             else if(endX>startX) {animation2();}
		     
              }		           		           
		return super.onTouchEvent(event);
	}
}
