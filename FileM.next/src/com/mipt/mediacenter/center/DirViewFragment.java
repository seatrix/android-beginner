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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.FileMainActivity;
import com.mipt.mediacenter.center.file.FileIconHelper;
import com.mipt.mediacenter.center.file.FileOperatorEvent;
import com.mipt.mediacenter.center.file.FileOperatorEvent.Model;
import com.mipt.mediacenter.center.file.FileViewInteractionHub;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.IFileInteractionListener;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.Util;
/**
 * @author fang
 * @version $Id: 2013-01-21 09:26:01Z slieer $ 
 *
 */
public class DirViewFragment extends Fragment implements
		IFileInteractionListener, FileMainActivity.IBackPressedListener,
		FileMainActivity.DataChangeListener {
	private FileMainActivity mActivity;
	private View mRootView;
	private FileItemAdapter mAdapter;
	private ArrayList<FileInfo> mFileNameList;
	private FileViewInteractionHub mFileViewInteractionHub;
	private FileIconHelper mFileIconHelper;
	private GridView gridView;
	private String orginPath;
	private TextView fileType;
	private TextView fileName;
	private TextView fileDate;
	private TextView fileSize;
	private int fileInfoType;
	private String currentFilePath;
	public static final String TAG = "DirViewFragment";
    public static final int MESSAGE_SETINFO = 111;

	public static DirViewFragment newInstance(String path, int type) {
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
		/**add OnItemClickListener*/
		gridView = (GridView) mRootView.findViewById(R.id.file_content);
		mFileIconHelper = new FileIconHelper(mActivity);
		
		Log.i(TAG, "mFileNameList:" +  mFileNameList);
		mAdapter = new FileItemAdapter(mActivity, mFileIconHelper,
				mFileNameList, mHandler, MESSAGE_SETINFO);
		gridView.setAdapter(mAdapter);
		
		mFileViewInteractionHub = new FileViewInteractionHub(this);
		mFileViewInteractionHub.setRootPath(orginPath);
		mFileViewInteractionHub.refreshFileList();
		gridView.setOnItemSelectedListener(fileChange);
		
		Model model = FileOperatorEvent.getModel(this.getActivity());
        if(model.equals(Model.SELECT_MODEL)){
            gridView.setOnItemClickListener(new FileOperatorEvent.SelectFile(mActivity));
        }else if(model.equals(Model.DEFAULT_BROSWER_MODEL)){
            //grid.setOnItemClickListener(listener);
            //FileViewInteractionHub hub = getFileViewInteractionHub();
            //hub.setupFileListView();
            //hub.refreshFileList();
        }

		
		gridView.requestFocus();
		
        //ListView rightMenu = (ListView)mRootView.findViewById(R.id.function_menu);
        //String[] menuItem = getResources().getStringArray(R.array.file_menu_items);
        //Log.i(TAG, Arrays.asList(menuItem).toString() + ",rightMenu:" + rightMenu);
        //rightMenu.setAdapter(new RightMenuList(mActivity, R.id.file_op_item, menuItem));
        //rightMenu.setOnItemClickListener(new RightMenu(menuItem));
		return mRootView;
	}

	@Override
	public boolean onBack() {
	    return mFileViewInteractionHub.onBackPressed();
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return mRootView.findViewById(id);
	}

	@Override
	public Context getContext() {
		return mActivity;
	}

	@Override
	public void onDataChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}

		});
	}

	@Override
	public String getDisplayPath(String path) {
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
		mActivity.runOnUiThread(r);
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		return mFileIconHelper;
	}

	@Override
	public FileInfo getItem(int pos) {
	    Log.i(TAG, "pos:" + pos);
		if (pos < 0 || pos > mFileNameList.size() - 1)
			return null;

		return mFileNameList.get(pos);
	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		Collections.sort(mFileNameList, sort.getComparator());
		onDataChanged();
	}

	@Override
	public Collection<FileInfo> getAllFiles() {
		return mFileNameList;
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {		
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}
		mActivity.setCurrentPath(Util.handlePath(path));
		final int pos = computeScrollPosition(path);
		mFileNameList.clear();
		File[] listFiles = file.listFiles();

		for (File child : listFiles) {
			// do not show selected file if in move state
			String absolutePath = child.getAbsolutePath();
			if (Util.shouldShowFile(absolutePath)) {
				FileInfo lFileInfo = Util.getFileInfo(child,
						null, false);
				if (lFileInfo != null) {
					if (!lFileInfo.isDir) {
						lFileInfo.fileType = fileInfoType;
					}else {
					    lFileInfo.count = (child != null && child.list() != null) ? (int)child.list().length : 0;                        
                    }
					mFileNameList.add(lFileInfo);
				}
			}
		}
		Log.i(TAG, "mFileNameList.size:" + mFileNameList.size() + ", sort" + sort);
		showEmptyView(mFileNameList.isEmpty());
		if (!mFileNameList.isEmpty() && pos == 0) {
		    Log.i(TAG, "pos:" + pos);
			setFileInfo(0, mFileNameList.size(), mFileNameList.get(pos));
		}
		gridView.setSelection(pos);
		sortCurrentList(sort);
		return true;
	}

	@Override
	public int getItemCount() {
		return mFileNameList.size();
	}

    @Override
    public void dataChange() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBackPos(final int pos, final String path) {
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

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mFileIconHelper.stopLoad();
    }	
    
    public FileViewInteractionHub getFileViewInteractionHub(){
        return mFileViewInteractionHub;
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
				    getActivity().onBackPressed();
				}
			});
		}
		setFileInfo(0, 0, null);
	}

	private final OnItemSelectedListener fileChange = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
		    Log.i(TAG, "OnItemSelectedListener...");
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
	
	private void setFileInfo(int pos, int size, FileInfo fi) {
		if (size == 0) {
			mActivity.resetCurrentNum();
		} else {
			mActivity.setCurrentNum((pos + 1) + "/" + size);
		}
		if (fi != null) {
			currentFilePath = fi.filePath;
			fileName.setText(fi.fileName);
			if (!fi.isDir) {
				fileType.setVisibility(View.VISIBLE);
				fileType.setText(Util.getTypeUpperCase(fi.filePath));
			} else {
				fileType.setVisibility(View.GONE);
				fileSize.setVisibility(View.GONE);
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

    public static class mHandler extends Handler{
        private DirViewFragment fragment;
        private  ListAdapter mAdapter;
        public mHandler(DirViewFragment fragment, ListAdapter mAdapter){
            this.fragment = fragment;
            this.mAdapter = mAdapter;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SETINFO:
                final int pos = msg.arg1;
                if (mAdapter != null) {
                    fragment.setFileInfo(pos, mAdapter.getCount(), (FileInfo)mAdapter.getItem(pos));
                }
                break;
            }
        }
    }	
}
