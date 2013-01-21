package com.mipt.mediacenter.center;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileCategoryHelper;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.file.FileIconHelper;
import com.mipt.mediacenter.center.file.FileViewInteractionHub;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.IFileInteractionListener;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.Util;
import com.mipt.fileMgr.center.FileMainActivity;
/**
 * 
 * @author fang
 * 
 */
public class DirViewFragment extends Fragment implements
		IFileInteractionListener, FileMainActivity.IBackPressedListener,
		FileMainActivity.DataChangeListener {
	private static final String LOG_TAG = "DirViewFragment";
	private FileMainActivity mActivity;
	private View mRootView;
	private FileItemAdapter mAdapter;
	private ArrayList<FileInfo> mFileNameList;
	private FileViewInteractionHub mFileViewInteractionHub;
	private FileCategoryHelper mFileCagetoryHelper;
	private FileIconHelper mFileIconHelper;
	private GridView gridView;
	private String orginPath;
	private int type;
	private TextView fileType;
	private TextView fileName;
	private TextView fileDate;
	private TextView fileSize;
	private int fileInfoType;
	private String currentFilePath;

	static DirViewFragment newInstance(String path, int type) {
		DirViewFragment f = new DirViewFragment();
		Bundle args = new Bundle();
		args.putInt(MediacenterConstant.INTENT_TYPE_VIEW, type);
		args.putString(MediacenterConstant.INTENT_EXTRA, path);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (FileMainActivity) getActivity();
		type = getArguments() != null ? getArguments().getInt(
				MediacenterConstant.INTENT_TYPE_VIEW)
				: MediacenterConstant.IntentFlags.PIC_ID;
		orginPath = getArguments() != null ? getArguments().getString(
				MediacenterConstant.INTENT_EXTRA) : Util.getSdDirectory();
		// getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
		mRootView = inflater.inflate(R.layout.cm_file_gridview_list, container,
				false);
		mFileNameList = MediaCenterApplication.getInstance().getData();
		MediaCenterApplication.getInstance().clearData();
		fileName = (TextView) mRootView.findViewById(R.id.cm_file_name);
		fileType = (TextView) mRootView.findViewById(R.id.cm_file_type);
		fileDate = (TextView) mRootView.findViewById(R.id.cm_file_date);
		fileSize = (TextView) mRootView.findViewById(R.id.cm_file_size);
		mFileCagetoryHelper = new FileCategoryHelper(mActivity);
		mFileViewInteractionHub = new FileViewInteractionHub(this);
		mFileCagetoryHelper.setCurCategory(getFc(type));
		gridView = (GridView) mRootView.findViewById(R.id.file_content);
		mFileIconHelper = new FileIconHelper(mActivity);
		mAdapter = new FileItemAdapter(mActivity, mFileIconHelper,
				mFileNameList, mHandler, MESSAGE_SETINFO);
		gridView.setAdapter(mAdapter);
		mFileViewInteractionHub.setRootPath(orginPath);
		mFileViewInteractionHub.refreshFileList();
		gridView.setOnItemSelectedListener(fileChange);
		gridView.requestFocus();
		return mRootView;
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return mFileViewInteractionHub.onBackPressed();
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return mRootView.findViewById(id);
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return mActivity;
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}

		});
	}

	@Override
	public String getDisplayPath(String path) {
		// TODO Auto-generated method stub
		String root = mFileViewInteractionHub.getRootPath();

		if (root.equals(path))
			return "";

		if (!root.equals("/")) {
			int pos = path.indexOf(root);
			if (pos == 0) {
				path = path.substring(root.length());
			}
		}

		return "" + path;
	}

	@Override
	public String getRealPath(String displayPath) {
		// TODO Auto-generated method stub
		String root = mFileViewInteractionHub.getRootPath();
		String ret = displayPath.substring(displayPath.indexOf("/"));
		if (!root.equals("/")) {
			ret = root + ret;
		}
		return ret;
	}

	@Override
	public void runOnUiThread(Runnable r) {
		// TODO Auto-generated method stub
		mActivity.runOnUiThread(r);
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		// TODO Auto-generated method stub
		return mFileIconHelper;
	}

	@Override
	public FileInfo getItem(int pos) {
		// TODO Auto-generated method stub
		if (pos < 0 || pos > mFileNameList.size() - 1)
			return null;

		return mFileNameList.get(pos);
	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub
		Collections.sort(mFileNameList, sort.getComparator());
		onDataChanged();
	}

	@Override
	public Collection<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return mFileNameList;
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {		
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}
		mActivity.setCurrentPath(getRootName(type) + Util.handlePath(path));
		final int pos = computeScrollPosition(path);
		ArrayList<FileInfo> fileList = mFileNameList;
		fileList.clear();
		File[] listFiles = file.listFiles(mFileCagetoryHelper.getFilter());
		if (listFiles == null) {
			return true;
		}

		for (File child : listFiles) {
			// do not show selected file if in move state
			String absolutePath = child.getAbsolutePath();
			if (Util.shouldShowFile(absolutePath)) {
				FileInfo lFileInfo = Util.GetFileInfo(child,
						mFileCagetoryHelper.getFilter(), false);
				if (lFileInfo != null) {
					if (!lFileInfo.isDir) {
						lFileInfo.fileType = fileInfoType;
					}
					fileList.add(lFileInfo);
				}
			}
		}
		showEmptyView(fileList.isEmpty());
		if (!fileList.isEmpty() && pos == 0) {
			setFileInfo(0, fileList.size(), fileList.get(pos));
		}
		gridView.setSelection(pos);
		sortCurrentList(sort);
		return true;
	}

	@Override
	public int getItemCount() {
		return mFileNameList.size();
	}

	private ArrayList<PathScrollPositionItem> mScrollPositionList = new ArrayList<PathScrollPositionItem>();
	private String mPreviousPath;

	private class PathScrollPositionItem {
		String path;
		int pos;

		PathScrollPositionItem(String s, int p) {
			path = s;
			pos = p;
		}
	}

	// execute before change, return the memorized scroll position
	private int computeScrollPosition(String path) {
		int pos = 0;
		if (mPreviousPath != null) {
			if (path.startsWith(mPreviousPath)) {
				int firstVisiblePosition = gridView.getSelectedItemPosition();
				if (mScrollPositionList.size() != 0
						&& mPreviousPath.equals(mScrollPositionList
								.get(mScrollPositionList.size() - 1).path)) {
					mScrollPositionList.get(mScrollPositionList.size() - 1).pos = firstVisiblePosition;
					// Log.i(TAG, "computeScrollPosition: update item: "
					// + mPreviousPath + " " + firstVisiblePosition
					// + " stack count:" + mScrollPositionList.size());
					pos = firstVisiblePosition;
				} else {
					mScrollPositionList.add(new PathScrollPositionItem(
							mPreviousPath, firstVisiblePosition));
					// Log.i(TAG, "computeScrollPosition: add item: "
					// + mPreviousPath + " " + firstVisiblePosition
					// + " stack count:" + mScrollPositionList.size());
				}
			} else {
				int i;
				for (i = 0; i < mScrollPositionList.size(); i++) {
					if (!path.startsWith(mScrollPositionList.get(i).path)) {
						break;
					}
				}
				// navigate to a totally new branch, not in current stack
				if (i > 0) {
					pos = mScrollPositionList.get(i - 1).pos;
				}

				for (int j = mScrollPositionList.size() - 1; j >= i - 1
						&& j >= 0; j--) {
					mScrollPositionList.remove(j);
				}
			}
		}

		mPreviousPath = path;
		return pos;
	}

	private FileCategory getFc(int type) {
		FileCategory reFc = FileCategory.All;
		fileInfoType = FileInfo.TYPE_MUSIC;
		if (type == MediacenterConstant.IntentFlags.PIC_ID) {
			reFc = FileCategory.Picture;
			fileInfoType = FileInfo.TYPE_PIC;
		} else if (type == MediacenterConstant.IntentFlags.MUSIC_ID) {
			fileInfoType = FileInfo.TYPE_MUSIC;
			reFc = FileCategory.Music;
		} else if (type == MediacenterConstant.IntentFlags.VIDEO_ID) {
			fileInfoType = FileInfo.TYPE_VIDEO;
			reFc = FileCategory.Video;
		}
		return reFc;

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
					onBack();
				}
			});
		}
		setFileInfo(0, 0, null);
	}

	private final OnItemSelectedListener fileChange = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if (mAdapter == null) {
				return;
			}
			FileInfo fi = mAdapter.getItem(arg2);
			if (fi != null) {
				setFileInfo(arg2, mAdapter.getCount(), fi);
			} else {
				setFileInfo(0, 0, null);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};

	void setFileInfo(int pos, int size, FileInfo fi) {
		if (size == 0) {
			mActivity.resetCurrentNum();
		} else {
			mActivity.setCurrentNum((pos + 1) + "/" + size);
		}
		if (fi != null) {
			currentFilePath = fi.filePath;
			if (fi.mediaName == null) {
			fileName.setText(fi.fileName);
				if (fi.fileType == FileInfo.TYPE_MUSIC) {
					getFileInfo(fi, new FileMainActivity.FileInfoCallback() {
						@Override
						public void fileInfoLoaded(String reallyName,
								String filePath) {
							if (!TextUtils.isEmpty(currentFilePath)
									&& filePath.equals(currentFilePath)) {
								fileName.setText(reallyName);
							}
						}
					});
				}
			} else {
				fileName.setText(fi.mediaName);
			}
			if (!fi.isDir) {
				fileType.setVisibility(View.VISIBLE);
				fileType.setText(Util.getTypeUpperCase(fi.filePath));
			} else {
				fileType.setVisibility(View.GONE);
			}
			fileDate.setText(Util.formatDateString(fi.modifiedDate) + "");
			if (fi.fileSize != 0) {
				fileSize.setText(Util.convertStorage(fi.fileSize) + "");
			}
		} else {
			currentFilePath = null;
			fileName.setText("");
			fileDate.setText("");
			fileType.setVisibility(View.GONE);
			fileSize.setText("");
		}

	}

	@Override
	public void dataChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackPos(final int pos, final String path) {
		// TODO Auto-generated method stub
		Util.runOnUiThread(mActivity, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				gridView.setSelection(pos);
			}
		});
	}

	@Override
	public Activity getmActivity() {
		// TODO Auto-generated method stub
		return mActivity;
	}

	static final int MESSAGE_SETINFO = 111;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SETINFO:
				final int pos = msg.arg1;
				if (mAdapter != null) {
					setFileInfo(pos, mAdapter.getCount(), mAdapter.getItem(pos));
				}
				break;

			}
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mFileIconHelper.stopLoad();
	}

	String getFileInfo(final FileInfo fi,
			final FileMainActivity.FileInfoCallback fileInfoCallback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				fileInfoCallback.fileInfoLoaded((String) message.obj,
						fi.filePath);
			}
		};
		new Thread() {
			@Override
			public void run() {
				if (fi.fileType == FileInfo.TYPE_MUSIC) {
					Util.getMusicInfo(fi);
				} else if (fi.fileType == FileInfo.TYPE_VIDEO) {
					Util.getVideoInfo(fi);
				}
				if (TextUtils.isEmpty(fi.mediaName)) {
					fi.mediaName = fi.fileName;
				}
				Message message = handler.obtainMessage(0, fi.mediaName);
				handler.sendMessage(message);
			}
		}.start();
		return fi.mediaName;
	}
}
