package com.mipt.fileMgr.center;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.ExitDialog;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.ActivitiesManager;

/**
 * 
 * @author fang
 * 
 */
public class FindDeviceActivity extends Activity {
    private static final String TAG = "FindDeviceActivity";
	private ExitDialog pullDevice;
	private DeviceInfo currentInfo;
	private int type = 0;
	private DeviceInfo removeDevice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//cxt = FindDeviceActivity.this;
		Intent intent = this.getIntent();
		type = intent.getIntExtra("method", 0);
		currentInfo = (DeviceInfo) intent.getSerializableExtra("device");
		removeDevice = (DeviceInfo) intent.getSerializableExtra("removedevice");
/*		deviceStr = new String[] { this.getString(R.string.category_video),
				this.getString(R.string.category_music),
				this.getString(R.string.category_picture) };
*/		ActivitiesManager.getInstance().registerActivity(
				ActivitiesManager.ACTIVITY_POP_VIEW, this);

        Log.i(TAG, "call FindDeviceActivity.....");
		doAction();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		type = intent.getIntExtra("method", 0);
		DeviceInfo current = (DeviceInfo) intent.getSerializableExtra("device");
		if (current != null) {
			currentInfo = current;
		}
		removeDevice = (DeviceInfo) intent.getSerializableExtra("removedevice");
		doAction();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	void doAction() {
		if (type == MediacenterConstant.MESSAGE_ADD) {
			if (pullDevice != null && pullDevice.isShowing()) {
				pullDevice.dismiss();
				pullDevice = null;
				Activity showActivity = ActivitiesManager.getInstance()
						.getActivity(ActivitiesManager.ACTIVITY_FILE_VIEW);
				if (showActivity != null) {
					final FileMainActivity fm = (FileMainActivity) showActivity;
					DeviceInfo di = fm.getCurrentDeviceInfo();
					if (di != null && currentInfo != null
							&& !di.devPath.equals(currentInfo.devPath)) {
						fm.finish();
					}
				}
			}
		} else if (type == MediacenterConstant.MESSAGE_REMOVE) {
			Activity showActivity = ActivitiesManager.getInstance()
					.getActivity(ActivitiesManager.ACTIVITY_FILE_VIEW);
			if (showActivity != null) {
				final FileMainActivity fm = (FileMainActivity) showActivity;
				DeviceInfo di = fm.getCurrentDeviceInfo();
				if (di != null && removeDevice != null
						&& di.devPath.equals(removeDevice.devPath)
						&& pullDevice == null) {
				}
			}

		}
	}

	public DeviceInfo geCurrentInfo() {
		return currentInfo;
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivitiesManager.getInstance().unRegisterActivity(
				ActivitiesManager.ACTIVITY_POP_VIEW);
	}
}
