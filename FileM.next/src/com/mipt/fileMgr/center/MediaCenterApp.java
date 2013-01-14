package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.text.TextUtils;

import com.mipt.fileMgr.center.server.DeviceInfo;
import com.mipt.fileMgr.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class MediaCenterApp extends Application {
	private static MediaCenterApp instance;
	private ArrayList<DeviceInfo> oldDeviceInfos;
	private HashMap<String, Boolean> currentScanDevice;

	public static MediaCenterApp getInstance() {
		return instance;
	}

	private ArrayList<FileInfo> fileInfos;
	private ArrayList<FileInfo> albumInfos;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance = this;
		fileInfos = new ArrayList<FileInfo>();
		albumInfos = new ArrayList<FileInfo>();
		oldDeviceInfos = new ArrayList<DeviceInfo>();
		currentScanDevice = new HashMap<String, Boolean>();
	}

	public HashMap<String, Boolean> getCurrentScanDevice() {
		return currentScanDevice;
	}

	public ArrayList<DeviceInfo> getOldDeviceInfos() {
		return oldDeviceInfos;
	}

	public ArrayList<FileInfo> getAlbumData() {
		return albumInfos;
	}

	public void resetAlbumData() {
		albumInfos.clear();
	}

	public ArrayList<FileInfo> getData() {
		return fileInfos;
	}

	public void resetData() {
		fileInfos.clear();
	}

	public void resetAddData(ArrayList<FileInfo> _data) {
		fileInfos.clear();
		fileInfos.addAll(_data);
	}

	public FileInfo getCurrentData(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		for (FileInfo fi : fileInfos) {
			if (filePath.equals(fi.filePath)) {
				return fi;
			}
		}
		return null;
	}

	public FileInfo getDataByPos(int pos) {
		int size = getDataSize();
		if (pos < 0 || pos > size) {
			return null;
		}
		if (!fileInfos.isEmpty()) {
			return fileInfos.get(pos);
		}
		return null;
	}

	public int getFilePos(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return 0;
		}
		if (fileInfos != null) {
			for (int i = 0; i < fileInfos.size(); i++) {
				if (filePath.equals(fileInfos.get(i).filePath)) {
					return i;

				}
			}
		}
		return 0;
	}

	public boolean fileExits(String filePath) {
		if (fileInfos != null) {
			for (FileInfo fi : fileInfos) {
				if (filePath.equals(fi.filePath)) {
					return true;
				}
			}
		}
		return false;
	}

	public FileInfo getNextFile(String filePath) {
		if (fileInfos != null) {
			int size = getDataSize();
			int pos = getFilePos(filePath) + 1;
			if (pos < 0) {
				pos = 0;
			} else {
				pos = (pos >= size) ? pos : size;
			}
			return fileInfos.get(pos);
		}
		return null;
	}

	public FileInfo getPreFile(String filePath) {
		if (fileInfos != null) {
			int size = getDataSize();
			int pos = getFilePos(filePath) - 1;
			if (pos < 0) {
				pos = 0;
			} else {
				pos = (pos >= size) ? pos : size;
			}
			return fileInfos.get(pos);
		}
		return null;
	}

	public int getDataSize() {
		return fileInfos.size();
	}

	public void clearData() {
		if (fileInfos != null && !fileInfos.isEmpty()) {
			fileInfos.clear();
		}
	}
}
