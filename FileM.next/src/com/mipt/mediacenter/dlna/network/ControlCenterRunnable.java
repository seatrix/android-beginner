/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.mediacenter.dlna.network;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import com.mipt.mediacenter.dlna.util.CommonUtil;
import android.content.Context;
import android.os.Process;




public class ControlCenterRunnable implements Runnable{

	private static final CommonLog log = LogFactory.createLog();	
	private static final int REFRESH_DEVICES_INTERVAL = 30 * 1000; 
	
	public static interface ISearchDeviceListener{
		public void onSearchComplete(boolean searchSuccess);
	}
	
	
	private ControlPoint mCP = null;
	private Context mContext = null;
	private boolean mStartComplete = false;
	private boolean mIsExit = false;
	private ISearchDeviceListener mSearchDeviceListener;
	
	
	public ControlCenterRunnable(Context context, ControlPoint controlPoint){
		mContext = context;
		mCP = controlPoint; 
	}
	
	public void  setCompleteFlag(boolean flag){
		mStartComplete = flag;
	}
	public void setSearchListener(ISearchDeviceListener listener){
		mSearchDeviceListener = listener;
	}
	
	public void emptyMethod(){
	}
	public void notifyThread(){
		synchronized (this) {
			emptyMethod();
			notifyAll();
		}
	}
	
	public void reset(){
		setCompleteFlag(false);
		notifyThread();
	}
	
	public void exit(){
		mIsExit = true;
		notifyThread();
	}
	
	@Override
	public void run() {
		log.e("ControlCenterRunnable run...");		
		Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);	
		
		while(true)
		{
			refreshDevices();			
				if (mIsExit){
					break;
				}
				
			synchronized(this)
				{
				try{
					wait(REFRESH_DEVICES_INTERVAL);
				}
				catch(Exception e){
					e.printStackTrace();
				}	
			}
				if (mIsExit){
					break;
			}
		}
		
		log.e("ControlCenterRunnable over...");		
	}

	private void refreshDevices(){
		log.e("refreshDevices...");
		if (!CommonUtil.checkNetState(mContext)){
			log.e("checkNetState = false...");
			return ;
		}
		
		try {
			if (mStartComplete){
				multiSearchMessage();
				boolean searchRet = mCP.search();
				log.e("mCP.search() ret = "  + searchRet);
				if (mSearchDeviceListener != null){
					mSearchDeviceListener.onSearchComplete(searchRet);
				}
			}else{
				boolean startRet = mCP.start();
				log.e("mCP.start() ret = "  + startRet);
				if (startRet){
					mStartComplete = true;
					multiSearchMessage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void multiSearchMessage(){
		log.e("multiSearchMessage");
		for(int i = 0; i < 3;i++){
			if (mIsExit){
				return ;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mCP.search();
		}
	}
}
