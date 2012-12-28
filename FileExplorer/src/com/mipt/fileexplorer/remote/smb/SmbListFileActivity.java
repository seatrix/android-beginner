package com.mipt.fileexplorer.remote.smb;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mipt.fileexplorer.R;
import com.mipt.fileexplorer.local.FileExplorerTabActivity.IBackPressedListener;

public class SmbListFileActivity extends Fragment  implements IBackPressedListener{
	public static final String TAG = "SmbListFileActivity";
	private Activity mActivity;
	private View mRootView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "init Smb.List.File.Activity....");
		Log.i(TAG, "replication output info....");
        mActivity = getActivity();
        
        mRootView = inflater.inflate(R.layout.smb_file_list, container, false);
        ListView listView = (ListView)mRootView.findViewById(R.id.list_view);
		ProgressBar progressBar = (ProgressBar) mRootView.findViewById(R.id.progress_bar);

		String url = (String)getArguments().get("url");
		Log.i(TAG, "visit remote computer by NTLM... url:" + url);
		Log.i(TAG, "listView:" + listView );

		Handler handler = new SmbAsynFileAdapter.BakFileHandler(mActivity, progressBar,
				listView);
		Runnable runnable = new SmbAsynFileAdapter.BakFileRunnable(handler, url);
		new Thread(runnable).start();

		// OnItemClickListener listener = new OnItemClick(this);
		// listView.setOnItemClickListener(listener);
		
		return mRootView;
	}

	@Override
	public boolean onBack() {
		return false;
	}
}
