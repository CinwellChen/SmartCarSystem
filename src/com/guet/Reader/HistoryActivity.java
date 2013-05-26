package com.guet.Reader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.guet.Reader.Adapter.HistoryDbAdapter;
import com.guet.Reader.db.DbHelper;
import com.guet.SmartCarSystem.R;

public class HistoryActivity extends Activity {
	private DbHelper dbHelper;
	private ListView listView;
	private HistoryDbAdapter historyDbAdapter;
	private Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		listView = (ListView) findViewById(R.id.historylist);
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String path = cursor.getString(1);
				Intent intent = new Intent(getApplicationContext(),
						TxtReaderActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("key", path);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		refreshList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.history_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println(dbHelper.getWritableDatabase().delete("HistoryList",
				null, null));
		cursor.deactivate();
		refreshList();
		cursor.requery();
		return super.onOptionsItemSelected(item);
	}

	private void refreshList() {
		dbHelper = new DbHelper(this, "TxtReader_db", 1);
		cursor = dbHelper.getReadableDatabase().rawQuery(
				"select * from HistoryList", null);
		if (cursor.moveToFirst()) {
			historyDbAdapter = new HistoryDbAdapter(this, cursor);
			listView.setAdapter(historyDbAdapter);
		} else {
			Toast.makeText(this, "ÎÞÔÄ¶Á¼ÇÂ¼", Toast.LENGTH_SHORT).show();
			
		}
		dbHelper.close();
	}

	@Override
	protected void onDestroy() {
		if (dbHelper != null)
			dbHelper.close();
		if (cursor != null)
			cursor.close();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		refreshList();
		super.onResume();
	}
}
