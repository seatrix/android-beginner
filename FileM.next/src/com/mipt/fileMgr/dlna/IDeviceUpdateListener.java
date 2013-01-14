/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.fileMgr.dlna;

public interface IDeviceUpdateListener {

	public void onDeviceUpdate(boolean isSelDeviceRemove);
	public void onDeviceClear();
}
