/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.mediacenter.dlna;

import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.content.Context;
import android.content.Intent;


/**
 *dlna listener factory. 
 */
public class DeviceUpdateBrocastFactory {
	private static final CommonLog log = LogFactory.createLog();
	
	private DeviceUpdateBrocastReceiver mReceiver;	
	private Context mContext;
	
	public DeviceUpdateBrocastFactory(Context context){
		mContext = context;
	}
	
	public void registerListener(IDeviceUpdateListener listener){
		if (mReceiver == null){
			mReceiver = new DeviceUpdateBrocastReceiver();
//			mContext.registerReceiver(mReceiver, new IntentFilter(ADD_DEVICES));
//			mContext.registerReceiver(mReceiver, new IntentFilter(REMOVE_DEVICES));
//			mContext.registerReceiver(mReceiver, new IntentFilter(CLEAR_DEVICES));
			mReceiver.setListener(listener);
		}
	}
	
	public void unRegisterListener(){
		if (mReceiver != null){
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}
	
	
	
	
	
	public static final String ADD_DEVICES = "com.mipt.mediacenter.dlna.brocast.add_device";
	public static final String REMOVE_DEVICES = "com.mipt.mediacenter.dlna.brocast.remove_device";
	public static final String REMOVE_EXTRA_FLAG = "com.mipt.mediacenter.dlna.brocast.remove_extra_flag";
	public static final String CLEAR_DEVICES = "com.mipt.mediacenter.dlna.brocast.clear_device";
	public static final String SEARCH_DEVICES_FAIL = "com.mipt.mediacenter.dlna.brocast.search_devices_fail";
	
	
	public static  void sendAddBrocast(Context context){
		log.e("sendAddBrocast");
		Intent intent = new Intent(ADD_DEVICES);
		context.sendBroadcast(intent);
	}
	
	public static void sendRemoveBrocast(Context context, boolean isSelected){
		log.e("sendRemoveBrocast isSelected = " + isSelected);
		Intent intent = new Intent(REMOVE_DEVICES);
		intent.putExtra(REMOVE_EXTRA_FLAG, isSelected);
		context.sendBroadcast(intent);
	}
	
	public static void sendClearBrocast(Context context){
		log.e("sendClearBrocast");
		Intent intent = new Intent(CLEAR_DEVICES);
		context.sendBroadcast(intent);
	}
	
	public static void sendSearchDeviceFailBrocast(Context context){
		log.e("sendSearchDeviceFailBrocast");
		Intent intent = new Intent(SEARCH_DEVICES_FAIL);
		context.sendBroadcast(intent);
	}
}
