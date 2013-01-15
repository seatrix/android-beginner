/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.mediacenter.dlna.network;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mipt.mediacenter.dlna.AllShareProxy;
import com.mipt.mediacenter.dlna.DeviceUpdateBrocastFactory;
import com.mipt.mediacenter.dlna.util.UpnpUtil;


public class DlnaService extends Service implements DeviceChangeListener, ControlCenterRunnable.ISearchDeviceListener{

	private static final CommonLog log = LogFactory.createLog();
	
	public static final String SEARCH_DEVICES = "com.mipt.mediacenter.dlna.service.search_device";
	public static final String RESET_SEARCH_DEVICES = "com.mipt.mediacenter.service.dlna.reset_search_device";
	
	
	private  ControlPoint mControlPoint;
	private  Thread mSearchDeviceThread = null;
	private  ControlCenterRunnable mControlCenterRunnable = null;
	private  AllShareProxy mAllShareProxy;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		log.e("DlnaService onCreate");
		init();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	
		
		if (intent != null && intent.getAction() != null){
			String action = intent.getAction();
			if (DlnaService.SEARCH_DEVICES.equals(action)) {
				search();
			}else if (DlnaService.RESET_SEARCH_DEVICES.equals(action)){
				reset();
			}		
		}else{
			log.e("intent = " + intent);
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		log.e("DlnaService onDestroy");
		unInit();
		super.onDestroy();
	}
	

	private void init(){
		mControlPoint = new ControlPoint();
		mControlPoint.addDeviceChangeListener(this);
		mControlPoint.addSearchResponseListener(new SearchResponseListener() {		
			@Override
			public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
			}
		});
	
		mControlCenterRunnable = new ControlCenterRunnable(this, mControlPoint);
		mControlCenterRunnable.setSearchListener(this);
		mAllShareProxy = AllShareProxy.getInstance(this);
		
	}
	
	private void unInit(){
		mControlCenterRunnable.setSearchListener(null);
		mControlCenterRunnable.exit();
		mControlPoint.stop();
	}

	private void search(){
		
		if (mSearchDeviceThread == null){
			mSearchDeviceThread = new Thread(mControlCenterRunnable);
			mSearchDeviceThread.start();
		}else{
			mControlCenterRunnable.notifyThread();
		}
	}
	
	private void reset(){
		
		if (mSearchDeviceThread == null){
			mSearchDeviceThread = new Thread(mControlCenterRunnable);
			mSearchDeviceThread.start();
		}else{
			mControlCenterRunnable.reset();
		}
	}
	
	
	@Override
	public void deviceAdded(Device dev) {
		if (UpnpUtil.isValidDevice(dev)){
			mAllShareProxy.addDevice(dev);
		}	
	}

	@Override
	public void deviceRemoved(Device dev) {
		if (UpnpUtil.isMediaServerDevice(dev)){
			mAllShareProxy.removeDevice(dev);
		}		
	}
	
	@Override
	public void onSearchComplete(boolean searchSuccess) {

		if (!searchSuccess){
			DeviceUpdateBrocastFactory.sendSearchDeviceFailBrocast(this);
		}
	}

}
