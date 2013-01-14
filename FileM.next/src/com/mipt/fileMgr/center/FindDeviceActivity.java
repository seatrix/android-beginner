package com.mipt.fileMgr.center;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.server.DeviceInfo;
import com.mipt.fileMgr.center.server.MediacenterConstant;
import com.mipt.fileMgr.utils.ActivitiesManager;

/**
 * 
 * @author fang
 * 
 */
public class FindDeviceActivity extends Activity {
	private FindDeviceDialog fDialog;
	private String[] deviceStr;
	private Context cxt;
	private ExitDialog pullDevice;
	private DeviceInfo currentInfo;
	private int type = 0;
	private DeviceInfo removeDevice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		cxt = FindDeviceActivity.this;
		Intent intent = this.getIntent();
		type = intent.getIntExtra("method", 0);
		currentInfo = (DeviceInfo) intent.getSerializableExtra("device");
		removeDevice = (DeviceInfo) intent.getSerializableExtra("removedevice");
		deviceStr = new String[] { this.getString(R.string.category_video),
				this.getString(R.string.category_music),
				this.getString(R.string.category_picture) };
		ActivitiesManager.getInstance().registerActivity(
				ActivitiesManager.ACTIVITY_POP_VIEW, this);
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
			if (fDialog == null) {
				fDialog = new FindDeviceDialog(this,
						R.style.show_choose_type_dialog,
						new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								final String name = (String) arg0
										.getItemAtPosition(arg2);
								Intent intent = new Intent(cxt,
										FileMainActivity.class);
								intent.putExtra(
										MediacenterConstant.INTENT_EXTRA,
										currentInfo);
								intent.putExtra(
										MediacenterConstant.INTENT_TYPE_VIEW,
										getTpyeByName(name));
								startActivity(intent);
								fDialog.dismiss();
								fDialog = null;
								FindDeviceActivity.this.finish();
							}

						}, deviceStr, currentInfo.devName);
				fDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						FindDeviceActivity.this.finish();
					}
				});
			} else {
				fDialog.setName(currentInfo.devName);
			}
			fDialog.show();
		} else if (type == MediacenterConstant.MESSAGE_REMOVE) {
			Activity showActivity = ActivitiesManager.getInstance()
					.getActivity(ActivitiesManager.ACTIVITY_FILE_VIEW);
			if (fDialog != null && fDialog.isShowing()
					&& currentInfo.devPath.equals(removeDevice.devPath)
					&& showActivity == null) {
				fDialog.dismiss();
				fDialog = null;
				FindDeviceActivity.this.finish();
			}
			if (showActivity != null) {
				final FileMainActivity fm = (FileMainActivity) showActivity;
				DeviceInfo di = fm.getCurrentDeviceInfo();
				if (di != null && removeDevice != null
						&& di.devPath.equals(removeDevice.devPath)
						&& pullDevice == null) {
					if (fDialog != null && fDialog.isShowing()) {
						fDialog.dismiss();
						fDialog = null;
						FindDeviceActivity.this.finish();
					}
					pullDevice = new ExitDialog(cxt, R.style.exit_dialog,
							false, cxt.getString(R.string.current_sd_remove),
							new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									fm.finish();
									FindDeviceActivity.this.finish();
								}
							});
					pullDevice.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK)
								return true;
							return false;
						}
					});
					if (!pullDevice.isShowing()) {
						pullDevice.show();
					}
				}
			}

		}
	}

	public DeviceInfo geCurrentInfo() {
		return currentInfo;
	}

	class FindDeviceDialog extends Dialog {
		private OnItemClickListener listener;
		private Context context;
		private String[] mStrings;
		private ArrayAdapter<String> adapter;
		private String devName;
		private TextView title;

		public FindDeviceDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		public FindDeviceDialog(Context context, int theme) {
			super(context, theme);
			this.context = context;
		}

		public FindDeviceDialog(Context _context, int theme,
				OnItemClickListener _listener, String[] strs, String _devName) {
			super(_context, theme);
			this.context = _context;
			this.listener = _listener;
			mStrings = strs;
			devName = _devName;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.cm_pop_dialog);
			iniUI();
		}

		private void iniUI() {
			ListView lv = (ListView) findViewById(R.id.tpe_item_select);
			lv.setDividerHeight(0);
			title = (TextView) findViewById(R.id.item_select_title_tag);
			title.setText(getString(R.string.cm_pop_title) + devName);
			adapter = new ArrayAdapter<String>(context, R.layout.cm_pop_item,
					mStrings);
			// adapter.get
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(listener);
		}

		public void setName(String _devName) {
			devName = _devName;
			title.setText(getString(R.string.cm_pop_title) + devName);
		}
	}

	private int getTpyeByName(String name) {
		int type = MediacenterConstant.IntentFlags.PIC_ID;
		if (name.equals(getString(R.string.category_music))) {
			type = MediacenterConstant.IntentFlags.MUSIC_ID;
		} else if (name.equals(getString(R.string.category_video))) {
			type = MediacenterConstant.IntentFlags.VIDEO_ID;
		}
		return type;
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (fDialog != null && fDialog.isShowing()) {
			fDialog.dismiss();
			fDialog = null;
		}
		if (pullDevice != null && pullDevice.isShowing()) {
			pullDevice.dismiss();
			pullDevice = null;
		}
		ActivitiesManager.getInstance().unRegisterActivity(
				ActivitiesManager.ACTIVITY_POP_VIEW);
	}
}
