package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.file.FileIconHelper;
import com.mipt.fileMgr.center.file.IntentBuilder;
import com.mipt.fileMgr.center.server.DeviceInfo;
import com.mipt.fileMgr.center.server.FileInfo;
import com.mipt.fileMgr.center.server.MediacenterConstant;
import com.mipt.fileMgr.dlna.AllShareProxy;
import com.mipt.fileMgr.dlna.AllShareProxy.ControlRequestProxy;
import com.mipt.fileMgr.dlna.network.Item;
import com.mipt.fileMgr.dlna.util.UpnpUtil;
import com.mipt.fileMgr.utils.LinkedListStack;
import com.mipt.fileMgr.utils.Util;

/**
 * 
 * @author fang
 * 
 */
public class DLANViewFragment extends Fragment implements
		FileMainActivity.IBackPressedListener,
		FileMainActivity.DataChangeListener {
	private static final String TAG = "DLANViewFragment";
	public static final int ROOT_UPDATE = 2;
	private Activity mActivity;
	private View mRootView;
	private Device currentDevice;
	private int type;
	private GridView fileGridView;
	private ControlRequestProxy requestProxy;
	private LinkedListStack stack;
	private DlanAdapter adapter;
	private AllShareProxy proxy;
	private TextView fileName;
	private TextView fileDate;
	private TextView fileSize;
	private TextView currentNum;
	private LinearLayout progressBar;
	private FileIconHelper mFileIconHelper;
	private String currentPath;
	private TextView currentTextView;

	static DLANViewFragment newInstance(DeviceInfo di, int type) {
		DLANViewFragment f = new DLANViewFragment();
		Bundle args = new Bundle();
		args.putInt(MediacenterConstant.INTENT_TYPE_VIEW, type);
		args.putSerializable(MediacenterConstant.INTENT_EXTRA, di);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = getActivity();
		mRootView = inflater.inflate(R.layout.cm_file_gridview_list, container,
				false);
		stack = new LinkedListStack();
		fileName = (TextView) mRootView.findViewById(R.id.cm_file_name);
		fileDate = (TextView) mRootView.findViewById(R.id.cm_file_date);
		fileSize = (TextView) mRootView.findViewById(R.id.cm_file_size);
		mRootView.findViewById(R.id.cm_file_type).setVisibility(View.GONE);
		currentNum = (TextView) mActivity.findViewById(R.id.current_num_tag);
		currentTextView = (TextView) mActivity
				.findViewById(R.id.current_path_tag);
		currentPath = (String) currentTextView.getText();
		mActivity.findViewById(R.id.choose_txt_img_tag)
				.setVisibility(View.GONE);
		mActivity.findViewById(R.id.choose_txt_tag).setVisibility(View.GONE);
		progressBar = (LinearLayout) mActivity
				.findViewById(R.id.cm_progress_small);
		fileGridView = (GridView) mRootView.findViewById(R.id.file_content);
		mFileIconHelper = new FileIconHelper(mActivity);
		type = getFileTypeByOrginType(getArguments() != null ? getArguments()
				.getInt(MediacenterConstant.INTENT_TYPE_VIEW)
				: MediacenterConstant.IntentFlags.PIC_ID);
		DeviceInfo diTemp = getArguments() != null ? (DeviceInfo) getArguments()
				.getSerializable(MediacenterConstant.INTENT_EXTRA) : null;
		List<Device> listDevice = AllShareProxy.getInstance(mActivity)
				.getDeviceList();
		currentDevice = isDeviceHas(diTemp, listDevice);
		adapter = new DlanAdapter(mActivity, mFileIconHelper);
		fileGridView.setAdapter(adapter);
		if (currentDevice == null) {
			Toast.makeText(mActivity,
					mActivity.getString(R.string.current_dlan_remove), 1)
					.show();
			mActivity.finish();
		} else {
			proxy = AllShareProxy.getInstance(mActivity);
			proxy.setSelectedDevice(currentDevice);
			requestProxy = proxy.getControlRequestProxy();
			Message message = mHandler.obtainMessage(MESSAGE_FRESH_BEGIN, this);
			message.obj = null;
			message.arg2 = ROOT_UPDATE;
			mHandler.removeMessages(MESSAGE_FRESH_BEGIN);
			mHandler.sendMessage(message);
			requestProxy.syncGetRoot(upnpCallbacknew);

		}
		fileGridView.setOnItemClickListener(itemSelectListenr);
		fileGridView.setOnItemSelectedListener(fileChange);
		return mRootView;
	}

	private ArrayList<FileInfo> upnpItem2File(List<Item> list) {
		ArrayList<FileInfo> returnList = new ArrayList<FileInfo>();
		FileInfo fileInfo;
		for (Item item : list) {
			fileInfo = new FileInfo();
			fileInfo.fileId = item.getStringid();
			fileInfo.fileName = item.getTitle();
			if (UpnpUtil.isPictureItem(item)) {
				fileInfo.fileType = FileInfo.TYPE_PIC;
			} else if (UpnpUtil.isVideoItem(item)) {
				fileInfo.fileType = FileInfo.TYPE_VIDEO;
			} else if (UpnpUtil.isAudioItem(item)) {
				fileInfo.fileType = FileInfo.TYPE_MUSIC;
			} else {
				fileInfo.isDir = true;
			}
			fileInfo.duration = item.getDuration();
			fileInfo.imgPath = item.getAlbumUri();
			fileInfo.filePath = item.getRes();
			fileInfo.albumName = item.getAlbum();
			fileInfo.artist = item.getArtist();
			fileInfo.modifiedDate = item.getDate();
			fileInfo.fileSize = item.getSize();
			if (fileInfo.isDir || fileInfo.fileType == type) {
				returnList.add(fileInfo);
			}

		}
		return returnList;
	}

	public void runOnUiThread(Runnable r) {
		if (mActivity != null) {
			mActivity.runOnUiThread(r);
		}

	}

	private void showEmptyView(boolean show) {
		RelativeLayout emptyView = (RelativeLayout) mRootView
				.findViewById(R.id.empty_view);
		if (emptyView != null) {
			emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
			emptyView.requestFocus();
			// emptyView.setSelected(true);
			emptyView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!onBack()) {
						mActivity.finish();
					}

				}
			});
		}

	}

	private Device isDeviceHas(DeviceInfo di, List<Device> listDevice) {
		if (di == null || listDevice == null || listDevice.isEmpty()) {
			return null;
		}
		for (Device d : listDevice) {
			if (di.devId.equals(d.getUDN())) {
				return d;
			}
		}
		return null;
	}

	class DlanAdapter extends BaseAdapter {
		public static final String TAG = "DlanAdapter";
		private final Context mContext;
		private FileIconHelper mHelper;
		private ArrayList<FileInfo> cList;

		public DlanAdapter(Activity activity) {
			mContext = activity;
			inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			cList = MediaCenterApp.getInstance().getData();
		}

		public DlanAdapter(Activity activity, FileIconHelper _mFileIconHelper) {
			mContext = activity;
			inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mHelper = _mFileIconHelper;
			cList = MediaCenterApp.getInstance().getData();
		}

		public ArrayList<FileInfo> getCurrentList() {
			return cList;
		}

		public int getFilePos(String filePath) {
			if (TextUtils.isEmpty(filePath)) {
				return 0;
			}
			if (cList != null) {
				for (int i = 0; i < cList.size(); i++) {
					if (filePath.equals(cList.get(i).filePath)) {
						return i;

					}
				}
			}
			return 0;
		}

		public void refreshDate(final ArrayList<FileInfo> _cList) {
			cList.clear();
			if (_cList != null) {
				cList.addAll(_cList);
			}

		}

		LayoutInflater inflater;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.cm_file_item, null);
				holder = new ViewHolder();
				holder.fileImage = (ImageView) convertView
						.findViewById(R.id.file_image);
				holder.fileImageFrame = (ImageView) convertView
						.findViewById(R.id.file_image_frame);
				holder.name = (TextView) convertView
						.findViewById(R.id.file_name);
				holder.childCnt = (TextView) convertView
						.findViewById(R.id.file_child_cnt);
				holder.videoTag = (ImageView) convertView
						.findViewById(R.id.file_video_tag);
				holder.musicImage = (ImageView) convertView
						.findViewById(R.id.music_image);
				holder.dirImage = (ImageView) convertView
						.findViewById(R.id.dir_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (cList.get(position) != null) {
				FileInfo file = cList.get(position);
				holder.name.setText(file.fileName);
				if (file.isDir) {
					holder.fileImageFrame.setVisibility(View.GONE);
					holder.musicImage.setVisibility(View.GONE);
					holder.videoTag.setVisibility(View.GONE);
					holder.fileImage.setVisibility(View.GONE);
					holder.dirImage.setImageResource(R.drawable.cm_folder);
					holder.dirImage.setVisibility(View.VISIBLE);
				} else {
					holder.dirImage.setVisibility(View.GONE);
					holder.fileImageFrame.setVisibility(View.VISIBLE);
					mHelper.setDlanIcon(file, holder.fileImage,
							handleDeviceName(currentDevice.getUDN()),
							holder.videoTag, holder.fileImageFrame,
							holder.musicImage);
				}
			}
			final int currentPos = position;
			convertView.setOnHoverListener(new View.OnHoverListener() {

				@Override
				public boolean onHover(View v, MotionEvent event) {
					if (MotionEvent.ACTION_HOVER_ENTER == event.getAction()) {
						if (mHandler != null) {
							Message message = mHandler.obtainMessage(
									MESSAGE_SETINFO, this);
							message.arg1 = currentPos;
							mHandler.removeMessages(MESSAGE_SETINFO);
							mHandler.sendMessage(message);
						}
						return true;
					} else if (MotionEvent.ACTION_HOVER_EXIT == event
							.getAction()) {
						// v.setSelected(false);
						return true;
					}
					return false;
				}
			});
			return convertView;
		}

		class ViewHolder {
			ImageView fileImageFrame;
			ImageView fileImage;
			ImageView dirImage;
			ImageView musicImage;
			TextView name;
			TextView childCnt;
			ImageView videoTag;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (cList != null) {
				return cList.size();
			} else
				return 0;
		}

		@Override
		public FileInfo getItem(int position) {
			// TODO Auto-generated method stub
			return cList == null || cList.isEmpty() ? null : cList
					.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

	}

	class BackDate {
		public ArrayList<FileInfo> dateList;
		public int pos;
		public String title;
	}

	private OnItemClickListener itemSelectListenr = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			FileInfo file = adapter.getItem(position);
			if (file.isDir) {
				Message message = mHandler.obtainMessage(MESSAGE_FRESH_BEGIN,
						this);
				message.obj = file;
				message.arg1 = position;
				mHandler.removeMessages(MESSAGE_FRESH_BEGIN);
				mHandler.sendMessage(message);
				requestProxy.syncGetItems(file.fileId, upnpCallbacknew);
			} else {
				IntentBuilder.viewFile(mActivity, adapter.getItem(position),
						null, true, handleDeviceName(currentDevice.getUDN()));
			}

		}

	};

	private AllShareProxy.ControlRequestCallback upnpCallbacknew = new AllShareProxy.ControlRequestCallback() {

		@Override
		public void onGetItems(List<Item> list) {
			// TODO Auto-generated method stub
			oldData.clear();
			oldData.addAll(adapter.getCurrentList());
			if (list == null || list.isEmpty() || upnpItem2File(list).isEmpty()) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showEmptyView(true);
						updateAdapter(new ArrayList<FileInfo>(), 0);
						adapter.refreshDate(new ArrayList<FileInfo>());
						setFileInfo(0, 0, null);
						adapter.notifyDataSetChanged();
					}

				});
				mHandler.sendEmptyMessage(MESSAGE_FRESH_END);
			} else {

				Message message = mHandler.obtainMessage(MESSAGE_FRESH_END,
						this);
				ArrayList<FileInfo> data = upnpItem2File(list);
				message.obj = data;
				mHandler.removeMessages(MESSAGE_FRESH_END);
				mHandler.sendMessage(message);

			}

		}

	};

	@Override
	public boolean onBack() {
		if (!onOperationUpLevel()) {
			return false;
		}
		return true;
	}

	public boolean onOperationUpLevel() {
		if (!stack.imEmpty()) {
			Object object = stack.pop();
			if (object != null) {
				BackDate backDate = (BackDate) object;
				ArrayList<FileInfo> temp = new ArrayList<FileInfo>();
				temp.addAll(backDate.dateList);

				if (backDate.title != null) {
					currentTextView.setText(backDate.title);
				} else {
					currentTextView.setText(currentPath);
				}

				int size = backDate.dateList.size();
				showEmptyView(size == 0);
				if (size == 0) {
					setFileInfo(0, 0, null);
				} else {
					setFileInfo(backDate.pos, size, temp.get(backDate.pos));
				}
				updateAdapter(temp, backDate.pos);
				return true;
			}
		}
		return false;

	}

	private void updateAdapter(final ArrayList<FileInfo> _dateList,
			final int pos) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.refreshDate(_dateList);
				adapter.notifyDataSetChanged();
				fileGridView.setSelection(pos);
			}

		});
	}

	private final OnItemSelectedListener fileChange = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if (adapter == null) {
				return;
			}
			FileInfo fi = adapter.getItem(arg2);
			if (fi != null) {
				setFileInfo(arg2, adapter.getCount(), fi);
			} else {
				setFileInfo(0, 0, null);
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	void setFileInfo(int pos, int size, FileInfo fi) {
		currentNum.setText((pos + 1) + "/" + size);
		if (fi != null) {
			fileName.setText(fi.fileName);
			if (fi.modifiedDate != 0) {
				fileDate.setVisibility(View.VISIBLE);
				fileDate.setText(Util.formatDateString(fi.modifiedDate) + "");
			} else {
				fileDate.setVisibility(View.GONE);
			}

			if (fi.fileSize != 0) {
				fileSize.setText(Util.convertStorage(fi.fileSize) + "");
			} else {
				fileSize.setText("");
			}
		} else {
			fileName.setText("");
			fileDate.setText("");
			fileSize.setText("");
		}

	}

	private String handleDeviceName(String name) {
		if (name == null) {
			return name;
		}
		String rName = name;
		if (rName.contains("uuid:")) {
			rName = rName.replace("uuid:", "");
		}

		return rName;
	}

	static final int MESSAGE_FRESH_END = 102;
	static final int MESSAGE_FRESH_BEGIN = 101;
	static final int MESSAGE_FRESH_BACK = 103;
	static final int MESSAGE_SETINFO = 111;
	private String showTitle;
	private int lastPost;
	private boolean firstTime = false;
	private boolean pause = false;
	private ArrayList<FileInfo> oldData = new ArrayList<FileInfo>();
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_FRESH_BEGIN:
				lastPost = msg.arg1;
				if (msg.arg2 == ROOT_UPDATE) {
					firstTime = true;
					showTitle = null;
				} else {
					Object object = msg.obj;
					if (object != null) {
						FileInfo fi = (FileInfo) object;
						showTitle = fi.fileName;
					}

					firstTime = false;
				}
				pause = false;
				progressBar.setVisibility(View.VISIBLE);
				fileGridView.setClickable(false);
				break;
			case MESSAGE_FRESH_END:
				if (!pause) {
					if (!firstTime) {
						BackDate bd = new BackDate();
						bd.dateList = new ArrayList<FileInfo>();
						bd.dateList.addAll(oldData);
						if (bd.title == null) {
							bd.title = currentPath;
						}
						bd.title = currentTextView.getText().toString();
						currentTextView.setText(bd.title + "/" + showTitle);
						bd.pos = lastPost;
						stack.push(bd);
					} else {
						currentTextView.setText(currentPath);
						// bd.title = showTitle;
					}
					Object objectE = msg.obj;
					if (objectE != null) {
						ArrayList<FileInfo> fi = (ArrayList<FileInfo>) objectE;

						int size = fi.size();
						showEmptyView(size == 0);
						if (size == 0) {
							setFileInfo(0, 0, null);
						} else {
							setFileInfo(0, size, fi.get(0));
						}
						adapter.refreshDate(fi);
					}

					adapter.notifyDataSetChanged();
					fileGridView.setSelection(0);
					fileGridView.setClickable(true);
					progressBar.setVisibility(View.GONE);
				}

				break;
			case MESSAGE_FRESH_BACK:
				fileGridView.setClickable(true);
				progressBar.setVisibility(View.GONE);
				pause = true;
				break;
			case MESSAGE_SETINFO:
				final int pos = msg.arg1;
				if (adapter != null) {
					setFileInfo(pos, adapter.getCount(), adapter.getItem(pos));
				}

				break;

			}

		}
	};

	int getFileTypeByOrginType(int orginType) {
		int rType = FileInfo.TYPE_MUSIC;
		if (orginType == MediacenterConstant.IntentFlags.PIC_ID) {
			rType = FileInfo.TYPE_PIC;
		} else if (orginType == MediacenterConstant.IntentFlags.VIDEO_ID) {
			rType = FileInfo.TYPE_VIDEO;
		}
		return rType;

	}

	@Override
	public void dataChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackPos(final int pos, final String path) {
		// TODO Auto-generated method stub
		if (path.indexOf("http") == 0) {
			final int gridPos = adapter.getFilePos(path);
			Util.runOnUiThread(mActivity, new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					fileGridView.setSelection(gridPos);
				}
			});
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mFileIconHelper.stopLoad();
	}
}
