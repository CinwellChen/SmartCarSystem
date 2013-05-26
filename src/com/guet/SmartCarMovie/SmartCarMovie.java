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

//������Ϣϵͳ��2���֣���Ƶ������
public class SmartCarMovie extends Activity
{
	private MediaPlayer movieMediaPlayer;
	boolean movieIsPlay = false;
	boolean voiceOpen = true;
	boolean nextPlayFlag = false;
	private static String[] multiSelectitems1 = null;	// ѡ������
	private static String[] multiSelectitems2 = null;	// ѡ������
	private static String[] multiSelectitems3 = null;	// ѡ������
	private static String[] multiSelectitems4 = null;	// ѡ������
	
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
	
	private static ImageView btnPlayStop; // ��������ͣ
	private static Button btnViewChoice; // ����ѡ��(��С����ȫ�����ر�)
	private static Button btnStypeChoice; // ģʽѡ��(����Ƶ���š�˳��ѭ�����������)
	private static Button btnDelChoice; // ɾ��ѡ��(��ǰ���š���ǰѡ��ȫ��)
	private static Button btnAddChoice; // ���ѡ��(�ļ����ļ���)
	
	private static ImageView btnReset; // ��λ��ֹͣ
	private static ImageView fullScreenChange; // ȫ���л�
	private static ImageButton btnVoice; // ����
	private static SeekBar seekBarVoice; // ����������
	private static SeekBar seekBarPlayProcess; // ��Ƶ������
	private static ListView movieListView;	//��Ƶ�б�������
	private static TextView playTime; // ��ǰ����ʱ��
	private static TextView durationTime; // �ܲ���ʱ��
	private static TextView textMovieState;	//��ǰ��Ƶ����״̬
	private static TextView textMoviePlayStype;	//��Ƶ����ģʽ
	
	protected static final int FILE_RESULT_CODE = 2;
	private Handler mHandler; // �����߳�ˢ��UIʹ��
	private SurfaceView surfaceView;	//��Ƶ����
	private SurfaceView surfaceViewFull;

	private AudioManager audioManager; // ����������
	private int soundVolume = 0; 	// ��������
	private int soundMaxVolume = 0; // �������ֵ
	private int soundSaveVolume = 0; // ����ʱ���������ֵ
	private int seekbarNum = 0; // ����������ֵ
	private int moviePosition; // ͻ���¼��������
	static int moviePlayType = 0; // ����ģʽ�л�
	
	// ��Ƶ�б����ݿ⣬������ʾӳ���
	private List<String> listNamesSave; // ��Ƶ�б�����
	private List<String> listTimesSave; // ��Ƶ�б�ʱ��

	MovieServiceApp myMovieListDb; // ��Ƶ���ݿ�
	movieListAdapter mAdapter; // ��ʾ������
	private int currentListItem; // ��ǰ������Ƶ������
	private String currentListPath = null;	//��ǰ�����ļ�ȫ·��
	

	private int currentListItemSave; // ��ǰѡ����
	private String selectItemNameSave; // ����ѡ�е�ѡ������
	private String clickItemNameSave; // ����ѡ�е�ѡ������

	protected static final String TAG = "Car";
	protected static final int MSG_PLAYTIME = 0;
	private final static int TIME = 6868;
	private final static int HIDE_CONTROLER = 1;
	
	movieMapToSeekBar movieThread1; // ���߳�1
	
	// ����ļ�ʱ���Ƿ񲥷���Ƶ
	boolean ThreadRunFlag = false; // ���ڵȴ��߳̽�����ʶ
	private boolean surfaceViewSavaFlag = false;	//���ڼ�¼�ָ�
	private boolean mediaPlayerIsPause = true;		//�ж��Ƿ�Ϊ��ͣ	
	
	protected static final String RESULT_ITEM = "result_item"; // ���ؽ�����ʶ
	protected static final String RESULT_STYPE = "result_stype"; // ���ؽ�����ͱ�ʶ
	protected static final String DISPLAY_AUDIO = "video";
	private static final String FILTER_STYPE = "filter_stype";		//�û��������ͱ�ʶ
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";	
	
	private View controlView = null;		//����������
	private PopupWindow controlWndow = null;
		
	private boolean isControllerShow = false;	//�Ƿ���ʾ��������ȫ����ʶ	
	private boolean isFullScreen = false;
	private boolean isFullBack = false;			//ȫ�����ر�ʶ
	private static int changeToFullPosition=0;	//ȫ���л����ڱ���ϵ�
	
	private View normalViewSave = null;
	private View fullViewSave = null;	
	private GestureDetector mGestureDetector = null;	//����ʶ��	

	
	//ϵͳ������������
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//������������,��ʼ������
		normalViewSave = getLayoutInflater().inflate(R.layout.moviehome2, null);
		fullViewSave = getLayoutInflater().inflate(R.layout.surfacefull, null);
		setContentView(normalViewSave);
		
		//"onResume()"�����������,���ڳ�ʼʱ��ʾ��������
		/*Looper.myQueue().addIdleHandler(new IdleHandler()
		{
			@Override
			public boolean queueIdle() 
			{	
				if(controlView != null )	//�ؼ����ڲ�Ϊ�գ�����ʾ
				{
					if( true )	//��AVD����������ʾ
						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
					else		//����ʵ����������ʾ(7����ʾ��)
						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
			     
					putFuncationName("queueIdle");
		        	isControllerShow = true;
				}
				return false;  
			}
        });	*/
	
		initMovieComponent(); // ��ʼ��GUIԪ��
		initMovieVariable(); // ��ʼ��ϵͳ����
		ReadSharedPreferences(); // ��ȡϵͳ���ò���
		initMovieObject(); // ��ʼ��ϵͳһЩ����Ķ��󣬽���Ӵ洢�ռ��ж���
		initMovieListView(); // ��ʼ�������б�
		movieListViewSetListener();	//��ʾ�б��¼�����		
		
		// ͨ��getStream/MaxVolume��õ�ǰ������С,��Ƶ�����������С,������seek���������ֵ
		soundVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundMaxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		seekBarVoice.setMax(soundMaxVolume);
		// �ѵ�ǰ������ֵ���ø�������
		seekBarVoice.setProgress(soundVolume);
	
		// ����һ��"Handler"���ڽ�����Ϣ
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case MSG_PLAYTIME:
					{
						// ��õ�ǰ���ŵĽ���ֵ
						int sekPosition = movieMediaPlayer.getCurrentPosition();
						//Log.d(TAG,"thread_positon=" + sekPosition);
						seekBarPlayProcess.setProgress(sekPosition);
						// ������ʾʱ��		
						playTime.setText( toTime(sekPosition));
					}
						break;
					
