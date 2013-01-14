package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.server.DeviceInfo;
import com.mipt.fileMgr.center.server.MediacenterConstant;
import com.mipt.fileMgr.dlna.AllShareProxy;
import com.mipt.fileMgr.dlna.DeviceUpdateBrocastFactory;
import com.mipt.fileMgr.utils.ActivitiesManager;
import com.mipt.fileMgr.utils.HandlerManager;
import com.mipt.fileMgr.utils.ToastFactory;
import com.mipt.fileMgr.utils.UsbScanManager;
import com.mipt.fileMgr.utils.Util;
import com.mipt.fileMgr.utils.Util.SDCardInfo;

/**
 * 
 * @author fang
 * 
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	//private RadioGroup tabGroup;
	//private int tabId = MediacenterConstant.IntentFlags.PIC_ID;
	//private int checkId = getcheckedIdByTabId(tabId);
	private Context cxt;
	private ArrayList<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
	private ArrayList<DeviceInfo> oldDeviceInfos = new ArrayList<DeviceInfo>();
	private static final String InstanceState = "tabId";
	private boolean createNew;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cm_activity_main);
		cxt = MainActivity.this;
		//tabGroup = (RadioGroup) findViewById(R.id.main_table);
		Log.i(TAG, "MainActivity.oncreate...");
		addBroadCast();
		/*
        if (savedInstanceState != null) {
			checkId = savedInstanceState.getInt(InstanceState);
			if (checkId != 0) {
				lastView = (RadioButton) findViewById(checkId);
				switchTab(checkId);
			}

		}*/
		initData();
		createNew = true;
/*		if (lastView == null) {
			RadioButton radio = (RadioButton) tabGroup.getChildAt(0);
			lastView = radio;
			radio.requestFocus();
			radio.setTextColor(Color.WHITE);
			radio.setTextSize(34);
			radio.setSelected(true);
			switchTab(radio.getId());
		}
*/
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putInt(InstanceState, getcheckedIdByTabId(tabId));
	}

	private void initData() {
		/*for (int i = 0; i < tabGroup.getChildCount(); i++) {
			RadioButton radio = (RadioButton) tabGroup.getChildAt(i);
			radio.requestFocus();
			radio.setOnFocusChangeListener(this);
			radio.setOnClickListener(this);
			radio.setOnHoverListener(new OnHoverListener() {
				@Override
				public boolean onHover(View v, MotionEvent event) {
					if (MotionEvent.ACTION_HOVER_ENTER == event.getAction()) {
						RadioButton radio = (RadioButton) v;
						lastView = radio;
						radio.requestFocus();
						radio.setTextColor(Color.WHITE);
						radio.setTextSize(34);
						radio.setSelected(true);
						switchTab(v.getId());
						return true;
					}
					return false;
				}
			});
		}
		tabGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switchTab(checkedId);
				// lastView = group.getFocusedChild();
			}
		});
		*/
	    
	    addFragmentToStack(false, deviceInfos);
		AllShareProxy.getInstance(cxt).initSearchEngine();
		onDataChanged(0, null);
		HandlerManager.getInstance().registerHandler(
				HandlerManager.MainHandler, mHandler);
		//oldDeviceInfos.addAll(deviceInfos);
	}

	//private RadioButton lastView;

