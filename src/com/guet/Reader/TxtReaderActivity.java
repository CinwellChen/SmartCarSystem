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
	private final float zoomScale = 1.8f;// 缩放比例
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
	private float textSize = 30;// 文字大小
	public int color;// 文字颜色
	private int currentY;// 书签位置
	final int SCROLL_STEP = 2; // 自动滚动的步长
	final int BEGIN_SCROLL = 1; // 开始滚屏
	final int END_SCROLL = 2; // 终止滚屏
	final int STOP_SCROLL = 3; // 结束滚屏
	final int COMPUTE_PROGRESS = 4; // 计算阅读进度
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
	 * 保存字体大小,书签位置,颜色
	 */
	public void saveData() {
		currentY = scrollView.getScrollY();// 获得书签位置
		//textSize = tv.getTextSize();// 获得字体大小
		editor.putInt(filePath + "currentY", currentY);// 保存当前文件书签位置
		editor.putInt(filePath + "color", color);// 保存颜色
		editor.putFloat(filePath + "textSize", textSize);// 保存当前文件字体大小
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
	 * 暂停计算阅读进度
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
			bgdialog.setTitle("请选择背景样式");
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
			colorPickerDialog = new ColorPickerDialog(this, "请选择正文字体颜色",
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
		case R.id.goto_line:// 设置goto行
			AlertDialog.Builder builder = new Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.seekbar_goto, null);
			builder.setView(layout).setTitle("请选择跳转的位置")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("确定", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							scrollView.scrollTo(0,
									progress * tv.getLineHeight());
						}
					}).setNegativeButton("取消", null).show();
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
			Line_Value.setText("转到第" + progress + "行");
			double value = (double) progress / tv.getLineCount();
			SeekBar_Value.setText((int) (value * 100) + "%");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("温馨提示").setMessage("确认退出阅读?")
					.setPositiveButton("确定", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton("取消", null).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void settext() {
		tv = (TextView) findViewById(R.id.txt);
		tv.setText(content);
		new ZoomTextView(tv, zoomScale);// 实例化手势缩放类
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
	 * 把读取到的文字显示出来(默认)
	 */
	public void refreshGUI() {
		content = readFile(getFileISR());
		settext();
	}

	/**
	 * 把读取到的文字显示出来
	 * 
	 * @param code编码
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
				// 已经滚动到底部
				handler.removeMessages(STOP_SCROLL);
				handler.removeMessages(BEGIN_SCROLL);
				break;
			case STOP_SCROLL:
				// 用户中断滚屏
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
	 * 计算阅读进度
	 * 
	 * @return 进度百分数
	 */
	public String calReadPercent() {
		DecimalFormat df = new DecimalFormat();
		int a = (int) ((double) scrollView.getScrollY() / tv.getHeight() * tv
				.getLineCount());
		df.applyPattern("##.#%");
		return df.format((double) a / tv.getLineCount());
	}

	/**
	 * 获取输入流(默认)
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
	 * 获取值得编码的输入流
	 * 
	 * @param code
	 *            编码
	 * @return 输入流
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
	 * 读取txt文件
	 * 
	 * @param inputStreamReader
	 *            输入流
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
	 * 判断编码
	 * 
	 * @param filePath
	 *            路径
	 * @return 编码
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
