package com.explorer.activity;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
//import android.os.SambaManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;

import com.explorer.R;
import com.explorer.common.CommonActivity;
//import com.explorer.jni.Jni;
//import com.explorer.jni.NfsClient;

/**
 * 页面容器
 * 
 * @author qian_wei
 */
public class TabBarExample extends TabActivity {

	// 图片数组
	private int myMenuRes[] = { R.drawable.tab1, R.drawable.tab2,
			R.drawable.tab3, R.drawable.tab4, };

	// 标签页视图
	TabHost tabHost;

	// 本地标签
	TabSpec firstTabSpec;

	// FTP标签
	// TabSpec secondTabSpec;

	// SAMBA标签
	TabSpec threeTabSpec;

	// NFS标签
	TabSpec fourTabSpec;

	// 标签栏
	private static TabWidget widget;

	private IntentFilter mIntenFilter = null;
	private BroadcastReceiver mReceiver = null;
	private static final String TAG = "TabBarExample";

	/**
	 * 页面显示
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);

		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		widget = (TabWidget) findViewById(android.R.id.tabs);
		firstTabSpec = tabHost.newTabSpec("tid1");
		threeTabSpec = tabHost.newTabSpec("tid3");
		fourTabSpec = tabHost.newTabSpec("tid4");

		// 创建标签页
		firstTabSpec.setIndicator(getString(R.string.local_tab_title),
				getResources().getDrawable(myMenuRes[0]));
		threeTabSpec.setIndicator(getString(R.string.lan_tab_title),
				getResources().getDrawable(myMenuRes[2]));
		fourTabSpec.setIndicator(getString(R.string.nfs_tab_title),
				getResources().getDrawable(myMenuRes[3]));

		// 设置标签页内容
		firstTabSpec.setContent(new Intent(this, MainExplorerActivity.class));
		threeTabSpec.setContent(new Intent(this, SambaActivity.class));
		fourTabSpec.setContent(new Intent(this, NFSActivity.class));

		// 将标签页填充到同一视图
		tabHost.addTab(firstTabSpec);
		tabHost.addTab(threeTabSpec);
		tabHost.addTab(fourTabSpec);

		DisplayMetrics metric = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		// 设置标签文字大小
		int count = widget.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = widget.getChildTabViewAt(i);
			view.getLayoutParams().height = 80;
			TextView text = (TextView) view.findViewById(android.R.id.title);
			if (densityDpi < 182) {
				text.setTextSize(28);
			} else {
				text.setTextSize(20);
			}
		}

		mIntenFilter = new IntentFilter();
		mIntenFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mIntenFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mIntenFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		mIntenFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mIntenFilter.addAction(ConnectivityManager.INET_CONDITION_ACTION);

		mIntenFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
		mIntenFilter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (0 != tabHost.getCurrentTab()) {
					boolean bIsConnect = true;
					final String action = intent.getAction();
					if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
						final NetworkInfo networkInfo = (NetworkInfo) intent
								.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						bIsConnect = networkInfo != null
								&& networkInfo.isConnected();
					} else if (action
							.equals(ConnectivityManager.CONNECTIVITY_ACTION)
							|| action
									.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
						NetworkInfo info = (NetworkInfo) (intent
								.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
						bIsConnect = info.isConnected();
					} else if (action
							.equals(EthernetManager.ETHERNET_STATE_CHANGED_ACTION)
							|| action
									.equals(EthernetManager.NETWORK_STATE_CHANGED_ACTION)) {
						int status = intent.getIntExtra(
								EthernetManager.EXTRA_ETHERNET_STATE,
								EthernetManager.ETHERNET_STATE_UNKNOWN);
						bIsConnect = (0 == status) || (4 == status);
					}
					;
					if (false == bIsConnect) {
						Toast
								.makeText(
										TabBarExample.this,
										getString(R.string.network_error_exitnetbrowse),
										Toast.LENGTH_LONG).show();
						tabHost.setCurrentTab(0);
					}
				}
			};
		};
		registerReceiver(mReceiver, mIntenFilter);

		// begin modify by qian_wei/xiong_cuifan 2011/11/08
		// for while first into application, the widget may fall down
		// widget.requestFocus();
		MainExplorerActivity mainActivity = (MainExplorerActivity) getCurrentActivity();
		mainActivity.expandableListView.requestFocus();
		// end modify by qian_wei/xiong_cuifan 2011/11/08

		// 标签切换时，焦点在标签上，刷新界面内容
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				switch (tabHost.getCurrentTab()) {
				case 0:
					widget.requestFocus();
					CommonActivity.cancleToast();
					MainExplorerActivity mainActivity = (MainExplorerActivity) getCurrentActivity();
					if (mainActivity.isFileCut) {
						if (!mainActivity.getPathTxt().getText().toString()
								.equals("")) {
							mainActivity.updateList(true);
							mainActivity.isFileCut = false;
						}
					}
					break;
				case 1:
					widget.requestFocus();
					CommonActivity.cancleToast();
					SambaActivity smbActivity = (SambaActivity) getCurrentActivity();
					if (smbActivity.IsNetworkDisconnect()) {
						tabHost.setCurrentTab(0);
					} else {
						if (smbActivity.isFileCut) {
							if (!smbActivity.getPathTxt().getText().toString()
									.equals("")
									&& !smbActivity
											.getPathTxt()
											.getText()
											.toString()
											.equals(smbActivity.getServerName())) {
								smbActivity.updateList(true);
								smbActivity.isFileCut = false;
							}
						}
					}
					break;
				case 2:
					widget.requestFocus();
					CommonActivity.cancleToast();
					NFSActivity nfsActivity = (NFSActivity) getCurrentActivity();
					if (nfsActivity.IsNetworkDisconnect()) {
						tabHost.setCurrentTab(0);
					} else {
						if (nfsActivity.isFileCut) {
							if (!nfsActivity.getPathTxt().getText().toString()
									.equals("")) {
								nfsActivity.updateList(true);
								nfsActivity.isFileCut = false;
							}
						}
					}
					break;
				}
			}
		});
	}

	/**
	 * 获得标签
	 *
	 * @return 标签
	 */
	public static TabWidget getWidget() {
		return widget;
	}

	/**
	 * 取消提示
	 */
	protected void onStop() {
		super.onStop();
		CommonActivity.cancleToast();
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	// begin modify by qian_wei/zhou_yong 2011/10/26
	// for to maintain constant link
	// /**
	// * 退出应用时，关闭长连接
	// */
	// protected void onDestroy() {
	// // 卸载SMB连接
	// SambaManager samba = (SambaManager) getSystemService("Samba");
	// samba.start("", "", "", "");
	// Jni jni = new Jni();
	// jni.umountlist();
	// // 卸载NFS连接
	// NfsClient nNfsClient = new NfsClient();
	// nNfsClient.umountAllNFS();
	// super.onDestroy();
	// }
	// end modify by qian_wei/zhou_yong 2011/10/26
}
