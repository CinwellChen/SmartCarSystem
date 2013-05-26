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

//车载信息系统：5部分：短信收发器
public class SmartCarSMS extends Activity {
	private static final String TAG = "Car";

	// 1主界面
	private Button buttonMessageSendPage;
	private Button buttonMessageLookNewPage;
	private Button buttonMessageLookOldPage;
	private Button buttonMessageReturnPage;
	private View viewMessageHome;
	private boolean isMessageHomeShow;

	// 2短信发送子界面
	private Button buttonNewPageSend;
	private Button buttonNewPageClear;
	private Button buttonNewPageReturn;
	private EditText editTextMessageSendNumber;
	private EditText editTextMessageSendContex;
	private View viewMessageSend;
	private boolean isMessageSendShow;

	// 3短信查看子界面
	private EditText editTextMessageBoxPerson;
	private EditText editTextMessageBoxDate;
	private EditText editTextMessageBoxContex;
	private Button buttonLookPageSend;
	private Button buttonLookPageDel;
	private Button buttonLookPageReturn;
	private ListView listViewMessageLook;
	private View viewMessageLook;
	private boolean isMessageLookShow;

	// 短信类型查看类型
	private static final int MESSAGE_OLD = 1; // 已读短信
	private static final int MESSAGE_NEW = 2; // 未读短信
	private int messageLookStype;

	Cursor messageLookNewCursor;
	Cursor messageLookOldCursor;
	private boolean isLookNewCursorOpen = false;
	private boolean isLookOldCursorOpen = false;

	private List<String> messageListSendIdSave = new ArrayList<String>(); // 保存最初版本
	private List<String> messageListSendId = new ArrayList<String>();
	private List<String> messageListSendPerson = new ArrayList<String>();

	smsListAdapter mAdapter; // 显示适配器
	private String messageListClick = "-1";
	private int currentItemPositon = -1;

	// 短信重发
	private PopupWindow reSendPopupWindow;
	private View reSendView;
	private boolean isReSendViewShow = false;
	private EditText editTextRePhone;
	private Button buttonReConfirm;
	private Button buttonReCancel;

	// 短信发送、传达成功倾听
	private String SEND_SMS_ACTION = "SEND_SMS_ACTION";
	private String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	private Intent sendIntent, deliverIntent;
	private PendingIntent sentPI, deliverPI;
	private android.telephony.SmsManager mSmsManager;
	private boolean isInSendMessage = false; // 发送短信标识

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		initGuiComponent(); // 初始化GUI控件
		initGuiEvent(); // 初始化控件事件
		messageSendReceivedInit(); // 初始化短信发送、传送成功倾听事件

		putFuncationName("onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 1关闭弹出窗口
		if (reSendPopupWindow.isShowing())
			reSendPopupWindow.dismiss();

		// 关闭数据库光标
		if (isLookNewCursorOpen)
			messageLookNewCursor.close();
		if (isLookOldCursorOpen)
			messageLookOldCursor.close();

		// 清窗列表
		messageListSendId.clear();
		messageListSendIdSave.clear();
		messageListSendPerson.clear();

		// 注销相关短信倾听
		unregisterReceiver(mSendBroadcastReceiver01);
		unregisterReceiver(mSendBroadcastReceiver02);

		putFuncationName("onDestroy");
	}

