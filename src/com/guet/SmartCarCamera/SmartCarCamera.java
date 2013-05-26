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
	private Camera mCamera;					//����ͷ����
	private SurfaceView mSurfaceView;		//��ʾ
	private SurfaceHolder mSurfaceHolder;	//��ʾ����
	private boolean mPreviewRunning = false;//Ԥ����ʶ
	
	// ������������㲥�¼�
	private static final String FINISH_CAMEAR_ACTION = "finish_Camera_Action";

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mirrorhome);
		
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceViewRearviewMirror);
		
		//����Ƿ��������ͷ
		if( checkCameraHardware(this) )
		{	
			mSurfaceHolder = mSurfaceView.getHolder();			
			mSurfaceHolder.setFixedSize(800, 480); // ���÷ֱ���		
			/* ��������Surface��ά���Լ��Ļ����������ǵȴ���Ļ����Ⱦ���潫�������͵��û���ǰ */		
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mSurfaceHolder.addCallback(new SurfaceCallback());
		}else {
			displayLongTip("δ���ֿ�������ͷ������Ӳ���豸������!");
			putLog("δ���ֿ�������ͷ������Ӳ���豸������!");
		}
		
		//�����¼���������ʾ��������
		mSurfaceView.setOnClickListener( new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				putLog("onClick...");
				displayTip("��ܰ��ʾ�������˳�..");
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
		
		//ע����ҳ��������
		//ע��������㲥�����¼�,������ҳͨ�ţ�����������
		IntentFilter recFilter = new IntentFilter(FINISH_CAMEAR_ACTION);
		//recFilter.setPriority(1000); // �������ȼ����
		registerReceiver( mReceiverBroadcastReceiver, recFilter);		
		
		putFuncationName("onCreate");
	}
	
	//��ҳ�㲥������
	private BroadcastReceiver mReceiverBroadcastReceiver = new BroadcastReceiver()
	{	
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			if (intent.getAction().equals(FINISH_CAMEAR_ACTION))
			{
				putLog("�յ�����������Ĺ㲥...");
				SmartCarCamera.this.finish();	//������������
			}
		}
	};
		
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//ע���㲥
		unregisterReceiver(mReceiverBroadcastReceiver);
		putFuncationName("onDestroy");
	}

	//������
	private final class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			// TODO Auto-generated method stub
			// ��surfaceCreated�����С��򿪡�����ͷ
			mCamera = Camera.open();
			putFuncationName("surfaceCreated");
		}

		@Override		
		//�÷���������ͷ��������׼���趨���Ĳ���������ʼ����Ļ������Ԥ�����档 
		//��mPreviewRunningΪtrueʱ����ζ������ͷ���ڼ���״̬����δ���رտ���ʹ������
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height)
		{
			// TODO Auto-generated method stub			
			if (mPreviewRunning)
			{
				mCamera.stopPreview();	//ֹͣԤ��	
				mPreviewRunning = false;
			}
			
			//��ȡ�����������Ȩ��������
			Camera.Parameters setp = mCamera.getParameters();		
			setp.setPreviewSize(width, height);
						
			//�ж��Ƿ�֧���Զ�����
			if( getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) )
			{
				//�����Զ��Խ� 
				setp.setFocusMode("auto");
				putLog("֧���Զ�����!");
			}else {
				putLog("��֧���Զ�����!");
			}
			
			//����ͼƬ��ʽ 
			//parameters.setPictureFormat(PixelFormat.JPEG);
			//����ͼƬ����ʱ�ķֱ��ʴ�С 
			//setp.setPictureSize(800, 480);
			mCamera.setParameters(setp);
			
			try
			{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();	//����Ԥ��
				mPreviewRunning = true;
				
			} catch (IOException e)
			{
				e.printStackTrace();
				// �ͷ������Դ���ÿ� 
				//mCamera.release(); 
				//mCamera = null;
			}
			
			putFuncationName("surfaceChanged");
		}

		@Override
		//ͨ���������ֹͣ����ͷ�����ͷ���ص���Դ,���������mPreviewRunningΪfalse�� 
		//�Դ�����ֹ��surfaceChanged�����еĳ�ͻ
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			// TODO Auto-generated method stub
			mCamera.stopPreview();	//ֹͣԤ��
			mPreviewRunning = false;
			mCamera.release();		//�ͷ�Ӳ��
			mCamera = null;
			putFuncationName("surfaceDestroyed");
		} 		
	}	
	
	//����豸�Ƿ��ṩ����ͷ
	private boolean checkCameraHardware(Context context)
	{ 
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){ 

	        // ����ͷ���� 
	        return true; 

	    } else {

	        // ����ͷ������ 
	        return false; 
	    } 
	}
	
	// ���������Ϣ
	private void putLog(String info)
	{
		Log.d(TAG, info);
	}	
	// ��ʾ��ʾ��Ϣ
	private void displayTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
	}
	// ��ʾ��ʾ��Ϣlong
	private void displayLongTip(String tipStr)
	{
		if (tipStr != null)
			Toast.makeText(this, tipStr, Toast.LENGTH_LONG).show();
	}
	// �����ǰ��������
	private void putFuncationName(String name)
	{
		Log.d(TAG, this.getLocalClassName() + ": " + name + "()");
	}
}