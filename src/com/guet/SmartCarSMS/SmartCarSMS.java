package com.guet.SmartCarSMS;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.guet.SmartCarSystem.R;

//������Ϣϵͳ��5���֣������շ���
public class SmartCarSMS extends Activity {
	private static final String TAG = "Car";

	// 1������
	private Button buttonMessageSendPage;
	private Button buttonMessageLookNewPage;
	private Button buttonMessageLookOldPage;
	private Button buttonMessageReturnPage;
	private View viewMessageHome;
	private boolean isMessageHomeShow;

	// 2���ŷ����ӽ���
	private Button buttonNewPageSend;
	private Button buttonNewPageClear;
	private Button buttonNewPageReturn;
	private EditText editTextMessageSendNumber;
	private EditText editTextMessageSendContex;
	private View viewMessageSend;
	private boolean isMessageSendShow;

	// 3���Ų鿴�ӽ���
	private EditText editTextMessageBoxPerson;
	private EditText editTextMessageBoxDate;
	private EditText editTextMessageBoxContex;
	private Button buttonLookPageSend;
	private Button buttonLookPageDel;
	private Button buttonLookPageReturn;
	private ListView listViewMessageLook;
	private View viewMessageLook;
	private boolean isMessageLookShow;

	// �������Ͳ鿴����
	private static final int MESSAGE_OLD = 1; // �Ѷ�����
	private static final int MESSAGE_NEW = 2; // δ������
	private int messageLookStype;

	Cursor messageLookNewCursor;
	Cursor messageLookOldCursor;
	private boolean isLookNewCursorOpen = false;
	private boolean isLookOldCursorOpen = false;

	private List<String> messageListSendIdSave = new ArrayList<String>(); // ��������汾
	private List<String> messageListSendId = new ArrayList<String>();
	private List<String> messageListSendPerson = new ArrayList<String>();

	smsListAdapter mAdapter; // ��ʾ������
	private String messageListClick = "-1";
	private int currentItemPositon = -1;

	// �����ط�
	private PopupWindow reSendPopupWindow;
	private View reSendView;
	private boolean isReSendViewShow = false;
	private EditText editTextRePhone;
	private Button buttonReConfirm;
	private Button buttonReCancel;

	// ���ŷ��͡�����ɹ�����
	private String SEND_SMS_ACTION = "SEND_SMS_ACTION";
	private String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	private Intent sendIntent, deliverIntent;
	private PendingIntent sentPI, deliverPI;
	private android.telephony.SmsManager mSmsManager;
	private boolean isInSendMessage = false; // ���Ͷ��ű�ʶ

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		initGuiComponent(); // ��ʼ��GUI�ؼ�
		initGuiEvent(); // ��ʼ���ؼ��¼�
		messageSendReceivedInit(); // ��ʼ�����ŷ��͡����ͳɹ������¼�

		putFuncationName("onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 1�رյ�������
		if (reSendPopupWindow.isShowing())
			reSendPopupWindow.dismiss();

		// �ر����ݿ���
		if (isLookNewCursorOpen)
			messageLookNewCursor.close();
		if (isLookOldCursorOpen)
			messageLookOldCursor.close();

		// �崰�б�
		messageListSendId.clear();
		messageListSendIdSave.clear();
		messageListSendPerson.clear();

		// ע����ض�������
		unregisterReceiver(mSendBroadcastReceiver01);
		unregisterReceiver(mSendBroadcastReceiver02);

		putFuncationName("onDestroy");
	}

