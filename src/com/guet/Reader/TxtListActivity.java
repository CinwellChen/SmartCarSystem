package com.guet.Reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.guet.Reader.Adapter.FileAdapter;
import com.guet.SmartCarSystem.R;

public class TxtListActivity extends Activity {
	private List<File> txtList = new ArrayList<File>();
	private ListView lv;
	private FileAdapter adapter;
	public ProgressDialog progressDialog;
	private int pos = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		lv = (ListView) findViewById(R.id.list);
		lv.setOnItemClickListener(new ItemListener());
		if (!Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "未发现SD卡", Toast.LENGTH_LONG).show();
		}
		fillList("/mnt/sdcard");
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showOperateMenu(txtList.get(position).getAbsoluteFile());
				pos = position;
				return false;
			}
		});
	}

	@Override
	protected void onPause() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		super.onPause();
	}

	private void showOperateMenu(File file) {
		String[] menu = { "删除", "清空" };
		new AlertDialog.Builder(this).setTitle("请您选择你要进行的操作")
				.setItems(menu, new FileClickListener(file, this)).show();
	}

	public class FileClickListener implements DialogInterface.OnClickListener {
		File file;// 需要进行操作的文件
		Context context;

		public FileClickListener(File choosefile, Context context) {
			file = choosefile;
			this.context = context;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				new AlertDialog.Builder(context)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage("确定要删除此文件吗?").setTitle(file.getName())
						.setPositiveButton("删除", new OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								if (file.delete()) {
									Toast.makeText(context, "删除成功.",
											Toast.LENGTH_SHORT).show();
									txtList.remove(pos);
									adapter.notifyDataSetChanged();
								} else {
									Toast.makeText(context, "删除失败.",
											Toast.LENGTH_SHORT).show();
								}
							}
						}).setNegativeButton("不删除", null).show();

			} else if (which == 1) {
				AlertDialog.Builder builder = new Builder(context);
				View layout = getLayoutInflater()
						.inflate(R.layout.rename, null);
				final TextView newName = (TextView) layout
						.findViewById(R.id.newname);
				newName.setText(file.getName());
				builder.setView(layout).setTitle("请输入新的文件名:")
						.setPositiveButton("确定", new OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								String newname = newName.getText().toString();
								if (!newname.equals(file.getName())) {

									// 获取全名
									final String allName = "mnt/sdcard/"
											+ newname;
									// 判断是否重名了
									if (new File(allName).exists()) {
										Toast.makeText(context,
												"无法重命名,因为原来已经存在此文件",
												Toast.LENGTH_SHORT).show();
									} else {
										if (file.renameTo(new File(allName))) {
											Toast.makeText(context, "重命名成功!",
													Toast.LENGTH_SHORT).show();
											txtList.remove(pos);
											txtList.add(file);
											adapter.notifyDataSetChanged();
										} else
											Toast.makeText(context, "重命名失败!",
													Toast.LENGTH_SHORT).show();
									}
								} else
									Toast.makeText(context,
											"无法重命名,因为原来已经存在此文件",
											Toast.LENGTH_SHORT).show();
							}
						}).setNegativeButton("取消", null).show();
			}
		}
	}

	public class ItemListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			Intent intent = new Intent(getApplicationContext(),
					TxtReaderActivity.class);
			Bundle bundle = new Bundle();
			File file = txtList.get(arg2);
			if (file.exists()) {
				showProgress();
				bundle.putString("key", file.getAbsolutePath());
				intent.putExtras(bundle);
				startActivity(intent);

			} else {
				Toast.makeText(getApplicationContext(), "文件不存在",
						Toast.LENGTH_SHORT).show();
				txtList.remove(pos);
				adapter.notifyDataSetChanged();
			}
		}
	}

	public void showProgress() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("读取中...");
		progressDialog.setMessage("请稍后");
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.show();
	}

	private void fillList(String path) {
		File dir = new File(path);
		File[] f = dir.listFiles();
		if (f == null)
			return;
		for (int i = 0; i < f.length; i++) {
			if (f[i].isDirectory()) {
				fillList(f[i].getAbsolutePath());
			} else {
				String strFileName = f[i].getName();
				if (strFileName.endsWith(".txt")) {
					txtList.add(f[i]);
				}
			}
		}
		adapter = new FileAdapter(this, txtList);
		lv.setAdapter(adapter);
	}
}
