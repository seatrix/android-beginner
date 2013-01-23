package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.DeviceFragment;
import com.mipt.mediacenter.center.FavFragment;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.ActivitiesManager;
import com.mipt.mediacenter.utils.HandlerManager;
import com.mipt.mediacenter.utils.ToastFactory;
import com.mipt.mediacenter.utils.UsbScanManager;
import com.mipt.mediacenter.utils.Util;
import com.mipt.mediacenter.utils.Util.SDCardInfo;

/**
 * 
 * @author fang
 * 
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	private Context cxt;
	private List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
	private List<DeviceInfo> oldDeviceInfos = new ArrayList<DeviceInfo>();
	private boolean createNew;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cm_activity_main);
		cxt = MainActivity.this;
		createNew = true;
		initData();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void initData() {
	    Log.i(TAG, "initData...");
	    addFragmentToStack(false, deviceInfos);
		//AllShareProxy.getInstance(cxt).initSearchEngine();
		//onDataChanged(0, null);
		HandlerManager.getInstance().registerHandler(
				HandlerManager.MainHandler, mHandler);
		//oldDeviceInfos.addAll(deviceInfos);
	}

	public interface DataChanged {
		void onDataChanged(int _tabId,
				List<DeviceInfo> _devs);
	}

	private void addFragmentToStack(boolean isFav,
			List<DeviceInfo> devs) {
        Log.i(TAG, "addFragmentToStack...createNew:" + createNew + ",isFav:" + isFav);
		if (isFav) {
		    //Log.i(TAG, "loading FavFragment...isFav is" + isFav);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment newFragment = FavFragment.newInstance();
			ft.replace(R.id.tabcontent, newFragment);
			ft.commitAllowingStateLoss();
			createNew = true;
		} else {
			if (createNew) {
			    Log.i(TAG, "devs:" + devs);
			    
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment newFragment = DeviceFragment.newInstance(-1,devs);
				Log.d(TAG,"newFragment == null" +(newFragment == null));
				ft.replace(R.id.tabcontent, newFragment);
				//ft.add(R.id.tabcontent, newFragment, "deviceFragment");
                ft.commitAllowingStateLoss();
                
				//ft.add(R.id.tabcontent, newFragment);
				//ft.commit();
				createNew = false;
				//Log.i(TAG, "loading DeviceFragment......:" + getFragmentManager().);
			} else {
				DataChanged dataChanged = (DataChanged) getFragmentManager()
						.findFragmentById(R.id.tabcontent);
				if (dataChanged != null) {
					dataChanged.onDataChanged(-1, devs);
				}
				Log.i(TAG, "loading DataChanged......");
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//this.unregisterReceiver(mReceiver);
		//AllShareProxy.getInstance(cxt).unInitSearchEngine();
		HandlerManager.getInstance().unRegisterHandler(
				HandlerManager.MainHandler);
		ToastFactory.getInstance().cancelToast();
	}

	public static final int MESSAGE_FRESH_DEVICE = 10001;
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

	public ArrayList<DeviceInfo> onDataChanged(int opType, String path) {
	    deviceInfos.clear();
		SDCardInfo sdInfoLocal = Util.getSDCardInfo();
		if (sdInfoLocal != null) {
			String localDeviceName = Util.getLocalDeviceName(cxt);
			if (localDeviceName != null) {
				localDeviceName = getString(R.string.local_sdcard)
						+ localDeviceName;
			}

			Log.i(TAG, "sdcard is not null," + sdInfoLocal);
			deviceInfos.add(new DeviceInfo(sdInfoLocal.path, localDeviceName,
					sdInfoLocal.path, sdInfoLocal.total, sdInfoLocal.used,
					DeviceInfo.TYPE_LOCAL, true, R.drawable.cm_sd_tag));

		} else {
			new DeviceInfo(null, getString(R.string.local_sdcard), null, null,
					null, DeviceInfo.TYPE_LOCAL, false,
					R.drawable.cm_sd_remove_tag);

		}
        ArrayList<DeviceInfo> usbList = UsbScanManager.getInstance().getDevices(cxt);		
        Log.i(TAG, "usb list.size:" + usbList.size());
        deviceInfos.addAll(usbList);
		if (!isHasTypeDevice(DeviceInfo.TYPE_USB, deviceInfos)) {
		    //not have usb devices.
		    deviceInfos.add(new DeviceInfo(null, getString(R.string.usb_device), null,
					null, null, DeviceInfo.TYPE_USB, false,
					R.drawable.cm_usb_remove_tag));
		}
		
		DeviceFragment f = (DeviceFragment) getFragmentManager()
		        .findFragmentById(R.id.tabcontent);
		DeviceInfo reomoveDevice = null;		
		if (f != null) {
		    Log.i(TAG, "next deviceInfos.size:" + deviceInfos.size());
		    f.dataChange(deviceInfos);
		}else{
		    Log.i(TAG, "DeviceFragment:" + (f != null ? f.toString() : null));
		}
		
		//DeviceFragment.instantiate(context, fname)
		
		if (opType == MediacenterConstant.MESSAGE_ADD) {
		    DeviceInfo di = Util.isNewDevice(usbList, oldDeviceInfos, path);
			if (di != null) {
				Intent intent = new Intent(cxt, FindDeviceActivity.class);
				intent.putExtra("device", di);
				intent.putExtra("method", opType);
				startActivity(intent);
			}
		} else if (opType == MediacenterConstant.MESSAGE_REMOVE) {
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
						intent.putExtra("method", opType);
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
		return (ArrayList<DeviceInfo>)deviceInfos;
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

	private boolean isHasTypeDevice(int type, List<DeviceInfo> deviceInfos) {
		boolean isHas = false;
		for (DeviceInfo di : deviceInfos) {
			if (di.type == type) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

/*	private void addBroadCast() {
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.ADD_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.REMOVE_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.CLEAR_DEVICES));
		registerReceiver(mReceiver, new IntentFilter(
				DeviceUpdateBrocastFactory.SEARCH_DEVICES_FAIL));

	}
*/
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
