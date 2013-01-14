/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.fileMgr.dlna;

import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceUpdateBrocastReceiver extends BroadcastReceiver{

	private static final CommonLog log = LogFactory.createLog();
	
	private IDeviceUpdateListener mListener;
	
	public void setListener(IDeviceUpdateListener listener){
		mListener  = listener;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (DeviceUpdateBrocastFactory.ADD_DEVICES.equalsIgnoreCase(action) || 
				DeviceUpdateBrocastFactory.REMOVE_DEVICES.equalsIgnoreCase(action)){			
			boolean isSelDeviceChange = intent.getBooleanExtra(DeviceUpdateBrocastFactory.REMOVE_EXTRA_FLAG, false);
			if (mListener != null){
				mListener.onDeviceUpdate(isSelDeviceChange);
			}
		}else if (DeviceUpdateBrocastFactory.CLEAR_DEVICES.equalsIgnoreCase(action)){
			if (mListener != null){
				mListener.onDeviceClear();
			}
		}

	}
}
