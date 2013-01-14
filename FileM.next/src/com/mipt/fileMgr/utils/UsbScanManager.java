package com.mipt.fileMgr.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.MediaCenterApp;
import com.mipt.fileMgr.center.file.GroupInfo;
import com.mipt.fileMgr.center.server.DeviceInfo;
import com.mipt.fileMgr.center.server.MediacenterConstant;
import com.mipt.fileMgr.utils.Util.SDCardInfo;

/**
 * 
 * @author fang
 * 
 */
public class UsbScanManager {
	private ArrayList<DeviceInfo> oldDeviceInfos;
	private static UsbScanManager instance;
	final static String MOUNT_LABLE = "mountLable";
	private HashMap<String, Boolean> currentScanDevice ;
	// 设备类型
	final static String MOUNT_TYPE = "mountType";

	// 设备路径
	public final static String MOUNT_PATH = "mountPath";

	// 设备卷标
	public final static String MOUNT_NAME = "mountName";

	private UsbScanManager() {
		oldDeviceInfos = MediaCenterApp.getInstance()
				.getOldDeviceInfos();
		currentScanDevice = MediaCenterApp.getInstance()
				.getCurrentScanDevice();
	}

	public Boolean currentDeviceIsScan(String name) {
		Boolean is = currentScanDevice.get(name) == null ? false : true;
		return is;
	}

	public void registerScanDevice(String name) {
		currentScanDevice.put(name, true);
	}

	public void unRegisterScanDevice(String name) {
		currentScanDevice.remove(name);
	}

	public static UsbScanManager getInstance() {
		if (instance == null)
			instance = new UsbScanManager();
		return instance;
	}

	public void addOldList(final ArrayList<DeviceInfo> _oldDeviceInfos) {
		oldDeviceInfos.clear();
		oldDeviceInfos.addAll(_oldDeviceInfos);
	}

	public ArrayList<DeviceInfo> getOldList() {
		return oldDeviceInfos;
	}

	public ArrayList<DeviceInfo> getDevices(Context cxt) {
		ArrayList<DeviceInfo> temp = new ArrayList<DeviceInfo>();
		ArrayList<GroupInfo> groupInfos = getMountEquipmentList(cxt);
		if (!groupInfos.isEmpty()) {
			for (GroupInfo ginfo : groupInfos) {
				List<Map<String, String>> tempChild = ginfo.getChildList();
				for (Map<String, String> map : tempChild) {
					String path = map.get(UsbScanManager.MOUNT_PATH);
					SDCardInfo sdInfo = Util.getSDCardInfo(new File(path));
					if (sdInfo != null
							&& sdInfo.path
									.indexOf(MediacenterConstant.LOCAL_SDCARD_PATH) != 0) {
						temp.add(new DeviceInfo(sdInfo.path, cxt
								.getString(R.string.usb_device)
								+ "-"
								+ map.get(UsbScanManager.MOUNT_NAME),
								sdInfo.path, sdInfo.total, sdInfo.used,
								DeviceInfo.TYPE_USB, true,
								R.drawable.cm_usb_tag));
					}
				}
			}
		}
		return temp;
	}

	public ArrayList<GroupInfo> getMountEquipmentList(Context cxt) {
		String[] mountType = cxt.getResources().getStringArray(
				R.array.mountType);
		MountInfo info = new MountInfo(cxt);
		ArrayList<GroupInfo> groupListTemp = new ArrayList<GroupInfo>();
		List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
		GroupInfo group = null;
		for (int j = 0; j < mountType.length; j++) {
			group = new GroupInfo();
			childList = new ArrayList<Map<String, String>>();
			for (int i = 0; i < info.index; i++) {
				if (info.type[i] == j) {
					if (info.path[i] != null && info.path[i].contains("/mnt")) {
						Map<String, String> map = new HashMap<String, String>();
						// map.put(MOUNT_DEV, info.dev[i]);
						map.put(MOUNT_TYPE, String.valueOf(info.type[i]));
						map.put(MOUNT_PATH, info.path[i]);
						// map.put(MOUNT_LABLE, info.label[i]);
						map.put(MOUNT_LABLE, "");
						map.put(MOUNT_NAME, info.partition[i]);
						childList.add(map);
					}
				}
			}
			if (childList.size() > 0) {
				group.setChildList(childList);
				group.setName(mountType[j]);
				groupListTemp.add(group);
			}
		}
		return groupListTemp;
	}

}
