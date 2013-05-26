package com.guet.Reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.guet.Reader.Utils.ColorPickerDialog;
import com.guet.Reader.Utils.ColorPickerDialog.OnColorChangedListener;
import com.guet.Reader.Utils.ZoomTextView;
import com.guet.Reader.db.DbHelper;
import com.guet.SmartCarSystem.R;

public class TxtReaderActivity extends Activity {
	private String content;
	private final float zoomScale = 1.8f;// ���ű���
	private TextView tv;
	private String filePath;
	private static final String GBK = "GBK";
	private SeekBar seekBar;
	private TextView SeekBar_Value;
	private static final String utf8 = "UTF-8";
	private TextView Line_Value;
	protected static final int END_COMPUTE_PROGRESS = 5;
	private ScrollView scrollView;
	private final String NAME = "LEOYFJ";
	SharedPreferences sharedPreferences;
	private float textSize = 30;// ���ִ�С
	public int color;// ������ɫ
	private int currentY;// ��ǩλ��
	final int SCROLL_STEP = 2; // �Զ������Ĳ���
	final int BEGIN_SCROLL = 1; // ��ʼ����
	final int END_SCROLL = 2; // ��ֹ����
	final int STOP_SCROLL = 3; // ��������
	final int COMPUTE_PROGRESS = 4; // �����Ķ�����
	private boolean isAutoScrolling = false;
	private TextView title;
	private TextView titlepercent;
	private ColorPickerDialog colorPickerDialog;
	private Editor editor;
	private DbHelper dbHelper;
	protected int progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.txtreader);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		handler.sendEmptyMessageDelayed(COMPUTE_PROGRESS, 200);
		sharedPreferences = getSharedPreferences(NAME, 0);
		editor = sharedPreferences.edit();
		title = (TextView) findViewById(R.id.titlefilename);
		titlepercent = (TextView) findViewById(R.id.titlepercent);
		dbHelper = new DbHelper(this, "TxtReader_db", 1);
		Bundle bunde = getIntent().getExtras();
		filePath = bunde.getString("key");
		refreshGUI();
	}

	private void insertOrUpdateData(SQLiteDatabase db, String path,
			int scrollY, int height) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("path", path);
		contentValues.put("progress", calReadPercent());
		Cursor cursor = db.rawQuery("select * from HistoryList", null);
		if (cursor.moveToFirst()) {
			do {
				if (cursor.getString(cursor.getColumnIndex("path")).equals(
						filePath)) {
					db.update("HistoryList", contentValues, "path=?",
							new String[] { filePath });
					return;
				}
			} while (cursor.moveToNext());
			System.out.println(db.insert("HistoryList", null, contentValues));
		} else {
			db.insert("HistoryList", null, contentValues);
		}
		cursor.close();
	}

	/**
	 * ���������С,��ǩλ��,��ɫ
	 */
	public void saveData() {
		currentY = scrollView.getScrollY();// �����ǩλ��
		//textSize = tv.getTextSize();// ��������С
		editor.putInt(filePath + "currentY", currentY);// ���浱ǰ�ļ���ǩλ��
		editor.putInt(filePath + "color", color);// ������ɫ
		editor.putFloat(filePath + "textSize", textSize);// ���浱ǰ�ļ������С
		editor.commit();
		insertOrUpdateData(dbHelper.getWritableDatabase(), filePath, currentY,
				tv.getHeight() / tv.getLineCount());
	}

	public int readBookMark() {
		int pos = sharedPreferences.getInt(filePath + "currentY", 0);
		return pos;
	}

	/*public float readTextSize() {
		float size = sharedPreferences.getFloat(filePath + "textSize", 25);
		return size;
	}*/

	public int readColor() {
		color = sharedPreferences.getInt(filePath + "color", Color.BLACK);
		return color;
	}

	@Override
	protected void onDestroy() {
		saveData();
		pauseCalProgress();
		if (dbHelper != null)
			dbHelper.close();
		super.onDestroy();
	}

	/**
	 * ��ͣ�����Ķ�����
	 */
	private void pauseCalProgress() {
		handler.sendEmptyMessage(END_SCROLL);
		handler.sendEmptyMessage(END_COMPUTE_PROGRESS);
	}

	@Override
	protected void onPause() {
		pauseCalProgress();
		super.onPause();
	}

	@Override
	protected void onResume() {
		resumeCalProgress();
		super.onResume();
	}

	private void resumeCalProgress() {
		handler.sendEmptyMessage(COMPUTE_PROGRESS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.reader_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.changebg:
			AlertDialog.Builder bgdialog = new AlertDialog.Builder(this);
			bgdialog.setTitle("��ѡ�񱳾���ʽ");
			bgdialog.setItems(R.array.bgstyle, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						scrollView.setBackgroundResource(R.drawable.huanbao);
						break;
					case 1:
						scrollView.setBackgroundResource(R.drawable.yangpi);
						break;
					case 2:
						scrollView.setBackgroundResource(R.drawable.yangyan);
						break;
					case 3:
						scrollView.setBackgroundResource(R.drawable.menghuan);
						break;
					case 4:
						scrollView.setBackgroundResource(R.drawable.shimu);
						break;
					case 5:
						scrollView.setBackgroundResource(R.drawable.fanggu);
						break;
					}
				}
			});
			bgdialog.show();
			break;
		case R.id.changecolor:
			colorPickerDialog = new ColorPickerDialog(this, "��ѡ������������ɫ",
					new OnColorChangedListener() {

						public void colorChanged(int color) {
							tv.setTextColor(color);
							TxtReaderActivity.this.color = color;
						}
					});
			colorPickerDialog.show();
			break;
		case R.id.auto_scroll:
			isAutoScrolling = !isAutoScrolling;
			if (isAutoScrolling) {
				handler.sendEmptyMessage(BEGIN_SCROLL);
			} else {
				handler.sendEmptyMessage(STOP_SCROLL);
			}
			break;
		case R.id.goto_line:// ����goto��
			AlertDialog.Builder builder = new Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.seekbar_goto, null);
			builder.setView(layout).setTitle("��ѡ����ת��λ��")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("ȷ��", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							scrollView.scrollTo(0,
									progress * tv.getLineHeight());
						}
					}).setNegativeButton("ȡ��", null).show();
			Line_Value = (TextView) layout.findViewById(R.id.Line_Value);
			SeekBar_Value = (TextView) layout.findViewById(R.id.SeekBar_Value);
			seekBar = (SeekBar) layout.findViewById(R.id.SeekBar);
			seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
			seekBar.setMax(tv.getLineCount());
			seekBar.setProgress((int) ((double) scrollView.getScrollY() / tv
					.getLineHeight()));
			break;
		default:
			refreshGUI(utf8);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public class SeekBarChangeListener implements OnSeekBarChangeListener {

		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			TxtReaderActivity.this.progress = progress;
			Line_Value.setText("ת����" + progress + "��");
			double value = (double) progress / tv.getLineCount();
			SeekBar_Value.setText((int) (value * 100) + "%");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("��ܰ��ʾ").setMessage("ȷ���˳��Ķ�?")
					.setPositiveButton("ȷ��", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton("ȡ��", null).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void settext() {
		tv = (TextView) findViewById(R.id.txt);
		tv.setText(content);
		new ZoomTextView(tv, zoomScale);// ʵ��������������
		//tv.setTextSize(readTextSize());
		tv.setTextColor(readColor());
		title.setText(filePath);
		scrollView = (ScrollView) findViewById(R.id.scrollview);
		scrollView.post(new Runnable() {

			public void run() {
				scrollView.scrollTo(0, readBookMark());
			}
		});
	}

	/**
	 * �Ѷ�ȡ����������ʾ����(Ĭ��)
	 */
	public void refreshGUI() {
		content = readFile(getFileISR());
		settext();
	}

	/**
	 * �Ѷ�ȡ����������ʾ����
	 * 
	 * @param code����
	 */
	public void refreshGUI(String code) {
		content = readFile(getFileISR(code));
		settext();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BEGIN_SCROLL:
				if (scrollView.getScrollY() >= tv.getLineCount()
						* tv.getLineHeight()) {
					handler.sendEmptyMessage(END_SCROLL);
				} else {
					scrollView.scrollTo(0, scrollView.getScrollY()
							+ SCROLL_STEP);
					handler.sendEmptyMessageDelayed(BEGIN_SCROLL, 10);
				}
				break;
			case END_SCROLL:
				// �Ѿ��������ײ�
				handler.removeMessages(STOP_SCROLL);
				handler.removeMessages(BEGIN_SCROLL);
				break;
			case STOP_SCROLL:
				// �û��жϹ���
				handler.removeMessages(END_SCROLL);
				handler.removeMessages(BEGIN_SCROLL);
				break;
			case COMPUTE_PROGRESS:
				titlepercent.setText(calReadPercent());
				handler.sendEmptyMessageDelayed(COMPUTE_PROGRESS, 200);
				break;
			case END_COMPUTE_PROGRESS:
				handler.removeMessages(COMPUTE_PROGRESS);
				break;
			}

		}
	};

	/**
	 * �����Ķ�����
	 * 
	 * @return ���Ȱٷ���
	 */
	public String calReadPercent() {
		DecimalFormat df = new DecimalFormat();
		int a = (int) ((double) scrollView.getScrollY() / tv.getHeight() * tv
				.getLineCount());
		df.applyPattern("##.#%");
		return df.format((double) a / tv.getLineCount());
	}

	/**
	 * ��ȡ������(Ĭ��)
	 * 
	 * @return
	 */
	public InputStreamReader getFileISR() {
		try {
			FileInputStream fInputStream = new FileInputStream(filePath);
			String code = getEncoding(filePath);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fInputStream, code);
			return inputStreamReader;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��ȡֵ�ñ����������
	 * 
	 * @param code
	 *            ����
	 * @return ������
	 */
	public InputStreamReader getFileISR(String code) {
		try {
			FileInputStream fInputStream = new FileInputStream(filePath);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fInputStream, code);
			return inputStreamReader;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��ȡtxt�ļ�
	 * 
	 * @param inputStreamReader
	 *            ������
	 * @return
	 */
	public String readFile(InputStreamReader inputStreamReader) {
		try {
			StringBuffer SB = new StringBuffer();
			BufferedReader in = new BufferedReader(inputStreamReader);
			if (!new File(filePath).exists()) {
				return null;
			}
			while (in.ready()) {
				SB.append(in.readLine() + "\n");
			}
			in.close();
			return SB.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �жϱ���
	 * 
	 * @param filePath
	 *            ·��
	 * @return ����
	 * @throws Exception
	 */
	public String getEncoding(String filePath) throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				filePath));
		int p = (bin.read() << 8) + bin.read();
		String code = null;
		switch (p) {
		case 0xefbb:
			code = utf8;
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = GBK;
		}
		return code;
	}

}
