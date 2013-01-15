package com.mipt.mediacenter.center;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mipt.fileMgr.center.FileMainActivity;
import com.mipt.fileMgr.center.FindDeviceActivity;
import com.mipt.fileMgr.center.MainActivity;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.ActivitiesManager;
import com.mipt.mediacenter.utils.HandlerManager;
import com.mipt.mediacenter.utils.UsbScanManager;
import com.mipt.mediacenter.utils.Util;

/**
 * 
 * @author fang
 */
public class USBReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String path = intent.getData().getPath();
		if (!"A6".equals(android.os.Build.MODEL)) {
			File f = new File(path);
			if (f.exists()) {
				File[] list = f.listFiles();
				for (int i = 0; i < list.length; i++) {
					path = list[i].getAbsolutePath();
				}
			}
		}
		Handler mHandler = HandlerManager.getInstance().getHandler(
				HandlerManager.MainHandler);
		if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			Log.i("USBReceiver", "---------USBReceiver---ACTION_MEDIA_MOUNTED-"
					+ path);
			if (mHandler != null) {
				Message message = mHandler.obtainMessage(
						MainActivity.MESSAGE_FRESH_DEVICE, this);
				message.arg2 = MediacenterConstant.MESSAGE_ADD;
				message.obj = path;
				mHandler.removeMessages(MainActivity.MESSAGE_FRESH_DEVICE);
				mHandler.sendMessage(message);
			} else {
				onDataChanged(context, MediacenterConstant.MESSAGE_ADD, path);
			}

		} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
				|| action.equals(Intent.ACTION_MEDIA_REMOVED)
				|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
			try {
				Thread.currentThread();
				Thread.sleep(500);
			} catch (Exception e) {
			}
			Log.i("USBReceiver",
					"---------USBReceiver---ACTION_MEDIA_UNMOUNTED-" + path);
			if (mHandler != null) {
				Message message = mHandler.obtainMessage(
						MainActivity.MESSAGE_FRESH_DEVICE, this);
				message.arg2 = MediacenterConstant.MESSAGE_REMOVE;
				message.obj = path;
				mHandler.removeMessages(MainActivity.MESSAGE_FRESH_DEVICE);
				mHandler.sendMessage(message);
			} else {
				onDataChanged(context, MediacenterConstant.MESSAGE_REMOVE, path);
			}
		} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {

			// "---------ACTION_MEDIA_SCANNER_STARTED----");

		} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			// "---------ACTION_MEDIA_SCANNER_FINISHED----");
		}

	}

	private void onDataChanged(final Context ctx, int remove, String path) {
		UsbScanManager manager = UsbScanManager.getInstance();
		ArrayList<DeviceInfo> usbList = manager.getDevices(ctx);
		if (remove == MediacenterConstant.MESSAGE_ADD) {
			DeviceInfo di = Util.isNewDevice(usbList, manager.getOldList());
			manager.addOldList(usbList);
			if (di != null) {
				Intent intent = new Intent(ctx, FindDeviceActivity.class);
				intent.putExtra("device", di);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("method", remove);
				ctx.startActivity(intent);
			}

		} else if (remove == MediacenterConstant.MESSAGE_REMOVE) {
			DeviceInfo reomoveDevice = Util.isRemoveDevice(path, usbList,
					manager.getOldList());
			manager.addOldList(usbList);
			if (reomoveDevice != null) {
				Activity showActivity = ActivitiesManager.getInstance()
						.getActivity(ActivitiesManager.ACTIVITY_FILE_VIEW);
				if (showActivity != null) {
					FileMainActivity fm = (FileMainActivity) showActivity;
					DeviceInfo di = fm.getCurrentDeviceInfo();
					if (di != null && reomoveDevice != null
							&& di.devId.equals(reomoveDevice.devId)) {
						Intent intent = new Intent(ctx,
								FindDeviceActivity.class);
						intent.putExtra("removedevice", reomoveDevice);
						intent.putExtra("method", remove);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ctx.startActivity(intent);
					} else {
						Activity findActivity = ActivitiesManager.getInstance()
								.getActivity(
										ActivitiesManager.ACTIVITY_POP_VIEW);
						if (findActivity != null) {
							findActivity.finish();
						}
					}

				} else {
					Activity findActivity = ActivitiesManager.getInstance()
							.getActivity(ActivitiesManager.ACTIVITY_POP_VIEW);
					if (findActivity != null) {
						FindDeviceActivity fa = (FindDeviceActivity) findActivity;
						if (fa.geCurrentInfo().devId
								.equals(reomoveDevice.devId)) {
							findActivity.finish();
						}
					}
				}

			}
		}

	}

}
