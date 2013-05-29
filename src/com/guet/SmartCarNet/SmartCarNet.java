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

//车载信息系统：6部分：上网浏览器
public class SmartCarNet extends Activity
{
	private static final String TAG = "Car";
	private TextView textViewWebInformation; // 信息栏
	private EditText editTextWebAddress; // 地址栏

	private Button buttonWebMin; // 最小化，即返回主页
	private Button buttonWebClose; // 关闭
	private Button buttonWebBack; // 后退
	private Button buttonWebGo; // 前进
	private Button buttonWebStop; // 停止
	private Button buttonWebRefresh;// 刷新
	private Button buttonWebHome; // 主页
	private Button buttonWebOpen; // 打开

	private WebView mWebView;
	private WebSettings mWebSettings;
	private String currentLoadUrl = ""; // 当前加载的网址
	private boolean isLoadWeb = false;
	String mainHomeUrl = "http://www.baidu.com/"; // 主页

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS); 
		
		setContentView(R.layout.nethome);
		initGuiComponet(); // 初始化元件
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
		//检查系列存在检查文件
		File homeFile = new File("/mnt/sdcard/SmartCarSystem/homeIsExist");
		if( !homeFile.exists() )
		{
			//文件不存在，证明主页已关闭，表示其它子页不能返回
			finish();	//退出
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
	 
	// 返回方法
	private void returnHome()
	{
		// 调用"home"Activity
		Intent intent = new Intent();
		intent.setClass(SmartCarNet.this, SmartCarSystem.class);
		
		//FLAG_ACTIVITY_REORDER_TO_FRONT标志，能防止重复实例化一个Activity
		//进去后，会跳过"onCreate()",直接到"onRestart()"-->"onStart()"
		intent.addFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		startActivity(intent);

		// SmartCarMusic.this.finish(); //不能用finish，会触发onDestroy();
		putFuncationName("returnHome");
	}

	// 初始化元件
	private void initGuiComponet()
	{
		// 1查找ID控件
		textViewWebInformation = (TextView) findViewById(R.id.textViewWebInformation);
		editTextWebAddress = (EditText) findViewById(R.id.editTextWebAddress);
		// 输入完成事件倾听	
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
					doWebOpenWork(); // 输入完成后加载网页
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

		// 2设置事件
		buttonWebMin.setOnClickListener(btnOnClickListener);
		buttonWebClose.setOnClickListener(btnOnClickListener);
		buttonWebBack.setOnClickListener(btnOnClickListener);
		buttonWebGo.setOnClickListener(btnOnClickListener);
		buttonWebStop.setOnClickListener(btnOnClickListener);
		buttonWebRefresh.setOnClickListener(btnOnClickListener);
		buttonWebHome.setOnClickListener(btnOnClickListener);
		buttonWebOpen.setOnClickListener(btnOnClickListener);


		// 4设置游览器
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
		mWebSettings.setJavaScriptEnabled(true); // 支持javascript
		mWebSettings.setAllowFileAccess(true); // 允许访问文件数据
		mWebSettings.setBuiltInZoomControls(true); // 支持缩放
		mWebSettings.setSavePassword(false); // 不保存密码

		// 5点击链接使用当前游览器
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}

			// 加载完成时要做的工作
			@Override
			public void onPageFinished(WebView view, String url)
			{
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				editTextWebAddress.clearFocus();
				mWebView.requestFocus();
				
			}

			// 开始加载网页时要做的工作
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				isLoadWeb = true;
			}

			// 加载错误时要做的工作
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl)
			{
				// TODO Auto-generated method stub
				putLog("webLoadError:" + description);
				isLoadWeb = false;
			}
		});

		// 6处理网页中的一些对话框信息
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			// 1对话框
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result)
			{
				// 构建一个Builder来显示网页中的 alert 对话框
				Builder builder = new Builder(SmartCarNet.this);
				builder.setTitle("提示对话框");
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

			// 带按钮的对话框
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result)
			{
				Builder builder = new Builder(SmartCarNet.this);
				builder.setTitle("带选择的对话框");
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

			// 设置网页加载的进度条
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

			// 设置应用程序的标题
			public void onReceivedTitle(WebView view, String title)
			{
				// SmartCarNet.this.setTitle(title);
				super.onReceivedTitle(view, title);
				
				if( title.length() > 20 )	//长度限制
				{
					title = title.substring(0, 19);
					title = title + "...";
				}				
				textViewWebInformation.setText(title + "——车载浏览器");
			}
		});

		// 7根据设备分辨率设置网页缩放
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int mDensity = dm.densityDpi;	// 屏幕密度（每寸像素：120/160/240/320）  
		putLog("当前屏蔽密度为：" + mDensity);

		if (mDensity == 240)
		{ 
			// 可以让不同的 density 的情况下，可以让页面进行适配
			mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		} else if (mDensity == 160)
		{
			mWebView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
		} else
		{
			mWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
		}
	
		// 8初始化加载百度主页
		editTextWebAddress.clearFocus();
		mWebView.requestFocus();
		currentLoadUrl = mainHomeUrl;
		mWebView.loadUrl(mainHomeUrl);
	}
	
	// 重写按钮，使用按Back时后退，而不是前一个Activity
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

	// 按钮单击事件
	private OnClickListener btnOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			switch (v.getId())
			{
			case R.id.buttonWebMin: // 最小化
				returnHome();
				break;
			case R.id.buttonWebClose: // 关闭
				finish();
				break;
			case R.id.buttonWebBack: // 后退
				if (mWebView.canGoBack())
					mWebView.goBack();
				else
					displayTip("无法后退!");
				break;
			case R.id.buttonWebGo: // 前进
				if (mWebView.canGoForward())
					mWebView.goForward();
				else
					displayTip("无法前进!");
				break;
			case R.id.buttonWebStop: // 停止
				if (isLoadWeb)
				{
					mWebView.stopLoading();
					isLoadWeb = false;
				} else
					displayTip("已加载完成!");
				break;
			case R.id.buttonWebRefresh: // 刷新
				if (currentLoadUrl.length() > 0)
					mWebView.loadUrl(currentLoadUrl);
				else
					displayTip("当前没有要刷新的网页!");
				break;
			case R.id.buttonWebHome: // 主页
				if (isLoadWeb)
				{
					mWebView.stopLoading();
					isLoadWeb = false;
				}
				mWebView.loadUrl(mainHomeUrl);
				break;
			case R.id.buttonWebOpen: // 打开
				doWebOpenWork(); // 执行打开网页动作
				break;
			default:

				break;
			}
		}
	};

	// 执行打开网页动作
	private void doWebOpenWork()
	{
		String openStr = editTextWebAddress.getText().toString();
		if (openStr.length() > 0)
		{
			currentLoadUrl = "http://" + openStr + "/";
			putLog("当前加载的网页地址为：" + currentLoadUrl);
			mWebView.loadUrl(currentLoadUrl);
		} else
		{
			displayTip("请输入一个有效网址!");
		}
	}

	// 输出调试信息
	private void putLog(String info)
	{
		Log.d(TAG, info);
	}	
	// 显示提示信息long
	private void displayTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}
	// 输出当前函数名称
	private void putFuncationName(String name)
	{
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}
}
