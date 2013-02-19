package com.mipt.mediacenter.center;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.utils.cifs.CifsConstants;
import com.mipt.mediacenter.utils.cifs.LanInfo;
import com.mipt.mediacenter.utils.cifs.LanNodeInfo;
import com.mipt.mediacenter.utils.cifs.UdpGetClientMacAddr;


public class CifsLanBrowserAdpter {
	public final static String TAG = "SmbAsynLanBrowserAdpter";
	public final static Pattern ASCII = Pattern.compile("[\\x21-\\x7E]+");

	public static class LanBrowserAdapter extends ArrayAdapter<LanNodeInfo> {
		public LanBrowserAdapter(Context thiz, List<LanNodeInfo> servers) {
			super(thiz, R.layout.smb_device_list_item, servers);
		}

		@Override
		public View getView(int position, View row, ViewGroup parent) {
			if (row == null) {
				 LayoutInflater inflater = (LayoutInflater) getContext()
						 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				 row = inflater.inflate(R.layout.smb_device_list_item, parent, false);
		    }

			TextView netbiosName = (TextView) row
					.findViewById(R.id.netbios_name);
			TextView ip = (TextView) row.findViewById(R.id.ip);
			LanNodeInfo info = getItem(position);
			
			netbiosName.setText(info.name);
			ip.setText("(" + info.ip + ")");
			return row;
		}
	}

	public static class BakLanBrowserHandler extends Handler {
		private Activity act = null;
		private ProgressBar bar = null;
		private ListView listView;
		private List<LanNodeInfo>nodeInfoList;
		
		public BakLanBrowserHandler(Activity act, ProgressBar bar,
				ListView listView, List<LanNodeInfo> nodeInfoList) {
			this.act = act;
			this.bar = bar;
			this.listView = listView;
			this.nodeInfoList = nodeInfoList; 
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CifsConstants.GUI_THREADING_NOTIIER:
				bar.setVisibility(View.VISIBLE);
				if(msg.obj != null){
					//Log.i(TAG, "obtain Msg:" + msg.obj.toString());
					LanNodeInfo nodeInfo = (LanNodeInfo) msg.obj;
					
					//Log.i(TAG, "add ...listadapter:" + nodeInfo);
					nodeInfoList.add(nodeInfo);
					((ArrayAdapter<LanNodeInfo>)listView.getAdapter()).notifyDataSetChanged();
					listView.setSelection(0);
				}
				
				break;
			case CifsConstants.GUI_STOP_NOTIFIER:
				listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
				bar.setVisibility(View.GONE);
/*				
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
			m.what = CifsConstants.GUI_THREADING_NOTIIER;
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
						
		                int fisrtBlankIndex = info.name.indexOf(" ");
		                String name = fisrtBlankIndex != -1  ? info.name.substring(0, fisrtBlankIndex) : info.name;
		                
		                Matcher matcher = ASCII.matcher(name);
		                if (! matcher.matches()){
		                    Log.i(TAG, "illegal name:" + name);
		                    continue;
		                }
		                info.name = name;
		                
						info.num = idx++;
						m = handler.obtainMessage();
						m.what = CifsConstants.GUI_THREADING_NOTIIER;
						m.obj = info;
						handler.sendMessage(m);
						//list.add(info);
					} catch (SocketTimeoutException e) {
					    //Log.e(TAG, e.getMessage(), e);
					} catch (Exception e) {
					    Log.e(TAG, e.getMessage(), e);
					}
				}
				Log.i(TAG, "scan finish ! comsume time:" + (System.currentTimeMillis() - start));
				m = handler.obtainMessage();
				m.what = CifsConstants.GUI_STOP_NOTIFIER;
/*				if(list != null){
					m.obj = list;					
				}
*/				handler.sendMessage(m);
			}
		}
	}
	

	
}
