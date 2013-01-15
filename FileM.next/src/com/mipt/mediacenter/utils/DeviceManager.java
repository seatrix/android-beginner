package com.mipt.mediacenter.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

/**
 * 
 * @author fang
 * 
 */
public class DeviceManager {

	private static String TAG = "DeviceManager";
	private ArrayList<String> totalDevicesList;
	private Context mContext;
	private StorageManager manager;
	String[] volumeList;

	public DeviceManager(Context _mContext) {
		this.mContext = _mContext;
		totalDevicesList = new ArrayList<String>();
		if (manager == null) {
		manager = (StorageManager) mContext
				.getSystemService(Context.STORAGE_SERVICE);
		}
		volumeList = manager.getVolumePaths();
		for (int i = 0; i < volumeList.length; i++) {
			totalDevicesList.add(volumeList[i]);
		}

	}

	public ArrayList<String> getMountedDevicesList() {
		String state;
		ArrayList<String> mountedDevices = new ArrayList<String>();
		try {
			for (int i = 0; i < totalDevicesList.size(); i++) {
				state = manager.getVolumeState(totalDevicesList.get(i));
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					mountedDevices.add(totalDevicesList.get(i));
				}
			}
		} catch (Exception rex) {
		}
		return mountedDevices;
	}

	public boolean hasMultiplePartition(String dPath) {
		try {
			File file = new File(dPath);
			String minor = null;
			String major = null;
			for (int i = 0; i < totalDevicesList.size(); i++) {
				if (dPath.equals(totalDevicesList.get(i))) {
					String[] list = file.list();
					for (int j = 0; j < list.length; j++) {
						int lst = list[j].lastIndexOf("_");
						if (lst != -1 && lst != (list[j].length() - 1)) {
							major = list[j].substring(0, lst);
							minor = list[j]
									.substring(lst + 1, list[j].length());
							try {
								Integer.valueOf(major);
								Integer.valueOf(minor);
							} catch (NumberFormatException e) {
								return false;
							}
						} else {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG, "hasMultiplePartition() exception e");
			return false;
		}
	}
}