					//�����ӳ���������ʾ
					case HIDE_CONTROLER:
					{
						if( isFullScreen )	//����ȫ��
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

		// ��������Ƶ�������߳�
		movieThread1 = new movieMapToSeekBar();
		movieThread1.start();
		ThreadRunFlag = true;	

		// ��Ƶ��Ƶ�������϶�
		seekBarPlayProcess.setOnSeekBarChangeListener( seekbarPlayListener );
		// ����������¼�����
		movieMediaPlayer.setOnCompletionListener( completionPlayListener );
	
		//���ƶ��������¼�
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
		// ���ϵ�д��ڼ���ļ�
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile.exists())
		{
			// �ļ������ڣ�֤����ҳ�ѹرգ���ʾ������ҳ���ܷ���
			finish(); // �˳�
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

	// ���е绰��ʱ,����ļ�ʱ������
	@Override
	protected void onPause()
	{
		// ��ǰ������������������ļ���������ҳ 3�������ִ��ֹͣ
		if( currentListPath != null)
		{
			if (movieMediaPlayer.isPlaying() || mediaPlayerIsPause )					
			{			
				// ���浱ǰ���ŵ�λ��
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
	
	// ����绰��,����ļ�������
	@Override
	protected void onResume()
	{		
		super.onResume();
		putFuncationName("onResume");
	}

	// ���´����󣬱����ڴ�Խ�ϵͳ�Ὣֹͣ����ͣActivityɱ�������ٻָ�
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		Log.w(TAG, "onRestoreInstanceState()!");
	}

	// ��δ֪�¼�����ʱ����������(ϵ�л���ϵͳӲ��)����ϵͳ�ڴ���ţ���ACTIVITY�ᱻɱ��
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		Log.w(TAG, "onSaveInstanceState()!");
	}
	
	// ��ʼ��GUI�ؼ�
	private void initMovieComponent()
	{
		vfmovie = (ViewFlipper)findViewById(R.id.movievp);
		in_righttoleft = AnimationUtils.loadAnimation(this,R.anim.enter_righttoleft);
		out_righttoleft = AnimationUtils.loadAnimation(this,R.anim.out_righttoleft);
		in_lefttoright = AnimationUtils.loadAnimation(this,R.anim.enter_lefttoright);
		out_lefttoright = AnimationUtils.loadAnimation(this,R.anim.out_lefttoright);
		enterMovie = (Button)findViewById(R.id.entermovie);
		enterList = (Button)findViewById(R.id.enterlist);
		//��������ȫ��ID����
		controlView = getLayoutInflater().inflate(R.layout.control, null);
		controlWndow =   new PopupWindow(controlView, 
						LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);		

		// 1���ҿ���ID*************************************************		
		//��������
		btnReset = (ImageView) controlView.findViewById(R.id.ImageViewReset);
		btnPlayStop = (ImageView) controlView.findViewById(R.id.imageViewPlayStop);
		btnVoice = (ImageButton)findViewById(R.id.imageButtonVoice);
		fullScreenChange = (ImageView) controlView.findViewById(R.id.imageViewFullChange);
		
		//���ؼ���
		seekBarVoice = (SeekBar) findViewById(R.id.seekBarVoice);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		movieListView = (ListView) findViewById(R.id.listViewMovie);
		seekBarPlayProcess = (SeekBar) controlView.findViewById(R.id.seekBarPlay);
		
		//ʱ�����ʾ��
		textMovieState = (TextView) controlView.findViewById(R.id.textMovieState);
		textMoviePlayStype = (TextView) controlView.findViewById(R.id.textMoviePlayStype);
		playTime = (TextView) controlView.findViewById(R.id.moviePlayTime);
		durationTime = (TextView)controlView. findViewById(R.id.movieDurationTime);
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);		
		//surfaceViewFull = (SurfaceView)fullView.findViewById(R.id.surfaceViewFull);		
		
		//ѡ����
		btnAddChoice = (Button) findViewById(R.id.movieBtnAddFiles1);
		btnAddChoice = (Button) findViewById(R.id.movieBtnAddFiles1);
		btnDelChoice = (Button) findViewById(R.id.movieBtnDelFiles1);
		btnStypeChoice = (Button) findViewById(R.id.movieBtnPlayStype1);
		btnViewChoice = (Button) findViewById(R.id.movieBtnViewStype1);
		
		/////////////
		enterMovie.setOnClickListener(movieClickListener);
		enterList.setOnClickListener(movieClickListener);
		// 2��������¼�*************************************************
		btnReset.setOnClickListener(movieClickListener);
		btnPlayStop.setOnClickListener(movieClickListener);
		btnVoice.setOnClickListener(movieClickListener);
		fullScreenChange.setOnClickListener(movieClickListener);
	
		//�¼������¼�		
		btnAddChoice.setOnClickListener(movieChoiceListener);
		btnDelChoice.setOnClickListener(movieChoiceListener);
		btnStypeChoice.setOnClickListener(movieChoiceListener);
		btnViewChoice.setOnClickListener(movieChoiceListener);

		// �����Ƶ�ļ�
		seekBarVoice.setOnSeekBarChangeListener(voiceChangeListener);
	}

	// ��ʼ��ϵͳ����
	private void initMovieVariable()
	{
		// ��ʼ����������
		movieMediaPlayer = new MediaPlayer(); // ��Ƶ�ļ���������
		// currentListItem = 0; // ��ǰ������Ƶ��ų�ʼΪ0
		moviePlayType = 1; // ��ʼΪ˳�򲥷�ģʽ
		playTime.setText(""); 	// ����ʱ�����ʱ����ʾ��ʼ��
		durationTime.setText("");
		seekBarPlayProcess.setEnabled(false); // ��ֹ�϶�������
		btnReset.setEnabled(false); // ��λ��ʼ��ֹ
		textMoviePlayStype.setText("˳�򲥷�");
		
		// ��Ƶ��ʵ��
		listNamesSave = new ArrayList<String>();
		listTimesSave = new ArrayList<String>();
		
		multiSelectitems1 = getResources().getStringArray(R.array.addFileStype); 
		multiSelectitems2 = getResources().getStringArray(R.array.delFileStype); 
		multiSelectitems3 = getResources().getStringArray(R.array.moviePlayStype); 
		multiSelectitems4 = getResources().getStringArray(R.array.movieViewStype); 

		//��Ƶ��ʾ����
        surfaceView.getHolder().setFixedSize(320,240);//���÷ֱ���
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback()); 
	}

