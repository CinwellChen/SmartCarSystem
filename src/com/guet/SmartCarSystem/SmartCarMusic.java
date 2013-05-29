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

//������Ϣϵͳ��1���֣���Ƶ������
public class SmartCarMusic extends Activity {
	// ϵͳ��������
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
	private static Button btnMusicClose; // ȫ���ر�
	private static Button btnMusicMin; // ��С����������ҳ
	private static ImageView btnHome; // ��С����������ҳ
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
	private static TextView playTime; // ��ǰ����ʱ��
	private static TextView durationTime; // �ܲ���ʱ��
	private static TextView textMusicVoice;
	private static TextView textMusicState;
	private static TextView textSongTitle; // ������
	private static TextView textSongArtist; // ������
	private static TextView textSongAlbum; // ר����
	private static TextView textSongLrc; // LRC�����ʾ
	private static Button btnScanFiles; // ɨ���ļ�
	private static Button btnDelFiles; // ɾ���ļ�

	// �����Ƶ�ļ�����
	private static Button btnAddFiles; // ��������ļ���һ������
	private static Button btnAddFolders; // ��������ļ��У���Ҫɨ��
	protected static final int FILE_RESULT_CODE = 1;

	private Handler mHandler; // �����߳�ˢ��UIʹ��

	// ��ȡ��ǰ������ʱ��
	int currentSongPosition = 0;
	int currentSongMinute = 0;
	int currentSongSecond = 0;

	private AudioManager audioManager; // ����������
	private int soundVolume = 0; // ��������
	private int soundMaxVolume = 0; // �������ֵ
	private int soundSaveVolume = 0; // ����ʱ���������ֵ
	private int seekbarNum = 0; // ����������ֵ
	private int musicPosition; // ͻ���¼��������
	static int musicPlayType = 0; // ���ֲ���ģʽ�л�
	static int randomNumberBase = 0; // ���������������1

	private Map<String, Map<Long, String>> mapsLrcManager = new HashMap<String, Map<Long, String>>();
	// String:�����,HashMapΪ��ӦLRC��ʵ�Hash�����
	private Map<Long, String> currentLrcHash = new HashMap<Long, String>();

	// �����б����ݿ⣬������ʾӳ���
	private List<String> listNumbersSave;// ��Ƶ�б����
	private List<String> listNamesSave; // ��Ƶ�б�����
	private List<String> listTimesSave; // ��Ƶ�б�ʱ��

	MusicServiceApp myMusicListDb; // ��Ƶ���ݿ�
	musicListAdapter mAdapter; // ��ʾ������
	private int currentListItem; // ��ǰ���Ÿ���������

	private int currentListItemSave; // ��ǰѡ����
	private String selectItemNameSave; // ����ѡ�е�ѡ������
	private String clickItemNameSave; // ����ѡ�е�ѡ������

	protected static final String TAG = "Car";
	protected static final int MSG_PLAYTIME = 0;
	private static final int MSG_LRCPOS = 1;

	songMapToSeekBar songThread1; // ���߳�1
	LrcParser songLrcParser; // LRC��ʴ�����1
	long songLrcPosition = 0;
	long songLrci = 1;
	boolean findLrcFlag = false;
	boolean addFilesFlag = false; // ����ļ�ʱ��������onPause(),onResume()��ʶ��
	// ����ļ�ʱ���Ƿ񲥷�����
	boolean returnHomeFlag = false; // ������ҳ��ʶ������С��
	boolean ThreadRunFlag = false; // ���ڵȴ��߳̽�����ʶ

