package com.mipt.fileexplorer.remote.smb;

import java.util.Date;

import jcifs.smb.NtStatus;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileexplorer.R;
import com.mipt.fileexplorer.remote.smb.SmbUtils.SmbResult;

public class SmbAsynFileAdapter {
	public static final String TAG = "SmbAsynFileAdapter";
	
	public static class SmbServerListAdapter extends ArrayAdapter<SmbFile> {
		private Context thizContext;
		private SmbFile[] servers;
		SmbServerListAdapter(Context thiz, SmbFile[] servers) {
			super(thiz, R.layout.smb_file_item, servers);
			thizContext = thiz;
			this.servers = servers; 
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)thizContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View row = inflater.inflate(R.layout.smb_file_item, parent, false);
			TextView fileName = (TextView)row.findViewById(R.id.file_name);
			TextView lastDate = (TextView)row.findViewById(R.id.last_date);
			
			SmbFile file = servers[position];
			Log.i(TAG, "position:" + position + ", file:" + file.toString() + ",fileName:" + fileName + ",lastDate:" + lastDate);
			fileName.setText(file.getName());
			//Constants.simpleDateFormat.format(new Date(file.getLastModified()))
			lastDate.setText(new Date(file.getLastModified()).toLocaleString());				
			return row;
		}
	}	
	
	public static class BakFileRunnable implements Runnable {
		private Handler handler;
		private String url;
		public BakFileRunnable(Handler handler, String url){
			this.handler = handler;
			this.url = url;
		}
		@Override
		public void run() {
			Log.i(TAG, "start new thread, running...");
			Message m = handler.obtainMessage();
			m.what = Constants.GUI_THREADING_NOTIIER;
			handler.sendMessage(m);
						
			m = handler.obtainMessage();
			m.obj =  SmbUtils.dirDetail(url);
			//Log.i(TAG, "get smb len:" + m.obj);
							
			m.what = Constants.GUI_STOP_NOTIFIER;			
			handler.sendMessage(m);	
		}
	}

	static class BakFileHandler extends Handler{
		private Activity act = null;
		private ProgressBar bar = null;
		private ListView listView;
		
		public BakFileHandler(Activity act,ProgressBar bar, ListView listView){
			this.act = act;
			this.bar = bar;
			this.listView = listView;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.GUI_THREADING_NOTIIER:
				bar.setVisibility(View.VISIBLE);
				break;
			case Constants.GUI_STOP_NOTIFIER:
				bar.setVisibility(View.GONE);

				if(msg.obj != null){
					SmbResult result = (SmbResult)msg.obj;
					//Log.i(TAG, result.toString());
					//Toast.makeText(act, result.toString(), Toast.LENGTH_LONG).show();
					
					if(result.status == SmbResult.STATUS_OK){
						SmbFile[] files = result.smbFiles;						
						//Log.i(TAG, Arrays.toString(files));
						Log.i(TAG, "file amount:" + files.length + ",fileListView:" + listView.toString());
						ArrayAdapter<SmbFile> adapter = new SmbAsynFileAdapter.SmbServerListAdapter(act, files);
						listView.setAdapter(adapter);						
						
					}else if(result.status == SmbResult.STATUS_SMB_ERROR){
						if(result.ntStatus == NtStatus.NT_STATUS_LOGON_FAILURE){
							Toast.makeText(act, "NtStatus:" + result.ntStatus + ", name or password error.", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(act, "NtStatus:" + result.ntStatus, Toast.LENGTH_LONG).show();
						}
					}else{
						Log.i(TAG, "other error.");
					}
					//listView.setAdapter(new SmbServerListAdapter(act, reStr));
				}else{
					//Log.i(TAG, "" + reStr);
					Toast.makeText(act, R.string.no_lan_browser , Toast.LENGTH_SHORT).show();					
				}
				break;
			}
		};
	}
	
	static class OnItemClick implements OnItemClickListener{
		private Activity activity;
		private SmbFile[] files;
		
		public OnItemClick(Activity activity, SmbFile[] files){
			this.activity = activity;
			this.files = files;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			SmbFile file = files[position];
			try {
				if(file.isDirectory()){
					Toast.makeText(activity, "this is a dir.", Toast.LENGTH_SHORT).show();
					//
				}else{
					//
					Toast.makeText(activity, "this is a file.", Toast.LENGTH_SHORT).show();
				}
			} catch (SmbException e) {
				e.printStackTrace();
			}
		}		
	}
	
}
