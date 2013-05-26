package com.guet.Reader.Adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guet.SmartCarSystem.R;

public class FileAdapter extends BaseAdapter {
	private List<File> fileList = new ArrayList<File>();
	private Context context;

	public FileAdapter(Context context, List<File> fileList) {
		this.context = context;
		this.fileList = fileList;
	}

	public int getCount() {
		return fileList.size();
	}

	public Object getItem(int position) {
		return fileList.get(position);
	}


	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_layout, null);
		}
		ImageView file_icon = (ImageView) convertView
				.findViewById(R.id.list_txt_icon);
		TextView file_name = (TextView) convertView
				.findViewById(R.id.list_file_name);
		TextView file_size = (TextView) convertView
				.findViewById(R.id.list_file_size);
		TextView file_last_time = (TextView) convertView
				.findViewById(R.id.list_file_last_time);
		file_icon.setImageResource(R.drawable.ic_file_text);
		file_name.setText(fileList.get(position).getName());
		file_size.setText(fileList.get(position).length() / 1024 + "kb");
		file_last_time.setText(dateformat.format(fileList.get(position)
				.lastModified()));

		return convertView;
	}

}
