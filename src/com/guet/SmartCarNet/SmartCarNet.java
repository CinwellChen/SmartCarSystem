package com.guet.SmartCarNet;

import java.io.File;

import com.guet.SmartCarSystem.R;
import com.guet.SmartCarSystem.SmartCarSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

//������Ϣϵͳ��6���֣����������
public class SmartCarNet extends Activity
{
	private static final String TAG = "Car";
	private TextView textViewWebInformation; // ��Ϣ��
	private EditText editTextWebAddress; // ��ַ��

	private Button buttonWebMin; // ��С������������ҳ
	private Button buttonWebClose; // �ر�
	private Button buttonWebBack; // ����
	private Button buttonWebGo; // ǰ��
	private Button buttonWebStop; // ֹͣ
	private Button buttonWebRefresh;// ˢ��
	private Button buttonWebHome; // ��ҳ
	private Button buttonWebOpen; // ��

	private WebView mWebView;
	private WebSettings mWebSettings;
	private String currentLoadUrl = ""; // ��ǰ���ص���ַ
	private boolean isLoadWeb = false;
	String mainHomeUrl = "http://www.baidu.com/"; // ��ҳ

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS); 
		
		setContentView(R.layout.nethome);
		initGuiComponet(); // ��ʼ��Ԫ��
		putFuncationName("onCreate");
	}
		
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		mWebView.removeAllViews();
		putFuncationName("onDestroy");
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		putFuncationName("onPause");
	}

	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub		
		//���ϵ�д��ڼ���ļ�
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if( !homeFile.exists() )
		{
			//�ļ������ڣ�֤����ҳ�ѹرգ���ʾ������ҳ���ܷ���
			finish();	//�˳�
		}
		super.onRestart();
		putFuncationName("onRestart");
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		putFuncationName("onResume");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
	if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
		finish();
	}
	    return false;
	}
	 
	// ���ط���
	private void returnHome()
	{
		// ����"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarNet.this, SmartCarSystem.class);
		
		//FLAG_ACTIVITY_REORDER_TO_FRONT��־���ܷ�ֹ�ظ�ʵ����һ��Activity
		//��ȥ�󣬻�����"onCreate()",ֱ�ӵ�"onRestart()"-->"onStart()"
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		startActivity(intent);

		// SmartCarMusic.this.finish(); //������finish���ᴥ��onDestroy();
		putFuncationName("returnHome");
	}

	// ��ʼ��Ԫ��
	private void initGuiComponet()
	{
		// 1����ID�ؼ�
		textViewWebInformation = (TextView) findViewById(R.id.textViewWebInformation);
		editTextWebAddress = (EditText) findViewById(R.id.editTextWebAddress);
		// ��������¼�����	
		editTextWebAddress
		.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					doWebOpenWork(); // ������ɺ������ҳ
					return true;
				}
				return false;
			}
		});

		buttonWebMin = (Button) findViewById(R.id.buttonWebMin);
		buttonWebClose = (Button) findViewById(R.id.buttonWebClose);
		buttonWebBack = (Button) findViewById(R.id.buttonWebBack);
		buttonWebGo = (Button) findViewById(R.id.buttonWebGo);
		buttonWebStop = (Button) findViewById(R.id.buttonWebStop);
		buttonWebRefresh = (Button) findViewById(R.id.buttonWebRefresh);
		buttonWebHome = (Button) findViewById(R.id.buttonWebHome);
		buttonWebOpen = (Button) findViewById(R.id.buttonWebOpen);

		mWebView = (WebView) findViewById(R.id.webViewNet);

		// 2�����¼�
		buttonWebMin.setOnClickListener(btnOnClickListener);
		buttonWebClose.setOnClickListener(btnOnClickListener);
		buttonWebBack.setOnClickListener(btnOnClickListener);
		buttonWebGo.setOnClickListener(btnOnClickListener);
		buttonWebStop.setOnClickListener(btnOnClickListener);
		buttonWebRefresh.setOnClickListener(btnOnClickListener);
		buttonWebHome.setOnClickListener(btnOnClickListener);
		buttonWebOpen.setOnClickListener(btnOnClickListener);


		// 4����������
		mWebView.setFocusable(true);
		mWebView.setClickable(true);
		mWebView.setLongClickable(true);
		mWebView.setFocusable(true);
		mWebView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				editTextWebAddress.clearFocus();
				mWebView.requestFocus();
				
			}
		});

		mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true); // ֧��javascript
		mWebSettings.setAllowFileAccess(true); // ��������ļ�����
		mWebSettings.setBuiltInZoomControls(true); // ֧������
		mWebSettings.setSavePassword(false); // ����������

		// 5�������ʹ�õ�ǰ������
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}

			// �������ʱҪ���Ĺ���
			@Override
			public void onPageFinished(WebView view, String url)
			{
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				editTextWebAddress.clearFocus();
				mWebView.requestFocus();
				
			}

			// ��ʼ������ҳʱҪ���Ĺ���
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				isLoadWeb = true;
			}

			// ���ش���ʱҪ���Ĺ���
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl)
			{
				// TODO Auto-generated method stub
				putLog("webLoadError:" + description);
				isLoadWeb = false;
			}
		});

		// 6������ҳ�е�һЩ�Ի�����Ϣ
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			// 1�Ի���
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result)
			{
				// ����һ��Builder����ʾ��ҳ�е� alert �Ի���
				Builder builder = new Builder(SmartCarNet.this);
				builder.setTitle("��ʾ�Ի���");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								// TODO Auto-generated method stub
								result.confirm();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			// ����ť�ĶԻ���
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result)
			{
				Builder builder = new Builder(SmartCarNet.this);
				builder.setTitle("��ѡ��ĶԻ���");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								// TODO Auto-generated method stub
								result.confirm();
							}
						});
				builder.setNeutralButton(android.R.string.cancel,
						new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								// TODO Auto-generated method stub
								result.cancel();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			// ������ҳ���صĽ�����
			public void onProgressChanged(WebView view, int newProgress)
			{ 
				SmartCarNet.this.getWindow().setFeatureInt(
						Window.FEATURE_PROGRESS, newProgress * 100);
				super.onProgressChanged(view, newProgress);

				final ProgressBar progressHorizontal = (ProgressBar)findViewById(R.id.WebViewProgress); 
				setProgressBarVisibility(true); 
				
				if (newProgress !=100) {
					progressHorizontal.setVisibility(View.VISIBLE );
					progressHorizontal.setProgress(newProgress);
				}
				else {
					progressHorizontal.setVisibility(View.GONE );
				}
			}

			// ����Ӧ�ó���ı���
			public void onReceivedTitle(WebView view, String title)
			{
				// SmartCarNet.this.setTitle(title);
				super.onReceivedTitle(view, title);
				
				if( title.length() > 20 )	//��������
				{
					title = title.substring(0, 19);
					title = title + "...";
				}				
				textViewWebInformation.setText(title + "�������������");
			}
		});

		// 7�����豸�ֱ���������ҳ����
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int mDensity = dm.densityDpi;	// ��Ļ�ܶȣ�ÿ�����أ�120/160/240/320��  
		putLog("��ǰ�����ܶ�Ϊ��" + mDensity);

		if (mDensity == 240)
		{ 
			// �����ò�ͬ�� density ������£�������ҳ���������
			mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		} else if (mDensity == 160)
		{
			mWebView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
		} else
		{
			mWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
		}
	
		// 8��ʼ�����ذٶ���ҳ
		editTextWebAddress.clearFocus();
		mWebView.requestFocus();
		currentLoadUrl = mainHomeUrl;
		mWebView.loadUrl(mainHomeUrl);
	}
	
	// ��д��ť��ʹ�ð�Backʱ���ˣ�������ǰһ��Activity
	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		if (mWebView.canGoBack())
		{
			mWebView.goBack();
		}
		return;
	}

	// ��ť�����¼�
	private OnClickListener btnOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			switch (v.getId())
			{
			case R.id.buttonWebMin: // ��С��
				returnHome();
				break;
			case R.id.buttonWebClose: // �ر�
				finish();
				break;
			case R.id.buttonWebBack: // ����
				if (mWebView.canGoBack())
					mWebView.goBack();
				else
					displayTip("�޷�����!");
				break;
			case R.id.buttonWebGo: // ǰ��
				if (mWebView.canGoForward())
					mWebView.goForward();
				else
					displayTip("�޷�ǰ��!");
				break;
			case R.id.buttonWebStop: // ֹͣ
				if (isLoadWeb)
				{
					mWebView.stopLoading();
					isLoadWeb = false;
				} else
					displayTip("�Ѽ������!");
				break;
			case R.id.buttonWebRefresh: // ˢ��
				if (currentLoadUrl.length() > 0)
					mWebView.loadUrl(currentLoadUrl);
				else
					displayTip("��ǰû��Ҫˢ�µ���ҳ!");
				break;
			case R.id.buttonWebHome: // ��ҳ
				if (isLoadWeb)
				{
					mWebView.stopLoading();
					isLoadWeb = false;
				}
				mWebView.loadUrl(mainHomeUrl);
				break;
			case R.id.buttonWebOpen: // ��
				doWebOpenWork(); // ִ�д���ҳ����
				break;
			default:

				break;
			}
		}
	};

	// ִ�д���ҳ����
	private void doWebOpenWork()
	{
		String openStr = editTextWebAddress.getText().toString();
		if (openStr.length() > 0)
		{
			currentLoadUrl = "http://" + openStr + "/";
			putLog("��ǰ���ص���ҳ��ַΪ��" + currentLoadUrl);
			mWebView.loadUrl(currentLoadUrl);
		} else
		{
			displayTip("������һ����Ч��ַ!");
		}
	}

	// ���������Ϣ
	private void putLog(String info)
	{
		Log.d(TAG, info);
	}	
	// ��ʾ��ʾ��Ϣlong
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
}
