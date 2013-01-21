package com.mipt.mediacenter.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.MediaCenterApplication;
import com.mipt.mediacenter.center.file.GroupInfo;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.Util.SDCardInfo;

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
		oldDeviceInfos = new ArrayList<DeviceInfo>();
		currentScanDevice = new HashMap<String, Boolean>();
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
	public void clearOldList() {
		oldDeviceInfos.clear();
	}

	public ArrayList<DeviceInfo> getOldList() {
		return oldDeviceInfos;
	}

	public ArrayList<DeviceInfo> getDevices(Context cxt) {
		ArrayList<DeviceInfo> temp = new ArrayList<DeviceInfo>();
		if ("A6".equals(android.os.Build.MODEL)) {
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
		} else {
			DeviceManager dm = new DeviceManager(cxt);
			ArrayList<String> strs = dm.getMountedDevicesList();
			for (String s : strs) {
				if (dm.hasMultiplePartition(s)) {
					File f = new File(s);
					if (f.exists()) {
						File[] list = f.listFiles();
						for (int i = 0; i < list.length; i++) {
							String path = list[i].getAbsolutePath();
							addA4usbDevice(temp, cxt, path);
						}
					}
				} else {
					addA4usbDevice(temp, cxt, s);
				}
			}
		}
		return temp;
	}
	private void addA4usbDevice(ArrayList<DeviceInfo> temp, final Context cxt,
			String path) {
		if (!TextUtils.isEmpty(path) && !isHasDevice(temp, path)) {
			SDCardInfo sdInfo = Util.getSDCardInfo(new File(path));
								if (sdInfo != null
										&& path.indexOf(MediacenterConstant.LOCAL_SDCARD_PATH) != 0) {
				temp.add(new DeviceInfo(path,
						cxt.getString(R.string.usb_device) + "-"
								+ getA4Name(path), sdInfo.path, sdInfo.total,
						sdInfo.used, DeviceInfo.TYPE_USB, true,
						R.drawable.cm_usb_tag));
								}
							}
						}

	private boolean isHasDevice(ArrayList<DeviceInfo> temp, String path) {
		if (temp == null || temp.isEmpty()) {
			return false;
				}
		for (DeviceInfo di : temp) {
			if (!TextUtils.isEmpty(di.devPath) && di.devPath.equals(path)) {
				return true;
			}
		}
		return false;
	}
	private String getA4Name(String path) {
		String pathRreturn = path;
		if (!TextUtils.isEmpty(pathRreturn)) {
			pathRreturn = pathRreturn
					.substring(pathRreturn.lastIndexOf("/") + 1);
		}
		return pathRreturn;
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
