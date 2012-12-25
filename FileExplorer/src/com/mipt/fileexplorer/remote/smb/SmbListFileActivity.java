package com.mipt.fileexplorer.remote.smb;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mipt.fileexplorer.R;

public class SmbListFileActivity extends Activity {
	public static final String TAG = "SmbListFileActivity";
	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smb_file_list);
		ListView listView = (ListView) findViewById(R.id.list_view);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

		String url = getIntent().getStringExtra("url");
		Log.i(TAG, "visit remote computer by NTLM... url:" + url);

		handler = new SmbAsynFileAdapter.BakFileHandler(this, progressBar,
				listView);
		runnable = new SmbAsynFileAdapter.BakFileRunnable(handler, url);
		new Thread().start();

		// OnItemClickListener listener = new OnItemClick(this);
		// listView.setOnItemClickListener(listener);
	}

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}
}
