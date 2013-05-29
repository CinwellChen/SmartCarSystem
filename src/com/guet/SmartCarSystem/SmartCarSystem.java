package com.guet.SmartCarSystem;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.guet.Reader.MainTabActivity;
import com.guet.SmartCarCamera.SmartCarCamera;
import com.guet.SmartCarMap.AMapActivity;
import com.guet.SmartCarMovie.SmartCarMovie;
import com.guet.SmartCarNet.SmartCarNet;
import com.guet.SmartCarPhone.SmartCarPhone;
import com.guet.SmartCarSMS.SmartCarSMS;

//车载信息系统：主页
public class SmartCarSystem extends Activity {
	protected static final String TAG = "Car";

	// 声明GUI控件
	private static Button btnClose; // 退出系统
	private static ImageButton btnMusic; // 进入音频播放器
	private static ImageButton btnMovie; // 进入视频播放器
	private static ImageButton btnMap; // 进入地图导航
	private static ImageButton btnPhone; // 进入电话拨打
	private static ImageButton btnSMS; // 进入短信收发
	private static ImageButton btnNet; // 进入浏览器
	private static ImageButton btnCameara; // 进入倒车摄像
	private static ImageButton btnTXT; // 进入电子书


	// 短信接收
	private static final String mACTION = "android.provider.Telephony.SMS_RECEIVED";
	// 结束倒车摄像广播事件
	private static final String FINISH_CAMEAR_ACTION = "finish_Camera_Action";

	// 创建
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// (1)设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.home); // 显示系统主界面

		initCarComponent(); // 系统GUI控件初始化
		initCarOthers(); // 系统其它初始化工作

		// 注册短信接收倾听
		// 注册短信接收
		IntentFilter recFilter = new IntentFilter(mACTION);
		recFilter.setPriority(1000); // 设置优先级最大
		registerReceiver(mReceiverBroadcastReceiver, recFilter);

