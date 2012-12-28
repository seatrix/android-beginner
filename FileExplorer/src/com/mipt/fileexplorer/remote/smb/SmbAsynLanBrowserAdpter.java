package com.mipt.fileexplorer.remote.smb;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileexplorer.R;

public class SmbAsynLanBrowserAdpter {
	public final static String TAG = "SmbAsynLanBrowserAdpter";
	

	public static class LanBrowserAdapter extends ArrayAdapter<LanNodeInfo> {
		public LanBrowserAdapter(Context thiz, List servers) {
			super(thiz, R.layout.smb_main_lan_borwser, servers);
		}

		@Override
		public View getView(int position, View row, ViewGroup parent) {
			if (row == null) {
				 LayoutInflater inflater = (LayoutInflater) getContext()
						 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				 row = inflater.inflate(R.layout.smb_main_lan_borwser, parent, false);
		    }

					
			TextView netbiosName = (TextView) row
					.findViewById(R.id.netbios_name);
			TextView ip = (TextView) row.findViewById(R.id.ip);
			LanNodeInfo info = getItem(position);
			
			int fisrtBlankIndex = info.name.indexOf(" ");
			String name = fisrtBlankIndex != -1  ? info.name.substring(0, fisrtBlankIndex) : info.name;
			netbiosName.setText(name);
			ip.setText("(" + info.ip + ")");
			return row;
		}
	}

	public static class BakLanBrowserHandler extends Handler {
		private Activity act = null;
		private ProgressBar bar = null;
		private ListView listView;

		List<LanNodeInfo>nodeInfoList = new ArrayList<LanNodeInfo>();
		
		public BakLanBrowserHandler(Activity act, ProgressBar bar,
				ListView listView) {
			this.act = act;
			this.bar = bar;
			this.listView = listView;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.GUI_THREADING_NOTIIER:
				bar.setVisibility(View.VISIBLE);
				if(msg.obj != null){
					//Log.i(TAG, "obtain Msg:" + msg.obj.toString());
					LanNodeInfo nodeInfo = (LanNodeInfo) msg.obj;
					
					if(listView.getAdapter() == null){
						Log.i(TAG, "init ...listadapter.");
						listView.setAdapter(new SmbAsynLanBrowserAdpter
								.LanBrowserAdapter(act, nodeInfoList));
						
					}else {
						//Log.i(TAG, "add ...listadapter.");
						nodeInfoList.add(nodeInfo);
						((ArrayAdapter<LanNodeInfo>)listView.getAdapter()).notifyDataSetChanged();
						listView.setSelection(0);
					}
				}				
				
				break;
			case Constants.GUI_STOP_NOTIFIER:
				listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
				bar.setVisibility(View.GONE);
/*				bar.setVisibility(View.GONE);
				if(msg.obj != null){
					@SuppressWarnings("unchecked")
					List<LanNodeInfo> nodeInfoList = (List<LanNodeInfo>) msg.obj;
					LanNodeInfo[] info = new LanNodeInfo[nodeInfoList.size()];
					nodeInfoList.toArray(info);
					ArrayAdapter<LanNodeInfo> adapter = new SmbAsynLanBrowserAdpter.LanBrowserAdapter(
							act, info);
					listView.setAdapter(adapter);					
				}else {
					Toast.makeText(act, R.string.no_lan_browser, Toast.LENGTH_LONG);
				}
*/			}
		}

		public List<LanNodeInfo> getNodeInfoList() {
			return nodeInfoList;
		}
	}

	public static class BakLanBrowserRunnable implements Runnable {
		private Handler handler;

		public BakLanBrowserRunnable(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			Log.i(TAG, "BakLanBrowserRunnable...");
			Message m = handler.obtainMessage();
			m.what = Constants.GUI_THREADING_NOTIIER;
			handler.sendMessage(m);
			
			String[] ipInfo = LanInfo.getIPInfo();

			//List<LanNodeInfo> list = new ArrayList<LanNodeInfo>();
			if (ipInfo != null) {
				Log.i(TAG, "net ip is " + ipInfo[0]);
				Log.i(TAG, "net segment is " + ipInfo[1]);

				long start = System.currentTimeMillis();
				int idx = 1;
				for (int i = 0; i < 256; i++) {
					String adr = ipInfo[1].concat(".").concat(String.valueOf(i));
					//Log.i(TAG, "scan ip is " + adr);
					try {
						UdpGetClientMacAddr udp = new UdpGetClientMacAddr(adr);
						LanNodeInfo info = udp.getRemoteMacAddr();
						
						info.num = idx++;
						m = handler.obtainMessage();
						m.what = Constants.GUI_THREADING_NOTIIER;
						m.obj = info;
						handler.sendMessage(m);
						//list.add(info);
					} catch (SocketTimeoutException e) {

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Log.i(TAG, "scan finish ! comsume time:" + (System.currentTimeMillis() - start));
				
				m = handler.obtainMessage();
				m.what = Constants.GUI_STOP_NOTIFIER;
/*				if(list != null){
					m.obj = list;					
				}
*/				handler.sendMessage(m);
			}
		}
	}
	
	public static class ListOnItemClickListener implements ListView.OnItemClickListener{
		private Activity activity;
		//private ListView listView;
		
		public ListOnItemClickListener(Activity activity){
			this.activity = activity;
			//this.listView = listView;
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Object ojb = arg0.getItemAtPosition(arg2);
			Toast.makeText(activity, ojb.toString(), Toast.LENGTH_LONG).show();
			LanNodeInfo nodeInfo = (LanNodeInfo)ojb;
			final String ip = nodeInfo.ip;
			
			//Log.i(TAG, "this netware share no privilege.");
			LayoutInflater inflater = activity.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.smb_user_password_input, null); 
			
			AlertDialog.Builder builder = new Builder(activity);
			builder.setView(layout)
			.setPositiveButton(R.string.confirm, new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText nameEdit = (EditText)layout.findViewById(R.id.user_name_edit);
					EditText passEdit = (EditText)layout.findViewById(R.id.password_edit);
					
					String name = null;
					String passwd = null;
					if(nameEdit.getText().length() == 0){
						Log.i(TAG, "set default user name.");
						name = "slieer";
					}else{
						name = nameEdit.getText().toString();
					}
					
					if(passEdit.getText().length() == 0){
						Log.i(TAG, "set default password.");
						passwd = "slieer";
					}else{
						passwd = passEdit.getText().toString();
					}
					
	        		String url = SmbUtils.URL_SHOW_LAN_NODE + name + ":" + passwd + "@" + ip + "/";
	        		Log.i(TAG, "smb url:" + url + ", call SmbListFileActivity...");
	        		
	        		activity.getActionBar()
					FragmentManager fragmentManager = activity.getFragmentManager(); 
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction(); 
					SmbListFileActivity fragment = new SmbListFileActivity();
					
					Bundle bundle = new Bundle();  
					bundle.putString("url", url);  
					fragment.setArguments(bundle);  

					fragmentTransaction.add(R.id.smb_main_layout,fragment);
					
					fragmentTransaction.commit();	        		
				}
				
			}) 
			.setNegativeButton(R.string.cancel, null).show();
		}
	};
	
	public static class ListonItemSelectedListener implements ListView.OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	};
	
}