/*	private int getcheckedIdByTabId(int tabId) {
		int checkedId = R.id.pic_id;
		if (tabId == MediacenterConstant.IntentFlags.VIDEO_ID) {
			checkedId = R.id.video_id;
		} else if (tabId == MediacenterConstant.IntentFlags.MUSIC_ID) {
			checkedId = R.id.music_id;
		}
		return checkedId;
	}

	private void switchTab(final int checkedId) {
		ToastFactory.getInstance().cancelToast();
		for (int i = 0; i < tabGroup.getChildCount(); i++) {
			RadioButton radio = (RadioButton) tabGroup.getChildAt(i);
			if (lastView != null && radio.getId() != lastView.getId()) {
				radio.setSelected(false);
				radio.setTextColor(0xff0092e0);
				radio.setTextSize(32);
			}

		}
		switch (checkedId) {

		// case R.id.favorite_id:
		// currentPath.setText(getString(R.string.tab_favorite));
		// tabId = MediacenterConstant.IntentFlags.FAV_ID;
		// addFragmentToStack(tabId, true, null);
		// break;

		case R.id.pic_id:
			tabId = MediacenterConstant.IntentFlags.PIC_ID;
			addFragmentToStack(tabId, false, deviceInfos, R.id.pic_id);
			break;
		case R.id.video_id:
			tabId = MediacenterConstant.IntentFlags.VIDEO_ID;
			addFragmentToStack(tabId, false, deviceInfos, R.id.video_id);
			break;
		case R.id.music_id:
			tabId = MediacenterConstant.IntentFlags.MUSIC_ID;
			addFragmentToStack(tabId, false, deviceInfos, R.id.music_id);
			break;

		}

	}
*/
	public interface DataChanged {
		void onDataChanged(int _tabId,
				ArrayList<DeviceInfo> _devs);
	}

	private void addFragmentToStack(boolean isFav,
			ArrayList<DeviceInfo> devs) {
		if (isFav) {
		    Log.i(TAG, "loading FavFragment...");
		    
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment newFragment = FavFragment.newInstance();
			ft.replace(R.id.tabcontent, newFragment);
			ft.commitAllowingStateLoss();
			createNew = true;
		} else {
			/*if (createNew) {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				Fragment newFragment = DeviceFragment.newInstance(tabFavorate,
						devs);
				ft.replace(R.id.tabcontent, newFragment);
				ft.commitAllowingStateLoss();
				createNew = false;
			} else {
				DataChanged dataChanged = (DataChanged) getFragmentManager()
						.findFragmentById(R.id.tabcontent);
				if (dataChanged != null) {
					dataChanged.onDataChanged(tabFavorate, devs);
				}
			}*/
		}
	}