		putFuncationName("onCreate");
	}

	// 系统GUI控件初始化
	private void initCarComponent() {
		// 1-1音乐相关控件
		btnClose = (Button) findViewById(R.id.btnClose);
		btnMusic = (ImageButton) findViewById(R.id.ImageButtonMusic);
		btnMovie = (ImageButton) findViewById(R.id.ImageButtonMovie);
		btnMap = (ImageButton) findViewById(R.id.ImageButtonMap);
		btnPhone = (ImageButton) findViewById(R.id.ImageButtonPhone);
		btnSMS = (ImageButton) findViewById(R.id.ImageButtonMessage);
		btnNet = (ImageButton) findViewById(R.id.ImageButtonInternet);
		btnCameara = (ImageButton) findViewById(R.id.ImageButtonCamera);
		btnTXT = (ImageButton) findViewById(R.id.ImageButtonTXT);

		// 1-2事件倾听
		btnClose.setOnClickListener(CarBtnListener);
		btnMusic.setOnClickListener(CarBtnListener);
		btnMovie.setOnClickListener(CarBtnListener);
		btnMap.setOnClickListener(CarBtnListener);
		btnPhone.setOnClickListener(CarBtnListener);
		btnSMS.setOnClickListener(CarBtnListener);
		btnNet.setOnClickListener(CarBtnListener);
		btnCameara.setOnClickListener(CarBtnListener);
		btnTXT.setOnClickListener(CarBtnListener);
	}

	// 多个按钮单击倾听事件
	private OnClickListener CarBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 1进入音频播放器
			case R.id.ImageButtonMusic:
				// 调用"音乐"Activity
				Intent intent1 = new Intent();
				intent1.setClass(SmartCarSystem.this, SmartCarMusic.class);

				// FLAG_ACTIVITY_REORDER_TO_FRONT标志，能防止重复实例化一个Activity
				// 进去后，会跳过"onCreate()",直接到"onRestart()"-->"onStart()"
				intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent1);
				Log.i(TAG, "Car: btnMusic.setOnClickListener");
				break;

			// 2进入视频频播放器
			case R.id.ImageButtonMovie:
				// 调用"视频"Activity
				Intent intent2 = new Intent();
				intent2.setClass(SmartCarSystem.this, SmartCarMovie.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent2);
				Log.i(TAG, "Car: btnMovie.setOnClickListener");
				break;

			// 3进入地图导航
			case R.id.ImageButtonMap:
				// 调用"地图"Activity
				Intent intent3 = new Intent();
				intent3.setClass(SmartCarSystem.this, AMapActivity.class);
				intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent3);
				Log.i(TAG, "Car: btnMap.setOnClickListener");
				break;

			// 4进入电话拨打器
			case R.id.ImageButtonPhone:
				// 弹出电话拨打器
				Intent intent4 = new Intent();
				intent4.setClass(SmartCarSystem.this, SmartCarPhone.class);
				intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent4);
				putLog("电话拨打器已显示!");
				Log.i(TAG, "Car: btnPhone.setOnClickListener");
				break;

			// 5进入短信收发界面
			case R.id.ImageButtonMessage:
				// 调用"短信收发"Activity
				Intent intent5 = new Intent();
				intent5.setClass(SmartCarSystem.this, SmartCarSMS.class);
				intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent5);
				Log.i(TAG, "Car: btnSMS.setOnClickListener");
				break;

			// 6进入浏览器界面
			case R.id.ImageButtonInternet:
				// 调用"浏览器"Activity
				Intent intent6 = new Intent();
				intent6.setClass(SmartCarSystem.this, SmartCarNet.class);
				intent6.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent6);
				Log.i(TAG, "Car: btnNet.setOnClickListener");
				break;

			// 7进入倒车界面
			case R.id.ImageButtonCamera:
				// 调用"倒车摄像"Activity
				Intent intent7 = new Intent();
				intent7.setClass(SmartCarSystem.this, SmartCarCamera.class);
				intent7.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent7);
				Log.i(TAG, "Car: btnCamera.setOnClickListener");
				break;

				// 8进入电子书界面
							case R.id.ImageButtonTXT:
								Intent intent8 = new Intent();
								intent8.setClass(SmartCarSystem.this, MainTabActivity.class);
								intent8.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								startActivity(intent8);
								Log.i(TAG, "Car: btnCamera.setOnClickListener");
								break;
								
			// 退出整个系统
			case R.id.btnClose: {
				doExitWork();
				finish();
			}
				break;

			default:
				break;
			}
		}

	};

	// 执行退出系统删除工作
	private void doExitWork() {
		// 检查系列存在检查文件
		File homeFile2 = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (homeFile2.exists()) {
			homeFile2.delete(); // 存在则删除
			Log.e(TAG, "文件：" + homeFile2.getName() + "已删除!");
		}
	}

	// 系统其它初始化工作
	private void initCarOthers() {
		// 检查“”目录是否已创建，不存在则创建
		File dirFile = new File("/mnt/sdcard/SmartCarSystem");
		if (!dirFile.exists()) {
			try {
				dirFile.mkdirs();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				Log.e(TAG, "创建目录：" + dirFile.getName() + "失败!");
			}
		}

		// 检查系列存在检查文件
		File homeFile1 = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile1.exists()) {
			// 不存在则创建
			try {
				homeFile1.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				Log.e(TAG, "创建文件：" + homeFile1.getName() + "失败!");
			}
		}
	}

	@Override
	protected void onDestroy() {
		putFuncationName("onDestroy");
		super.onDestroy();

		// 注销短信接收倾听
		unregisterReceiver(mReceiverBroadcastReceiver);
	}

	@Override
	protected void onPause() {
		putFuncationName("onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		putFuncationName("onResume");
		super.onResume();
	}

	// 输出当前函数名称
	private void putFuncationName(String name) {
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}

	// 输出调试信息
	private void putLog(String info) {
		Log.d(TAG, info);
	}

	// 短信接收倾听
	private BroadcastReceiver mReceiverBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			putLog("收到新短信1...");

			// TODO Auto-generated method stub
			if (intent.getAction().equals(mACTION)) {
				abortBroadcast(); // 阻止广播，系统不会有提示!!!

				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");
					android.telephony.SmsMessage[] messages = new android.telephony.SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						messages[i] = android.telephony.SmsMessage
								.createFromPdu((byte[]) pdus[i]);
					}

					for (android.telephony.SmsMessage message : messages) {
						String msg = message.getMessageBody();
						String to = message.getOriginatingAddress();

						putLog("新短信：");
						putLog("to:" + to);
						putLog("msg:" + msg);

						// 保存至收件箱
						ContentValues values = new ContentValues();
						values.put("protocol", "0"); // 短信类型
						values.put("read", "0"); // 未读类型
						values.put("address", to);
						values.put("body", msg);
						getContentResolver().insert(
								Uri.parse("content://sms/inbox"), values);

						// 提示
						displayLongTip("您收到一条来自\"" + to + "\"的新短信...");
					}
				}
			}
		}
	};

	// 发送结束摄像头广播，用于倒车
	private void doFinishRearviewMirrorWork() {
		Intent fIntent = new Intent();
		fIntent.setAction(FINISH_CAMEAR_ACTION);
		sendBroadcast(fIntent); // 发送广播事件
	}

	// 显示提示信息short
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// 显示提示信息long
	private void displayLongTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}
}