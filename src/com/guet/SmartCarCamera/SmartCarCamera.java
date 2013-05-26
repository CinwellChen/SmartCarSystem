package com.guet.SmartCarCamera;


import java.io.IOException;

import com.guet.SmartCarSystem.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class SmartCarCamera extends Activity
{
	private static final String TAG = "Car";
	private Camera mCamera;					//摄像头控制
	private SurfaceView mSurfaceView;		//显示
	private SurfaceHolder mSurfaceHolder;	//显示控制
	private boolean mPreviewRunning = false;//预览标识
	
	// 结束倒车摄像广播事件
	private static final String FINISH_CAMEAR_ACTION = "finish_Camera_Action";

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mirrorhome);
		
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceViewRearviewMirror);
		
		//检查是否存在摄像头
		if( checkCameraHardware(this) )
		{	
			mSurfaceHolder = mSurfaceView.getHolder();			
			mSurfaceHolder.setFixedSize(800, 480); // 设置分辨率		
			/* 下面设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前 */		
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mSurfaceHolder.addCallback(new SurfaceCallback());
		}else {
			displayLongTip("未发现可用摄像头！请检查硬件设备后再试!");
			putLog("未发现可用摄像头！请检查硬件设备后再试!");
		}
		
		//触屏事件，用于显示弹出弹出
		mSurfaceView.setOnClickListener( new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				putLog("onClick...");
				displayTip("温馨提示：长按退出..");
			}
		});		
		mSurfaceView.setOnLongClickListener( new OnLongClickListener()
		{			
			@Override
			public boolean onLongClick(View v)
			{
				// TODO Auto-generated method stub
				finish();
				return true;
			}
		});
		
		//注册主页接收倾听
		//注册主程序广播接收事件,用于主页通信，结束本程序
		IntentFilter recFilter = new IntentFilter(FINISH_CAMEAR_ACTION);
		//recFilter.setPriority(1000); // 设置优先级最大
		registerReceiver( mReceiverBroadcastReceiver, recFilter);		
		
		putFuncationName("onCreate");
	}
	
	//主页广播接收类
	private BroadcastReceiver mReceiverBroadcastReceiver = new BroadcastReceiver()
	{	
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			if (intent.getAction().equals(FINISH_CAMEAR_ACTION))
			{
				putLog("收到主程序结束的广播...");
				SmartCarCamera.this.finish();	//结束倒车摄像
			}
		}
	};
		
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//注销广播
		unregisterReceiver(mReceiverBroadcastReceiver);
		putFuncationName("onDestroy");
	}

	//控制类
	private final class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			// TODO Auto-generated method stub
			// 在surfaceCreated方法中“打开”摄像头
			mCamera = Camera.open();
			putFuncationName("surfaceCreated");
		}

		@Override		
		//该方法让摄像头做好拍照准备设定它的参数，并开始在屏幕中启动预览画面。 
		//当mPreviewRunning为true时，意味着摄像头处于激活状态，并未被关闭可以使用它。
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height)
		{
			// TODO Auto-generated method stub			
			if (mPreviewRunning)
			{
				mCamera.stopPreview();	//停止预览	
				mPreviewRunning = false;
			}
			
			//获取相机参数控制权、并设置
			Camera.Parameters setp = mCamera.getParameters();		
			setp.setPreviewSize(width, height);
						
			//判断是否支持自动调焦
			if( getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) )
			{
				//设置自动对焦 
				setp.setFocusMode("auto");
				putLog("支持自动调焦!");
			}else {
				putLog("不支持自动调焦!");
			}
			
			//设置图片格式 
			//parameters.setPictureFormat(PixelFormat.JPEG);
			//设置图片保存时的分辨率大小 
			//setp.setPictureSize(800, 480);
			mCamera.setParameters(setp);
			
			try
			{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();	//启动预览
				mPreviewRunning = true;
				
			} catch (IOException e)
			{
				e.printStackTrace();
				// 释放相机资源并置空 
				//mCamera.release(); 
				//mCamera = null;
			}
			
			putFuncationName("surfaceChanged");
		}

		@Override
		//通过这个方法停止摄像头，并释放相关的资源,在这儿设置mPreviewRunning为false， 
		//以此来防止在surfaceChanged方法中的冲突
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			// TODO Auto-generated method stub
			mCamera.stopPreview();	//停止预览
			mPreviewRunning = false;
			mCamera.release();		//释放硬件
			mCamera = null;
			putFuncationName("surfaceDestroyed");
		} 		
	}	
	
	//检查设备是否提供摄像头
	private boolean checkCameraHardware(Context context)
	{ 
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){ 

	        // 摄像头存在 
	        return true; 

	    } else {

	        // 摄像头不存在 
	        return false; 
	    } 
	}
	
	// 输出调试信息
	private void putLog(String info)
	{
		Log.d(TAG, info);
	}	
	// 显示提示信息
	private void displayTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}
	// 显示提示信息long
	private void displayLongTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}
	// 输出当前函数名称
	private void putFuncationName(String name)
	{
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}
}