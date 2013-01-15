package com.mipt.fileMgr.center;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.FavFragment;
import com.mipt.mediacenter.center.file.FileIconHelper;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.utils.Util;

/**
 * 
 * @author fang
 * 
 */
public class FavListActivity extends Activity {
	private static final String LOG_TAG = "DeviceFragment";
	private FileIconHelper mFileIconHelper;
	private GridView gridView;
	private FavFragment.favAdapter adapter;
	private TextView fileName;
	private TextView fileDate;
	private TextView fileSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fav_more_list);
		initView();
		mFileIconHelper = new FileIconHelper(this);
		adapter = new FavFragment.favAdapter(this, mFileIconHelper);
		ArrayList<FileInfo> listTemp = new ArrayList<FileInfo>();
		adapter.addList(listTemp);
		gridView.setAdapter(adapter);

		gridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Log.i(LOG_TAG, "--onItemSelected--");
				FileInfo file = (FileInfo) arg0.getAdapter().getItem(arg2);
				setFileInfo(file);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.i(LOG_TAG, "--onNothingSelected--");
			}
			//
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// FileInfo file = (FileInfo) arg0.getAdapter().getItem(arg2);
				// setText(fileName, file.fileName);
				// setText(fileDate, Util.formatDateString(FavListActivity.this,
				// file.fileDate));
				// setText(fileSize, Util.convertStorage(file.fileSize));
			}

		});
		if (!listTemp.isEmpty()) {
			gridView.setSelection(0);
			setFileInfo(listTemp.get(0));
		}

	}

	void setFileInfo(FileInfo file) {
		setText(fileName, file.fileName);
		setText(fileDate,
				Util.formatDateString(FavListActivity.this, file.modifiedDate));
		setText(fileSize, Util.convertStorage(file.fileSize));
	}

	void initView() {
		gridView = (GridView) findViewById(R.id.more_fav_content);
		fileName = (TextView) findViewById(R.id.file_name);
		fileDate = (TextView) findViewById(R.id.file_date);
		fileSize = (TextView) findViewById(R.id.file_size);
		((TextView) findViewById(R.id.current_path))
				.setText(getString(R.string.tab_favorite));
	}

	void setText(TextView view, String text) {
		view.setText(text);
	}
}