	// ��ʼ���ؼ�
	private void initGuiComponent() {
		// 1��ҳ
		viewMessageHome = getLayoutInflater().inflate(R.layout.smsmain, null);
		setContentView(viewMessageHome);
		isMessageHomeShow = true;

		buttonMessageSendPage = (Button) findViewById(R.id.buttonMessageSendPage);
		buttonMessageLookNewPage = (Button) findViewById(R.id.buttonMessageLookNewPage);
		buttonMessageLookOldPage = (Button) findViewById(R.id.buttonMessageLookOldPage);
		buttonMessageReturnPage = (Button) findViewById(R.id.buttonMessageReturnPage);

		// 2���ͽ���
		viewMessageSend = getLayoutInflater().inflate(R.layout.messagesend,
				null);
		isMessageSendShow = false;
		// 3���Ų鿴�ӽ�����ҳ
		viewMessageLook = getLayoutInflater().inflate(R.layout.messagelook,
				null);
		isMessageLookShow = false;

		// ת������
		reSendView = getLayoutInflater().inflate(R.layout.messageresend, null);
		reSendPopupWindow = new PopupWindow(reSendView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		reSendPopupWindow.setFocusable(true);
		reSendPopupWindow.setTouchable(true);

		buttonReConfirm = (Button) reSendView
				.findViewById(R.id.buttonMessageReConfirm);
		buttonReCancel = (Button) reSendView
				.findViewById(R.id.buttonMessageReCancel);
		editTextRePhone = (EditText) reSendView
				.findViewById(R.id.editTextMessageRePerson);
	}

	// ��ʼ���ؼ��¼�
	private void initGuiEvent() {
		// 1��ҳ
		buttonMessageSendPage.setOnClickListener(btnOnClickListener);
		buttonMessageLookNewPage.setOnClickListener(btnOnClickListener);
		buttonMessageLookOldPage.setOnClickListener(btnOnClickListener);
		buttonMessageReturnPage.setOnClickListener(btnOnClickListener);

		// ��������
		buttonReConfirm.setOnClickListener(btnOnClickListener);
		buttonReCancel.setOnClickListener(btnOnClickListener);
	}

	// ��ť�����¼�����
	private OnClickListener btnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 1��ҳ
			case R.id.buttonMessageSendPage: // ��ת��������ҳ
				if (isMessageHomeShow && !isMessageSendShow) {
					isMessageHomeShow = false;
					setContentView(viewMessageSend);
					isMessageSendShow = true;

					if (buttonNewPageSend == null) {
						buttonNewPageSend = (Button) findViewById(R.id.buttonSendOk);
						buttonNewPageClear = (Button) findViewById(R.id.buttonSendClear);
						buttonNewPageReturn = (Button) findViewById(R.id.buttonSendReturn);
						editTextMessageSendNumber = (EditText) findViewById(R.id.editTextMessageSendNumber);
						editTextMessageSendContex = (EditText) findViewById(R.id.editTextMessageSendContex);

						// 2���ͽ���
						buttonNewPageSend
								.setOnClickListener(btnOnClickListener);
						buttonNewPageClear
								.setOnClickListener(btnOnClickListener);
						buttonNewPageReturn
								.setOnClickListener(btnOnClickListener);
					}
					putLog("������ŷ���ҳ��");
				}
				break;
			case R.id.buttonMessageLookNewPage: // ���Ų鿴�ӽ���(δ������)
				putLog("buttonMessageLookNewPage");
				messageLookWindowShow(MESSAGE_NEW);
				break;

			case R.id.buttonMessageLookOldPage: // ���Ų鿴�ӽ���(�Ѷ�����)
				putLog("buttonMessageLookOldPage");
				messageLookWindowShow(MESSAGE_OLD);
				break;
			case R.id.buttonMessageReturnPage: // ����SmartCar��ҳ
				finish();
				break;

			// 2���ͽ���
			case R.id.buttonSendOk: // ���ŷ��Ͳ���
				doMessageSendWork(editTextMessageSendNumber.getText()
						.toString(), editTextMessageSendContex.getText()
						.toString());
				putLog("buttonNewPageSend");

				break;
			case R.id.buttonSendClear: // ��ղ���
				editTextMessageSendNumber.setText(null);
				editTextMessageSendContex.setText(null);
				putLog("buttonNewPageClear");
				break;
			case R.id.buttonSendReturn: // ����һ�ϼ�
				putLog("buttonNewPageReturn");
				if (!isMessageHomeShow && isMessageSendShow) {
					isMessageSendShow = false;
					setContentView(viewMessageHome);
					isMessageHomeShow = true;
					putLog("�ӷ���ҳ�淵��");
				}
				break;

			// ���Ų鿴ҳ��
			case R.id.buttonMessageLookSend: // ת��
				messsageReSendWindowShow(); // ����ת������
				putLog("buttonMessageLookSend");
				break;
			case R.id.buttonMessageLookDel:
				putLog("buttonMessageLookDel");
				doMessageDelWork(); // ִ�е�ǰ����ɾ������
				break;
			case R.id.buttonMessageLookReturn: // ������һ��
				putLog("buttonMessageLookReturn");
				if (!isMessageHomeShow && isMessageLookShow) {
					isMessageLookShow = false;
					setContentView(viewMessageHome);
					isMessageHomeShow = true;
					putLog("�Ӳ鿴ҳ�淵��");

					// �ر����ݿ�
					if (isLookNewCursorOpen)
						messageLookNewCursor.close();
					if (isLookOldCursorOpen)
						messageLookOldCursor.close();
					isLookNewCursorOpen = false;
					isLookOldCursorOpen = false;
				}
				break;

			// ��������
			case R.id.buttonMessageReConfirm:
				if (editTextRePhone.getText().length() > 0) {
					// �رմ���
					reSendPopupWindow.dismiss();
					isReSendViewShow = false;

					// ���ж���ת��
					doMessageSendWork(editTextRePhone.getText().toString(),
							editTextMessageBoxContex.getText().toString());
				} else {
					displayTip("���������ռ��˺���!");
				}
				break;
			case R.id.buttonMessageReCancel:
				if (reSendPopupWindow.isShowing()) {
					reSendPopupWindow.dismiss();
					isReSendViewShow = false;
				}
				break;
			default:
				break;
			}
		}
	};

	// ִ�ж���ת��
	private void messsageReSendWindowShow() {
		if (editTextMessageBoxContex.getText().length() > 0) {
			if (reSendPopupWindow.isShowing()) {
				putLog("��������ʾ....");
			} else {
				putLog("���ڳ�ʼ����������....");
				reSendPopupWindow.showAtLocation(listViewMessageLook,
						Gravity.LEFT, +120, -60);
				isReSendViewShow = true;
			}
		} else {
			displayTip("��ǰû�п�ת���Ķ�������!");
		}
	}

	// ִ�е�ǰ����ɾ������
	private void doMessageDelWork() {
		// �жϵ�ǰ�Ƿ��е���ѡ�е�ѡ��
		if (messageListClick.length() > 0 && currentItemPositon >= 0) {
			// �����ʾ
			editTextMessageBoxPerson.setText(null);
			editTextMessageBoxDate.setText(null);
			editTextMessageBoxContex.setText(null);

			// ִ���б�ɾ������
			String delId = messageListSendId.get(currentItemPositon);
			messageListSendId.remove(currentItemPositon);
			messageListSendPerson.remove(currentItemPositon);

			// ����������
			// �½���������������ʾ
			mAdapter = new smsListAdapter(this, messageListSendPerson,
					messageListSendId);
			listViewMessageLook.setAdapter(mAdapter);

			messageListClick = "";
			currentItemPositon = -1;
			// ˢ��ѡ��
			mAdapter.clickItemId = messageListClick;
			mAdapter.notifyDataSetInvalidated();

			// �������ݿ����
			getContentResolver().delete(Uri.parse("content://sms"), "_id=?",
					new String[] { "" + delId });
		} else {
			putLog("��ǰû��ѡ��ɾ����ѡ��!");
			displayTip("��ǰû��ѡ��ɾ����ѡ��");
		}
	}

	// ���Ų鿴������ʾ
	private void messageLookWindowShow(int showStype) {
		if (isMessageHomeShow && !isMessageLookShow) {
			isMessageHomeShow = false;
			setContentView(viewMessageLook);
			if (buttonLookPageSend == null) {
				// 4���Ų鿴�ӽ���
				buttonLookPageSend = (Button) findViewById(R.id.buttonMessageLookSend);
				buttonLookPageDel = (Button) findViewById(R.id.buttonMessageLookDel);
				buttonLookPageReturn = (Button) findViewById(R.id.buttonMessageLookReturn);
				editTextMessageBoxPerson = (EditText) findViewById(R.id.editTextMessageBoxPerson);
				editTextMessageBoxDate = (EditText) findViewById(R.id.editTextMessageBoxData);
				editTextMessageBoxContex = (EditText) findViewById(R.id.editTextMessageBoxContex);
				listViewMessageLook = (ListView) findViewById(R.id.listViewMessageLook);
				// 4���Ų鿴�ӽ���
				buttonLookPageSend.setOnClickListener(btnOnClickListener);
				buttonLookPageDel.setOnClickListener(btnOnClickListener);
				buttonLookPageReturn.setOnClickListener(btnOnClickListener);

				// �б��¼�����
				listViewMessageLook
						.setOnItemClickListener(new OnItemClickListener() {
							// ѡ����ţ�����
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								mAdapter.clickItemId = messageListSendId
										.get(position);
								mAdapter.notifyDataSetInvalidated();
								messageListClick = mAdapter.clickItemId;
								currentItemPositon = position;
								doMessageLookWork(position); // ִ�ж��Ų鿴�¼�
							}
						});
			}

			isMessageLookShow = true;
			messageLookStype = showStype;
			if (messageLookStype == MESSAGE_NEW) {
				putLog("����δ�����Ų鿴ҳ��");
			} else {
				putLog("�����Ѷ����Ų鿴ҳ��");
			}
			// ��ʼ��
			messageLookWindowInit();
		}
	}

	// ִ�ж��Ų鿴�¼�
	private void doMessageLookWork(int messageNum) {

		String mesContex, id0;
		int realPositon;

		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		// ���ݵ�ǰѡ���ȡ�������ݽ�����ʾ
		if (isLookNewCursorOpen) {
			// ����LISTVIEW�б�Ż�ȡID��
			id0 = messageListSendId.get(messageNum);
			// ��ID�Ų�ѯ���ݿ������ݵ�λ��
			realPositon = messageListSendIdSave.lastIndexOf(id0);
			if (realPositon >= 0) {
				// �鵽����
				messageLookNewCursor.moveToPosition(realPositon);

				// ��ȡ��ϵ�ˣ���Ϊ�գ���ȡ�ֻ�����
				String messsagePerson = messageLookNewCursor.getString(2);
				if (messsagePerson == null) {
					messsagePerson = messageLookNewCursor.getString(1);
				}

				Date date = new Date(
						Long.parseLong(messageLookNewCursor
								.getString(messageLookNewCursor
										.getColumnIndex("date"))));// �Ӷ����л��ʱ��
				String time = sfd.format(date); // ת��ʱ��
				mesContex = messageLookNewCursor.getString(6); // ��ȡ��������

				// ��ʾ
				if (messsagePerson != null && date != null && mesContex != null) {
					// ��ʾ����
					editTextMessageBoxPerson.setText(messsagePerson);
					editTextMessageBoxDate.setText(time);
					editTextMessageBoxContex.setText(mesContex);
				}

				// ���¸ö���Ϊ�Ѷ�
				// 2�޸Ķ���δ����ʶ
				String readId = messageListSendId.get(messageNum);
				ContentValues values = new ContentValues();
				values.put("read", "1"); // �޸Ķ���Ϊ�Ѷ�ģʽ
				getContentResolver().update(Uri.parse("content://sms/inbox"),
						values, " _id=?", new String[] { "" + readId });
			} else {
				putLog("realPostionС��0���޷������ݿ��в���!");
			}
		} else if (isLookOldCursorOpen) {
			// �鵽����
			// ����LISTVIEW�б�Ż�ȡID��
			id0 = messageListSendId.get(messageNum);
			// ��ID�Ų�ѯ���ݿ������ݵ�λ��
			realPositon = messageListSendIdSave.lastIndexOf(id0);
			if (realPositon >= 0) {
				// �鵽����
				messageLookOldCursor.moveToPosition(realPositon);

				// ��ȡ��ϵ�ˣ���Ϊ�գ���ȡ�ֻ�����
				String messsagePerson = messageLookOldCursor.getString(2);
				if (messsagePerson == null) {
					messsagePerson = messageLookOldCursor.getString(1);
				}

				Date date = new Date(
						Long.parseLong(messageLookOldCursor
								.getString(messageLookOldCursor
										.getColumnIndex("date"))));// �Ӷ����л��ʱ��
				String time = sfd.format(date); // ת��ʱ��
				mesContex = messageLookOldCursor.getString(6); // ��ȡ��������

				// ��ʾ
				if (messsagePerson != null && date != null && mesContex != null) {
					// ��ʾ����
					editTextMessageBoxPerson.setText(messsagePerson);
					editTextMessageBoxDate.setText(time);
					editTextMessageBoxContex.setText(mesContex);
				}
			} else {
				putLog("realPostionС��0���޷������ݿ��в���!");
			}

		} else {
			// ���ݿ��ѹر�
			putLog("���ݿ��ѹر�!�޷��鿴��ǰ��������...");
		}
	}

	// ���Ų鿴�����ʼ��
	private void messageLookWindowInit() {
		Uri uri = Uri.parse("content://sms/inbox");
		// inbox�ռ���

		if (messageLookStype == MESSAGE_NEW) {
			putLog("���ڳ�ʼ���Ѷ����Ų鿴ҳ��...");

			// 1�����ݿ��ж�ȡδ����
			messageLookNewCursor = this.getContentResolver().query(uri,
			// ����ַ��������ʾҪ��ѯ����
					new String[] { "_id", // id��
							"address", // �Է�����
							"person", // person�������ˣ������������ͨѶ¼����Ϊ����������İ����Ϊnull
							"date", // ���ڣ�long�ͣ���1256539465022�����Զ�������ʾ��ʽ��������
							"protocol", // ͨ��Э�飬integer,
										// 0:SMS_RPOTO���ţ�1:MMS_PROTO����
							"read", // �Ƿ��Ķ�
							"body" // ����

					}, "protocol=? and read=?", // ��ѯ�������൱��sql�е�where���
					new String[] { "0", "0" }, // ��ѯ������ʹ�õ�������: Э��0-���ţ��Ķ���ʶ0->δ��
												// ��ѯδ���Ķ���
					null // ��ѯ���������ʽ
					);

			if (messageLookNewCursor != null) {
				isLookNewCursorOpen = true; // ��ʶ���ݿ��Ѵ�
				messageListSendId.clear(); // ���
				messageListSendPerson.clear();
				messageListSendIdSave.clear();

				// ��ȡ��ʾ�б�Listview����
				messageLookNewCursor.moveToFirst(); // ��λ
				for (int i = 0; i < messageLookNewCursor.getCount(); i++) {
					messageLookNewCursor.moveToPosition(i); // ��λ

					// ��ȡ����
					int messageid = messageLookNewCursor.getInt(0);

					// ��ȡ��ϵ�ˣ������ƣ������ú�����ʾ
					String messsagePerson = messageLookNewCursor.getString(2);
					if (messsagePerson == null) {
						messsagePerson = messageLookNewCursor.getString(1);
					}
					messageListSendId.add(String.valueOf(messageid));
					messageListSendIdSave.add(String.valueOf(messageid));
					messageListSendPerson.add(messsagePerson);
				}

				// ���
				editTextMessageBoxPerson.setText(null);
				editTextMessageBoxDate.setText(null);
				editTextMessageBoxContex.setText(null);

				messageListClick = "";
				currentItemPositon = -1;
				initMessageListView(); // ����������������ʾ
				if (messageLookNewCursor.getCount() == 0) {
					displayTip("δ�����¶���...");
				}

			} else {
				putLog("��ѯ���ݿ��쳣������ΪNull!");
				messageLookNewCursor.close(); // �ͷ�
				isLookNewCursorOpen = false;
			}

		} else if (messageLookStype == MESSAGE_OLD) // �Ѷ����Ų鿴
		{
			putLog("���ڳ�ʼ���Ѷ����Ų鿴ҳ��...");

			// 1�����ݿ��ж�ȡδ����
			messageLookOldCursor = this.getContentResolver().query(uri,
			// ����ַ��������ʾҪ��ѯ����
					new String[] { "_id", // id��
							"address", // �Է�����
							"person", // person�������ˣ������������ͨѶ¼����Ϊ����������İ����Ϊnull
							"date", // ���ڣ�long�ͣ���1256539465022�����Զ�������ʾ��ʽ��������
							"protocol", // ͨ��Э�飬integer,
										// 0:SMS_RPOTO���ţ�1:MMS_PROTO����
							"read", // �Ƿ��Ķ�
							"body" // ����

					}, "protocol=? and read=?", // ��ѯ�������൱��sql�е�where���
					new String[] { "0", "1" }, // ��ѯ������ʹ�õ�������: Э��0-���ţ��Ķ���ʶ1->�Ѷ�
												// ��ѯ�Ѷ��Ķ���
					null // ��ѯ���������ʽ
					);

			if (messageLookOldCursor != null) {
				isLookOldCursorOpen = true; // ��ʶ���ݿ��Ѵ�
				messageListSendId.clear(); // ���
				messageListSendPerson.clear();
				messageListSendIdSave.clear();

				// ��ȡ��ʾ�б�Listview����
				messageLookOldCursor.moveToFirst(); // ��λ
				for (int i = 0; i < messageLookOldCursor.getCount(); i++) {
					messageLookOldCursor.moveToPosition(i); // ��λ

					// ��ȡ����
					int messageid = messageLookOldCursor.getInt(0);

					// ��ȡ��ϵ�ˣ������ƣ������ú�����ʾ
					String messsagePerson = messageLookOldCursor.getString(2);
					if (messsagePerson == null) {
						messsagePerson = messageLookOldCursor.getString(1);
					}
					messageListSendId.add(String.valueOf(messageid));
					messageListSendIdSave.add(String.valueOf(messageid));
					messageListSendPerson.add(messsagePerson);
				}

				// ���
				editTextMessageBoxPerson.setText(null);
				editTextMessageBoxDate.setText(null);
				editTextMessageBoxContex.setText(null);

				messageListClick = "";
				currentItemPositon = -1;
				initMessageListView(); // ����������������ʾ
				if (messageLookOldCursor.getCount() == 0) {
					displayTip("δ�����¶���...");
				}
			} else {
				putLog("��ѯ���ݿ��쳣������ΪNull!");
				messageLookOldCursor.close(); // �ͷ�
				isLookOldCursorOpen = false;
			}
		}
	}

	// ��ʼ���б�
	private void initMessageListView() {
		// �½���������������ʾ
		mAdapter = new smsListAdapter(this, messageListSendPerson,
				messageListSendId);
		listViewMessageLook.setAdapter(mAdapter);

		// ˢ��ѡ��
		mAdapter.clickItemId = messageListClick;
		mAdapter.notifyDataSetInvalidated();
	}

	// ���ŷ��ͽ�������
	private void messageSendReceivedInit() {
		// ��ȡ���Ź�����
		mSmsManager = android.telephony.SmsManager.getDefault();

		// create the sentIntent parameter
		sendIntent = new Intent(SEND_SMS_ACTION);
		sentPI = PendingIntent.getBroadcast(SmartCarSMS.this, 0, sendIntent, 0);

		// create the deilverIntent parameter
		deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		deliverPI = PendingIntent.getBroadcast(SmartCarSMS.this, 0,
				deliverIntent, 0);

		// register the broadcast receivers
		registerReceiver(mSendBroadcastReceiver01, new IntentFilter(
				SEND_SMS_ACTION));

		// ע�ᷢ�ͺ󣬳ɹ�����
		registerReceiver(mSendBroadcastReceiver02, new IntentFilter(
				DELIVERED_SMS_ACTION));
	}

	// ���ŷ��ͳɹ���������1
	private BroadcastReceiver mSendBroadcastReceiver01 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isInSendMessage) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					// ���ͳɹ�
					putLog("�����ѳɹ�����!");
					displayTip("�����ѳɹ�����!");
					isInSendMessage = false;
					break;

				default:
					// ���ͳɹ�
					putLog("���ŷ���ʧ��!");
					displayTip("���ŷ���ʧ��!");
					isInSendMessage = false;
					break;
				}
			}
		}
	};

	// ���ŷ��ͱ����ս�������2
	private BroadcastReceiver mSendBroadcastReceiver02 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// SMS delivered action
			if (isInSendMessage) {
				putLog("�����ѱ��ɹ�����!");
			}
		}
	};

	// ִ�ж��ŷ��Ͳ���
	private void doMessageSendWork(String sendPhone, String sendContex) {
		// ��ȡ�ռ��˺��롢��������
		String phoneNo = sendPhone;
		String message = sendContex;

		// ���
		if (phoneNo.length() > 0 && message.length() > 0) {

		} else {
			displayTip("������绰�Ͷ�������!!!");
			return;
		}

		isInSendMessage = true;
		// ��ַ���
		if (message.length() > 70) {
			List<String> messageTexts = mSmsManager.divideMessage(message);
			for (String subtext : messageTexts) {
				mSmsManager.sendTextMessage(phoneNo, null, subtext, sentPI,
						deliverPI);
			}
		} else {
			// ���Ȳ�����ֱ�ӷ���
			mSmsManager.sendTextMessage(phoneNo, null, message, sentPI,
					deliverPI);

			// ���淢�Ͷ��ŵ�������������
			ContentValues values = new ContentValues();
			values.put("address", phoneNo);
			values.put("body", message);
			getContentResolver()
					.insert(Uri.parse("content://sms/sent"), values);
		}
	}

	// ���������Ϣ
	private void putLog(String info) {
		Log.d(TAG, info);
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