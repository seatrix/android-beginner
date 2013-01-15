package com.mipt.mediacenter.center;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileIconHelper;
import com.mipt.mediacenter.center.file.IntentBuilder;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.Util;
import com.mipt.fileMgr.center.FileMainActivity;
/**
 * 
 * @author fang
 * 
 */
public class AllFileViewFragment extends Fragment implements
		FileMainActivity.DataChangeListener {
	private static final String LOG_TAG = "VideoMusicFragment";
	private Activity mActivity;
	private View mRootView;
	private FileItemAdapter mAdapter;
	private FileIconHelper mFileIconHelper;
	private GridView gridView;
	private TextView currentNum;
	private int type;
	private TextView fileName;
	private TextView fileType;
	private TextView fileDate;
	private TextView fileSize;

	public static AllFileViewFragment newInstance(ArrayList<FileInfo> _dateList,
			int type) {
		AllFileViewFragment f = new AllFileViewFragment();
		Bundle args = new Bundle();
		args.putInt(MediacenterConstant.INTENT_TYPE_VIEW, type);
		args.putSerializable(MediacenterConstant.INTENT_EXTRA, _dateList);
		f.setArguments(args);
		return f;
	}

	@Override
	public void dataChange() {

		Util.runOnUiThread(mActivity, new Runnable() {
			@Override
			public void run() {
				// mAdapter.appendDataList(false, newData);
				mAdapter.notifyDataSetChanged();
				int cnt = mAdapter.getCount();
				showEmptyView(cnt == 0);
				//
				if (cnt != 0) {
					int pos = gridView.getSelectedItemPosition();
					if (pos == -1) {
						pos += 1;
					}
					FileInfo fi = mAdapter.getItem(pos);
					if (fi != null) {
						setFileInfo(pos, cnt, fi);
					}
				} else {
					currentNum.setText("0/0");
				}

			}

		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = getActivity();
		mRootView = inflater.inflate(R.layout.cm_file_gridview_list, container,
				false);
		gridView = (GridView) mRootView.findViewById(R.id.file_content);
		currentNum = (TextView) mActivity.findViewById(R.id.current_num_tag);
		fileName = (TextView) mRootView.findViewById(R.id.cm_file_name);
		fileType = (TextView) mRootView.findViewById(R.id.cm_file_type);
		fileDate = (TextView) mRootView.findViewById(R.id.cm_file_date);
		fileSize = (TextView) mRootView.findViewById(R.id.cm_file_size);
		type = getArguments() != null ? getArguments().getInt(
				MediacenterConstant.INTENT_TYPE_VIEW)
				: MediacenterConstant.IntentFlags.PIC_ID;
		ArrayList<FileInfo> _dataList = (ArrayList<FileInfo>) (getArguments() != null ? getArguments()
				.getSerializable(MediacenterConstant.INTENT_EXTRA)
				: new ArrayList<FileInfo>());
		mFileIconHelper = new FileIconHelper(mActivity);
		mAdapter = new FileItemAdapter(mActivity, mFileIconHelper, _dataList,
				mHandler, MESSAGE_SETINFO);
		gridView.setAdapter(mAdapter);
		Util.setText(mRootView, R.id.current_path, getRootName(type));
		// showEmptyView(_dataList.size() == 0);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				IntentBuilder.viewFile(mActivity, mAdapter.getItem(position),
						null, false, null);
			}
		});
		gridView.setOnItemSelectedListener(fileChange);
		gridView.requestFocus();
		currentNum.setText("0/0");
		return mRootView;
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
					mActivity.finish();
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
			// TODO Auto-generated method stub

		}

	};

	void setFileInfo(int pos, int size, FileInfo fi) {
		if (size == 0) {
			currentNum.setText("0/0");
		} else {
			currentNum.setText((pos + 1) + "/" + size);
		}
		if (fi != null) {
			fileName.setText(fi.fileName);
			fileType.setText(Util.getTypeUpperCase(fi.filePath));
			fileDate.setText(Util.formatDateString(fi.modifiedDate) + "");
			if (fi.fileSize != 0) {
				fileSize.setText(Util.convertStorage(fi.fileSize) + "");
			}
		} else {
			fileName.setText("");
			fileType.setText("");
			fileDate.setText("");
			fileSize.setText("");
		}

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

	static final int MESSAGE_SETINFO = 112;
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
}
