package com.mipt.mediacenter.center.file;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.IFileInteractionListener;
import com.mipt.mediacenter.utils.ToastFactory;

/**
 * 
 * @author fang
 * 
 */
public class FileViewInteractionHub {
	private static final String LOG_TAG = "FileViewInteractionHub";
	private IFileInteractionListener mFileViewListener;
	private FileSortHelper mFileSortHelper;
	private ProgressDialog progressDialog;
	private Context mContext;
	private Activity mActivity;
	private int backPost = 0;

	public int getBackPost() {
		return backPost;
	}

	public String getRootPath() {
		return mRoot;
	}

	private GridView mFileListView;

	private void setupFileListView() {
		mFileListView = (GridView) mFileViewListener
				.getViewById(R.id.file_content);
		mFileListView.setLongClickable(true);
		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
	}

	public void onListItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileInfo lFileInfo = mFileViewListener.getItem(position);
		if (lFileInfo == null) {
			Log.e(LOG_TAG, "file does not exist on position:" + position);
			return;
		}
		if (!lFileInfo.isDir) {
			IntentBuilder.viewFile(mActivity, lFileInfo, null, false, null);
			return;
		}
		mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
		refreshFileList();
		backPost = position;
	}

	private String getAbsoluteName(String path, String name) {
		return path.equals("/") ? path + name : path + File.separator + name;
	}

	public FileViewInteractionHub(IFileInteractionListener fileViewListener) {
		assert (fileViewListener != null);
		mFileViewListener = fileViewListener;
		mFileSortHelper = FileSortHelper.getInstance();
		mContext = mFileViewListener.getContext();
		mActivity = mFileViewListener.getmActivity();
		setupFileListView();
	}

	public void setRootPath(String path) {
		mRoot = path;
		mCurrentPath = path;
	}

	public void setCurrentPath(String path) {
		mCurrentPath = path;
	}

	public void sortCurrentList() {
		mFileViewListener.sortCurrentList(mFileSortHelper);
	}

	public FileInfo getItem(int pos) {
		return mFileViewListener.getItem(pos);
	}

	private String mCurrentPath;

	private String mRoot;

	public void refreshFileList() {
		// onRefreshFileList returns true indicates list has changed
		if (!mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper)) {
			ToastFactory
					.getInstance()
					.getToast(mContext,
							mContext.getString(R.string.current_sd_remove))
					.show();
			mActivity.finish();
		}
	}

	public boolean onBackPressed() {
		if (!onOperationUpLevel()) {
			return false;
		}
		return true;
	}

	public boolean onOperationUpLevel() {
		if (!mCurrentPath.equals(mRoot)) {
			mCurrentPath = new File(mCurrentPath).getParent();
			refreshFileList();
			return true;
		}

		return false;
	}
}
