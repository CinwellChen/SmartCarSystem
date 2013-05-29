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

//������Ϣϵͳ����ҳ
public class SmartCarSystem extends Activity {
	protected static final String TAG = "Car";

	// ����GUI�ؼ�
	private static Button btnClose; // �˳�ϵͳ
	private static ImageButton btnMusic; // ������Ƶ������
	private static ImageButton btnMovie; // ������Ƶ������
	private static ImageButton btnMap; // �����ͼ����
	private static ImageButton btnPhone; // ����绰����
	private static ImageButton btnSMS; // ��������շ�
	private static ImageButton btnNet; // ���������
	private static ImageButton btnCameara; // ���뵹������
	private static ImageButton btnTXT; // ���������


	// ���Ž���
	private static final String mACTION = "android.provider.Telephony.SMS_RECEIVED";
	// ������������㲥�¼�
	private static final String FINISH_CAMEAR_ACTION = "finish_Camera_Action";

	// ����
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// (1)����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.home); // ��ʾϵͳ������

		initCarComponent(); // ϵͳGUI�ؼ���ʼ��
		initCarOthers(); // ϵͳ������ʼ������

		// ע����Ž�������
		// ע����Ž���
		IntentFilter recFilter = new IntentFilter(mACTION);
		recFilter.setPriority(1000); // �������ȼ����
		registerReceiver(mReceiverBroadcastReceiver, recFilter);