	// ��ʼ��ϵͳһЩ����Ķ��󣬽���Ӵ洢�ռ��ж���
	private void initMovieObject()
	{
		// **********************************************************
		// 1��ʼ����Ƶ�б����ݿ�
		try
		{
			Log.d(TAG, "�������ݿ�:");
			myMovieListDb = new MovieServiceApp(this);
			Log.d(TAG, "  �ɹ���");
			
			Log.d(TAG, "��ʼ���洢���е���Ƶ����!");			
			initMovieDbData();			

		} catch (Throwable e1)
		{
			Log.e(TAG, e1.toString());
			Log.d(TAG, "  ʧ�ܣ�");
		}
		
		// 2��Ƶ�б�-����"���ݿ�"����
		File nameFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListNames.db");
		if (!nameFile.exists()) // ���ļ������ڣ��򴴽�
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
		// 3��Ƶ�б�-ʱ��"���ݿ�"����
		File timeFile = new File(""
				+ "/mnt/sdcard/SmartCarSystem/SmartCarMovie-ListTimes.db");
		if (!timeFile.exists()) // ���ļ������ڣ��򴴽�
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

	// ��ʼ����Ƶ���ݿ�
	void initMovieDbData() throws Throwable
	{
		Cursor cursorTemp;

		// ��ȡ�ⲿ�洢����������Ƶ�ļ�
		cursorTemp = this.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				// ����ַ��������ʾҪ��ѯ����
				new String[]
				{ 
						MediaStore.Video.Media.DISPLAY_NAME,// 0
						// ��Ƶ�ļ���
						MediaStore.Video.Media.TITLE, // 1						
						// ��Ƶ������
						MediaStore.Audio.Media.DATA, // 2
						// ��Ƶ�ļ���·��
						MediaStore.Audio.Media.DURATION, // 3
						// ��Ƶ����ʱ��

				}, null, // ��ѯ�������൱��sql�е�where���
				null, // ��ѯ������ʹ�õ�������
				null // ��ѯ���������ʽ
				);

		// �����ҵ�����Ƶ���ݣ���ӵ����ݿ���
		if (cursorTemp != null)
		{
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr;

			// ��Cursor�з������������ListView����ʾ
			for (int i = 0; i < cursorTemp.getCount(); i++)
			{
				cursorTemp.moveToPosition(i);

				// �ȶ�ȡ��Ƶ����
				nameStr = cursorTemp.getString(0);

				// ������Ƶ�ļ�����
				String movieFilter = nameStr.substring(nameStr.length() - 3);
				Log.d(TAG,movieFilter);
				if( movieFilter.equals("3gp") || movieFilter.equals("mp4") 
						|| movieFilter.equals(".rm")|| movieFilter.equals("mvb")
						|| movieFilter.equals("avi") || movieFilter.equals("wmv")
						||movieFilter.equals("MP4") )
				{
					
				}	
				else {
					Log.d(TAG, "ϵͳ�Ը��ļ�: " + nameStr + "�ݲ�֧��!");
					continue;
				}
				Log.d(TAG, "��Ƶɨ���ļ�: " + nameStr );

				// �鿴���ݿ��Ƿ����
				myMovie movie1 = new myMovie();
				movie1 = myMovieListDb.find(nameStr);
				if (movie1 == null)
				{
					// �������ڣ������
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);					

					myMovie movie2 = new myMovie(); // ����NEWһ��������Ϊǰ��ΪNULL��ֵ
					movie2.Set( nameStr, titleStr, pathStr, timeStr );
					myMovieListDb.save(movie2);
					addCount++;
				}
			}
			cursorTemp.moveToFirst(); // �������λ
			cursorTemp.close(); // �رգ��ͷ���Դ

			if (addCount > 0)
			{
				Log.d(TAG, "��ʼ����Ƶ���ݿ⣺����" + addCount + "������Ƶ�ļ�...");
			} else
			{
				Log.d(TAG, "��ʼ����Ƶ���ݿ⣺δ��������Ƶ�ļ�...");
			}
		}		
	}
	// ��ʼ����Ƶ�����б�
	private void initMovieListView()
	{
		int position, listSize;

		// �½���������������ʾ
		mAdapter = new movieListAdapter(this, listNamesSave,
				listTimesSave);
		movieListView.setAdapter(mAdapter);

		// ˢ��ѡ��
		currentListItem = currentListItemSave;
		mAdapter.clickItemName = clickItemNameSave;
		mAdapter.selectItemName = selectItemNameSave;
		mAdapter.notifyDataSetInvalidated();

		listViewFlush(); // ˢ����ʾ
	}
	
	// ϵͳ���ò�����ȡ
	void ReadSharedPreferences()
	{
		SharedPreferences user = getSharedPreferences("user_info_movie",
				Activity.MODE_PRIVATE);
	
		currentListItemSave = user.getInt("currentListItemSave", -1);
		selectItemNameSave = user.getString("selectItemNameSave", "");
		clickItemNameSave = user.getString("clickItemNameSave", "");
	}

	// ϵͳ���ò�������
	void WriteSharedPreferences()
	{
		SharedPreferences user = getSharedPreferences("user_info_movie",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = user.edit();
	
		// ��ȡ����
		currentListItemSave = currentListItem;
		selectItemNameSave = mAdapter.selectItemName;
		clickItemNameSave = mAdapter.clickItemName;
		// ����
		editor.putInt("currentListItemSave", currentListItem);
		editor.putString("selectItemNameSave", selectItemNameSave);
		editor.putString("clickItemNameSave", clickItemNameSave);

		editor.commit();
	}

	// Acitity��Destroy�¼�
	@Override
	protected void onDestroy()
	{
		//�������ڴ���
		if(controlWndow.isShowing())
		{
			controlWndow.dismiss();
		}
		
		//�Ƴ���Ϣ
		mHandler.removeMessages(MSG_PLAYTIME);
		mHandler.removeMessages(HIDE_CONTROLER);
		
		// �ر�music���ţ�ʱ����ʾ�߳�
		movieThread1.shouldRun = false;
		while (ThreadRunFlag == true);

		// if( movieIsPlay == true )
		if (movieMediaPlayer.isPlaying())
		{ // �����ڲ���
			movieMediaPlayer.stop(); // ֹͣ
		}
		
		movieMediaPlayer.reset(); // ��λ
		movieMediaPlayer.release(); // �ͷ�ռ���豸		

		// �ر����ݿ�
		Log.d(TAG, "���ڹر����ݿ�...");
		myMovieListDb.closeDb(); // �ر����ݿ�
	
		saveMusicObject(); // ����һЩϵͳ����
		WriteSharedPreferences(); // ����ϵͳ���ò���
		System.gc(); // ��ȷ�ͷ��ڴ�

		super.onDestroy();
		putFuncationName("onDestroy");
	}

	// ����ϵͳһЩ����
	private void saveMusicObject()
	{
		// *********************************************************
		// 1����MovieList Name����������
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
		// 2����MovieList Time����������
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

	// ��дonActivityResult,���������Ƶ�ļ�ʱ��������
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// super.onActivityResult(requestCode, resultCode, data);
		if (FILE_RESULT_CODE == requestCode)
		{
			Bundle bundle = null;

			// ���ؽ�����������ͱ�������
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
					Log.d(TAG, "�ļ�(��)���(�������): ");
					// ������ӵ��ļ����ļ���
					for (int i = 0; i < dataSize; i++)
					{
						Log.d(TAG, "[" + i + "]=" + returnSelectItems.get(i));
					}

					if (resturnStype.equals(DISPLAY_FILES))
					{
						// ��Ϊ�ļ���ӣ�����任
						returnFileItems = returnSelectItems;
					} else if (resturnStype.equals(DISPLAY_FOLDERS))
					{
						// ��Ϊ�ļ�����ӣ�������ļ��н���ɨ����ļ�
						// �����ļ����б������ļ��б�,���ļ��б�����0�򷵻�NULL
						returnFileItems = fileScanFromFolders(returnSelectItems);

						if (returnFileItems == null)
						{
							Log.d(TAG, "�ļ�(��)ɨ����Ϊ�գ�");
							return;
						} else
						{
							Log.d(TAG, "�ļ�(��)ɨ�������£�");
							dataSize = returnFileItems.size();
							for (int j = 0; j < dataSize; j++)
							{
								Log.d(TAG, "[" + j + "]="
										+ returnFileItems.get(j));
							}
						}
					}

					// �����ļ�(��)��Ӻ���
					try
					{
						doAddMovieFile(returnFileItems);
					} catch (Throwable e)
					{
						Log.d(TAG, "�����ļ�(��)��Ӻ���: ʧ�ܣ�");
						Log.d(TAG, e.toString());
					}
				} else
				{
					Log.d(TAG, "��ǰû��Ҫɨ����ļ��л���ӵ��ļ���");
				}

			}
		}
	}
	
	
	//�����¼�����
	private void gestureDetectorListener()
	{
		//����ʶ��
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

	//��ʾ�б��¼�����
	private void movieListViewSetListener()
	{
		// ��Ƶ�б����¼�����
		movieListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			// ��ѡ����Ƶʱ��������Ƶ����
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				currentListItem = position;
				mAdapter.selectItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();

				String currentMovieName = mAdapter.selectItemName;	
				Log.i(TAG, "currentMovieName=" + currentMovieName);
//				if(controlView != null )	//�ؼ����ڲ�Ϊ�գ�����ʾ
//				{
//					if( true )	//��AVD����������ʾ
//						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
//					else		//����ʵ����������ʾ(7����ʾ��)
//						controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
//			     
//					putFuncationName("queueIdle");
//		        	isControllerShow = true;
//				}
//				// ������Ƶ���Ŵ������
				animation1();
				showController();
				surfaceView.setVisibility(View.VISIBLE);
				playMovie(currentMovieName);
				return true; // ����true Click���ᱻ����
			}
		});

		//�����¼���ʾ
		movieListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// ������ʾ����
				mAdapter.clickItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();
			}
		});
	}
	
	//��Ƶ1��ʾ�ص�����
	private final class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder) 
		{
			movieThread1.shouldContinue = false;	//��������ʾ����
			
			if( surfaceViewSavaFlag == true && isFullScreen == false)
			{
				if ((moviePosition > 0) && (mAdapter.selectItemName != null))			
				{
					//Log.i(TAG, "moviePosition = " + moviePosition);			
					try
					{
						movieMediaPlayer.reset();//����Ϊ��ʼ״̬
						
						/* ����VideoӰƬ��SurfaceHolder���� */
						movieMediaPlayer.setDisplay(surfaceView.getHolder());
						movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						movieMediaPlayer.setDataSource(currentListPath);
						movieMediaPlayer.prepare();//����	
						movieMediaPlayer.start();//����
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
			
			//��ȫ������
			if( isFullBack == true && isFullScreen == true )
			{									
				try
				{	
					movieMediaPlayer.reset();
					movieMediaPlayer.setDisplay(surfaceView.getHolder());
					movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					
					if( movieIsPlay || mediaPlayerIsPause )	//������ʾ����ͣ���򷵻�ʱ����
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
						//��ͣ
					}
					
					showController();				
					isFullBack = false;
					isFullScreen = false;
					fullScreenChange.setEnabled(true);
					Log.d(TAG,"ȫ�����سɹ�...");
					movieThread1.shouldContinue = true;	//��������ʾ����
				} 
				 catch (IOException e)
				{					
					e.printStackTrace();
					Log.e(TAG,"ȫ������ʧ��!!!");
				}
			}
			
			movieThread1.shouldContinue = true;	//��������ʾ����	
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
	
	//��Ƶ2��ʾ�ص�����
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
						movieMediaPlayer.reset();//����Ϊ��ʼ״̬
						
						/* ����VideoӰƬ��SurfaceHolder���� */
						movieMediaPlayer.setDisplay(surfaceViewFull.getHolder());
						movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						movieMediaPlayer.setDataSource(currentListPath);
						movieMediaPlayer.prepare();//����	
						movieMediaPlayer.start();//����
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
				//ȫ���л�
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
					
					Log.d(TAG, "ȫ���л��ɹ�...");
					fullScreenChange.setEnabled(true);
					
				} 
				 catch (IOException e)
				{					
					e.printStackTrace();
					Log.d(TAG,"ȫ���л�ʧ��!!!");
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
		
	// �������Ƹı��¼�
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

		// ��ֵ�仯�¼�
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser)
		{
			seekbarNum = seekBarVoice.getProgress();

			// ֱ�Ӹı�
			if (voiceOpen == true)
			{
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						seekbarNum, AudioManager.FLAG_PLAY_SOUND);
			} else
			{
				// ����ģʽ
				soundSaveVolume = seekbarNum; // ֻ���治����
			}

			soundVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (soundVolume == 0)
			{
				// ����
				btnVoice.setImageResource(R.drawable.movievoiceclose);
			} else
			{
				// �Ǿ���
				btnVoice.setImageResource(R.drawable.movievoiceopen);
			}
		}
	};
	
	// �����ť���������¼�
	private OnClickListener movieClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				switch (v.getId())
				{
				
				//ȫ���л�
				case R.id.imageViewFullChange:
				{
					changeToFullScreen();
				} break;
				
				case R.id.ImageViewReset: // ��λ
				{
					movieMediaPlayer.stop(); // ֹͣ
					movieIsPlay = false;
					seekBarPlayProcess.setProgress(0);
					if (seekBarPlayProcess.isEnabled())
					{
						seekBarPlayProcess.setEnabled(false); // ֹͣʱ��ֹ�϶�������
					}
					playTime.setText("00:00:00");
					textMovieState.setText("ֹͣ");
					btnPlayStop.setImageResource(R.drawable.musicplaystart);
				}
					break;
					
				case R.id.imageViewPlayStop: // ���Ż�ֹͣ
				{
					if (movieMediaPlayer.isPlaying())
					{
						// ��"����"��"��ͣ",����Ϊ��ͣͼ��
						movieMediaPlayer.pause();
						textMovieState.setText("��ͣ");
						mediaPlayerIsPause = true;
						btnPlayStop.setImageResource(R.drawable.musicplaystart);
					} else
					{
						if (listNamesSave.size() > 0)
						{
							if (movieIsPlay == false)
							{
								// ���Σ�Ҫ��ʼ������Դ
								// ��"��ͣ"��"����",����Ϊ����ͼ��
								if (currentListItem == -1)
									currentListItem = 0; // ��Ե�һ������

								String playName = listNamesSave.get(currentListItem);										
								
								if (playName != null)
								{
									playMovie(playName);
									currentMovieChange();
								}
							} else
							{
								movieMediaPlayer.start();						
								textMovieState.setText("������");
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

				case R.id.imageButtonVoice: // �������ƿ���
				{
					if (voiceOpen == true)
					{
						// �ر�����,����ģʽ
						btnVoice.setImageResource(R.drawable.movievoiceclose);
						// ���澲��ֵ,����Ƶ������Ϊ0
						soundSaveVolume = audioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC);
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								0, AudioManager.FLAG_PLAY_SOUND);
						voiceOpen = false;
					} else
					{
						// ������Ϊ0�����ʱ����Ч
						if (soundSaveVolume != 0)
						{
							// ������,����ģʽ
							btnVoice
									.setImageResource(R.drawable.movievoiceopen);
							// �ָ�����ֵ
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
				// ����쳣��Ϣ
				Log.e(TAG, e.toString());
			}
		}

	};
	
	//��Ƶ������������¼�
	private OnCompletionListener completionPlayListener = new OnCompletionListener()
	{
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			Log.d(TAG,"����������¼�....");
			// ��ǰ��Ƶ��������¼�
			switch (moviePlayType)
			{
			case 1: // "˳�򲥷�"ģʽ
			{
				// ��ת����һ�ײ���
				nextMovie();
			}
				break;

			case 2: // "����ѭ��"ģʽ
			{
				currentListItem--;
				nextMovie();
			}
				break;

			case 3: // "�б�ѭ��"ģʽ
			{
				nextMovie();
			}
				break;

			case 4: // "���"ģʽ
			{
				// ��List�б���Ч��Χ������һ�������Ƶ,�ٽ��в���
				if (listNamesSave.size() == 0)
				{
					// û����Ƶ
					nextMovie(); // ����next�Ա�ֹͣ
				} else if (listNamesSave.size() == 1)
				{
					// ֻʣһ�׸�
					String playName = listNamesSave.get(0);
					if (playName != null)
					{
						playMovie(playName);
						currentMovieChange(); // ��ǰ��Ƶ����
					}
				} else
				{
					// ������Ƶ����
					int maxNumber = listNamesSave.size();
					int randomSongNumber;

					// ���������(˫�أ���ֹ�ظ���)����С��Χ 0-maxNumber
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
							currentMovieChange(); // ��ǰ��Ƶ����
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
	
	//��Ƶ���Ž������¼�����
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
				// �ж��Ƿ����϶����϶�ʱ�Ÿ���
				// ��ֹƽʱ��������
				if (movieThread1.shouldContinue == false)
				{
					// ��̬��ʾ��ǰ�϶�ʱ��
					playTime.setText(toTime(progress));
				}
			}
		}
	};
	
	// �����ť���������¼�
	private OnClickListener movieChoiceListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				//1�����Ƶ�ļ�ѡ��
				case R.id.movieBtnAddFiles1:
				{					
					showDialog(listAddStype); // ��ʾ��ѡ��ť�Ի���
				}					
					break;
				
				//2ɾ����Ƶ�ļ�ѡ��
				case R.id.movieBtnDelFiles1:
				{
					showDialog(listDelStype); // ��ʾ��ѡ��ť�Ի���
				}					
					break;
				
				//3��Ƶģʽ����ѡ��
				case R.id.movieBtnPlayStype1:
				{
					showDialog(listPlayStype); // ��ʾ��ѡ��ť�Ի���
				}					
					break;
				
				//4����ģʽѡ��	
				case R.id.movieBtnViewStype1:
				{					
					showDialog(listViewStype); // ��ʾ��ѡ��ť�Ի���
				}					
					break;
	
				default:
					break;					
			}
			
			
		}		
	};
	
	
	// ��дonCreateDialog����
	@Override
	protected Dialog onCreateDialog(final int id)
	{ 		
		Dialog dialog = null;
		String[] disItems = null;
		int width = 300;
		
		switch (id)
		{ 
			// ��id�����ж�			
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
			Builder b = new AlertDialog.Builder(this); 	// ����Builder����
			b.setItems(disItems,  new DialogInterface.OnClickListener()
			{					
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Log.d(TAG,"�㵱ǰѡ�����: "+ which);
					//displayTip("�㵱ǰѡ�����: "+ which);
					
					switch (id)
					{
						//����ļ�
						case listAddStype:
						{
							switch (which)
							{
								case 0:		//�Զ�ɨ��
								{
									try
									{
										scanFilesFromExt();
									} catch (Throwable e)
									{										
										Log.e(TAG,"�Զ�ɨ������ļ�ʧ��");
										Log.e(TAG,e.toString());
									}
								} break;
								
								case 1:		//���Ŀ¼
								{
									doAddFilesView(DISPLAY_FOLDERS);
								} break;
								
								case 2:		//����ļ�
								{
									doAddFilesView(DISPLAY_FILES);
								} break;
	
								default:
									break;
							}
						} break;
						
						//ɾ���ļ�
						case listDelStype:
						{
							switch (which)
							{
								case 0:		//��յ�ǰ�б�
								{
									doDelAllMovieFile(); 									
								} break;
								
								case 1:		//ɾ��ѡ���ļ�
								{
									doDelMusicFile();	
								} break;
	
								default:
									break;
							}
						} break;
						
						//����ģʽ
						case listPlayStype:
						{
							switch (which)
							{
								case 2: //����ѭ��
									moviePlayType = 2;
									textMoviePlayStype.setText("����ѭ��");
									break;
	
								case 1: //�б�ѭ��
									moviePlayType = 3;
									textMoviePlayStype.setText("�б�ѭ��");
									break;
	
								case 0: //�������
									moviePlayType = 4;
									textMoviePlayStype.setText("�������");
									break;
	
								case 3: //˳�򲥷�
									moviePlayType = 1; // ��λ
									textMoviePlayStype.setText("˳�򲥷�");
									break;
	
								default:
									break;
							}
						} break;
						
						//��������
						case listViewStype:
						{
							switch (which)
							{
								case 0:		//�˳��ر�
								{
									doExitWork();
								} break;
								
								case 1:		//������ҳ
								{
									returnHome();
								} break;
								
								case 2:		//ȫ������
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
	
			//�����Ե���ѡ�����������ʾ��ߡ�����
			dialog = b.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			//dialog.getWindow().setLayout(200, 300);
			
//			if( false )	//��AVD��
//				dialog.getWindow().setLayout(200, width);
//			else 		//�ڿ�������
//				dialog.getWindow().setLayout(200, 450);
//			
//			LayoutParams a = dialog.getWindow().getAttributes();
//			
//			if( false )	//��AVD��
//			{
//				a.x = 135;
//				a.y = -200;
//			}else {		//�ڿ�������
//				a.x = 250;
//				a.y = -200;
//			}
//			dialog.getWindow().setAttributes(a);
			
		}
		
		return dialog; // ����Dialog����
	}
	
	//�л���ȫ����ʾ
	private void changeToFullScreen()
	{	
		//ȫ����ʾ
		if( (isFullScreen == false) && movieMediaPlayer.isPlaying() )
		{
			fullScreenChange.setEnabled(false);
			
			//ֹͣ����ϵ㣬�ȴ��л�
			changeToFullPosition = movieMediaPlayer.getCurrentPosition();
			movieMediaPlayer.stop();	//ֹͣ
			
			setContentView(fullViewSave);
			
			if( surfaceViewFull == null )
			{
				surfaceViewFull = (SurfaceView)findViewById(R.id.surfaceViewFull);
				
				//���õ����¼�
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
				
				//����				
				surfaceViewFull.getHolder().setFixedSize(800,480);//���÷ֱ���
			    surfaceViewFull.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		        surfaceViewFull.getHolder().addCallback(new SurfaceCallbackFull());
			} 
			
			else 
			{
				Log.d(TAG, "surfaceViewFull�Ѵ���! ��Ҫ�ظ�����!");
			}
		  	Log.d(TAG,"ȫ����ʾ...");
		  	fullScreenChange.setImageResource(R.drawable.unfullscreennormal);
		  	isFullScreen = true;
		  	
		}else if( isFullScreen == true )
		{
			fullScreenChange.setEnabled(false);
			
			//ֹͣ����ϵ㣬�ȴ��л�
			changeToFullPosition = movieMediaPlayer.getCurrentPosition();
			movieMediaPlayer.stop();	//ֹͣ
		
			setContentView(normalViewSave);
		  	Log.d(TAG,"��׼��ʾ...");
		  	fullScreenChange.setImageResource(R.drawable.fullscreennormal);
		  	isFullBack = true;
		}
	}
	
	//��ʱ�ر�
	private void hideControllerDelay()
	{
		mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	//��ʾ������
	private void showController()
	{	
		if( true )	//��AVD����������ʾ
			controlWndow.showAtLocation(surfaceView, Gravity.LEFT, 0, +200);
		else		//����ʵ����������ʾ(7����ʾ��)
			controlWndow.showAtLocation(surfaceView, Gravity.LEFT, -100, +200);
       
       isControllerShow = true;
	}
	
	//��ʱ���ؿ�����
	private void cancelDelayHide()
	{
		mHandler.removeMessages(HIDE_CONTROLER);
	}
	
	//���ؿ�����
	private void hideController()
	{	
		controlWndow.dismiss();			
		isControllerShow = false;
	}

	// ˢ��listView��ǰ����ʾ
	private void listViewFlush()
	{
		int positionView = listNamesSave.indexOf(mAdapter.selectItemName);

		if (positionView >= 0)
		{
			// ��ȡ��ǰ��ʾ��ǰ�����һ������
			int lastPosition = movieListView.getLastVisiblePosition();
			int firstPositon = movieListView.getFirstVisiblePosition();

			// �ж��Ƿ�����ͼ��
			if ((positionView > lastPosition) || (positionView < firstPositon))
			{
				// ���ǣ���ѡ����Ϊ��ǰ��ͼ
				movieListView.setSelection(positionView);
			}
		}
	}		

	// ������Ƶ
	void playMovie(String movieName)
	{
		if (movieName == null)
			return; // ��ֹ��Ƶ����Ϊ��

		// ������Ƶ���ƣ������ݿ�����ȡ����
		myMovie movieTemp = new myMovie();
		try
		{
			movieTemp = myMovieListDb.find(movieName);
			if (movieTemp == null)
			{
				Log.d(TAG, "�����ݿ����޷����Ҹ���Ƶ��" + movieName + "!");
				Log.d(TAG, "�޷���λ����Ƶ�ļ�...");
				return;
			}
		} catch (Throwable e)
		{
			Log.d(TAG, e.toString());
			Log.d(TAG, "�����ݿ��в�����Ƶ -" + movieName + ": �쳣��");			
			return;
		}
		
		// 2׼����Ƶ��Ƶ����
		try
		{
			currentListPath = movieTemp.path;
			
			movieMediaPlayer.reset();
			//��ȫ��ģʽ
			if( isFullScreen == false )
			{	
				movieMediaPlayer.setDisplay(surfaceView.getHolder());
			}else 
			{
				//ȫ��ģʽ
				movieMediaPlayer.setDisplay(surfaceViewFull.getHolder());
			}
			movieMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			movieMediaPlayer.setDataSource(movieTemp.path);
			movieMediaPlayer.prepare();			
			movieMediaPlayer.start();
			
			// ������λ
			if (!btnReset.isEnabled())	btnReset.setEnabled(true);			
			
		} catch (IOException e)
		{			
			Log.d(TAG,"������Ƶ�ļ���" + movieName + "����!");
			Log.d(TAG,e.toString());
		}

		// �ɡ����š��л�����ֹͣ����ť
		textMovieState.setText("������");
		btnPlayStop.setImageResource(R.drawable.musicplaypause);
		movieIsPlay = true;
		mediaPlayerIsPause = false;
		
		if (!seekBarPlayProcess.isEnabled())
		{
			seekBarPlayProcess.setEnabled(true); // ����ʱʹ���϶�������
		}

		// ���½�����
		int currentMoviePosition = movieMediaPlayer.getDuration();
		seekBarPlayProcess.setMax(currentMoviePosition);	//bar��С��λΪms			
		durationTime.setText( "/ " + toTime(currentMoviePosition) );		
	}

	// ��һ����Ƶ
	void nextMovie()
	{
		// �ж���Ƶ�б��Ƿ���Ϊ��
		if (listNamesSave.size() == 0)
		{
			doNoMovieWork();
			return;
		}

		String playName;

		if (++currentListItem >= listNamesSave.size())
		{
			currentListItem = 0;

			// ��Ϊ"�б�ѭ��"ģʽ,�����ڲ������һ��ʱ�������һ��,���������
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
					// ��Ϊ˳�򲥷ţ���ֻ��һ�׸�ʱ��ȡ�����һ�׸�ѡ
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

		currentMovieChange(); // ��ǰ��Ƶ����
	}

	// ���ݵ�ǰ��Ƶ�ı䣬����
	private void currentMovieChange()
	{
		// ��Ƶ����"currentListItem"�б�ˢ��,����
		if (currentListItem < listNamesSave.size() && currentListItem >= 0)
		{
			mAdapter.selectItemName = listNamesSave.get(currentListItem);
			mAdapter.notifyDataSetInvalidated();

			listViewFlush(); // ˢ����ʾ
		}
	}	

	// ���ļ����б��У������ļ����������ļ��б�
	private List<String> fileScanFromFolders(List<String> Folders)
	{
		int i;
		List<String> movieTempList = new ArrayList<String>();

		for (i = 0; i < Folders.size(); i++)
		{
			String dirStr = Folders.get(i); // ���λ�ȡÿ����Ŀ¼
			if (dirStr != null)
			{
				File subDir = new File(dirStr);
				Log.d(TAG, "����Ҫɨ����ļ���Ϊ��" + subDir.getAbsolutePath());

				// ���ϵͳ�ؼ�Ŀ¼����Ӧɨ��,���кܶ࣬���洦����
				// ���Ҫ��ȷ��ЩĿ¼����ɨ��ģ�
				if (dirStr.equals("/mnt/secure") || dirStr.equals("/root")
						|| dirStr.equals("/data"))

				{
					Log.d(TAG, "ϵͳ�ؼ�Ŀ¼���Թ�ɨ�裡");
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

		// �����ļ��򷵻��б����򷵻�NULL
		if (movieTempList.size() > 0)
			return movieTempList;
		else
			return null;
	}

	// ���ⲿ�洢��SD����ɨ����Ƶ�ļ�
	void scanFilesFromExt() throws Throwable
	{
		Cursor cursorTemp;

		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		// ��ȡ�ⲿ�洢����������Ƶ�ļ�
		cursorTemp = this.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				// ����ַ��������ʾҪ��ѯ����
				new String[]
				{ 
						MediaStore.Video.Media.DISPLAY_NAME,// 0
						// ��Ƶ�ļ���
						MediaStore.Video.Media.TITLE, // 1						
						// ��Ƶ������
						MediaStore.Audio.Media.DATA, // 2
						// ��Ƶ�ļ���·��
						MediaStore.Audio.Media.DURATION, // 3
						// ��Ƶ����ʱ��

				}, null, // ��ѯ�������൱��sql�е�where���
				null, // ��ѯ������ʹ�õ�������
				null // ��ѯ���������ʽ
				);

		// �����ҵ�����Ƶ���ݣ���ӵ����ݿ���
		if (cursorTemp != null)
		{
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr;

			// ��Cursor�з������������ListView����ʾ
			for (int i = 0; i < cursorTemp.getCount(); i++)
			{
				cursorTemp.moveToPosition(i);

				// �ȶ�ȡ��Ƶ�ļ���
				nameStr = cursorTemp.getString(0);

				// ������Ƶ�ļ�����
				String movieFilter = nameStr.substring(nameStr.length() - 3);
				if( movieFilter.equals("3gp") || movieFilter.equals("mp4") 
						|| movieFilter.equals(".rm")|| movieFilter.equals("mvb")
						|| movieFilter.equals("avi") || movieFilter.equals("wmv") )
				{

					
				}					
				else {
					Log.d(TAG, "ϵͳ�Ը��ļ�: " + nameStr + "�ݲ�֧��!");
					continue;
				}			
				Log.d(TAG, "��Ƶɨ���ļ�: " + nameStr );

				// �鿴���ݿ��Ƿ����
				myMovie movie1 = new myMovie();
				movie1 = myMovieListDb.find(nameStr);
				if (movie1 == null)
				{
					// �������ڣ������
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					
					myMovie moive2 = new myMovie(); // ����NEWһ��������Ϊǰ��ΪNULL��ֵ

					//�������ݿ�
					moive2.Set(nameStr, titleStr, pathStr, timeStr );
					myMovieListDb.save(moive2);

				} else
				{
					// ����������ݿ�����

					// ��鵱ǰ��Ƶ�����б��Ƿ���ڣ������ڣ�����β�����
					if (listNamesSave.contains(nameStr) == false)
					{
						addCount++;
						timeStr = movie1.time; // �����ݿ��л�ȡʱ��

						// ������Ƶ�б�
						listNamesSave.add(nameStr);
						listTimesSave.add(timeStr);						

					} else
					{
						Log.d(TAG, "���ļ��������ڵ�ǰ�����б��������!");
					}
				}
			}
			cursorTemp.moveToFirst(); // �������λ
			cursorTemp.close(); // �رգ��ͷ���Դ

			// �����û���ӽ��
			if (addCount > 0)
			{
				// �½���������������ʾ
				mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
				movieListView.setAdapter(mAdapter);

				// ����֮ǰ����ʾ
				mAdapter.clickItemName = clickItemNameSave;
				mAdapter.selectItemName = selectItemNameSave;
				// mAdapter.notifyDataSetInvalidated();
				mAdapter.notifyDataSetChanged();

				displayTip("�ɹ����" + addCount + "��������Ƶ�ļ�...");
			} else
			{
				displayTip("û��Ҫ��ӵ���Ƶ...");
			}
		} else
		{
			Log.d(TAG, "�޷���ȡAndroid�ڲ�����Ƶ���ݿ�");
		}
		
	}
	
	//����ļ�
	private void doAddFilesView( String viewStype )
	{		 
		Intent intent = new Intent(SmartCarMovie.this,
				MyFileManager.class);
		// Newһ��Bundle���󣬲���Ҫ���ݵ����ݴ���,�����ļ������Ҫ����ʾ����Ƶ�ļ�
		Bundle bundle = new Bundle();
		bundle.putString(REQUEST_STYPE, viewStype);
		bundle.putString(FILTER_STYPE,DISPLAY_AUDIO);
		intent.putExtras(bundle);

		// �з��ؽ��
		startActivityForResult(intent, FILE_RESULT_CODE);
	}

	// �����Ƶ�ļ�(���ļ���)
	void doAddMovieFile(List<String> dataItem) throws Throwable
	{
		int i, size, addCount = 0, tempInt;
		String addFileName, addFilePath;
		String nameStr,titleStr, pathStr, timeStr;

		MediaPlayer tempPlayer = new MediaPlayer(); // ���ڻ�ȡ��Ƶ���ų���
		myMovie movieTemp = new myMovie();

		// ��ȡ�ɵ�ѡ��͵�����
		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		size = dataItem.size(); // ��ȡ������С

		Log.d(TAG, "����ΪҪ��ӵ��ļ���");

		// ���μ��ÿһ���ļ�
		for (i = 0; i < size; i++)
		{
			addFilePath = dataItem.get(i); // �ȵõ�������ȫ�����ƣ���/mnt/sdcard/x.3gp

			// �����ļ���ȫ���з����ļ���
			File fileTemp = new File(addFilePath);
			addFileName = fileTemp.getName();
			Log.d(TAG, "");
			Log.d(TAG, "[" + i + "]=" + addFileName);

			// 1�ȴ����ݿ��в����Ƿ����
			myMovie movie1 = new myMovie();
			movie1 = myMovieListDb.find(addFileName);
			Log.d(TAG, "1.�����ݿ��в���: ");

			if (movie1 != null)
			{
				Log.d(TAG, "  �ɹ�...");
				Log.d(TAG, "2.������º�̨���ݿ�...");

				// ֱ�������ݿ���ȡ����
				nameStr = addFileName; // 1�ļ���
				timeStr = movie1.time;
			} else
			{
				// �����ݿ��в����ڣ���Ը���Ƶ���ݽ��з�������ӵ����ݿ���
				Log.d(TAG, "  ʧ��...");
				Log.d(TAG, "2.���ڶԸ���Ƶ�ļ����н���...");
				
				nameStr = addFileName;
				titleStr = "ĳ֪";
				pathStr = addFilePath;	//����·����

				// ��ȡ��Ƶ�ĳ���
				tempPlayer.reset();
				tempPlayer.setDataSource(addFilePath);
				tempPlayer.prepare();
				// tempPlayer.stop();

				tempInt = tempPlayer.getDuration();
				timeStr = toTime(tempInt);
				// Log.d(TAG,"�������䲥��ʱ��Ϊ��" + timeStr);

				// ����һ��movie������
				movieTemp.Set(nameStr, titleStr, pathStr, timeStr );
				Log.d(TAG, "music: " + movieTemp.toString());

				Log.d(TAG, "3.������������ݿ�...");
				if (myMovieListDb.save(movieTemp))
				{
					Log.d(TAG, "  �ɹ���");
				} else
				{
					Log.d(TAG, "  ʧ�ܣ�");
				}
			}

			// ��鵱ǰ��Ƶ�����б��Ƿ���ڣ������ڣ�����β�����
			if (listNamesSave.contains(addFileName) == false)
			{
				addCount++;

				// ������Ƶ�б�
				listNamesSave.add(addFileName);
				listTimesSave.add(timeStr);

			} else
			{
				Log.d(TAG, "���ļ��������ڵ�ǰ�����б��������!");
			}
		}

		tempPlayer.release(); // �ͷ�Ӳ��

		// �����û���ӽ��
		if (addCount > 0)
		{
			// �½���������������ʾ
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			// ����֮ǰ����ʾ
			mAdapter.clickItemName = clickItemNameSave;
			mAdapter.selectItemName = selectItemNameSave;
			// mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();

			displayTip("�ɹ����" + addCount + "������Ƶ�ļ�...");
		} else
		{
			displayTip("û��Ҫ��ӵ�����Ƶ�ļ�...");
		}

	}

	// ����ɾ��������Ƶ�б���
	private void doDelAllMovieFile()
	{
		if (listNamesSave.size() > 0)
		{
			listNamesSave.clear(); // ����б�			
			listTimesSave.clear();

			// currentListItem = -1;
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			mAdapter.selectItemName = "";
			mAdapter.clickItemName = "";
			mAdapter.notifyDataSetInvalidated();
		} else
		{
			displayTip("��ǰ�б�Ϊ��!�������!");
		}
	}

	// ɾ��ĳһ����Ƶ�ļ�
	void doDelMusicFile()
	{
		String musicNamePlay = "", delFileName, musicNameNext = "";

		if (mAdapter.clickItemName.length() > 0)
		{
			// ��ȡҪɾ������Ƶ�ļ�
			delFileName = mAdapter.clickItemName; 
			Log.d(TAG, "��ǰҪɾ�����ļ�����Ϊ��" + delFileName);

			// ���浱ǰ������
			musicNamePlay = mAdapter.selectItemName;
			if (musicNamePlay.equals(delFileName))
			{
				Log.d(TAG, "��ǰɾ����ƵΪ��ǰѡ����Ƶ!");
				musicNamePlay = "";
			}

			// �������ƣ�������Ӧ��λ��
			int location = listNamesSave.indexOf(delFileName);
			Log.d(TAG, "����λ��Ϊ��" + location);
			if (location >= 0)
			{
				// ���ҵ���ɾ����Ӧ��¼
				listNamesSave.remove(location);
				listTimesSave.remove(location);

				// �жϵ�ǰɾ�����Ƿ�Ϊ��ǰ����
				if (mAdapter.clickItemName.equals(mAdapter.selectItemName))
				{
					// ����ǰ������Ƶɾ��
					mAdapter.selectItemName = ""; // ȡ����Ƶ����
				}

				// ������ѡ��������һ�ף�Ҫ�ж��Ƿ������һ��
				if (listNamesSave.size() > 0)
				{
					// ��ǰɾ�������һ��
					if (location == listNamesSave.size())
					{
						// ������һ��
						musicNameNext = listNamesSave.get(0);

					} else if (location < listNamesSave.size())
					{
						// ������һ��
						musicNameNext = listNamesSave.get(location);
					}

				} else
				{
					musicNameNext = "";
				}

			} else
			{
				musicNameNext = ""; // ���Ҳ������ÿմ���
			}

			// �½���������������ʾ
			mAdapter = new movieListAdapter(this,listNamesSave, listTimesSave);
			movieListView.setAdapter(mAdapter);

			// ˢ�»򱣳���ʾ
			// ��ǰ��Ƶɾ����,����ѡ��ָ����һ��
			mAdapter.clickItemName = musicNameNext;
			mAdapter.selectItemName = musicNamePlay;
			mAdapter.notifyDataSetInvalidated();
			// mAdapter.notifyDataSetChanged();

			// ����ǰ��ͼת����һ�׵�������Ƶ
			int positionView = listNamesSave.indexOf(mAdapter.clickItemName);
			if (positionView >= 0)
			{
				// ��ȡ��ǰ��ʾ��ǰ�����һ������
				int lastPosition = movieListView.getLastVisiblePosition();
				int firstPositon = movieListView.getFirstVisiblePosition();

				// �ж��Ƿ�����ͼ��
				if ((positionView > lastPosition)
						|| (positionView < firstPositon))
				{
					// ���ǣ���ѡ����Ϊ��ǰ��ͼ
					movieListView.setSelection(positionView);
				}
			}

		} else
		{
			displayTip("��ǰû��ѡ�е��ļ�...");
		}
	}
	
	// ִ��û����Ƶ���Ź���
	private void doNoMovieWork()
	{
		// �ж��Ƿ����ڲ���
		if (movieMediaPlayer.isPlaying())
		{
			movieMediaPlayer.stop();
		}

		seekBarPlayProcess.setProgress(0);
		if (seekBarPlayProcess.isEnabled())
		{
			seekBarPlayProcess.setEnabled(false); // ֹͣʱ��ֹ�϶�������
		}
		
		playTime.setText("");		 // ����ʱ�����ʱ����ʾˢ��
		durationTime.setText("");
		textMovieState.setText("ֹͣ");
		btnPlayStop.setImageResource(R.drawable.musicplaystart);
		movieIsPlay = false;
		currentListItem = -1;
	}

	// ʱ���ַ���
	public String toTime(int time)
	{
		//time-->ms
		
		time /= 1000;				//time-->��
		int minute = time / 60;		//tims/60-->��
		int hour = minute / 60;		//Сʱ
		int second = time % 60;		//��
		
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	// ��ʾ��ʾ��Ϣ
	private void displayTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// �����ǰ��������
	private void putFuncationName(String name)
	{
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}

	// ����������
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
					songPlayTimeUpdate(); // ���½�����
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

			ThreadRunFlag = false; // �߳̽�����ʶ
		}
	}

	// ��Ƶ����ʱ�����
	private void songPlayTimeUpdate()
	{		
		Message m = new Message();
		m.what = MSG_PLAYTIME;						
		SmartCarMovie.this.mHandler.sendMessage(m);		
	}
	
	// ���ط���
	private void returnHome()
	{
		// ����"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarMovie.this, SmartCarSystem.class);
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		
		putFuncationName("returnHome");
	}

	// ִ�г����˳�����
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
