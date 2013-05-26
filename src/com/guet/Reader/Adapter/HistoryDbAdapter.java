package com.guet.Reader.Adapter;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.guet.SmartCarSystem.R;

public class HistoryDbAdapter extends SimpleCursorAdapter {

	public HistoryDbAdapter(Context context, Cursor c) {
		super(context, R.layout.history_line, c, new String[] {"path","progress"}, new int[] {R.id.history_file_name,R.id.history_progress});
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.history_line,
					null);
		}
		TextView filename = (TextView) view
				.findViewById(R.id.history_file_name);
		TextView progress = (TextView) view.findViewById(R.id.history_progress);

		File file = new File(cursor.getString(1));
		if (file.exists()) {
			filename.setText(file.getName());
		}
		progress.setText(cursor.getString(2));
	}

}
