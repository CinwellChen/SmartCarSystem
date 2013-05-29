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
	// �绰����ؼ�
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
	int cursorPostion; // �༭����ʹ��

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.phonehome);
		initPhoneCompent(); // ��ʼ���绰������
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
	protected void onResume() {
		super.onResume();
		putFuncationName("onResume");
	}

	// ��ʼ���绰����ؼ�
	void initPhoneCompent() {
		// �����Ӵ���
		// phoneView = getLayoutInflater().inflate(R.layout.phonehome, null);
		// phonePopupWindow = new PopupWindow(phoneView,
		// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// �������ԣ�ע��һ��Ҫ��showǰ���ò���Ч!!!
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

		// ����¼�����
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

		// �ο���ʾ��Ԫ��
		// imageViewReference = (ImageView) findViewById(R.id.imageView1);
	}

	private OnLongClickListener phoneBtnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			editTextPhomeNumber.setText(null);
			return false;
		}
	};

	// �绰�������¼�����
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

			case R.id.buttonCallPhone: // ����绰
				doCallPhoneWork();
				break;
			case R.id.buttonPhoneReturn: // ����
				// if (isPhoneViewShow) {
				// phonePopupWindow.dismiss();
				// isPhoneViewShow = false;
				// ����"home"Activity
				finish();
				/*Intent intent = new Intent();
				intent.setClass(SmartCarPhone.this, SmartCarSystem.class);

				// FLAG_ACTIVITY_REORDER_TO_FRONT��־���ܷ�ֹ�ظ�ʵ����һ��Activity
				// ��ȥ�󣬻�����"onCreate()",ֱ�ӵ�"onRestart()"-->"onStart()"
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

				startActivity(intent);*/

				// SmartCarMusic.this.finish(); ������finish���ᴥ��onDestroy();
				putFuncationName("returnHome");
				putLog("�绰������������!");
				// }
				break;

			case R.id.ButtonPhoneDel: // ɾ��
				cursorPostion = editTextPhomeNumber.getSelectionStart();
				if (cursorPostion > 0)
					editTextPhomeNumber.getText().delete(cursorPostion - 1,
							cursorPostion);
				break;
			default:
				break;
			}

			// ���º�����ʾ
			if (tempStr != null) {
				// �ڹ�괦�����ַ�
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

	// �����绰�¼���ʼ��
	private void setPhoneStateListener() {
		telLis = new MyPhoneStateListener();
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(telLis, PhoneStateListener.LISTEN_CALL_STATE);
	}

	// �绰�¼�������д
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
						isCalled = false; // ��ʶ��绰����
						Intent it = getIntent();
						it.setAction(Intent.ACTION_MAIN);
						it.putExtra("isGoBack", true); // ��ʶ�Ƿ��أ�����OnCreate()
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

	// ִ�е绰������
	private void doCallPhoneWork() {
		// ��ȡ�绰����
		String strInput = editTextPhomeNumber.getText().toString();
		if (strInput.length() > 0) {
			putLog("��ǰ�����ĵ绰����Ϊ��" + strInput);
			isCalled = true;
			setPhoneStateListener(); // ��������
			dial(strInput); // ���õ绰�������񣬲��ر�ϵͳ
		} else {
			// ������绰����
			displayTip("��ǰ������绰����!");
		}

		putFuncationName("doCallPhoneWork");
	}

	// ִ�е绰������ϵͳ��������
	public void dial(String telNum) {
		// doExitWork(); // ��ֹ������Activity������������Ӱ�죬�ر�
		Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
		startActivity(it);
		//finish();
		putFuncationName("dial");
	}

	// ���������Ϣ
	private void putLog(String info) {
		Log.d(TAG, info);
	}

	// ��ʾ��ʾ��Ϣlong
	@SuppressWarnings("unused")
	private void displayLongTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}

	// ��ʾ��ʾ��Ϣlong
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// �����ǰ��������
	private void putFuncationName(String name) {
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}
}
