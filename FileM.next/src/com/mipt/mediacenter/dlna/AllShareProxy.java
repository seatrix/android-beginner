/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.mediacenter.dlna;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.UPnPStatus;
import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.content.Context;
import android.content.Intent;

import com.mipt.mediacenter.dlna.network.DlnaService;
import com.mipt.mediacenter.dlna.network.Item;
import com.mipt.mediacenter.dlna.util.ParseUtil;


public class AllShareProxy {
	
	public static interface ControlRequestCallback
	{
		public void onGetItems(final List<Item> list);
	}

	private static final CommonLog log = LogFactory.createLog();

	private static  AllShareProxy instance;
	private Context mContext;
	private ControlRequestProxy mControlRequestProxy;


	private List<Device> mDeviceList = new ArrayList<Device>();
	private Device mSelectedDevice;

	public static synchronized AllShareProxy getInstance(Context context) {
		if (instance == null){
			instance  = new AllShareProxy(context);
		}
		return instance;
	}

	private AllShareProxy(Context context) {
		mContext = context;
		mControlRequestProxy = new ControlRequestProxy();
	}
	

	public void initSearchEngine(){
	
		mContext.startService(new Intent(DlnaService.SEARCH_DEVICES));
	}
	
	public void restartSearchEngine(){

		mContext.startService(new Intent(DlnaService.RESET_SEARCH_DEVICES));
		clear();
	}
	
	public void unInitSearchEngine(){

		mContext.stopService(new Intent(mContext, DlnaService.class));
		clear();
	}
	

	
	public synchronized List<Device> getDeviceList() {
		return mDeviceList;
	}
	
	public void setSelectedDevice(Device selectedDevice) {
		log.e("setSelectedDevice = " + mSelectedDevice);
		mSelectedDevice = selectedDevice;
	}
	
	
	public ControlRequestProxy getControlRequestProxy(){
		return mControlRequestProxy;
	}
	
	
	
	

	public Device getSelectedDevice() {
		return mSelectedDevice;
	}
	
	public synchronized void  addDevice(Device d) {
		log.i("DeviceDataCenter addDevice = " + d.toString() + "\n"  + d.getDeviceNode().toString());
		mDeviceList.add(d);
		log.i("addDevice devices.size = " + mDeviceList.size());
		DeviceUpdateBrocastFactory.sendAddBrocast(mContext);
	}


	public synchronized void removeDevice(Device d)
	{	
		log.i("DeviceDataCenter removeDevice = " + d.toString() + "\n"  + d.getDeviceNode().toString());
		int size = mDeviceList.size();
		for(int i = 0; i < size; i++)
		{
			String udnString = mDeviceList.get(i).getUDN();
			if (d.getUDN().equalsIgnoreCase(udnString)){
				Device device = mDeviceList.remove(i);
				boolean ret = false;
				if (mSelectedDevice != null){
					ret = mSelectedDevice.getUDN().equalsIgnoreCase(device.getUDN());
				}
				if (ret){
					setSelectedDevice(null);
				}
				DeviceUpdateBrocastFactory.sendRemoveBrocast(mContext, ret);
				break;
			}
		}
		log.i("removeDevice devices.size = " + mDeviceList.size());
	}


	
	private synchronized void  clear()
	{
		mDeviceList = new ArrayList<Device>();
		mSelectedDevice = null;
		DeviceUpdateBrocastFactory.sendClearBrocast(mContext);
	}
	
	

	public class ControlRequestProxy{
		public void syncGetRoot(final ControlRequestCallback callback) {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					List<Item> list = getRoot(mContext);
					if (callback != null){
						callback.onGetItems(list);
					}
				}
			});
			
			thread.start();
		}
		public void syncGetItems(final String id,final ControlRequestCallback callback) {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					List<Item> list = getItems(mContext, id);
					if (callback != null){
						callback.onGetItems(list);
					}
				}
			});
			
			thread.start();
			

		}
		
	
	
		private List<Item> getRoot(Context context) {
			
			Device selDevice = AllShareProxy.getInstance(context).getSelectedDevice();
			if (selDevice == null) {
				log.e("no selDevice!!!");
				return null;
			}

			org.cybergarage.upnp.Service service = selDevice
			.getService("urn:schemas-upnp-org:service:ContentDirectory:1");
			if (service == null)
			{
				log.e("no service for ContentDirectory!!!");
				return null;
			}

			Action action = service.getAction("Browse");
			if(action == null)
			{
				log.e("action for Browse is null!!!");
				return null;
			}
			ArgumentList argumentList = action.getArgumentList();
			argumentList.getArgument("ObjectID").setValue(0);
			argumentList.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
			argumentList.getArgument("StartingIndex").setValue("0");
			argumentList.getArgument("RequestedCount").setValue("0");
			argumentList.getArgument("Filter").setValue("*");
			argumentList.getArgument("SortCriteria").setValue("");
			
			ArgumentList actionInputArgList = action.getInputArgumentList();	
			int size = actionInputArgList.size();
			for(int i = 0; i < size; i++){
				Argument argument =  (Argument) (actionInputArgList.get(i));
				argument.getArgumentNode().print();
			}
	
			if (action.postControlAction()) {
				ArgumentList outArgList = action.getOutputArgumentList();
				Argument result = outArgList.getArgument("Result");
			
				log.e("result value = \n" + result.getValue());	
				
				
				List<Item> items = null;
				try {
					items = ParseUtil.parseResult(result);
				} catch (Exception e) {
					log.e("catch exception!!! e = " + e.getMessage());
				}
				return items;
			} else {
				UPnPStatus err = action.getControlStatus();
				log.e("Error Code = " + err.getCode());
				log.e("Error Desc = " + err.getDescription());
			}
			return null;
		}
	
		private List<Item> getItems(Context context, String id) {
			Device selDevice = AllShareProxy.getInstance(context).getSelectedDevice();
			if (selDevice == null) {
				log.e("no service for ContentDirectory!!!");
				return null;
			}
			log.e("getItems id = " + id);
			org.cybergarage.upnp.Service service = selDevice
			.getService("urn:schemas-upnp-org:service:ContentDirectory:1");
			if (service == null)
			{
				log.e("no service for ContentDirectory!!!");
				return null;
			}
			
			Action action = service.getAction("Browse");
			if(action == null)
			{
				log.e("action for Browse is null");
				return null;
			}
			
			ArgumentList argumentList = action.getArgumentList();
			argumentList.getArgument("ObjectID").setValue(id);
			argumentList.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
			argumentList.getArgument("StartingIndex").setValue("0");
			argumentList.getArgument("RequestedCount").setValue("0");
			argumentList.getArgument("Filter").setValue("*");
			argumentList.getArgument("SortCriteria").setValue("");


			if (action.postControlAction()) {
				ArgumentList outArgList = action.getOutputArgumentList();
				Argument result = outArgList.getArgument("Result");
				log.e("result value = \n" + result.getValue());	
				
				List<Item> items = null;
				try {
					items = ParseUtil.parseResult(result);
				} catch (Exception e) {
					log.e("catch exception!!! e = " + e.getMessage());
				}
				return items;
			} else {
				UPnPStatus err = action.getControlStatus();
				System.out.println("Error Code = " + err.getCode());
				System.out.println("Error Desc = " + err.getDescription());
			}
			return null;
		}
	}

}