	protected static final String RESULT_ITEM = "result_item"; // ���ؽ�����ʶ
	protected static final String RESULT_STYPE = "result_stype"; // ���ؽ�����ͱ�ʶ
	private static final String FILTER_STYPE = "filter_stype"; // �û��������ͱ�ʶ
	private static final String REQUEST_STYPE = "filesDisplayStyle";
	private static final String DISPLAY_FILES = "files";
	private static final String DISPLAY_FOLDERS = "folders";
	protected static final String MUSIC_PATH = "/mnt/sdcard/";
	protected static final String DISPLAY_AUDIO = "audio";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.musichome);

		initMusicComponent(); // ��ʼ��GUIԪ��t
		initMusicVariable(); // ��ʼ��ϵͳ����
		ReadSharedPreferences(); // ��ȡϵͳ���ò���
		initMusicObject(); // ��ʼ��ϵͳһЩ����Ķ��󣬽���Ӵ洢�ռ��ж���
		initMusicListView(); // ��ʼ�������б�

		// �����б����¼�����
		musicListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			// ��ѡ������ʱ���������ֲ���
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentListItem = position;
				mAdapter.selectItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();

				String currentSongName = mAdapter.selectItemName + ".mp3";
				Log.i(TAG, "currentSongName=" + currentSongName);

				// �������ֲ��Ŵ������
				
				animation1();
				playMusic(currentSongName);
				
				return true;
				// ����true Click���ᱻ����
			}
		});

		musicListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ������ʾ����
				mAdapter.clickItemName = listNamesSave.get(position);
				mAdapter.notifyDataSetInvalidated();
			}
		});

		// ���ֲ���ģʽ�����¼�
		MusicPlayModel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (musicPlayType) {
				case 1: // ��˳�򲥷�-->����ѭ��
					MusicPlayModel.setImageResource(R.drawable.musicplaytype2);
					musicPlayType = 2;
					textSongPlayStype.setText("����ѭ��");

					break;

				case 2: // �ɵ���ѭ��-->�б�ѭ��
					MusicPlayModel.setImageResource(R.drawable.musicplaytype3);
					musicPlayType = 3;
					textSongPlayStype.setText("�б�ѭ��");
					break;

				case 3: // ���б�ѭ��-->�������
					MusicPlayModel.setImageResource(R.drawable.musicplaytype4);
					musicPlayType = 4;
					textSongPlayStype.setText("�������");
					break;

				case 4: // ���������-->˳�򲥷�
					MusicPlayModel.setImageResource(R.drawable.musicplaytype1);
					musicPlayType = 1; // ��λ
					textSongPlayStype.setText("˳�򲥷�");
					break;

				default:
					break;
				}
			}
		});

		// ͨ��getStream/MaxVolume��õ�ǰ������С,���ֵ����������С,������seek���������ֵ
		soundVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundMaxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		seekBarVoice.setMax(soundMaxVolume);

		// �ѵ�ǰ������ֵ���ø�������
		seekBarVoice.setProgress(soundVolume);
		textMusicVoice.setText("����(" + soundVolume + ")");

		// ����һ��"Handler"���ڽ��յ�ǰ��������ʱ��仯
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_PLAYTIME: {
					// ������ʾʱ��
					playTime.setText(msg.arg1 + ":" + msg.arg2);
				}
					break;

				case MSG_LRCPOS: {
					songLrci = ((long) msg.arg1);

					if (currentLrcHash != null) // ʹ��ǰ�ж��Ƿ�ΪNULL
					{
						// �ӵ�ǰ����HashMapȡ������ʾ
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

		// ���������ֽ������߳�
		songThread1 = new songMapToSeekBar();
		songThread1.start();
		ThreadRunFlag = true;

		// ���ָ����������϶�
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
						// �ж��Ƿ����϶����϶�ʱ�Ÿ���
						// ��ֹƽʱ��������
						if (songThread1.shouldContinue == false) {
							songPlayTimeUpdate(progress); // ��̬��ʾ��ǰ�϶�ʱ��
						}
					}
				});

		// ����������¼�����
		musicMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// ��ǰ������������¼�
				switch (musicPlayType) {
				case 1: // "˳�򲥷�"ģʽ
				{
					// ��ת����һ�ײ���
					nextMusic();
				}
					break;

				case 2: // "����ѭ��"ģʽ
				{
					currentListItem--;
					nextMusic();
				}
					break;

				case 3: // "�б�ѭ��"ģʽ
				{
					nextMusic();
				}
					break;

				case 4: // "���"ģʽ
				{
					// ��List�б���Ч��Χ������һ���������,�ٽ��в���
					if (listNamesSave.size() == 0) {
						// û�и���
						nextMusic(); // ����next�Ա�ֹͣ
					} else if (listNamesSave.size() == 1) {
						// ֻʣһ�׸�
						String playName = listNamesSave.get(0) + ".mp3";
						if (playName != null) {
							playMusic(playName);
							currentSongChange(); // ��ǰ��������
							
						}
					} else {
						// ���׸�������
						int maxNumber = listNamesSave.size();
						int randomSongNumber;

						// ���������(˫�أ���ֹ�ظ���)����С��Χ 0-maxNumber
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
								currentSongChange(); // ��ǰ��������
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

	// ��ʼ��GUI�ؼ�
	private void initMusicComponent() {
		// ���ҿ���ID
		
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
		

		// ��������¼�
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
		// ��������ļ�
		btnAddFiles.setOnClickListener(musicListener);
		btnAddFolders.setOnClickListener(musicListener);
		btnScanFiles.setOnClickListener(musicListener);
		btnDelFiles.setOnClickListener(musicListener); // ����ɾ�����ļ�
		btnDelFiles.setOnLongClickListener(musicLongListener); // ����ȫ��ɾ��
		seekBarVoice.setOnSeekBarChangeListener(voiceChangeListener);
		listshow.setOnClickListener(musicListener);
		enterPlay.setOnClickListener(musicListener);
	}

	// ��ʼ��ϵͳ����
	private void initMusicVariable() {
		// ��ʼ����������
		musicMediaPlayer = new MediaPlayer(); // �����ļ���������
		// currentListItem = 0; // ��ǰ���Ÿ�����ų�ʼΪ0
		musicPlayType = 1; // ��ʼΪ˳�򲥷�ģʽ
		textSongPlayStype.setText("˳�򲥷�");
		playTime.setText("00:00"); // ����ʱ�����ʱ����ʾ��ʼ��
		durationTime.setText("");
		textSongLrc.setText(""); // LRC������
		seekBarPlayProcess.setEnabled(false); // ��ֹ�϶�������
		btnReset.setEnabled(false); // ��λ��ʼ��ֹ
		songLrcParser = new LrcParser(); // �½���

		// ������ʵ��
		listNumbersSave = new ArrayList<String>();
		listNamesSave = new ArrayList<String>();
		listTimesSave = new ArrayList<String>();
	}

	// ��ʼ��ϵͳһЩ����Ķ��󣬽���Ӵ洢�ռ��ж���
	private void initMusicObject() {
		// **********************************************************
		// 1��ʼ�������б����ݿ�
		try {
			Log.d(TAG, "�������ݿ�:");
			myMusicListDb = new MusicServiceApp(this);
			Log.d(TAG, "  �ɹ���");

			// ���ϵͳ�Ƿ��1�������Ƿ���ɨ���SD���ȴ洢���е���Ƶ����
			if (SDMUSIC_HAVE_READ.equals("T")) {
				// �Ѿ���ȡ������������
				Log.d(TAG, "ϵͳ�ѳ�ʼ���洢���е���Ƶ����!");
			} else {
				// δ��ȡ������ʼ��ȡ,�����Ժ�ɨ�衱�͡���ӡ���Ƶ����
				Log.d(TAG, "ϵͳδ��ʼ���洢���е���Ƶ����!");

				// ɨ��SD���е�����,ע���˲�������ϵͳ��һ������������
				initMusicDbData();
				SDMUSIC_HAVE_READ = "T";
			}

		} catch (Throwable e1) {
			Log.e(TAG, e1.toString());
			Log.d(TAG, "  ʧ�ܣ�");
		}

		// **********************************************************
		// 2���HASH���ݿ����
		File lrcFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMusic-Lrc.db");
		if (!lrcFile.exists()) // ���ļ������ڣ��򴴽�
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
			// ��һ����쳣��һ������ʱ����֣�����Ӱ���������
			// ERROR/Car(28063): java.io.EOFException

			// ��LRC HASH���������������
			mapsLrcManager = (Map<String, Map<Long, String>>) lrcOi
					.readObject();
			lrcOi.close(); // �ر�

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
		// 3�����б�-����"���ݿ�"����
		File nameFile = new File(
				"/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListNames.db");
		if (!nameFile.exists()) // ���ļ������ڣ��򴴽�
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
		// 4�����б�-ʱ��"���ݿ�"����
		File timeFile = new File(""
				+ "/mnt/sdcard/SmartCarSystem/SmartCarMusic-ListTimes.db");
		if (!timeFile.exists()) // ���ļ������ڣ��򴴽�
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

	// ��ʼ�����������б�
	private void initMusicListView() {
		int position, listSize;

		// ������Ҫ��ʼ��listNumbersSave
		listSize = listNamesSave.size();
		listNumbersSave.clear(); // ���
		for (position = 1; position <= listSize; position++) {
			listNumbersSave.add(Integer.toString(position));
		}

		// �½���������������ʾ
		mAdapter = new musicListAdapter(this, listNumbersSave, listNamesSave,
				listTimesSave);
		musicListView.setAdapter(mAdapter);

		// ˢ��ѡ��
		currentListItem = currentListItemSave;
		mAdapter.clickItemName = clickItemNameSave;
		mAdapter.selectItemName = selectItemNameSave;
		mAdapter.notifyDataSetInvalidated();

		listViewFlush(); // ˢ����ʾ
	}

	// ˢ��listView��ǰ����ʾ
	private void listViewFlush() {
		int positionView = listNamesSave.indexOf(mAdapter.selectItemName);

		if (positionView >= 0) {
			// ��ȡ��ǰ��ʾ��ǰ�����һ������
			int lastPosition = musicListView.getLastVisiblePosition();
			int firstPositon = musicListView.getFirstVisiblePosition();

			// �ж��Ƿ�����ͼ��
			if ((positionView > lastPosition) || (positionView < firstPositon)) {
				// ���ǣ���ѡ����Ϊ��ǰ��ͼ
				musicListView.setSelection(positionView);
			}
		}
	}

	// ϵͳ���ò�����ȡ
	void ReadSharedPreferences() {
		SharedPreferences user = getSharedPreferences("user_info_music",
				Activity.MODE_PRIVATE);
		SDMUSIC_HAVE_READ = user.getString("SDMUSIC_HAVE_READ", "");
		currentListItemSave = user.getInt("currentListItemSave", -1);
		selectItemNameSave = user.getString("selectItemNameSave", "");
		clickItemNameSave = user.getString("clickItemNameSave", "");
	}

	// ϵͳ���ò�������
	void WriteSharedPreferences() {
		SharedPreferences user = getSharedPreferences("user_info_music",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = user.edit();
		editor.putString("SDMUSIC_HAVE_READ", SDMUSIC_HAVE_READ);

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
	protected void onDestroy() {
		// �ر�music���ţ�LRC��ʾ�߳�
		songThread1.shouldRun = false;
		while (ThreadRunFlag == true)
			;

		// �������ͷ�"musicMediaPlayer"��Ҫ��������߳̽���
		// ��Ϊ�����̻߳���õ��ö���Ҫ�ö���ɾ���������
		// FATAL EXCEPTION: Thread-10
		// java.lang.IllegalStateException
		// at android.media.MediaPlayer.isPlaying(Native Method)
		// at
		// com.guet.SmartCarSystem.SmartCarMusic$songMapToSeekBar.run(SmartCarMusic.java:2092)

		// if( musicIsPlay == true )
		if (musicMediaPlayer.isPlaying()) { // �����ڲ���
			musicMediaPlayer.stop(); // ֹͣ
		}

		musicMediaPlayer.reset(); // ��λ
		musicMediaPlayer.release(); // �ͷ�ռ���豸

		// �ر����ݿ�
		Log.d(TAG, "���ڹر����ݿ�...");
		myMusicListDb.closeDb(); // �ر����ݿ�

		saveMusicObject(); // ����һЩϵͳ����
		WriteSharedPreferences(); // ����ϵͳ���ò���
		System.gc(); // ��ȷ�ͷ��ڴ�

		super.onDestroy();
		putFuncationName("onDestroy");
	}

	// ����ϵͳһЩ����
	private void saveMusicObject() {
		// *********************************************************
		// 1����MusicList Name����������
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
		// 2����MusicList Time����������
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
		// 2����LRC HASH����������
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

	// ��дonActivityResult,���������Ƶ�ļ�ʱ��������
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		if (FILE_RESULT_CODE == requestCode) {
			Bundle bundle = null;

			// ���ؽ�����������ͱ�������
			List<String> returnSelectItems = new ArrayList<String>();
			List<String> returnFileItems = new ArrayList<String>();
			String resturnStype;

			if (data != null && (bundle = data.getExtras()) != null) {
				returnSelectItems = bundle.getStringArrayList(RESULT_ITEM);
				resturnStype = bundle.getString(RESULT_STYPE);
				int dataSize = returnSelectItems.size();

				if (dataSize > 0) {
					Log.d(TAG, "�ļ�(��)���(�������): ");
					// ������ӵ��ļ����ļ���
					for (int i = 0; i < dataSize; i++) {
						Log.d(TAG, "[" + i + "]=" + returnSelectItems.get(i));
					}

					if (resturnStype.equals(DISPLAY_FILES)) {
						// ��Ϊ�ļ���ӣ�����任
						returnFileItems = returnSelectItems;
					} else if (resturnStype.equals(DISPLAY_FOLDERS)) {
						// ��Ϊ�ļ�����ӣ�������ļ��н���ɨ����ļ�
						// �����ļ����б������ļ��б�,���ļ��б�����0�򷵻�NULL
						returnFileItems = fileScanFromFolders(returnSelectItems);

						if (returnFileItems == null) {
							Log.d(TAG, "�ļ�(��)ɨ����Ϊ�գ�");
							return;
						} else {
							Log.d(TAG, "�ļ�(��)ɨ�������£�");
							dataSize = returnFileItems.size();
							for (int j = 0; j < dataSize; j++) {
								Log.d(TAG,
										"[" + j + "]=" + returnFileItems.get(j));
							}
						}
					}

					// �����ļ�(��)��Ӻ���
					try {
						doAddMusicFile(returnFileItems);
					} catch (Throwable e) {
						Log.d(TAG, "�����ļ�(��)��Ӻ���: ʧ�ܣ�");
						Log.d(TAG, e.toString());
					}
				} else {
					Log.d(TAG, "��ǰû��Ҫɨ����ļ��л���ӵ��ļ���");
				}

			}

			// ����ļ����
			addFilesFlag = false;
		}
	}

	// ���ļ����б��У������ļ����������ļ��б�
	private List<String> fileScanFromFolders(List<String> Folders) {
		int i;
		List<String> musicTempList = new ArrayList<String>();

		for (i = 0; i < Folders.size(); i++) {
			String dirStr = Folders.get(i); // ���λ�ȡÿ����Ŀ¼
			if (dirStr != null) {
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

				if (subDir.listFiles(new MusicFilter()).length > 0) {
					for (File file : subDir.listFiles(new MusicFilter())) {
						musicTempList.add(file.getName());
					}
				}
			}
		}

		// �����ļ��򷵻��б����򷵻�NULL
		if (musicTempList.size() > 0)
			return musicTempList;
		else
			return null;
	}

	// ���е绰��ʱ,����ļ�ʱ������
	@Override
	protected void onPause() {
		// ��ǰ������������������ļ���������ҳ 3�������ִ��ֹͣ
		if (musicMediaPlayer.isPlaying() && (addFilesFlag == false)
				&& returnHomeFlag == false) {
			// ���浱ǰ���ŵ�λ��
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
		// ���ϵ�д��ڼ���ļ�
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile.exists()) {
			// �ļ������ڣ�֤����ҳ�ѹرգ���ʾ������ҳ���ܷ���
			finish(); // �˳�
		}

		super.onRestart();
		putFuncationName("onRestart");
	}

	@Override
	protected void onStop() {
		putFuncationName("onStop");
		super.onStop();
	}

	// ����绰��,����ļ�������
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

	// ���´����󣬱����ڴ�Խ�ϵͳ�Ὣֹͣ����ͣActivityɱ�������ٻָ�
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// this.fileName = savedInstanceState.getString("fileName");
		// this.musicPosition =savedInstanceState.getInt("musicPosition");
		// super.onRestoreInstanceState(savedInstanceState);
		//
		Log.e(TAG, "onRestoreInstanceState()!");
	}

	// ��δ֪�¼�����ʱ����������(ϵ�л���ϵͳӲ��)����ϵͳ�ڴ���ţ���ACTIVITY�ᱻɱ��
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// outState.putString("fileName", fileName);
		// outState.putInt("musicPosition", musicPosition);
		// super.onSaveInstanceState(outState);

		Log.e(TAG, "onSaveInstanceState()!");
	}

	// �������Ƹı��¼�
	private OnSeekBarChangeListener voiceChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		// ��ֵ�仯�¼�
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			seekbarNum = seekBarVoice.getProgress();

			// (2)ֱ�Ӹı�
			if (voiceOpen == true) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						seekbarNum, AudioManager.FLAG_PLAY_SOUND);
			} else {
				// ����ģʽ
				soundSaveVolume = seekbarNum; // ֻ���治����
			}

			soundVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (soundVolume == 0) {
				// ����
				textMusicVoice.setText("����!");
				btnVoice.setImageResource(R.drawable.musicvoiceclose);
			} else {
				// �Ǿ���
				textMusicVoice.setText("����(" + soundVolume + ")");
				btnVoice.setImageResource(R.drawable.musicvoiceopen);
			}
		}
	};

	// �����ť���������¼�
	private OnLongClickListener musicLongListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			boolean result = false;

			switch (v.getId()) {
			case R.id.musicBtnDelFiles: {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SmartCarMusic.this);
				// builder.setIcon(R.drawable.jg); //����ͼ��
				builder.setIcon(R.drawable.carparnter);
				builder.setTitle("�Ƿ�Ҫ��յ�ǰ�����б�"); // ���ñ���

				// ȷ�ϵ����¼�
				builder.setPositiveButton("��(Yes)",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d(TAG, "����˶Ի����ϵ�ȷ����ť");
								doDelAllMusicFile(); // ����ɾ�����и����б���
							}

						});

				// ȡ�������¼�
				builder.setNegativeButton("��(No)",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d(TAG, "����˶Ի����ϵ�ȡ����ť");
							}
						});

				builder.create().show(); // ��ʾ����
				result = true; // ������Ч
			}
				break;

			default:
				break;
			}

			return result;
		}
	};

	// �����ť���������¼�
	private OnClickListener musicListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				switch (v.getId()) {
				case R.id.ImageViewReset: // ��λ
				{
					musicMediaPlayer.stop(); // ֹͣ
					musicIsPlay = false;
					seekBarPlayProcess.setProgress(0);
					if (seekBarPlayProcess.isEnabled()) {
						seekBarPlayProcess.setEnabled(false); // ֹͣʱ��ֹ�϶�������
					}
					playTime.setText("00:00");
					textMusicState.setText("ֹͣ");
					btnPlayStop.setImageResource(R.drawable.musicplaystart);
				}
					break;

				case R.id.ImageViewBefore: // ��һ��
				{
					lastMusic();
				}
					break;

				case R.id.ImageViewNext: // ��һ��
				{
					nextPlayFlag = true;
					nextMusic();
				}
					break;

				case R.id.imageViewPlayStop: // ���Ż�ֹͣ
				{
					if (musicMediaPlayer.isPlaying()) {
						// ��"����"��"��ͣ",����Ϊ��ͣͼ��
						musicMediaPlayer.pause();
						textMusicState.setText("��ͣ");
						btnPlayStop.setImageResource(R.drawable.musicplaystart);
					} else {
						if (listNamesSave.size() > 0) {
							if (musicIsPlay == false) {
								// ���Σ�Ҫ��ʼ������Դ
								// ��"��ͣ"��"����",����Ϊ����ͼ��
								if (currentListItem == -1)
									currentListItem = 0; // ��Ե�һ������

								String playName = listNamesSave
										.get(currentListItem) + ".mp3";
								if (playName != null) {
									playMusic(playName);
									currentSongChange();
								}
							} else {
								musicMediaPlayer.start();
								textMusicState.setText("������");
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

				case R.id.buttonMusicClose: // ������ҳ�ر�
				{
					doExitWork(); // ִ�г����˳�����
				}
					break;

				case R.id.buttonMUsicMin: // ��С��
				case R.id.ImageViewHome: // ��������ҳ����ϵͳ��ҳ,���ر�����
				{
					returnHome();
				}
					break;

				case R.id.imageButtonVoice: // �������ƿ���
				{
					if (voiceOpen == true) {
						// �ر�����,����ģʽ
						btnVoice.setImageResource(R.drawable.musicvoiceclose);
						textMusicVoice.setText("����!");
						// ���澲��ֵ,������������Ϊ0
						soundSaveVolume = audioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC);
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								0, AudioManager.FLAG_PLAY_SOUND);

						voiceOpen = false;
					} else {
						// ������Ϊ0�����ʱ����Ч
						if (soundSaveVolume != 0) {
							// ������,����ģʽ
							btnVoice.setImageResource(R.drawable.musicvoiceopen);
							// �ָ�����ֵ
							audioManager.setStreamVolume(
									AudioManager.STREAM_MUSIC, soundSaveVolume,
									AudioManager.FLAG_PLAY_SOUND);

							textMusicVoice.setText("����(" + soundVolume + ")");
							voiceOpen = true;
						}
					}
				}
					break;

				case R.id.musicBtnAddFiles: // �����Ƶ�ļ�
				{
					Intent intent = new Intent(SmartCarMusic.this,
							MyFileManager.class);
					// Newһ��Bundle���󣬲���Ҫ���ݵ����ݴ���,�����ļ������Ҫ����ʾ����Ƶ�ļ�
					Bundle bundle = new Bundle();
					bundle.putString(REQUEST_STYPE, DISPLAY_FILES);
					bundle.putString(FILTER_STYPE, DISPLAY_AUDIO);
					intent.putExtras(bundle);

					// �з��ؽ��
					startActivityForResult(intent, FILE_RESULT_CODE);

					// ����ļ�����
					addFilesFlag = true;
				}
					break;

				case R.id.musicBtnAddFolders: // �����Ƶ�ļ���
				{

					Intent intent = new Intent(SmartCarMusic.this,
							MyFileManager.class);

					// Newһ��Bundle���󣬲���Ҫ���ݵ����ݴ���,�����ļ������Ҫ����ʾ���ļ���
					Bundle bundle = new Bundle();
					bundle.putString(REQUEST_STYPE, DISPLAY_FOLDERS);
					bundle.putString(FILTER_STYPE, DISPLAY_AUDIO);
					intent.putExtras(bundle);

					// �з��ؽ��
					startActivityForResult(intent, FILE_RESULT_CODE);

					// ����ļ�����
					addFilesFlag = true;
				}
					break;

				case R.id.musicBtnScanFiles: // ɨ�跽ʽ�����Ƶ�ļ���Ĭ��ΪSD��
				{
					try {
						scanFilesFromExt();
					} catch (Throwable e) {
						Log.e(TAG, e.toString());
					}

					Log.d(TAG, "case R.id.musicBtnScanFiles:");
				}
					break;

				case R.id.musicBtnDelFiles: // ɾ����ǰ�ļ�
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
				// ����쳣��Ϣ
				Log.e(TAG, e.toString());
			}
		}

	};

	// ���ط���
	private void returnHome() {
		returnHomeFlag = true;

		// ����"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarMusic.this, SmartCarSystem.class);

		// FLAG_ACTIVITY_REORDER_TO_FRONT��־���ܷ�ֹ�ظ�ʵ����һ��Activity
		// ��ȥ�󣬻�����"onCreate()",ֱ�ӵ�"onRestart()"-->"onStart()"
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		startActivity(intent);

		// SmartCarMusic.this.finish(); //������finish���ᴥ��onDestroy();
		putFuncationName("returnHome");
	}

	protected void animation2() {
		// TODO Auto-generated method stub
		viewflipper.setInAnimation(in_lefttoright);
		viewflipper.setOutAnimation(out_lefttoright);
		viewflipper.showPrevious();
	}

	// ִ�г����˳�����
	private void doExitWork() {
		SmartCarMusic.this.finish();
		Log.d(TAG, "doExitWork()");
	}

	// ��������
	void playMusic(String musicName) {
		String songTitleStr = null; // 3--������
		String songArtistStr = null; // 5--������
		String songAlbumStr = null; // 6--ר����
		String songFilePath = null; // 1--MP3·����
		String songFileName = null; // 2--MP3�ļ���
		String lrcFilePath = null;

		if (musicName == null)
			return; // ��ֹ��������Ϊ��

		try {
			// ���ݸ������ƣ������ݿ�����ȡ����
			Music musicTemp = new Music();
			try {
				musicTemp = myMusicListDb.find(musicName);
				if (musicTemp == null) {
					Log.d(TAG, "�����ݿ����޷����Ҹø�����" + musicName + "!");
					textSongLrc.setText("�޷���λ�ø����ļ�...");
					return;
				}
			} catch (Throwable e) {
				Log.d(TAG, e.toString());
				Log.d(TAG, "�����ݿ��в��Ҹ��� -" + musicName + ": �쳣��");
				textSongLrc.setText("�޷���λ�ø����ļ�...");
				return;
			}

			// ִ�����ˣ����ҵ�music
			songLrci = 1;
			findLrcFlag = false;
			boolean songLrcOk = false;

			// ������λ
			if (!btnReset.isEnabled()) {
				btnReset.setEnabled(true);
			}

			songFilePath = musicTemp.path; // ��ȡ�ļ�·��
			songFileName = musicTemp.name; // ��ȡ�ļ���
			songTitleStr = musicTemp.title; // ��ȡ������
			songArtistStr = musicTemp.artist; // ��ȡ������
			songAlbumStr = musicTemp.album; // ��ȡ����ID

			textSongTitle.setText(songTitleStr);
			textSongArtist.setText("���֣�" + songArtistStr);
			textSongAlbum.setText("ר����" + songAlbumStr);

			// (1)�����ж�SD�����Ƿ�����ӦLRC HASH����
			Log.i(TAG, "songFileName:" + songFileName + " find in SD DataLib!");

			currentLrcHash = (HashMap<Long, String>) mapsLrcManager
					.get(songFileName);
			// �˾�ܹؼ��������(HashMap<Long, String>)
			// (2)�˾�ܹؼ���������м��
			if (currentLrcHash != null && currentLrcHash.size() > 0) {
				// ��LRC HASH���ݿ�������Ӧ����
				// ֱ��ȡ������������ǰLRC ������ʾʹ�õ�HASH
				Log.i(TAG, "��SD�����ҵ���Ӧ��HashMap!");

				// ʹ�ܸ�ʽ���
				findLrcFlag = true;
			} else {
				// ȥ"mp3"
				lrcFilePath = songFilePath.substring(0,
						songFilePath.length() - 3);
				lrcFilePath = lrcFilePath + "lrc";
				File lrcFile = new File(lrcFilePath);

				if (!lrcFile.exists()) // �жϸ�LRC�ļ��Ƿ����
				{
					// ��ʾ�޷�����LRC�ļ�
					Log.i(TAG, "/////////////////�޷��鵽��LRC��Ҳ�޷���SD�����ҵ���Ӧ��HashMap!");
					textSongLrc.setText("�޷����ظ��ļ���\"lrc\"����ļ�...");
				} else {
					textSongLrc.setText("���ڼ��ظ��ļ���\"lrc\"����ļ�...");

					// �жϸ���ļ���ʽ,����������
					if (isUft8File(lrcFilePath) == true) {
						// LRC�ļ�����ΪUTF8
						songLrcOk = songLrcParser.setFile(lrcFilePath, true);
					} else {
						// LRC�ļ�����ΪGBK
						songLrcOk = songLrcParser.setFile(lrcFilePath, false);
					}

					// 2����LRC�ļ������󣬵õ�һ��LRC MAPSHASH
					Map<Long, String> mapsLrcSon = new HashMap<Long, String>();

					do {
						Thread.sleep(10);

					} while (!songLrcOk); // �ȴ�LRC�������!

					// ���õ���Hash���б���
					mapsLrcSon = songLrcParser.mapsToLrc;

					Log.i(TAG, "songFileName:" + songFileName
							+ " put in SD DataLib!");
					mapsLrcManager.put(songFileName, mapsLrcSon);
					currentLrcHash = mapsLrcSon; // ���õ���Hash������ǰLRCר����ʾHASH

					// ʹ�ܸ�ʽ���
					findLrcFlag = true;
					Log.i(TAG, "�ѴӲ��ҵ�LRC�ļ�������Ӧ��HashMap!");
				}
			}

			// 2׼��������Ƶ����
			musicMediaPlayer.reset();
			musicMediaPlayer.setDataSource(songFilePath);
			musicMediaPlayer.prepare();
			musicMediaPlayer.start();

			// �ɡ����š��л�����ֹͣ����ť
			textMusicState.setText("������");
			btnPlayStop.setImageResource(R.drawable.musicplaypause);
			musicIsPlay = true;
			if (!seekBarPlayProcess.isEnabled()) {
				seekBarPlayProcess.setEnabled(true); // ����ʱʹ���϶�������
			}

			// ���½�����
			currentSongPosition = musicMediaPlayer.getDuration();
			seekBarPlayProcess.setMax(currentSongPosition);

			// ��ʾ����ʱ�䳤��
			currentSongPosition = currentSongPosition / 1000; // ת��Ϊ��
			currentSongMinute = currentSongPosition / 60;
			currentSongSecond = currentSongPosition % 60;
			durationTime.setText(currentSongMinute + ":" + currentSongSecond);

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	// ��һ��
	void nextMusic() {
		// �жϸ����б��Ƿ���Ϊ��
		if (listNamesSave.size() == 0) {
			doNoMusicWork();
			return;
		}

		String playName;

		if (++currentListItem >= listNamesSave.size()) {
			currentListItem = 0;

			// ��Ϊ"�б�ѭ��"ģʽ,�����ڲ������һ��ʱ�������һ��,���������
			if (musicPlayType == 3 || nextPlayFlag == true) {
				nextPlayFlag = false;
				playName = listNamesSave.get(currentListItem) + ".mp3";
				if (playName != null) {
					playMusic(playName);
				}

			} else {
				doNoMusicWork();
				if (musicPlayType == 1) {
					// ��Ϊ˳�򲥷ţ���ֻ��һ�׸�ʱ��ȡ�����һ�׸�ѡ
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

		currentSongChange(); // ��ǰ��������
	}

	// ִ��û�и������Ź���
	private void doNoMusicWork() {
		// �ж��Ƿ����ڲ���
		if (musicMediaPlayer.isPlaying()) {
			musicMediaPlayer.stop();
		}

		seekBarPlayProcess.setProgress(0);
		if (seekBarPlayProcess.isEnabled()) {
			seekBarPlayProcess.setEnabled(false); // ֹͣʱ��ֹ�϶�������
		}
		playTime.setText("00:00"); // ����ʱ�����ʱ����ʾˢ��
		durationTime.setText("");
		textMusicState.setText("ֹͣ");
		btnPlayStop.setImageResource(R.drawable.musicplaystart);
		musicIsPlay = false;
		currentListItem = -1;

		// return;
	}

	// ���ݵ�ǰ�����ı䣬����
	private void currentSongChange() {
		// ��������"currentListItem"�б�ˢ��,����
		if (currentListItem < listNamesSave.size() && currentListItem >= 0) {
			mAdapter.selectItemName = listNamesSave.get(currentListItem);
			mAdapter.notifyDataSetInvalidated();

			listViewFlush(); // ˢ����ʾ
		}
	}

	// ��һ��
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
				// ��������
				currentListItem = listNamesSave.size();
			}
		} else {
			playName = listNamesSave.get(currentListItem) + ".mp3";
			if (playName != null) {
				playMusic(playName);
			}
		}

		currentSongChange(); // ��ǰ��������
	}

	// ��ʼ���������ݿ�
	void initMusicDbData() throws Throwable {
		Cursor cursorTemp;

		// ��ȡ�ⲿ�洢����������Ƶ�ļ�
		cursorTemp = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				// ����ַ��������ʾҪ��ѯ����
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,// 0
						// �����ļ���
						MediaStore.Audio.Media.TITLE, // 1
						// ����������
						MediaStore.Audio.Media.DATA, // 2
						// �����ļ���·��
						MediaStore.Audio.Media.DURATION, // 3
						// ���ֵ���ʱ��
						MediaStore.Audio.Media.ARTIST, // 4
						// ������
						MediaStore.Audio.Media.ALBUM // 5
				// ר����

				}, null, // ��ѯ�������൱��sql�е�where���
				null, // ��ѯ������ʹ�õ�������
				null // ��ѯ���������ʽ
				);

		// �����ҵ�����Ƶ���ݣ���ӵ����ݿ���
		if (cursorTemp != null) {
			cursorTemp.moveToFirst();
			int temp, addCount = 0;
			String nameStr, titleStr, pathStr, timeStr, artistStr, albumStr;

			// ��Cursor�з������������ListView����ʾ
			for (int i = 0; i < cursorTemp.getCount(); i++) {
				cursorTemp.moveToPosition(i);

				// �ȶ�ȡ��������
				nameStr = cursorTemp.getString(0);

				// ����mp3�ļ�����
				String mp3Filter = nameStr.substring(nameStr.length() - 3);
				if (!mp3Filter.equals("mp3")) {
					Log.d(TAG, "ϵͳ�Ը��ļ�: " + nameStr + "�ݲ�֧��!");
					continue;
				}

				// �鿴���ݿ��Ƿ����
				Music music1 = new Music();
				music1 = myMusicListDb.find(nameStr);
				if (music1 == null) {
					// �������ڣ������
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					artistStr = cursorTemp.getString(4);
					albumStr = cursorTemp.getString(5);

					Music music2 = new Music(); // ����NEWһ��������Ϊǰ��ΪNULL��ֵ
					music2.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
							albumStr, "T", "T");
					myMusicListDb.save(music2);
					addCount++;
				}
			}
			cursorTemp.moveToFirst(); // �������λ
			cursorTemp.close(); // �رգ��ͷ���Դ

			if (addCount > 0) {
				Log.d(TAG, "��ʼ���������ݿ⣺����" + addCount + "����Ƶ�ļ�...");
			} else {
				Log.d(TAG, "��ʼ���������ݿ⣺δ���ֿ�����Ƶ�ļ�...");
			}
		}
	}

	// ���ⲿ�洢��SD����ɨ����Ƶ�ļ�
	void scanFilesFromExt() throws Throwable {
		Cursor cursorTemp;

		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		// ��ȡ�ⲿ�洢����������Ƶ�ļ�
		cursorTemp = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				// ����ַ��������ʾҪ��ѯ����
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,// 0
						// �����ļ���
						MediaStore.Audio.Media.TITLE, // 1
						// ����������
						MediaStore.Audio.Media.DATA, // 2
						// �����ļ���·��
						MediaStore.Audio.Media.DURATION, // 3
						// ���ֵ���ʱ��
						MediaStore.Audio.Media.ARTIST, // 4
						// ������
						MediaStore.Audio.Media.ALBUM // 5
				// ר����

				}, null, // ��ѯ�������൱��sql�е�where���
				null, // ��ѯ������ʹ�õ�������
				null // ��ѯ���������ʽ
				);

		// �����ҵ�����Ƶ���ݣ���ӵ����ݿ���
		if (cursorTemp != null) {
			cursorTemp.moveToFirst();
			int temp, addCount = 0, itemNum;
			String nameStr, nameFind, titleStr, pathStr, timeStr, artistStr, albumStr;

			// ��Cursor�з������������ListView����ʾ
			for (int i = 0; i < cursorTemp.getCount(); i++) {
				cursorTemp.moveToPosition(i);

				// �ȶ�ȡ��������
				nameStr = cursorTemp.getString(0);

				// ����mp3�ļ�����
				String mp3Filter = nameStr.substring(nameStr.length() - 3);
				if (!mp3Filter.equals("mp3")) {
					Log.d(TAG, "ϵͳ�Ը��ļ�: " + nameStr + "�ݲ�֧��!");
					continue;
				}

				// �鿴���ݿ��Ƿ����
				Music music1 = new Music();
				music1 = myMusicListDb.find(nameStr);
				if (music1 == null) {
					// �������ڣ������
					titleStr = cursorTemp.getString(1);
					pathStr = cursorTemp.getString(2);
					temp = cursorTemp.getInt(3);
					timeStr = toTime(temp);
					artistStr = cursorTemp.getString(4);
					albumStr = cursorTemp.getString(5);

					Music music2 = new Music(); // ����NEWһ��������Ϊǰ��ΪNULL��ֵ

					// Ϊ��һ�γ�ʼ��
					music2.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
							albumStr, "T", "T");
					myMusicListDb.save(music2);

				} else {
					// ����������ݿ�����

					// ȥ��(.mp3)
					nameFind = nameStr.substring(0, nameStr.length() - 4);

					// ��鵱ǰ���������б��Ƿ���ڣ������ڣ�����β�����
					if (listNamesSave.contains(nameFind) == false) {
						addCount++;
						timeStr = music1.time; // �����ݿ��л�ȡʱ��

						// ���¸����б�
						listNamesSave.add(nameFind);
						listTimesSave.add(timeStr);

						// ��Ӹ�������β������ŵ������ɣ�
						itemNum = listNumbersSave.size() + 1;
						listNumbersSave.add(Integer.toString(itemNum));

					} else {
						Log.d(TAG, "���ļ��������ڵ�ǰ�����б��������!");
					}
				}
			}
			cursorTemp.moveToFirst(); // �������λ
			cursorTemp.close(); // �رգ��ͷ���Դ

			// �����û���ӽ��
			if (addCount > 0) {
				// �½���������������ʾ
				mAdapter = new musicListAdapter(this, listNumbersSave,
						listNamesSave, listTimesSave);
				musicListView.setAdapter(mAdapter);

				// ����֮ǰ����ʾ
				mAdapter.clickItemName = clickItemNameSave;
				mAdapter.selectItemName = selectItemNameSave;
				// mAdapter.notifyDataSetInvalidated();
				mAdapter.notifyDataSetChanged();

				displayTip("�ɹ����" + addCount + "�׸���...");
			} else {
				displayTip("û��Ҫ��ӵĸ���...");
			}
		} else {
			Log.d(TAG, "�޷���ȡAndroid�ڲ�����Ƶ���ݿ�");
		}
	}

	// ��������ļ�(���ļ���)
	void doAddMusicFile(List<String> dataItem) throws Throwable {
		int i, size, addCount = 0, tempInt;
		String addFileName, addFilePath, tempStr;
		String nameStr, nameFind, titleStr, pathStr, timeStr, artistStr, albumStr;

		MediaPlayer tempPlayer = new MediaPlayer(); // ���ڻ�ȡMP3����ʱ��
		Music musicTemp = new Music();

		// ��ȡ�ɵ�ѡ��͵�����
		String clickItemNameSave = mAdapter.clickItemName;
		String selectItemNameSave = mAdapter.selectItemName;

		size = dataItem.size(); // ��ȡ������С

		Log.d(TAG, "����ΪҪ��ӵ��ļ���");

		// ���μ��ÿһ���ļ�
		for (i = 0; i < size; i++) {
			addFilePath = dataItem.get(i); // �ȵõ�������ȫ�����ƣ���/mnt/sdcard/hao.mp3

			// �����ļ���ȫ���з����ļ���
			File fileTemp = new File(addFilePath);
			addFileName = fileTemp.getName();
			Log.d(TAG, "");
			Log.d(TAG, "[" + i + "]=" + addFileName);

			// 1�ȴ����ݿ��в����Ƿ����
			Music music1 = new Music();
			music1 = myMusicListDb.find(addFileName);
			Log.d(TAG, "1.�����ݿ��в���: ");

			if (music1 != null) {
				Log.d(TAG, "  �ɹ�...");
				Log.d(TAG, "2.������º�̨���ݿ�...");

				// ֱ�������ݿ���ȡ����
				nameStr = addFileName; // 1�ļ���
				timeStr = music1.time;
			} else {
				// �����ݿ��в����ڣ���Ը���Ƶ���ݽ��з�������ӵ����ݿ���
				Log.d(TAG, "  ʧ��...");
				Log.d(TAG, "2.���ڶԸ��ļ����н���...");

				// Newһ��MP3��Ϣ������
				MusicInfoServer mp3Info = new MusicInfoServer(addFilePath);
				mp3Info.getMp3Info(); // ��������

				// �������ڣ������
				nameStr = addFileName; // 1�ļ���
				pathStr = addFilePath; // 3�ļ�����ȫ·��

				tempStr = mp3Info.info.songName; // 2������(����)
				if (tempStr != null) {
					titleStr = tempStr;
				} else {
					titleStr = "δ֪";
				}

				tempStr = mp3Info.info.artist; // 5������
				if (tempStr != null) {
					artistStr = tempStr;
				} else {
					artistStr = "δ֪";
				}

				tempStr = mp3Info.info.album; // 6ר��
				if (tempStr != null) {
					albumStr = tempStr;
				} else {
					albumStr = "δ֪";
				}

				// ��ȡ�����ĳ���
				tempPlayer.reset();
				tempPlayer.setDataSource(addFilePath);
				tempPlayer.prepare();
				// tempPlayer.stop();

				tempInt = tempPlayer.getDuration();
				timeStr = toTime(tempInt);
				// Log.d(TAG,"�������䲥��ʱ��Ϊ��" + timeStr);

				// ����һ��music������
				musicTemp.Set(nameStr, titleStr, pathStr, timeStr, artistStr,
						albumStr, "F", "T");
				Log.d(TAG, "music: " + musicTemp.toString());

				Log.d(TAG, "3.������������ݿ�...");
				if (myMusicListDb.save(musicTemp)) {
					Log.d(TAG, "  �ɹ���");
				} else {
					Log.d(TAG, "  ʧ�ܣ�");
				}
			}

			// ��鵱ǰ���������б��Ƿ���ڣ������ڣ�����β�����
			nameFind = nameStr.substring(0, nameStr.length() - 4);
			if (listNamesSave.contains(nameFind) == false) {
				addCount++;

				// ���¸����б�
				listNamesSave.add(nameFind);
				listTimesSave.add(timeStr);

				// ��Ӹ�������β������ŵ������ɣ�
				int itemNum = listNumbersSave.size() + 1;
				listNumbersSave.add(Integer.toString(itemNum));

			} else {
				Log.d(TAG, "���ļ��������ڵ�ǰ�����б��������!");
			}
		}

		tempPlayer.release(); // �ͷ�Ӳ��

		// �����û���ӽ��
		if (addCount > 0) {
			// �½���������������ʾ
			mAdapter = new musicListAdapter(this, listNumbersSave,
					listNamesSave, listTimesSave);
			musicListView.setAdapter(mAdapter);

			// ����֮ǰ����ʾ
			mAdapter.clickItemName = clickItemNameSave;
			mAdapter.selectItemName = selectItemNameSave;
			// mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();

			displayTip("�ɹ����" + addCount + "�׸���...");
		} else {
			displayTip("û��Ҫ��ӵĸ���...");
		}

	}

	// ����ɾ�����и����б���
	private void doDelAllMusicFile() {
		if (listNamesSave.size() > 0) {
			listNamesSave.clear(); // ����б�
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
			displayTip("��ǰ�б�Ϊ��!�������!");
		}
	}

	// ɾ��ĳһ�������ļ�
	void doDelMusicFile() {
		String musicNamePlay = "", delFileName, musicNameNext = "";

		if (mAdapter.clickItemName.length() > 0) {
			// ��ȡҪɾ������Ƶ�ļ�
			delFileName = mAdapter.clickItemName; // + ".mp3";
			Log.d(TAG, "��ǰҪɾ�����ļ�����Ϊ��" + delFileName + ".mp3");

			// ���浱ǰ������
			musicNamePlay = mAdapter.selectItemName;
			if (musicNamePlay.equals(delFileName)) {
				Log.d(TAG, "��ǰɾ������Ϊ��ǰѡ�����!");
				musicNamePlay = "";
			}

			// �������ƣ�������Ӧ��λ��
			int location = listNamesSave.indexOf(delFileName);
			Log.d(TAG, "����λ��Ϊ��" + location);
			if (location >= 0) {
				// ���ҵ���ɾ����Ӧ��¼
				listNamesSave.remove(location);
				listTimesSave.remove(location);

				// �жϵ�ǰɾ�����Ƿ�Ϊ��ǰ����
				if (mAdapter.clickItemName.equals(mAdapter.selectItemName)) {
					// ����ǰ���Ÿ���ɾ��
					mAdapter.selectItemName = ""; // ȡ����������
				}

				// ������ѡ��������һ�ף�Ҫ�ж��Ƿ������һ��
				if (listNamesSave.size() > 0) {
					// ��ǰɾ�������һ��
					if (location == listNamesSave.size()) {
						// ������һ��
						musicNameNext = listNamesSave.get(0);

					} else if (location < listNamesSave.size()) {
						// ������һ��
						musicNameNext = listNamesSave.get(location);
					}

				} else {
					musicNameNext = "";
				}

				// ��������б�
				int position, listSize;

				// ������Ҫ��ʼ��listNumbersSave
				listSize = listNamesSave.size();
				listNumbersSave.clear(); // ���
				for (position = 1; position <= listSize; position++) {
					listNumbersSave.add(Integer.toString(position));
				}

			} else {
				musicNameNext = ""; // ���Ҳ������ÿմ���
			}

			// �½���������������ʾ
			mAdapter = new musicListAdapter(this, listNumbersSave,
					listNamesSave, listTimesSave);
			musicListView.setAdapter(mAdapter);

			// ˢ�»򱣳���ʾ
			// ��ǰ����ɾ����,����ѡ��ָ����һ��
			mAdapter.clickItemName = musicNameNext;
			mAdapter.selectItemName = musicNamePlay;
			mAdapter.notifyDataSetInvalidated();
			// mAdapter.notifyDataSetChanged();

			// ����ǰ��ͼת����һ�׵����ĸ���
			int positionView = listNamesSave.indexOf(mAdapter.clickItemName);
			if (positionView >= 0) {
				// ��ȡ��ǰ��ʾ��ǰ�����һ������
				int lastPosition = musicListView.getLastVisiblePosition();
				int firstPositon = musicListView.getFirstVisiblePosition();

				// �ж��Ƿ�����ͼ��
				if ((positionView > lastPosition)
						|| (positionView < firstPositon)) {
					// ���ǣ���ѡ����Ϊ��ǰ��ͼ
					musicListView.setSelection(positionView);
				}
			}

		} else {
			displayTip("��ǰû��ѡ�е��ļ�...");
		}
	}

	// ʱ���ַ���
	public String toTime(int time) {
		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	// ����������
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
					// ��õ�ǰ���ŵĽ���ֵ
					seekBarPlayProcess.setProgress(musicMediaPlayer
							.getCurrentPosition());
					position = musicMediaPlayer.getCurrentPosition();
					songPlayTimeUpdate(position); // ���½�����
				}

				try {
					ThreadRunFlag = true;
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e(TAG, e.toString());
				}
			}

			ThreadRunFlag = false; // �߳̽�����ʶ
		}
	}

	// ��������ʱ�����
	private void songPlayTimeUpdate(int value) {
		int songTimeMinute;
		int songTimeSecond;

		// ��ms-->s,������벢��ʾ
		value = value / 1000;
		songTimeMinute = value / 60; // ȡ�÷�
		songTimeSecond = value % 60; // ȡ����

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

	// �ж��ļ��Ƿ�ΪUTF8��ʽ
	private static boolean isUft8File(String fileName) {
		boolean result = false;

		java.io.File f = new java.io.File(fileName);

		try {
			java.io.InputStream ios = new java.io.FileInputStream(f);

			byte[] b = new byte[3];
			ios.read(b);
			ios.close();
			if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
				Log.i(TAG, f.getName() + ": ����ΪUTF-8!");
				result = true;
			} else {
				Log.i(TAG, f.getName() + ": ������GBK!");
				result = false;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		return result;
	}

	// ��ʾ��ʾ��Ϣ
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// �����ǰ��������
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
