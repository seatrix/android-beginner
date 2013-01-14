package com.mipt.fileMgr.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

public class MountInfo {
	private static final String TAG = "MountInfo";
	public String[] path = new String[64];
	public int[] type = new int[64];
	public String[] label = new String[64];
	public String[] partition = new String[64];
	public int index = 0;
	private StorageManager mStorageManager = null;

	public MountInfo(Context context) {
		try {
			if (mStorageManager == null) {
				mStorageManager = (StorageManager) context
						.getSystemService(Context.STORAGE_SERVICE);
			}
			StorageVolume[] storageVolumes;
			storageVolumes = mStorageManager.getVolumeList();
			String[] devicePath = getDevicePath(storageVolumes);
			index = devicePath.length;

			for (int i = 0; i < index; i++) {
				path[i] = getUpdateFilePath(storageVolumes, devicePath[i]);
				label[i] = devicePath[i];
				partition[i] = label[i];
				if (path[i].contains("/mnt/nand")) {
					type[i] = 2;
					label[i] = "";
				} else {
					type[i] = 1;
				}
			}

			/*
			 * IBinder service = ServiceManager.getService("mount"); if (service
			 * != null) { IMountService mountService = IMountService.Stub
			 * .asInterface(service); List<android.os.storage.MountInfo>
			 * mountList = mountService.getAllMountInfo(); index =
			 * mountList.size(); int i=0; for(i=0;i<index;i++) {
			 * path[i]=mountList.get(i).mPath;
			 * partition[i]=mountList.get(i).mLinkedLabel;
			 * 
			 * label[i] = mountList.get(i).mVolumeLabel; if (label[i] == null) {
			 * label[i] = ""; }
			 * 
			 * Log.w("LABLE", label[i]); Log.w("partition", partition[i]);
			 * String typeStr = mountList.get(i).mDevType;
			 * 
			 * if (path[i].contains("/mnt/nand")) { type[i] = 2; label[i] = "";
			 * } else if (typeStr.equals("SDCARD")) { type[i] = 2; } else if
			 * (typeStr.equals("USB")) { type[i] = 1; } else if
			 * (typeStr.equals("SATA")) { type[i] = 0; } else if
			 * (typeStr.equals("UNKOWN")) { type[i] = 3; } } }
			 */
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String getMountDevices(String path) {
		int start = 0;
		start = path.lastIndexOf("/");
		String mountPath = path.substring(start + 1);
		return mountPath;
	}

	private String[] getDevicePath(StorageVolume[] storageVolumes) {
		String[] tmpPath = new String[storageVolumes.length];
		for (int i = 0; i < storageVolumes.length; i++) {
			tmpPath[i] = getMountDevices(storageVolumes[i].getPath());
		}
		int count = storageVolumes.length;
		// delete repeat
		for (int i = 0; i < storageVolumes.length; i++) {
			for (int j = i + 1; j < storageVolumes.length; j++) {
				try {
					if (tmpPath[i] != null) {
						if (tmpPath[j].equals(tmpPath[i]) && tmpPath[j] != null) {
							tmpPath[j] = null;
							count--;
						}
					}
				} catch (Exception e) {

				}
			}
		}
		String[] path = new String[count];
		int j = 0;
		for (int i = 0; i < storageVolumes.length; i++) {
			if (tmpPath[i] != null) {
				path[j] = tmpPath[i];
				j++;
			}
		}
		// sort
		for (int i = 0; i < count; i++) {
			for (int k = i + 1; k < count; k++) {
				if (path[i].compareTo(path[k]) > 0) {
					String tmp = path[k];
					path[k] = path[i];
					path[i] = tmp;
				}
			}
		}
		return path;
	}

	private String getUpdateFilePath(StorageVolume[] storageVolumes,
			String fileSuffix) {
		if (storageVolumes != null && storageVolumes.length > 0) {
			for (int i = 0; i < storageVolumes.length; i++) {
				if (storageVolumes[i].getPath().contains(fileSuffix)) {
					return storageVolumes[i].getPath();
				}
			}
		}
		return "/mnt/nand";

	}
}
