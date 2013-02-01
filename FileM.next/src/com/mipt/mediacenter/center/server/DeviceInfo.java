package com.mipt.mediacenter.center.server;

import java.io.Serializable;

import android.util.Log;

/**
 * 
 * @author fang
 * 
 */
public class DeviceInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int TYPE_LOCAL = 1;
	//public static final int TYPE_DLAN = 3;
	public static final int TYPE_USB = 2;
	public static final int TYPE_SMB = 3;
	public String devName;
	public String devPath;
	public String usedPercent;
	public long devSize;
	public long devUsedSize;
	public String desc;
	public int type;
	public boolean isLive;
	public int resId; // for pic
	public String devId;
	//public String dlanDes;

	public DeviceInfo() {
	}

	public DeviceInfo(String devId, String devName, String devPath,
			final Long size, final Long devUsedSize, int type, boolean isLive,
			int resId) {
		if (devId == null) {
			this.devId = type + devName;
		} else {
			this.devId = devId;
		}
		this.devName = devName;
		this.devPath = devPath;
		Log.i("DeviceInfo", "000000devPath0000:" + this.devPath);
		if (size != null && devUsedSize != null) {
			this.devSize = size;
			this.devUsedSize = devUsedSize;
		}
		this.type = type;
		this.isLive = isLive;
		this.resId = resId;
	}
}
