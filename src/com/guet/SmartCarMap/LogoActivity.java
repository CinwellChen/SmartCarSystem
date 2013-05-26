package com.guet.SmartCarMap;

import com.guet.SmartCarSystem.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class LogoActivity extends Activity {

	private ImageView splashLogo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	        						  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.logo);
		initView();
	}
	
	private void initView(){
		splashLogo = (ImageView) findViewById(R.id.splash_logo);
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
		anim.setDuration(200);
		anim.setFillAfter(true);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LogoActivity.this, AMapActivity.class);
				startActivity(intent);
				finish();
			}
		});
		splashLogo.setAnimation(anim);
	}
}