	// 初始化控件
	private void initGuiComponent() {
		// 1主页
		viewMessageHome = getLayoutInflater().inflate(R.layout.smsmain, null);
		setContentView(viewMessageHome);
		isMessageHomeShow = true;

		buttonMessageSendPage = (Button) findViewById(R.id.buttonMessageSendPage);
		buttonMessageLookNewPage = (Button) findViewById(R.id.buttonMessageLookNewPage);
		buttonMessageLookOldPage = (Button) findViewById(R.id.buttonMessageLookOldPage);
		buttonMessageReturnPage = (Button) findViewById(R.id.buttonMessageReturnPage);

		// 2发送界面
		viewMessageSend = getLayoutInflater().inflate(R.layout.messagesend,
				null);
		isMessageSendShow = false;
		// 3短信查看子界面主页
		viewMessageLook = getLayoutInflater().inflate(R.layout.messagelook,
				null);
		isMessageLookShow = false;

		// 转发界面
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

	// 初始化控件事件
	private void initGuiEvent() {
		// 1主页
		buttonMessageSendPage.setOnClickListener(btnOnClickListener);
		buttonMessageLookNewPage.setOnClickListener(btnOnClickListener);
		buttonMessageLookOldPage.setOnClickListener(btnOnClickListener);
		buttonMessageReturnPage.setOnClickListener(btnOnClickListener);

		// 弹出窗口
		buttonReConfirm.setOnClickListener(btnOnClickListener);
		buttonReCancel.setOnClickListener(btnOnClickListener);
	}

	// 按钮单击事件倾听
	private OnClickListener btnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 1主页
			case R.id.buttonMessageSendPage: // 跳转到发送主页
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

						// 2发送界面
						buttonNewPageSend
								.setOnClickListener(btnOnClickListener);
						buttonNewPageClear
								.setOnClickListener(btnOnClickListener);
						buttonNewPageReturn
								.setOnClickListener(btnOnClickListener);
					}
					putLog("进入短信发送页面");
				}
				break;
			case R.id.buttonMessageLookNewPage: // 短信查看子界面(未读短信)
				putLog("buttonMessageLookNewPage");
				messageLookWindowShow(MESSAGE_NEW);
				break;

			case R.id.buttonMessageLookOldPage: // 短信查看子界面(已读短信)
				putLog("buttonMessageLookOldPage");
				messageLookWindowShow(MESSAGE_OLD);
				break;
			case R.id.buttonMessageReturnPage: // 返回SmartCar主页
				finish();
				break;

			// 2发送界面
			case R.id.buttonSendOk: // 短信发送操作
				doMessageSendWork(editTextMessageSendNumber.getText()
						.toString(), editTextMessageSendContex.getText()
						.toString());
				putLog("buttonNewPageSend");

				break;
			case R.id.buttonSendClear: // 清空操作
				editTextMessageSendNumber.setText(null);
				editTextMessageSendContex.setText(null);
				putLog("buttonNewPageClear");
				break;
			case R.id.buttonSendReturn: // 返回一上级
				putLog("buttonNewPageReturn");
				if (!isMessageHomeShow && isMessageSendShow) {
					isMessageSendShow = false;
					setContentView(viewMessageHome);
					isMessageHomeShow = true;
					putLog("从发送页面返回");
				}
				break;

			// 短信查看页面
			case R.id.buttonMessageLookSend: // 转发
				messsageReSendWindowShow(); // 调出转发界面
				putLog("buttonMessageLookSend");
				break;
			case R.id.buttonMessageLookDel:
				putLog("buttonMessageLookDel");
				doMessageDelWork(); // 执行当前短信删除工作
				break;
			case R.id.buttonMessageLookReturn: // 返回上一级
				putLog("buttonMessageLookReturn");
				if (!isMessageHomeShow && isMessageLookShow) {
					isMessageLookShow = false;
					setContentView(viewMessageHome);
					isMessageHomeShow = true;
					putLog("从查看页面返回");

					// 关闭数据库
					if (isLookNewCursorOpen)
						messageLookNewCursor.close();
					if (isLookOldCursorOpen)
						messageLookOldCursor.close();
					isLookNewCursorOpen = false;
					isLookOldCursorOpen = false;
				}
				break;

			// 弹出窗口
			case R.id.buttonMessageReConfirm:
				if (editTextRePhone.getText().length() > 0) {
					// 关闭窗口
					reSendPopupWindow.dismiss();
					isReSendViewShow = false;

					// 进行短信转发
					doMessageSendWork(editTextRePhone.getText().toString(),
							editTextMessageBoxContex.getText().toString());
				} else {
					displayTip("请输入新收件人号码!");
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

	// 执行短信转发
	private void messsageReSendWindowShow() {
		if (editTextMessageBoxContex.getText().length() > 0) {
			if (reSendPopupWindow.isShowing()) {
				putLog("界面已显示....");
			} else {
				putLog("正在初始化弹出窗口....");
				reSendPopupWindow.showAtLocation(listViewMessageLook,
						Gravity.LEFT, +120, -60);
				isReSendViewShow = true;
			}
		} else {
			displayTip("当前没有可转发的短信内容!");
		}
	}

	// 执行当前短信删除工作
	private void doMessageDelWork() {
		// 判断当前是否有单击选中的选项
		if (messageListClick.length() > 0 && currentItemPositon >= 0) {
			// 清空显示
			editTextMessageBoxPerson.setText(null);
			editTextMessageBoxDate.setText(null);
			editTextMessageBoxContex.setText(null);

			// 执行列表删除工作
			String delId = messageListSendId.get(currentItemPositon);
			messageListSendId.remove(currentItemPositon);
			messageListSendPerson.remove(currentItemPositon);

			// 更新适配器
			// 新建适配器，更新显示
			mAdapter = new smsListAdapter(this, messageListSendPerson,
					messageListSendId);
			listViewMessageLook.setAdapter(mAdapter);

			messageListClick = "";
			currentItemPositon = -1;
			// 刷新选项
			mAdapter.clickItemId = messageListClick;
			mAdapter.notifyDataSetInvalidated();

			// 进行数据库更新
			getContentResolver().delete(Uri.parse("content://sms"), "_id=?",
					new String[] { "" + delId });
		} else {
			putLog("当前没有选择删除的选项!");
			displayTip("当前没有选择删除的选项");
		}
	}

	// 短信查看界面显示
	private void messageLookWindowShow(int showStype) {
		if (isMessageHomeShow && !isMessageLookShow) {
			isMessageHomeShow = false;
			setContentView(viewMessageLook);
			if (buttonLookPageSend == null) {
				// 4短信查看子界面
				buttonLookPageSend = (Button) findViewById(R.id.buttonMessageLookSend);
				buttonLookPageDel = (Button) findViewById(R.id.buttonMessageLookDel);
				buttonLookPageReturn = (Button) findViewById(R.id.buttonMessageLookReturn);
				editTextMessageBoxPerson = (EditText) findViewById(R.id.editTextMessageBoxPerson);
				editTextMessageBoxDate = (EditText) findViewById(R.id.editTextMessageBoxData);
				editTextMessageBoxContex = (EditText) findViewById(R.id.editTextMessageBoxContex);
				listViewMessageLook = (ListView) findViewById(R.id.listViewMessageLook);
				// 4短信查看子界面
				buttonLookPageSend.setOnClickListener(btnOnClickListener);
				buttonLookPageDel.setOnClickListener(btnOnClickListener);
				buttonLookPageReturn.setOnClickListener(btnOnClickListener);

				// 列表事件触发
				listViewMessageLook
						.setOnItemClickListener(new OnItemClickListener() {
							// 选择短信，单击
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								mAdapter.clickItemId = messageListSendId
										.get(position);
								mAdapter.notifyDataSetInvalidated();
								messageListClick = mAdapter.clickItemId;
								currentItemPositon = position;
								doMessageLookWork(position); // 执行短信查看事件
							}
						});
			}

			isMessageLookShow = true;
			messageLookStype = showStype;
			if (messageLookStype == MESSAGE_NEW) {
				putLog("进入未读短信查看页面");
			} else {
				putLog("进入已读短信查看页面");
			}
			// 初始化
			messageLookWindowInit();
		}
	}

	// 执行短信查看事件
	private void doMessageLookWork(int messageNum) {

		String mesContex, id0;
		int realPositon;

		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		// 根据当前选项抽取短信内容进行显示
		if (isLookNewCursorOpen) {
			// 根据LISTVIEW列表号获取ID号
			id0 = messageListSendId.get(messageNum);
			// 由ID号查询数据库中内容的位置
			realPositon = messageListSendIdSave.lastIndexOf(id0);
			if (realPositon >= 0) {
				// 抽到数据
				messageLookNewCursor.moveToPosition(realPositon);

				// 获取联系人，若为空，则取手机号码
				String messsagePerson = messageLookNewCursor.getString(2);
				if (messsagePerson == null) {
					messsagePerson = messageLookNewCursor.getString(1);
				}

				Date date = new Date(
						Long.parseLong(messageLookNewCursor
								.getString(messageLookNewCursor
										.getColumnIndex("date"))));// 从短信中获得时间
				String time = sfd.format(date); // 转换时间
				mesContex = messageLookNewCursor.getString(6); // 获取发件内容

				// 显示
				if (messsagePerson != null && date != null && mesContex != null) {
					// 显示数据
					editTextMessageBoxPerson.setText(messsagePerson);
					editTextMessageBoxDate.setText(time);
					editTextMessageBoxContex.setText(mesContex);
				}

				// 更新该短信为已读
				// 2修改短信未读标识
				String readId = messageListSendId.get(messageNum);
				ContentValues values = new ContentValues();
				values.put("read", "1"); // 修改短信为已读模式
				getContentResolver().update(Uri.parse("content://sms/inbox"),
						values, " _id=?", new String[] { "" + readId });
			} else {
				putLog("realPostion小于0，无法在数据库中查找!");
			}
		} else if (isLookOldCursorOpen) {
			// 抽到数据
			// 根据LISTVIEW列表号获取ID号
			id0 = messageListSendId.get(messageNum);
			// 由ID号查询数据库中内容的位置
			realPositon = messageListSendIdSave.lastIndexOf(id0);
			if (realPositon >= 0) {
				// 抽到数据
				messageLookOldCursor.moveToPosition(realPositon);

				// 获取联系人，若为空，则取手机号码
				String messsagePerson = messageLookOldCursor.getString(2);
				if (messsagePerson == null) {
					messsagePerson = messageLookOldCursor.getString(1);
				}

				Date date = new Date(
						Long.parseLong(messageLookOldCursor
								.getString(messageLookOldCursor
										.getColumnIndex("date"))));// 从短信中获得时间
				String time = sfd.format(date); // 转换时间
				mesContex = messageLookOldCursor.getString(6); // 获取发件内容

				// 显示
				if (messsagePerson != null && date != null && mesContex != null) {
					// 显示数据
					editTextMessageBoxPerson.setText(messsagePerson);
					editTextMessageBoxDate.setText(time);
					editTextMessageBoxContex.setText(mesContex);
				}
			} else {
				putLog("realPostion小于0，无法在数据库中查找!");
			}

		} else {
			// 数据库已关闭
			putLog("数据库已关闭!无法查看当前短信内容...");
		}
	}

	// 短信查看界面初始化
	private void messageLookWindowInit() {
		Uri uri = Uri.parse("content://sms/inbox");
		// inbox收件箱

		if (messageLookStype == MESSAGE_NEW) {
			putLog("正在初始化已读短信查看页面...");

			// 1从数据库中读取未短信
			messageLookNewCursor = this.getContentResolver().query(uri,
			// 这个字符串数组表示要查询的列
					new String[] { "_id", // id号
							"address", // 对方号码
							"person", // person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
							"date", // 日期，long型，如1256539465022，可以对日期显示格式进行设置
							"protocol", // 通信协议，integer,
										// 0:SMS_RPOTO短信；1:MMS_PROTO彩信
							"read", // 是否阅读
							"body" // 内容

					}, "protocol=? and read=?", // 查询条件，相当于sql中的where语句
					new String[] { "0", "0" }, // 查询条件中使用到的数据: 协议0-短信；阅读标识0->未读
												// 查询未读的短信
					null // 查询结果的排序方式
					);

			if (messageLookNewCursor != null) {
				isLookNewCursorOpen = true; // 标识数据库已打开
				messageListSendId.clear(); // 清空
				messageListSendPerson.clear();
				messageListSendIdSave.clear();

				// 获取显示列表Listview数据
				messageLookNewCursor.moveToFirst(); // 复位
				for (int i = 0; i < messageLookNewCursor.getCount(); i++) {
					messageLookNewCursor.moveToPosition(i); // 移位

					// 获取数据
					int messageid = messageLookNewCursor.getInt(0);

					// 获取联系人：先名称，无则用号码显示
					String messsagePerson = messageLookNewCursor.getString(2);
					if (messsagePerson == null) {
						messsagePerson = messageLookNewCursor.getString(1);
					}
					messageListSendId.add(String.valueOf(messageid));
					messageListSendIdSave.add(String.valueOf(messageid));
					messageListSendPerson.add(messsagePerson);
				}

				// 清空
				editTextMessageBoxPerson.setText(null);
				editTextMessageBoxDate.setText(null);
				editTextMessageBoxContex.setText(null);

				messageListClick = "";
				currentItemPositon = -1;
				initMessageListView(); // 调用适配器进行显示
				if (messageLookNewCursor.getCount() == 0) {
					displayTip("未发现新短信...");
				}

			} else {
				putLog("查询数据库异常，返回为Null!");
				messageLookNewCursor.close(); // 释放
				isLookNewCursorOpen = false;
			}

		} else if (messageLookStype == MESSAGE_OLD) // 已读短信查看
		{
			putLog("正在初始化已读短信查看页面...");

			// 1从数据库中读取未短信
			messageLookOldCursor = this.getContentResolver().query(uri,
			// 这个字符串数组表示要查询的列
					new String[] { "_id", // id号
							"address", // 对方号码
							"person", // person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
							"date", // 日期，long型，如1256539465022，可以对日期显示格式进行设置
							"protocol", // 通信协议，integer,
										// 0:SMS_RPOTO短信；1:MMS_PROTO彩信
							"read", // 是否阅读
							"body" // 内容

					}, "protocol=? and read=?", // 查询条件，相当于sql中的where语句
					new String[] { "0", "1" }, // 查询条件中使用到的数据: 协议0-短信；阅读标识1->已读
												// 查询已读的短信
					null // 查询结果的排序方式
					);

			if (messageLookOldCursor != null) {
				isLookOldCursorOpen = true; // 标识数据库已打开
				messageListSendId.clear(); // 清空
				messageListSendPerson.clear();
				messageListSendIdSave.clear();

				// 获取显示列表Listview数据
				messageLookOldCursor.moveToFirst(); // 复位
				for (int i = 0; i < messageLookOldCursor.getCount(); i++) {
					messageLookOldCursor.moveToPosition(i); // 移位

					// 获取数据
					int messageid = messageLookOldCursor.getInt(0);

					// 获取联系人：先名称，无则用号码显示
					String messsagePerson = messageLookOldCursor.getString(2);
					if (messsagePerson == null) {
						messsagePerson = messageLookOldCursor.getString(1);
					}
					messageListSendId.add(String.valueOf(messageid));
					messageListSendIdSave.add(String.valueOf(messageid));
					messageListSendPerson.add(messsagePerson);
				}

				// 清空
				editTextMessageBoxPerson.setText(null);
				editTextMessageBoxDate.setText(null);
				editTextMessageBoxContex.setText(null);

				messageListClick = "";
				currentItemPositon = -1;
				initMessageListView(); // 调用适配器进行显示
				if (messageLookOldCursor.getCount() == 0) {
					displayTip("未发现新短信...");
				}
			} else {
				putLog("查询数据库异常，返回为Null!");
				messageLookOldCursor.close(); // 释放
				isLookOldCursorOpen = false;
			}
		}
	}

	// 初始化列表
	private void initMessageListView() {
		// 新建适配器，更新显示
		mAdapter = new smsListAdapter(this, messageListSendPerson,
				messageListSendId);
		listViewMessageLook.setAdapter(mAdapter);

		// 刷新选项
		mAdapter.clickItemId = messageListClick;
		mAdapter.notifyDataSetInvalidated();
	}

	// 短信发送接收倾听
	private void messageSendReceivedInit() {
		// 获取短信管理器
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

		// 注册发送后，成功接收
		registerReceiver(mSendBroadcastReceiver02, new IntentFilter(
				DELIVERED_SMS_ACTION));
	}

	// 短信发送成功发送倾听1
	private BroadcastReceiver mSendBroadcastReceiver01 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isInSendMessage) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					// 发送成功
					putLog("短信已成功发送!");
					displayTip("短信已成功发送!");
					isInSendMessage = false;
					break;

				default:
					// 发送成功
					putLog("短信发送失败!");
					displayTip("短信发送失败!");
					isInSendMessage = false;
					break;
				}
			}
		}
	};

	// 短信发送被接收接收倾听2
	private BroadcastReceiver mSendBroadcastReceiver02 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// SMS delivered action
			if (isInSendMessage) {
				putLog("短信已被成功接收!");
			}
		}
	};

	// 执行短信发送操作
	private void doMessageSendWork(String sendPhone, String sendContex) {
		// 获取收件人号码、发件内容
		String phoneNo = sendPhone;
		String message = sendContex;

		// 检测
		if (phoneNo.length() > 0 && message.length() > 0) {

		} else {
			displayTip("请输入电话和短信内容!!!");
			return;
		}

		isInSendMessage = true;
		// 拆分发送
		if (message.length() > 70) {
			List<String> messageTexts = mSmsManager.divideMessage(message);
			for (String subtext : messageTexts) {
				mSmsManager.sendTextMessage(phoneNo, null, subtext, sentPI,
						deliverPI);
			}
		} else {
			// 长度不超，直接发送
			mSmsManager.sendTextMessage(phoneNo, null, message, sentPI,
					deliverPI);

			// 保存发送短信的内容至发送箱
			ContentValues values = new ContentValues();
			values.put("address", phoneNo);
			values.put("body", message);
			getContentResolver()
					.insert(Uri.parse("content://sms/sent"), values);
		}
	}

	// 输出调试信息
	private void putLog(String info) {
		Log.d(TAG, info);
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