		putFuncationName("onCreate");
	}

	// ϵͳGUI�ؼ���ʼ��
	private void initCarComponent() {
		// 1-1������ؿؼ�
		btnClose = (Button) findViewById(R.id.btnClose);
		btnMusic = (ImageButton) findViewById(R.id.ImageButtonMusic);
		btnMovie = (ImageButton) findViewById(R.id.ImageButtonMovie);
		btnMap = (ImageButton) findViewById(R.id.ImageButtonMap);
		btnPhone = (ImageButton) findViewById(R.id.ImageButtonPhone);
		btnSMS = (ImageButton) findViewById(R.id.ImageButtonMessage);
		btnNet = (ImageButton) findViewById(R.id.ImageButtonInternet);
		btnCameara = (ImageButton) findViewById(R.id.ImageButtonCamera);
		btnTXT = (ImageButton) findViewById(R.id.ImageButtonTXT);

		// 1-2�¼�����
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

	// �����ť���������¼�
	private OnClickListener CarBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 1������Ƶ������
			case R.id.ImageButtonMusic:
				// ����"����"Activity
				Intent intent1 = new Intent();
				intent1.setClass(SmartCarSystem.this, SmartCarMusic.class);

				// FLAG_ACTIVITY_REORDER_TO_FRONT��־���ܷ�ֹ�ظ�ʵ����һ��Activity
				// ��ȥ�󣬻�����"onCreate()",ֱ�ӵ�"onRestart()"-->"onStart()"
				intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent1);
				Log.i(TAG, "Car: btnMusic.setOnClickListener");
				break;

			// 2������ƵƵ������
			case R.id.ImageButtonMovie:
				// ����"��Ƶ"Activity
				Intent intent2 = new Intent();
				intent2.setClass(SmartCarSystem.this, SmartCarMovie.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent2);
				Log.i(TAG, "Car: btnMovie.setOnClickListener");
				break;

			// 3�����ͼ����
			case R.id.ImageButtonMap:
				// ����"��ͼ"Activity
				Intent intent3 = new Intent();
				intent3.setClass(SmartCarSystem.this, AMapActivity.class);
				intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent3);
				Log.i(TAG, "Car: btnMap.setOnClickListener");
				break;

			// 4����绰������
			case R.id.ImageButtonPhone:
				// �����绰������
				Intent intent4 = new Intent();
				intent4.setClass(SmartCarSystem.this, SmartCarPhone.class);
				intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent4);
				putLog("�绰����������ʾ!");
				Log.i(TAG, "Car: btnPhone.setOnClickListener");
				break;

			// 5��������շ�����
			case R.id.ImageButtonMessage:
				// ����"�����շ�"Activity
				Intent intent5 = new Intent();
				intent5.setClass(SmartCarSystem.this, SmartCarSMS.class);
				intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent5);
				Log.i(TAG, "Car: btnSMS.setOnClickListener");
				break;

			// 6�������������
			case R.id.ImageButtonInternet:
				// ����"�����"Activity
				Intent intent6 = new Intent();
				intent6.setClass(SmartCarSystem.this, SmartCarNet.class);
				intent6.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent6);
				Log.i(TAG, "Car: btnNet.setOnClickListener");
				break;

			// 7���뵹������
			case R.id.ImageButtonCamera:
				// ����"��������"Activity
				Intent intent7 = new Intent();
				intent7.setClass(SmartCarSystem.this, SmartCarCamera.class);
				intent7.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent7);
				Log.i(TAG, "Car: btnCamera.setOnClickListener");
				break;

				// 8������������
							case R.id.ImageButtonTXT:
								Intent intent8 = new Intent();
								intent8.setClass(SmartCarSystem.this, MainTabActivity.class);
								intent8.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								startActivity(intent8);
								Log.i(TAG, "Car: btnCamera.setOnClickListener");
								break;
								
			// �˳�����ϵͳ
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

	// ִ���˳�ϵͳɾ������
	private void doExitWork() {
		// ���ϵ�д��ڼ���ļ�
		File homeFile2 = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (homeFile2.exists()) {
			homeFile2.delete(); // ������ɾ��
			Log.e(TAG, "�ļ���" + homeFile2.getName() + "��ɾ��!");
		}
	}

	// ϵͳ������ʼ������
	private void initCarOthers() {
		// ��顰��Ŀ¼�Ƿ��Ѵ������������򴴽�
		File dirFile = new File("/mnt/sdcard/SmartCarSystem");
		if (!dirFile.exists()) {
			try {
				dirFile.mkdirs();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				Log.e(TAG, "����Ŀ¼��" + dirFile.getName() + "ʧ��!");
			}
		}

		// ���ϵ�д��ڼ���ļ�
		File homeFile1 = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if (!homeFile1.exists()) {
			// �������򴴽�
			try {
				homeFile1.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				Log.e(TAG, "�����ļ���" + homeFile1.getName() + "ʧ��!");
			}
		}
	}

	@Override
	protected void onDestroy() {
		putFuncationName("onDestroy");
		super.onDestroy();

		// ע�����Ž�������
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

	// �����ǰ��������
	private void putFuncationName(String name) {
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}

	// ���������Ϣ
	private void putLog(String info) {
		Log.d(TAG, info);
	}

	// ���Ž�������
	private BroadcastReceiver mReceiverBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			putLog("�յ��¶���1...");

			// TODO Auto-generated method stub
			if (intent.getAction().equals(mACTION)) {
				abortBroadcast(); // ��ֹ�㲥��ϵͳ��������ʾ!!!

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

						putLog("�¶��ţ�");
						putLog("to:" + to);
						putLog("msg:" + msg);

						// �������ռ���
						ContentValues values = new ContentValues();
						values.put("protocol", "0"); // ��������
						values.put("read", "0"); // δ������
						values.put("address", to);
						values.put("body", msg);
						getContentResolver().insert(
								Uri.parse("content://sms/inbox"), values);

						// ��ʾ
						displayLongTip("���յ�һ������\"" + to + "\"���¶���...");
					}
				}
			}
		}
	};

	// ���ͽ�������ͷ�㲥�����ڵ���
	private void doFinishRearviewMirrorWork() {
		Intent fIntent = new Intent();
		fIntent.setAction(FINISH_CAMEAR_ACTION);
		sendBroadcast(fIntent); // ���͹㲥�¼�
	}

	// ��ʾ��ʾ��Ϣshort
	private void displayTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}

	// ��ʾ��ʾ��Ϣlong
	private void displayLongTip(String tipStr) {
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}
}