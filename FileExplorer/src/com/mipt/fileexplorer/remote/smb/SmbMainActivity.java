package com.mipt.fileexplorer.remote.smb;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mipt.fileexplorer.R;
import com.mipt.fileexplorer.local.FileExplorerTabActivity.IBackPressedListener;

public class SmbMainActivity extends Fragment implements IBackPressedListener{
	private static final String TAG = "SmbMain";
	private Activity mActivity;
	private View mRootView;
	private ListView listView;
	private ProgressBar progressBar;
	private Button button;
	private EditText editText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "on create view...");
        mActivity = getActivity();
        // getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mRootView = inflater.inflate(R.layout.smb_main, container, false);
		listView = (ListView)mRootView.findViewById(R.id.list_view);
		progressBar =(ProgressBar)mRootView.findViewById(R.id.progress_bar); 

		//editText = (EditText)mRootView.findViewById(R.id.edit_text);
		//editText.setText(SmbUtils.URL_EXAMLPLE);
		//textView = (TextView)mRootView.findViewById(R.id.text_view);
		
		//button = (Button)mRootView.findViewById(R.id.button);
		//button.setOnClickListener();
		Handler browserHandler = new SmbAsynLanBrowserAdpter.BakLanBrowserHandler(mActivity, progressBar, listView);
		new Thread(new SmbAsynLanBrowserAdpter.BakLanBrowserRunnable(browserHandler)).start();
		
		listView.setOnItemClickListener(new SmbAsynLanBrowserAdpter.ListOnItemClickListener(mActivity));
		return mRootView;
	}
	
	/*
	OnClickListener dirOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			try {
				Handler handler = new SmbAsynFileAdapter.BakFileHandler(mActivity, progressBar, listView);
				String urlStr = editText.getText().toString();
				Log.i(TAG, urlStr);
				new Thread(new SmbAsynFileAdapter.BakFileRunnable(handler, urlStr)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	*/
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}	
}
 