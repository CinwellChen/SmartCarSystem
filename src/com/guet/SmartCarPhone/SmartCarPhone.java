package com.guet.SmartCarPhone;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.guet.SmartCarSystem.R;
import com.guet.SmartCarSystem.SmartCarSystem;

public class SmartCarPhone extends Activity {

	private static final String TAG = "Car";
	// 电话拨打控件
	private EditText editTextPhomeNumber;
	private ImageButton buttonPhoneNum1, buttonPhoneNum0, buttonPhoneNum2,
			buttonPhoneNum3, buttonPhoneNum4, buttonPhoneNum5, buttonPhoneNum6,
			buttonPhoneNum7, buttonPhoneNum8, buttonPhoneNum9, buttonCallPhone,
			 buttonPhoneDelete;

	private Button buttonPhoneReturn;
	PhoneStateListener telLis;
	TelephonyManager tm;
	boolean isGoBack = false, isCalled = false;
	Editable editablePhone;
	int cursorPostion; // 编辑号码使用

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.phonehome);
		initPhoneCompent(); // 初始化电话拨打器
		putFuncationName("onCreate");
	}

	@Override
	protected void onDestroy() {
		putFuncationName("onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		putFuncationName("onPause");
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
	protected void onResume() {
		super.onResume();
		putFuncationName("onResume");
	}

	// 初始化电话拨打控件
	void initPhoneCompent() {
		// 搜索子窗口
		// phoneView = getLayoutInflater().inflate(R.layout.phonehome, null);
		// phonePopupWindow = new PopupWindow(phoneView,
		// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// 设置属性，注：一定要在show前设置才生效!!!
		// phonePopupWindow.setFocusable(true);
		// phonePopupWindow.setTouchable(true);

		editTextPhomeNumber = (EditText) findViewById(R.id.editTextOutPhoneNumber);
		editTextPhomeNumber.setInputType(InputType.TYPE_NULL);
		buttonPhoneNum0 = (ImageButton) findViewById(R.id.buttonPhoneNum0);
		buttonPhoneNum1 = (ImageButton) findViewById(R.id.buttonPhoneNum1);
		buttonPhoneNum2 = (ImageButton) findViewById(R.id.buttonPhoneNum2);
		buttonPhoneNum3 = (ImageButton) findViewById(R.id.buttonPhoneNum3);
		buttonPhoneNum4 = (ImageButton) findViewById(R.id.buttonPhoneNum4);
		buttonPhoneNum5 = (ImageButton) findViewById(R.id.buttonPhoneNum5);
		buttonPhoneNum6 = (ImageButton) findViewById(R.id.buttonPhoneNum6);
		buttonPhoneNum7 = (ImageButton) findViewById(R.id.buttonPhoneNum7);
		buttonPhoneNum8 = (ImageButton) findViewById(R.id.buttonPhoneNum8);
		buttonPhoneNum9 = (ImageButton) findViewById(R.id.buttonPhoneNum9);
		buttonPhoneDelete = (ImageButton) findViewById(R.id.ButtonPhoneDel);
		buttonCallPhone = (ImageButton) findViewById(R.id.buttonCallPhone);
		buttonPhoneReturn = (Button) findViewById(R.id.buttonPhoneReturn);

		// 添加事件倾听
		buttonPhoneNum0.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum1.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum2.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum3.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum4.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum5.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum6.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum7.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum8.setOnClickListener(phoneBtnClickListener);
		buttonPhoneNum9.setOnClickListener(phoneBtnClickListener);
		buttonPhoneDelete.setOnClickListener(phoneBtnClickListener);
		buttonPhoneDelete.setOnLongClickListener(phoneBtnLongClickListener);
		buttonCallPhone.setOnClickListener(phoneBtnClickListener);
		buttonPhoneReturn.setOnClickListener(phoneBtnClickListener);

		// 参考显示主元件
		// imageViewReference = (ImageView) findViewById(R.id.imageView1);
	}

	private OnLongClickListener phoneBtnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			editTextPhomeNumber.setText(null);
			return false;
		}
	};

	// 电话拨号器事件倾听
	private OnClickListener phoneBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String tempStr = null;

			switch (v.getId()) {
			case R.id.buttonPhoneNum0:
				tempStr = "0";
				break;
			case R.id.buttonPhoneNum1:
				tempStr = "1";
				break;
			case R.id.buttonPhoneNum2:
				tempStr = "2";
				break;
			case R.id.buttonPhoneNum3:
				tempStr = "3";
				break;
			case R.id.buttonPhoneNum4:
				tempStr = "4";
				break;
			case R.id.buttonPhoneNum5:
				tempStr = "5";
				break;
			case R.id.buttonPhoneNum6:
				tempStr = "6";
				break;
			case R.id.buttonPhoneNum7:
				tempStr = "7";
				break;
			case R.id.buttonPhoneNum8:
				tempStr = "8";
				break;
			case R.id.buttonPhoneNum9:
				tempStr = "9";
				break;

			case R.id.buttonCallPhone: // 拨打电话
				doCallPhoneWork();
				break;
			case R.id.buttonPhoneReturn: // 返回
				// if (isPhoneViewShow) {
				// phonePopupWindow.dismiss();
				// isPhoneViewShow = false;
				// 调用"home"Activity
				finish();
				/*Intent intent = new Intent();
				intent.setClass(SmartCarPhone.this, SmartCarSystem.class);

				// FLAG_ACTIVITY_REORDER_TO_FRONT标志，能防止重复实例化一个Activity
				// 进去后，会跳过"onCreate()",直接到"onRestart()"-->"onStart()"
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

				startActivity(intent);*/

				// SmartCarMusic.this.finish(); 不能用finish，会触发onDestroy();
				putFuncationName("returnHome");
				putLog("电话拨打器已隐藏!");
				// }
				break;

			case R.id.ButtonPhoneDel: // 删除
				cursorPostion = editTextPhomeNumber.getSelectionStart();
				if (cursorPostion > 0)
					editTextPhomeNumber.getText().delete(cursorPostion - 1,
							cursorPostion);
				break;
			default:
				break;
			}

			// 更新号码显示
			if (tempStr != null) {
				// 在光标处插入字符
				int index = editTextPhomeNumber.getSelectionStart();
				editablePhone = editTextPhomeNumber.getEditableText();
				if (index < 0 || index >= editablePhone.length()) {
					editablePhone.append(tempStr);
					// cursorPostion=editablePhone.length();
				} else {
					editablePhone.insert(index, tempStr);
				}
			}
		}
	};

	// 倾听电话事件初始化
	private void setPhoneStateListener() {
		telLis = new MyPhoneStateListener();
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(telLis, PhoneStateListener.LISTEN_CALL_STATE);
	}

	// 电话事件倾听重写
	class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (isCalled) {
					ActivityManager am = (ActivityManager) getApplicationContext()
							.getSystemService(Context.ACTIVITY_SERVICE);
					List<RunningTaskInfo> forGroundActv = am.getRunningTasks(1);
					RunningTaskInfo currentActv = forGroundActv.get(0);
					String actvName = currentActv.topActivity.getClassName();

					if ("com.android.phone.InCallScreen"
							.equalsIgnoreCase(actvName)) {
						isCalled = false; // 标识打电话结束
						Intent it = getIntent();
						it.setAction(Intent.ACTION_MAIN);
						it.putExtra("isGoBack", true); // 标识是返回，传给OnCreate()
						startActivity(it);
						Log.w(TAG, " -- CALL_STATE_IDLE -->Over ");
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.w(TAG, " -- CALL_STATE_OFFHOOK ");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				Log.w(TAG, " -- CALL_STATE_RINGING ");
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	}

	// 执行电话拨打动作
	private void doCallPhoneWork() {
		// 获取电话号码
		String strInput = editTextPhomeNumber.getText().toString();
		if (strInput.length() > 0) {
			putLog("当前拨出的电话号码为：" + strInput);
			isCalled = true;
			setPhoneStateListener(); // 启动监听
			dial(strInput); // 调用电话拨出服务，并关闭系统
		} else {
			// 无输入电话号码
			displayTip("当前无输入电话号码!");
		}

		putFuncationName("doCallPhoneWork");
	}

	// 执行电话拨出、系统结束事务
	public void dial(String telNum) {
		// doExitWork(); // 防止其它子Activity在整个过程中影响，关闭
		Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
		startActivity(it);
		//finish();
		putFuncationName("dial");
	}

	// 输出调试信息
	private void putLog(String info) {
		Log.d(TAG, info);
	}

	// 显示提示信息long
	@SuppressWarnings("unused")
	private void displayLongTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}

	// 显示提示信息long
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// 输出当前函数名称
	private void putFuncationName(String name) {
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}
}
