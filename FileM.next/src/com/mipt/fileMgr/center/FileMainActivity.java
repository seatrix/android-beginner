package com.mipt.fileMgr.center;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.AllFileViewFragment;
import com.mipt.mediacenter.center.DLANViewFragment;
import com.mipt.mediacenter.center.DirViewFragment;
import com.mipt.mediacenter.center.MediaCenterApplication;
import com.mipt.mediacenter.center.file.FileCategoryHelper;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.file.FilenameExtFilter;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.FileSortHelper.SortMethod;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.ActivitiesManager;
import com.mipt.mediacenter.utils.Util;

/**
 * 
 * @author fang
 * 
 */
public class FileMainActivity extends Activity {
	private static final String LOG_TAG = "FileActivity";
	private static final String DATA_BUNDEL = "data_bundel";
	private int viewType;
	private int tabId;
	private DeviceInfo dInfo;
	private ArrayList<FileInfo> dataList;
	private ArrayList<FileInfo> albumList;
	private Context cxt;
	private TextView currentPath;
	private TextView viewTypeTag;
	private String[] tyeStr;
	private ViewTypeChooseDialog showChoose;
	private LinearLayout progressBar;
	private GetFileTask getFileTask;
	private ScanFileTask scanFileTask;
	private FilenameExtFilter fileNameFilter;
	private FileSortHelper fsortHelper;
	private boolean isUserPause;
	private boolean isCheck;
	private TextView currentNum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cm_file_list);
		((LinearLayout) findViewById(R.id.tail_other_tag))
				.setVisibility(View.VISIBLE);
		cxt = FileMainActivity.this;
		currentPath = (TextView) findViewById(R.id.current_path_tag);
		viewTypeTag = (TextView) findViewById(R.id.view_type_tag);
		currentNum = (TextView) findViewById(R.id.current_num_tag);
		dInfo = (DeviceInfo) getIntent().getSerializableExtra(
				MediacenterConstant.INTENT_EXTRA);
		tabId = getIntent().getIntExtra(MediacenterConstant.INTENT_TYPE_VIEW,
				MediacenterConstant.IntentFlags.PIC_ID);
		if (savedInstanceState != null) {
			tabId = savedInstanceState
					.getInt(MediacenterConstant.IntentFlags.TAG_ID);
			dInfo = (DeviceInfo) savedInstanceState.getBundle(DATA_BUNDEL).get(
					MediacenterConstant.INTENT_EXTRA);
		}
		fsortHelper = FileSortHelper.getInstance();
		dataList = MediaCenterApplication.getInstance().getData();
		albumList = MediaCenterApplication.getInstance().getAlbumData();
		progressBar = (LinearLayout) findViewById(R.id.cm_progress_small);
		ActivitiesManager.getInstance().registerActivity(
				ActivitiesManager.ACTIVITY_FILE_VIEW, this);
		viewType = Util.getLastType(cxt, tabId + "");
		if (tabId == MediacenterConstant.IntentFlags.MUSIC_ID) {
			fileNameFilter = FileCategoryHelper.filters.get(FileCategory.Music);
		} else if (tabId == MediacenterConstant.IntentFlags.VIDEO_ID) {
			fileNameFilter = FileCategoryHelper.filters.get(FileCategory.Video);
		} else {
			fileNameFilter = FileCategoryHelper.filters
					.get(FileCategory.Picture);
		}
		posPath = null;
		isUserPause = false;
		isCheck = false;
		addFragmentToStack(viewType, dInfo);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle budle = new Bundle();
		budle.putSerializable(MediacenterConstant.INTENT_EXTRA, dInfo);
		outState.putBundle(DATA_BUNDEL, budle);
		outState.putInt(MediacenterConstant.IntentFlags.TAG_ID, tabId);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		dInfo = (DeviceInfo) intent
				.getSerializableExtra(MediacenterConstant.INTENT_EXTRA);
		tabId = intent.getIntExtra(MediacenterConstant.INTENT_TYPE_VIEW,
				MediacenterConstant.IntentFlags.PIC_ID);
		viewType = Util.getLastType(cxt, tabId + "");
		if (tabId == MediacenterConstant.IntentFlags.MUSIC_ID) {
			fileNameFilter = FileCategoryHelper.filters.get(FileCategory.Music);
		} else if (tabId == MediacenterConstant.IntentFlags.VIDEO_ID) {
			fileNameFilter = FileCategoryHelper.filters.get(FileCategory.Video);
		} else {
			fileNameFilter = FileCategoryHelper.filters
					.get(FileCategory.Picture);
		}
		posPath = null;
		isCheck = false;
		isUserPause = false;
		addFragmentToStack(viewType, dInfo);
		super.onNewIntent(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (getFileTask != null && !getFileTask.isStop()) {
			getFileTask.cancel();
			getFileTask = null;
		}
		if (scanFileTask != null && !scanFileTask.isStop()) {
			scanFileTask.cancel();
			scanFileTask = null;
		}
	}

	@Override
	protected void onResume() {
		if (tabId == MediacenterConstant.IntentFlags.MUSIC_ID) {
			tyeStr = new String[] { this.getString(R.string.file_view_type),
					this.getString(R.string.music_view_type),
					this.getString(R.string.album_view_type),
					this.getString(R.string.artist_view_type),
					this.getString(R.string.genre_view_type) };
		} else {
			tyeStr = new String[] { this.getString(R.string.file_view_type),
					this.getString(R.string.all_file_view_type) };

		}
		showChoose = new ViewTypeChooseDialog(cxt,
				R.style.show_choose_type_dialog, new OnItemClickListener() {
					@Override
					public void onItemClick(final AdapterView<?> arg0,
							View arg1, final int arg2, long arg3) {
						// TODO Auto-generated method stub
						posPath = null;
						isCheck = false;
						isUserPause = false;
						final int newViewType = getTypeByName((String) arg0
								.getItemAtPosition(arg2));
						if (viewType != newViewType) {							
							Util.runOnUiThread(FileMainActivity.this,
									new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											addFragmentToStack(newViewType,
													dInfo);

										}
									});
						}
						showChoose.dismiss();
					}
				}, tyeStr, getTypeNameById(viewType));

		if (posPath != null
				&& viewType != MediacenterConstant.FileViewType.VIEW_DIR
				&& dInfo.type != DeviceInfo.TYPE_DLAN && !isUserPause) {
			isCheck = true;
			scanFileTask = new ScanFileTask(cxt, dInfo, viewType, tabId,
					fileNameFilter, posPath);
			scanFileTask.execute();
		}
		super.onResume();
	}

	private void addFragmentToStack(int _viewTpe, DeviceInfo _dInfo) {
		Util.putLastType(cxt, tabId + "", _viewTpe);
		MediaCenterApplication.getInstance().resetData();
		MediaCenterApplication.getInstance().resetAlbumData();
		if (getFileTask != null && !getFileTask.isStop()) {
			getFileTask.cancel();
			getFileTask = null;
		}
		if (scanFileTask != null && !scanFileTask.isStop()) {
			scanFileTask.cancel();
			scanFileTask.reMoveMessage();
			scanFileTask = null;
		}
		mHandler.removeMessages(MESSAGE_SCAN_END);
		mHandler.removeMessages(MESSAGE_SCAN_BEGIN);
		mHandler.removeMessages(MESSAGE_CHANGE_DATA);
		viewType = _viewTpe;
		Fragment fg = getFragmentManager().findFragmentById(R.id.file_content);
		if (fg != null) {
			fg.onDetach();
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment newFragment = null;
		boolean task = false;
		String title = null;
		if (_dInfo != null && _dInfo.type == DeviceInfo.TYPE_DLAN) {
			newFragment = DLANViewFragment.newInstance(dInfo, tabId);
			currentPath.setText(getRootName(tabId) + "/" + dInfo.devName);
			viewTypeTag.setText(getString(R.string.file_view_type));
			task = false;
		} else {
			currentPath.setText(getRootName(tabId)
					+ Util.handlePath(dInfo.devPath));
			if (viewType == MediacenterConstant.FileViewType.VIEW_DIR) {
				viewTypeTag.setText(getString(R.string.file_view_type));
				newFragment = DirViewFragment.newInstance(dInfo.devPath, tabId);
				task = false;
				progressBar.setVisibility(View.GONE);
			} else {
				task = true;
				title = null;
				if (viewType == MediacenterConstant.FileViewType.VIEW_FILE) {
					viewTypeTag.setText(getString(R.string.all_file_view_type));
					newFragment = AllFileViewFragment.newInstance(dataList,
							tabId);
				} /*else if (viewType == MediacenterConstant.FileViewType.VIEW_MSUIC) {
					newFragment = MusicListViewFragment.newInstance(dataList,
							tabId, true);
					viewTypeTag.setText(getString(R.string.music_view_type));

				} else if (viewType == MediacenterConstant.FileViewType.VIEW_ALBUM) {
					title = "album";
					newFragment = MusicGridViewFragment.newInstance(albumList,
							tabId, dInfo.devPath, viewType, true);
					viewTypeTag.setText(getString(R.string.album_view_type));

				} else if (viewType == MediacenterConstant.FileViewType.VIEW_ARTIST) {
					newFragment = MusicGridViewFragment.newInstance(albumList,
							tabId, dInfo.devPath, viewType, true);
					viewTypeTag.setText(getString(R.string.artist_view_type));
					title = "artist";

				} else if (viewType == MediacenterConstant.FileViewType.VIEW_GENRE) {
					newFragment = MusicGridViewFragment.newInstance(albumList,
							tabId, dInfo.devPath, viewType, true);
					viewTypeTag.setText(getString(R.string.genre_view_type));
					title = "genre";

				}*/ else {
					title = null;
				}

			}
		}
		ft.replace(R.id.file_content, newFragment);
		ft.commit();
		if (task) {
			if (_dInfo.type == DeviceInfo.TYPE_USB) {
				scanFileTask = new ScanFileTask(cxt, dInfo, viewType, tabId,
						fileNameFilter);
				scanFileTask.execute();
			} else {
				getFileTask = new GetFileTask(cxt, dInfo, viewType, tabId,
						title);
				getFileTask.execute();
			}

		}
	}

	public interface IBackPressedListener {
		boolean onBack();
	}

	public interface DataChangeListener {
		void dataChange();

		void setBackPos(final int pos, final String path);
	}

	@Override
	public void onBackPressed() {
		if (viewType == MediacenterConstant.FileViewType.VIEW_DIR
				|| dInfo.type == DeviceInfo.TYPE_DLAN) {
			IBackPressedListener backPressedListener = (IBackPressedListener) getFragmentManager()
					.findFragmentById(R.id.file_content);
			if (backPressedListener != null && !backPressedListener.onBack()) {
				super.onBackPressed();
			}
		} else {
			if (scanFileTask != null && !scanFileTask.isStop()) {
				scanFileTask.cancel();
				posPath = null;
				isUserPause = true;
				// scanFileTask.onCancelled();
			} else {
				super.onBackPressed();
			}

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ActivitiesManager.getInstance().unRegisterActivity(
				ActivitiesManager.ACTIVITY_FILE_VIEW);
		if (getFileTask != null) {
			getFileTask.cancel();
			getFileTask = null;
		}
		if (scanFileTask != null) {
			scanFileTask.cancel();
			scanFileTask = null;
		}
	}

	public DeviceInfo getCurrentDeviceInfo() {
		return dInfo;
	}

	private String getRootName(int type) {
		String str = "/";
		if (type == MediacenterConstant.IntentFlags.PIC_ID) {
			str += getString(R.string.tab_pic);
		} else if (type == MediacenterConstant.IntentFlags.MUSIC_ID) {
			str += getString(R.string.category_music);
		} else if (type == MediacenterConstant.IntentFlags.VIDEO_ID) {
			str += getString(R.string.category_video);
		}
		return str;

	}

	private int getTypeByName(String name) {
		int type = MediacenterConstant.FileViewType.VIEW_DIR;
		if (name.equals(getString(R.string.file_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_DIR;
		} else if (name.equals(getString(R.string.all_file_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_FILE;
		} else if (name.equals(getString(R.string.genre_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_GENRE;
		} else if (name.equals(getString(R.string.music_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_MSUIC;
		} else if (name.equals(getString(R.string.album_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_ALBUM;
		} else if (name.equals(getString(R.string.artist_view_type))) {
			type = MediacenterConstant.FileViewType.VIEW_ARTIST;
		}
		return type;

	}

	private String getTypeNameById(int typeId) {
		String str = getString(R.string.file_view_type);
		if (typeId == MediacenterConstant.FileViewType.VIEW_FILE) {
			str = getString(R.string.all_file_view_type);
		} else if (typeId == MediacenterConstant.FileViewType.VIEW_GENRE) {
			str = getString(R.string.genre_view_type);
		} else if (typeId == MediacenterConstant.FileViewType.VIEW_MSUIC) {
			str = getString(R.string.music_view_type);
		} else if (typeId == MediacenterConstant.FileViewType.VIEW_ALBUM) {
			str = getString(R.string.album_view_type);
		} else if (typeId == MediacenterConstant.FileViewType.VIEW_ARTIST) {
			str = getString(R.string.artist_view_type);
		}

		return str;

	}

	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		if (arg1.getKeyCode() == KeyEvent.KEYCODE_MENU
				&& dInfo.type != DeviceInfo.TYPE_DLAN) {
			int cnt = getFragmentManager().getBackStackEntryCount();
			if (cnt < 1) {
				if (showChoose.isShowing()) {
					showChoose.dismiss();
				} else {
					showChoose.show();
				}
			}

		}
		return super.onKeyDown(arg0, arg1);

	}

	class ViewTypeChooseDialog extends Dialog {
		private OnItemClickListener listener;
		private Context context;
		private String[] mStrings;
		private ArrayAdapter<String> adapter;
		private String lastPosName;

		public ViewTypeChooseDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		public ViewTypeChooseDialog(Context context, int theme) {
			super(context, theme);
			this.context = context;
		}

		public ViewTypeChooseDialog(Context context, int theme,
				OnItemClickListener listener, final String[] strs,
				final String _lastPos) {
			super(context, theme);
			this.context = context;
			this.listener = listener;
			mStrings = strs;
			lastPosName = _lastPos;
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
			adapter = new ArrayAdapter<String>(context, R.layout.cm_pop_item,
					mStrings);

			// adapter.get
			lv.setAdapter(adapter);
			lv.setSelection(getPos(lastPosName, mStrings));
			lv.setOnItemClickListener(listener);
		}

		private int getPos(String posName, String[] names) {
			for (int i = 0; i < names.length; i++) {
				if (posName.endsWith(names[i])) {
					return i;
				}
			}
			return 0;
		}
	}

	static final int MESSAGE_SCAN_BEGIN = 101;
	static final int MESSAGE_SCAN_END = 102;
	static final int MESSAGE_SCAN_END_NO_DATA = 103;
	static final int MESSAGE_CHANGE_DATA = 104;
	static final int MESSAGE_SET_POS = 105;
	// boolean isCheck = true;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SCAN_BEGIN:
				progressBar.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_SCAN_END:
				progressBar.setVisibility(View.GONE);
				if (viewType == msg.arg2 && tabId == msg.arg1) {
					if (msg.obj != null) {
						ArrayList<FileInfo> fileInfo = (ArrayList<FileInfo>) msg.obj;
						if (fileInfo != null) {
							dataList.clear();
							dataList.addAll(fileInfo);
						}
					}
					Util.runOnUiThread(FileMainActivity.this, new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (dataList != null) {
								Collections.sort(dataList, fsortHelper
										.getComparator(SortMethod.name));
							}
							if (albumList != null && !albumList.isEmpty()) {
								Collections.sort(albumList, fsortHelper
										.getComparator(SortMethod.name));
							}
							DataChangeListener dataChangeListener = (DataChangeListener) getFragmentManager()
									.findFragmentById(R.id.file_content);
							if (dataChangeListener != null) {
								dataChangeListener.dataChange();
							}
						}
					});
				}
				break;
			case MESSAGE_CHANGE_DATA:
				progressBar.setVisibility(View.VISIBLE);
				if (msg.obj != null
						&& viewType == msg.arg2
						&& tabId == msg.arg1
						&& viewType != MediacenterConstant.FileViewType.VIEW_DIR) {
					FileInfo fi = (FileInfo) msg.obj;
					String handle = null;
					if (viewType == MediacenterConstant.FileViewType.VIEW_ALBUM) {
						handle = fi.albumName;
					} else if (viewType == MediacenterConstant.FileViewType.VIEW_ARTIST) {
						handle = fi.artist;
					} else if (viewType == MediacenterConstant.FileViewType.VIEW_GENRE) {
						handle = fi.genreName;
					} else {
						handle = null;
						if (isCheck) {
							isCheck = isHasFilePath(fi.filePath, dataList);
						}
						if (!isCheck) {
							dataList.add(fi);
						}
					}
					if (handle != null) {
						FileInfo newFileInfo = null;
						FileInfo tempFile = isHasFileInfo(handle, albumList);
						if (tempFile != null) {
							ArrayList<FileInfo> child = tempFile.childs;
							if (!isHasFilePath(fi.filePath, child)) {
								child.add(fi);
							}
							tempFile.count = child.size();

						} else {
							newFileInfo = new FileInfo();
							newFileInfo.fileName = handle;
							ArrayList<FileInfo> child = new ArrayList<FileInfo>();
							child.add(fi);
							newFileInfo.childs = child;
							newFileInfo.count = 1;
							albumList.add(newFileInfo);
						}
					}
					DataChangeListener dataChangeListener = (DataChangeListener) getFragmentManager()
							.findFragmentById(R.id.file_content);
					if (dataChangeListener != null) {
						// Collections.sort(dataList, comparator);
						dataChangeListener.dataChange();
					}
				}

				break;
			case MESSAGE_SCAN_END_NO_DATA:
				progressBar.setVisibility(View.GONE);
				break;
			case MESSAGE_SET_POS:
				progressBar.setVisibility(View.GONE);
				final String path = (String) msg.obj;
				int pos = MediaCenterApplication.getInstance().getFilePos(path);
				DataChangeListener dataChangeListener = (DataChangeListener) getFragmentManager()
						.findFragmentById(R.id.file_content);
				dataChangeListener.setBackPos(pos, path);
				break;

			}

		}
	};

	private Cursor query(Context ctx, Uri _uri, String[] prjs,
			String selections, String[] selectArgs, String order) {
		ContentResolver resolver = ctx.getContentResolver();
		if (resolver == null) {
			return null;
		}
		Cursor cursor = null;
		try {
			cursor = resolver.query(_uri, prjs, selections, selectArgs, order);
		} catch (Exception e) {

		}
		return cursor;
	}

	public ArrayList<FileInfo> handleTreeList(ArrayList<FileInfo> _orginList,
			Comparator comparator) {
		ArrayList<FileInfo> orginList = new ArrayList<FileInfo>();
		orginList.addAll(_orginList);
		ArrayList<FileInfo> returnList = new ArrayList<FileInfo>();
		for (FileInfo fi : orginList) {
			String date = Util.formatDateString(fi.modifiedDate);
			if (isHas(date, returnList)) {
				ArrayList<FileInfo> child = getChilelist(date, returnList);
				if (!isHasFilePath(fi.filePath, child)) {
					child.add(fi);
				}
			} else {
				FileInfo nFile = new FileInfo();
				nFile.fileName = date;
				nFile.fileId = fi.fileId;
				ArrayList<FileInfo> child = new ArrayList<FileInfo>();
				child.add(fi);
				nFile.childs = child;
				returnList.add(nFile);
			}
		}
		// Collections.sort(returnList, comparator);
		for (FileInfo f : returnList) {
			// Collections.sort(f.childs, comparator);
			f.count = f.childs.size();
		}
		return returnList;
	}

	private boolean isHas(String name, ArrayList<FileInfo> list) {
		boolean isHas = false;
		if (name == null || "".equals(name.trim()) || list == null
				|| list.isEmpty()) {
			return isHas;
		}
		for (FileInfo fi : list) {
			if (name.equals(fi.fileName)) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	private FileInfo isHasFileInfo(String name, ArrayList<FileInfo> list) {
		FileInfo fiRetrun = null;
		if (name == null || "".equals(name.trim()) || list == null
				|| list.isEmpty()) {
			return fiRetrun;
		}
		for (FileInfo fi : list) {
			if (name.equals(fi.fileName)) {
				fiRetrun = fi;
				break;
			}
		}
		return fiRetrun;
	}

	private boolean isHasFilePath(String filePath, ArrayList<FileInfo> list) {
		boolean isHas = false;
		if (filePath == null || "".equals(filePath.trim()) || list == null
				|| list.isEmpty()) {
			return isHas;
		}
		for (FileInfo fi : list) {
			if (filePath.equals(fi.filePath)) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	private ArrayList<FileInfo> getChilelist(String name,
			ArrayList<FileInfo> list) {
		for (FileInfo fi : list) {
			if (name.equals(fi.fileName)) {
				return fi.childs;
			}
		}
		return null;
	}

	public ArrayList<FileInfo> handleMuiscTreeList(
			ArrayList<FileInfo> _orginList, String type, Context ctx) {
		ArrayList<FileInfo> orginList = new ArrayList<FileInfo>();
		orginList.addAll(_orginList);
		ArrayList<FileInfo> returnList = new ArrayList<FileInfo>();
		for (FileInfo fi : orginList) {
			String date = fi.artist;
			if ("album".equals(type)) {
				date = fi.albumName;
			} else if ("genre".equals(type)) {
				date = fi.genreName;
			}
			if (isHas(date, returnList)) {
				ArrayList<FileInfo> child = getChilelist(date, returnList);
				if (!isHasFilePath(fi.filePath, child)) {
					child.add(fi);
				}
			} else {
				FileInfo nFile = new FileInfo();
				nFile.fileName = date;
				ArrayList<FileInfo> child = new ArrayList<FileInfo>();
				child.add(fi);
				nFile.childs = child;
				returnList.add(nFile);
			}
		}
		for (FileInfo f : returnList) {
			f.count = f.childs.size();
		}
		return returnList;
	}

	private static boolean isHasByPath(String path,
			final ArrayList<FileInfo> list) {
		path = path.trim();
		boolean isHas = false;
		if (path == null || "".equals(path) || list == null || list.isEmpty()) {
			return isHas;
		}
		for (FileInfo fi : list) {
			if (path.equals(fi.filePath.trim())) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	private String getSdCardsPath(String orginPath) {
		String selectPath = orginPath;
		if (selectPath.indexOf("/sdcard") == 0) {
			selectPath = "/mnt" + selectPath;
		}
		return selectPath;
	}

	private class GetFileTask extends
			AsyncTask<Void, FileInfo, ArrayList<FileInfo>> {
		private final DeviceInfo di;
		private ArrayList<FileInfo> dataListTask = new ArrayList<FileInfo>();
		private Context ctx;
		private int viewTypeTask;
		private int tabIdTask;
		private boolean isCancel = false;
		private String artisGet = null;

		public GetFileTask(final Context _ctx, final DeviceInfo _di,
				final int _viewType, final int _tabId, String _artisGet) {
			di = _di;
			// dataListTask = _dataList;
			// _dataList.clear();
			ctx = _ctx;
			viewTypeTask = _viewType;
			tabIdTask = _tabId;
			artisGet = _artisGet;
		}

		public boolean isStop() {
			return isCancel;
		}

		@Override
		public void onPreExecute() {
			mHandler.sendEmptyMessage(MESSAGE_SCAN_BEGIN);
			super.onPreExecute();
		}

		@Override
		public ArrayList<FileInfo> doInBackground(Void... params) {
			if (isCancelled() || isCancel) {
				return new ArrayList<FileInfo>();
			}
			int fileTypt = FileInfo.TYPE_PIC;
			String[] exts = FileCategoryHelper.PICTURE_EXTS;
			String selection = " _data like ?";
			String[] selectionArgs = { getSdCardsPath(di.devPath) + "%" };
			Uri _uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String nameIndex = MediaColumns.DISPLAY_NAME;
			String dateIndex = ImageColumns.DATE_TAKEN;
			String sizeIndex = MediaColumns.SIZE;
			String duration = null;
			String artistNameIndex = null;
			String albumNameIndex = null;
			String order = ImageColumns.DATE_TAKEN + " desc";

			if (tabIdTask == MediacenterConstant.IntentFlags.MUSIC_ID) {
				fileTypt = FileInfo.TYPE_MUSIC;
				_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				nameIndex = MediaColumns.DISPLAY_NAME;
				dateIndex = MediaColumns.DATE_ADDED;
				duration = AudioColumns.DURATION;
				sizeIndex = null;
				artistNameIndex = AudioColumns.ARTIST;
				// artistIdIndex = AudioColumns.ARTIST_ID;
				albumNameIndex = AudioColumns.ALBUM;
				// albumIdIndex = AudioColumns.ALBUM_ID;
				exts = FileCategoryHelper.AUDIO_EXTS;
				order = nameIndex + " asc";
			} else if (tabIdTask == MediacenterConstant.IntentFlags.VIDEO_ID) {
				fileTypt = FileInfo.TYPE_VIDEO;
				_uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				nameIndex = MediaColumns.TITLE;
				dateIndex = VideoColumns.DATE_TAKEN;
				sizeIndex = MediaColumns.SIZE;
				duration = VideoColumns.DURATION;
				exts = FileCategoryHelper.VIDEO_EXTS;
				order = nameIndex + " asc";
			}
			Cursor cursor = query(ctx, _uri, null, selection, selectionArgs,
					order);
			// MediaStore.Images.Media.
			if (cursor == null) {
				Toast.makeText(ctx, "nosdk", Toast.LENGTH_LONG).show();
				return null;
			}

			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					FileInfo file = new FileInfo();
					file.dbId = cursor.getInt(cursor.getColumnIndex("_id"));
					file.fileName = cursor.getString(cursor
							.getColumnIndex(nameIndex));
					file.filePath = cursor.getString(cursor
							.getColumnIndex("_data"));
					file.modifiedDate = cursor.getLong(cursor
							.getColumnIndex(dateIndex));

					if (sizeIndex != null) {
						file.fileSize = cursor.getLong(cursor
								.getColumnIndex(sizeIndex));
					}

					if (duration != null) {
						file.duration = cursor.getInt(cursor
								.getColumnIndex(VideoColumns.DURATION));
					}
					if (artistNameIndex != null) {
						file.artist = cursor.getString(cursor
								.getColumnIndex(artistNameIndex));
					}
					if (albumNameIndex != null) {
						file.albumName = cursor.getString(cursor
								.getColumnIndex(albumNameIndex));

						int index = cursor.getColumnIndex(AudioColumns.GENRE);
						if (index > 0) {
							file.genreName = cursor.getString(index);

						} else {
							file.genreName = cxt
									.getString(R.string.cm_genre_unknow);
						}

					}
					file.fileType = fileTypt;
					File lFile = new File(file.filePath);
					if (!isHasByPath(file.filePath, dataListTask)
							&& FileCategoryHelper.matchExts(
									Util.getExtFromFilename(file.filePath),
									exts) && lFile.exists()
							&& !Util.isHidden(file.filePath)) {
						file.modifiedDate = lFile.lastModified();
						dataListTask.add(file);
						if (artisGet == null) {
							this.publishProgress(file);
						}

					}
					if (!isCancelled() && !isCancel) {
						cursor.moveToNext();
					} else {
						cursor.close();
						return dataListTask;
					}
				}
			}
			cursor.close();
			if (artisGet != null) {
				ArrayList<FileInfo> dataTemp = new ArrayList<FileInfo>();
				dataTemp.addAll(dataListTask);
				dataList.clear();
				dataListTask = handleMuiscTreeList(dataTemp, artisGet, cxt);
			}

			return dataListTask;
		}

		@Override
		public void onPostExecute(ArrayList<FileInfo> fileInfo) {
			Message message = mHandler.obtainMessage(MESSAGE_SCAN_END, this);
			message.obj = fileInfo;
			message.arg1 = tabIdTask;
			message.arg2 = viewTypeTask;
			mHandler.removeMessages(MESSAGE_SCAN_END);
			mHandler.sendMessage(message);
			super.onPostExecute(fileInfo);

		}

		@Override
		protected void onProgressUpdate(FileInfo... values) {
			// TODO Auto-generated method stub
			Message message = mHandler.obtainMessage(MESSAGE_CHANGE_DATA, this);
			message.obj = values[0];
			message.arg1 = tabIdTask;
			message.arg2 = viewTypeTask;
			mHandler.removeMessages(MESSAGE_CHANGE_DATA);
			mHandler.sendMessage(message);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			isCancel = true;
			super.onCancelled();
		}

		public void cancel() {
			isCancel = true;
			mHandler.sendEmptyMessage(MESSAGE_SCAN_END_NO_DATA);
			onCancelled();
		}
	}

	String posPath = null;

	private class ScanFileTask extends
			AsyncTask<Void, FileInfo, ArrayList<FileInfo>> {
		private final DeviceInfo di;
		// private ArrayList<FileInfo> dataListTask = new ArrayList<FileInfo>();
		// private final Context ctx;
		private boolean isCancel = false;
		private FilenameFilter filter;
		private final int tabIdS;
		private final int viewTypeS;
		private int fileInfoType;
		private String goOnPath;
		private boolean sendMes = true;

		public ScanFileTask(final Context _ctx, final DeviceInfo _di,
				final int _viewType, final int _tabId,
				final FilenameFilter _filter) {
			di = _di;
			tabIdS = _tabId;
			// ctx = _ctx;
			viewTypeS = _viewType;
			filter = _filter;
			fileInfoType = getFileType(tabIdS);
			goOnPath = null;
		}

		public ScanFileTask(final Context _ctx, final DeviceInfo _di,
				final int _viewType, final int _tabId,
				final FilenameFilter _filter, String _posPath) {
			di = _di;
			tabIdS = _tabId;
			// ctx = _ctx;
			viewTypeS = _viewType;
			filter = _filter;
			fileInfoType = getFileType(tabIdS);
			goOnPath = _posPath;
		}

		public boolean isStop() {
			return isCancel;
		}

		@Override
		public void onPreExecute() {
			mHandler.sendEmptyMessage(MESSAGE_SCAN_BEGIN);
			super.onPreExecute();
		}

		@Override
		public ArrayList<FileInfo> doInBackground(Void... params) {
			if (isCancelled() || isCancel) {
				return new ArrayList<FileInfo>();
			}
			File file;
			if (goOnPath != null) {
				file = new File(goOnPath);
			} else {
				file = new File(di.devPath);
			}

			if (file.exists() && file.isDirectory()) {
				scanFile(file, filter, null);
			}
			return null;
		}

		@Override
		public void onPostExecute(ArrayList<FileInfo> fileInfo) {
			// mHandler.removeMessages(MESSAGE_CHANGE_DATA);
			if (sendMes) {
				Message message = mHandler
						.obtainMessage(MESSAGE_SCAN_END, this);
				message.obj = null;
				message.arg1 = tabIdS;
				message.arg2 = viewTypeS;
				mHandler.removeMessages(MESSAGE_SCAN_END);
				mHandler.sendMessage(message);
			}
			isCancel = true;
			super.onPostExecute(fileInfo);
		}

		@Override
		protected void onProgressUpdate(FileInfo... values) {
			// TODO Auto-generated method stub
			if (sendMes) {
				Message message = mHandler.obtainMessage(MESSAGE_CHANGE_DATA,
						this);
				message.obj = values[0];
				message.arg1 = tabIdS;
				message.arg2 = viewTypeS;
				mHandler.sendMessage(message);
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			isCancel = true;
			super.onCancelled();

		}

		public void cancel() {
			isCancel = true;
			Message message = mHandler.obtainMessage(MESSAGE_SCAN_END_NO_DATA,
					this);
			// mHandler.removeMessages(MESSAGE_SCAN_END_NO_DATA);
			mHandler.sendMessage(message);
			onCancelled();
		}

		public void reMoveMessage() {
			sendMes = false;
		}

		FileInfo lFileInfo = null;
		File[] listFiles = null;

		private void scanFile(File file, FilenameFilter filter,
				ArrayList<FileInfo> list) {
			if (isCancel) {
				if (file.getParentFile() != null) {
					posPath = file.getParentFile().getPath();
				} else {
					posPath = file.getPath();
				}
				return;
			}
			listFiles = file.listFiles(filter);
			if (listFiles != null) {
				for (File child : listFiles) {
					// do not show selected file if in move state
					String absolutePath = child.getAbsolutePath();
					if (Util.shouldShowFile(absolutePath)) {
						if (child.isDirectory()) {
							scanFile(child, filter, list);
						} else {
							lFileInfo = new FileInfo();
							String filePath = child.getPath();
							File lFile = new File(filePath);
							if (!lFile.exists()) {
								return;
							}
							// lFileInfo.canRead = lFile.canRead();
							// lFileInfo.canWrite = lFile.canWrite();
							lFileInfo.isHidden = lFile.isHidden();
							lFileInfo.fileName = child.getName();
							lFileInfo.modifiedDate = lFile.lastModified();
							lFileInfo.isDir = lFile.isDirectory();
							if (!lFileInfo.isDir) {
								lFileInfo.fileType = fileInfoType;
							}
							lFileInfo.fileSize = lFile.length();
							lFileInfo.filePath = filePath;
							if (!isCancel) {
								posPath = null;
								if (tabIdS == MediacenterConstant.IntentFlags.MUSIC_ID) {
									if (getMusicInfo(lFileInfo) == null) {
										return;
									}
									if (TextUtils.isEmpty(lFileInfo.albumName)) {
										lFileInfo.albumName = getString(R.string.cm_unknow);
									}
									if (TextUtils.isEmpty(lFileInfo.artist)) {
										lFileInfo.artist = getString(R.string.cm_unknow);
									}
									if (TextUtils.isEmpty(lFileInfo.genreName)) {
										lFileInfo.genreName = getString(R.string.cm_unknow);
									}
								}
								if (lFileInfo.fileSize > 10240) {
									this.publishProgress(lFileInfo);
								}

							} else {
								if (lFile.getParentFile() != null) {
									posPath = lFile.getParentFile().getPath();
								} else {
									posPath = filePath;
								}
								// lFile.getParentFile().getAbsolutePath();
								return;
							}

						}

					}
				}
			}
		}
	}

	private FileInfo getMusicInfo(FileInfo _fi) {
		if (_fi.filePath == null) {
			return null;
		}
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(_fi.filePath);
		} catch (Exception e) {
			mmr.release();
			return null;
		}
		String str = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		if (!TextUtils.isEmpty(str)) {
			_fi.duration = Integer.valueOf(str);
		}
		_fi.albumName = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
		_fi.artist = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String gener = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
		if (!TextUtils.isEmpty(gener)) {
			gener = gener.replace("(", "").replace(")", "").replace("[", "")
					.replace("]", "");
			if (Util.isNumeric(gener)) {
				int pos = Integer.parseInt(gener);
				if (pos < ID3_GENRES.length) {
					_fi.genreName = ID3_GENRES[Integer.parseInt(gener)];
				}
			} else {
				_fi.genreName = gener;
			}
		}
		mmr.release();
		return _fi;
	}

	private RandomAccessFile ran = null;

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		if (getFileTask != null) {
			getFileTask.cancel();

		}
		if (scanFileTask != null) {
			scanFileTask.cancel();
		}
		super.onLowMemory();

	}

	private static final String[] ID3_GENRES = {
			// ID3v1 Genres
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
			"Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
			"Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
			"Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
			"Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk",
			"Fusion", "Trance", "Classical", "Instrumental", "Acid", "House",
			"Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass",
			"Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
			"Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
			"Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
			"Christian Rap", "Pop/Funk", "Jungle",
			"Native American",
			"Cabaret",
			"New Wave",
			"Psychadelic",
			"Rave",
			"Showtunes",
			"Trailer",
			"Lo-Fi",
			"Tribal",
			"Acid Punk",
			"Acid Jazz",
			"Polka",
			"Retro",
			"Musical",
			"Rock & Roll",
			"Hard Rock",
			// The following genres are Winamp extensions
			"Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
			"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
			"Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
			"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
			"Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
			"Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella",
			"Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House",
			"Hardcore", "Terror", "Indie", "Britpop", "Negerpunk",
			"Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
			"Black Metal", "Crossover", "Contemporary Christian",
			"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime",
			"JPop", "Synthpop",
	// 148 and up don't seem to have been defined yet.
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG,
				"0000onActivityResult00000000000000000000000000requestCode:"
						+ requestCode + ",resultCode:" + resultCode);
		if (requestCode == MediacenterConstant.ACTIVITYR_RESULT_CODE) {
			switch (resultCode) {
			case RESULT_OK:
				if (data != null) {
					Bundle b = data.getExtras();
					if (b != null) {
						String path = b
								.getString(MediacenterConstant.INTENT_EXTRA);
						if (!TextUtils.isEmpty(path)) {
							Message message = mHandler.obtainMessage(
									MESSAGE_SET_POS, this);
							message.obj = path;
							mHandler.removeMessages(MESSAGE_SET_POS);
							mHandler.sendMessage(message);
						}
					}

				}

				break;
			default:
				break;
			}
		}

	}

	private int getFileType(int tabId) {
		int fileType = FileInfo.TYPE_MUSIC;
		if (tabId == MediacenterConstant.IntentFlags.PIC_ID) {
			fileType = FileInfo.TYPE_PIC;
		} else if (tabId == MediacenterConstant.IntentFlags.VIDEO_ID) {
			fileType = FileInfo.TYPE_VIDEO;
		}
		return fileType;

	}
	public void resetCurrentNum() {
		currentNum.setText("0/0");
	}
	public void setCurrentNum(String num) {
		currentNum.setText(num);
	}
	public void setCurrentPath(String path) {
		currentPath.setText(path);
	}
}
