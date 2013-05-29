package com.guet.Reader;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.guet.SmartCarSystem.R;

public class MainTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 设置全屏模式,去除标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.maintab);
		TabHost tabHost = getTabHost();
		Intent historyIntent = new Intent(this, HistoryActivity.class);
		Intent listIntent = new Intent(this, TxtListActivity.class);
		TabSpec historySpec = tabHost.newTabSpec("history");
		historySpec.setIndicator("阅读记录",
				getResources().getDrawable(android.R.drawable.ic_menu_recent_history));
		historySpec.setContent(historyIntent);
		tabHost.addTab(historySpec);
		TabSpec listSpec = tabHost.newTabSpec("list");
		listSpec.setIndicator(
				"扫描电子书",
				getResources().getDrawable(
						android.R.drawable.ic_search_category_default));
		listSpec.setContent(listIntent);
		tabHost.addTab(listSpec);
	}
}