/*	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		RadioButton radio = (RadioButton) v;
		if (hasFocus) {
			lastView = radio;
			radio.setTextColor(Color.WHITE);
			radio.setTextSize(34);
			radio.setSelected(true);
			switchTab(v.getId());
		}

	}

	@Override
	public void onClick(View v) {
		RadioButton radio = (RadioButton) v;
		lastView = radio;
		radio.setTextColor(Color.WHITE);
		radio.setTextSize(34);
		radio.setSelected(true);
		switchTab(v.getId());
	}
*/
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DeviceUpdateBrocastFactory.ADD_DEVICES.equalsIgnoreCase(action)
					|| DeviceUpdateBrocastFactory.REMOVE_DEVICES
							.equalsIgnoreCase(action)
					|| DeviceUpdateBrocastFactory.CLEAR_DEVICES
							.equalsIgnoreCase(action)) {

				Message message = mHandler.obtainMessage(MESSAGE_FRESH_DEVICE,
						this);
				message.obj = null;
				mHandler.removeMessages(MESSAGE_FRESH_DEVICE);
				mHandler.sendMessage(message);

			}

		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(mReceiver);
		AllShareProxy.getInstance(cxt).unInitSearchEngine();
		HandlerManager.getInstance().unRegisterHandler(
				HandlerManager.MainHandler);
		ToastFactory.getInstance().cancelToast();
	}

	static final int MESSAGE_FRESH_DEVICE = 10001;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_FRESH_DEVICE:
				int addOrRemove = msg.arg2;
				String path = null;
				if (msg.obj != null) {
					path = (String) msg.obj;
				}
				onDataChanged(addOrRemove, path);
				break;
			}

		}
	};

	private ArrayList<DeviceInfo> onDataChanged(int remove, String path) {
		ArrayList<DeviceInfo> dlanList = cover2DLANDevice(AllShareProxy
				.getInstance(cxt).getDeviceList());
		ArrayList<DeviceInfo> usbList = UsbScanManager.getInstance()
				.getDevices(cxt);
		ArrayList<DeviceInfo> temp = new ArrayList<DeviceInfo>();
		SDCardInfo sdInfoLocal = Util.getSDCardInfo();
		if (sdInfoLocal != null) {
			String localDeviceName = Util.getLocalDeviceName(cxt);
			if (localDeviceName != null) {
				localDeviceName = getString(R.string.local_sdcard)
						+ localDeviceName;
			}
			temp.add(new DeviceInfo(sdInfoLocal.path, localDeviceName,
					sdInfoLocal.path, sdInfoLocal.total, sdInfoLocal.used,
					DeviceInfo.TYPE_LOCAL, true, R.drawable.cm_sd_tag));

		} else {
			new DeviceInfo(null, getString(R.string.local_sdcard), null, null,
					null, DeviceInfo.TYPE_LOCAL, false,
					R.drawable.cm_sd_remove_tag);

		}
		temp.addAll(usbList);
		if (!isHasTypeDevice(DeviceInfo.TYPE_USB, temp)) {
			temp.add(new DeviceInfo(null, getString(R.string.usb_device), null,
					null, null, DeviceInfo.TYPE_USB, false,
					R.drawable.cm_usb_remove_tag));
		}
		temp.addAll(dlanList);
		if (!isHasTypeDevice(DeviceInfo.TYPE_DLAN, temp)) {
			temp.add(new DeviceInfo(null, "DLNA", null, null, null,
					DeviceInfo.TYPE_DLAN, false, R.drawable.cm_dlan_remove_tag));
		}
		deviceInfos = temp;
		DeviceInfo reomoveDevice = null;
/*		if (tabId != MediacenterConstant.IntentFlags.FAV_ID) {
		}
*/			DeviceFragment f = (DeviceFragment) getFragmentManager()
					.findFragmentById(R.id.tabcontent);
			if (f != null) {
				f.dataChange(deviceInfos);
			}
		if (remove == MediacenterConstant.MESSAGE_ADD) {
			DeviceInfo di = Util.isNewDevice(usbList, oldDeviceInfos);
			if (di != null) {
				Intent intent = new Intent(cxt, FindDeviceActivity.class);
				intent.putExtra("device", di);
				intent.putExtra("method", remove);
				startActivity(intent);
			}
		} else if (remove == MediacenterConstant.MESSAGE_REMOVE) {
			reomoveDevice = Util.isRemoveDevice(path, deviceInfos,
					oldDeviceInfos);
			if (reomoveDevice != null) {
				Activity showActivity = ActivitiesManager.getInstance()
						.getActivity(ActivitiesManager.ACTIVITY_FILE_VIEW);
				if (showActivity != null) {
					FileMainActivity fm = (FileMainActivity) showActivity;
					DeviceInfo di = fm.getCurrentDeviceInfo();
					if (di != null && reomoveDevice != null
							&& di.devId.equals(reomoveDevice.devId)) {
						Intent intent = new Intent(cxt,
								FindDeviceActivity.class);
						intent.putExtra("removedevice", reomoveDevice);
						intent.putExtra("method", remove);
						startActivity(intent);
					} else {
						Activity findActivity = ActivitiesManager.getInstance()
								.getActivity(
										ActivitiesManager.ACTIVITY_POP_VIEW);
						if (findActivity != null) {
							findActivity.finish();
						}
					}

				} else {
					Activity findActivity = ActivitiesManager.getInstance()
							.getActivity(ActivitiesManager.ACTIVITY_POP_VIEW);
					if (findActivity != null) {
						FindDeviceActivity fa = (FindDeviceActivity) findActivity;
						if (fa.geCurrentInfo().devId
								.equals(reomoveDevice.devId)) {
							findActivity.finish();
						}
					}
				}

			}
		}
		oldDeviceInfos.clear();
		oldDeviceInfos.addAll(deviceInfos);
		return temp;
	}

	private ArrayList<DeviceInfo> cover2DLANDevice(List<Device> devices) {
		ArrayList<DeviceInfo> temp = new ArrayList<DeviceInfo>();
		if (devices != null && !devices.isEmpty()) {
			for (Device d : devices) {
				DeviceInfo di = new DeviceInfo();
				di.devId = d.getUDN();
				di.devName = d.getFriendlyName();
				di.isLive = true;
				di.type = DeviceInfo.TYPE_DLAN;
				di.resId = R.drawable.cm_dlan_tag;
				temp.add(di);
			}
		}
		return temp;
	}

	private boolean isHasTypeDevice(int type, ArrayList<DeviceInfo> deviceInfos) {
		boolean isHas = false;
		for (DeviceInfo di : deviceInfos) {
			if (di.type == type) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	void addBroadCast() {
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.ADD_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.REMOVE_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.CLEAR_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.SEARCH_DEVICES_FAIL));

	}

/*	
	int i = 0;
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
			Fragment fg = getFragmentManager()
					.findFragmentById(R.id.tabcontent);
			fg.getView().requestFocus();
			i = 0;
			return true;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (lastView != null) {
				lastView.requestFocus();
			} else {
				tabGroup.requestFocus();
			}
			if (i > 1) {
				this.finish();
				return true;
			} else {
				ToastFactory.getInstance()
						.getToast(cxt, cxt.getString(R.string.cm_back_toast))
						.show();
				i++;
				return false;
			}

		} else {
			i = 0;
			return super.dispatchKeyEvent(event);
		}
	}
*/

}
