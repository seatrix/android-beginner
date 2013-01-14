package com.mipt.fileMgr.center;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.db.DatabaseIfc;
import com.mipt.fileMgr.center.db.DatabaseImpl;
import com.mipt.fileMgr.center.file.FileIconHelper;
import com.mipt.fileMgr.center.server.FileInfo;
import com.mipt.fileMgr.center.server.MediacenterConstant;
import com.mipt.fileMgr.utils.Util;

/**
 * 
 * @author fang
 * 
 */
public class FavFragment extends Fragment {
	private static final String LOG_TAG = "FavFragment";
	private Activity mActivity;
	private GridView gridViewPlay;
	private GridView gridViewAdd;
	private View rootView;
	private FileIconHelper mFileIconHelper;
	private favAdapter adapterPlay;
	private favAdapter adapterAdd;
	private ArrayList<FileInfo> dataList;
	private DatabaseIfc di;

	static FavFragment newInstance() {
		FavFragment f = new FavFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = this.getActivity();
		Log.i(LOG_TAG, "in FavFragment.oncreateView...");
		
		rootView = inflater.inflate(R.layout.fav_list, container, false);
		Util.setText(rootView, R.id.current_play_tag,
				getString(R.string.tab_favorite));
		Util.setText(rootView, R.id.current_date_tag,
				Util.getLastTime(mActivity, MediacenterConstant.LAST_PLYA_TIEM));
		gridViewPlay = (GridView) rootView.findViewById(R.id.current_content);
		gridViewAdd = (GridView) rootView.findViewById(R.id.recentadd_content);
		mFileIconHelper = new FileIconHelper(mActivity);
		adapterPlay = new favAdapter(mActivity, mFileIconHelper);
		adapterAdd = new favAdapter(mActivity, mFileIconHelper);
		di = new DatabaseImpl(mActivity);
		dataList = di.getFiles(DatabaseIfc.RECENT_PLAY, Util.getLastTimeLong(
				mActivity, MediacenterConstant.LAST_PLYA_TIEM), 6);
		Log.i(LOG_TAG, "--size:" + dataList.size());
		// ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		// list.addAll(dataList);
		// List<FileInfo> temp = list;
		// if (!list.isEmpty() && list.size() > 5) {
		// gridViewPlay.setSelection(0);
		// temp = list.subList(0, 5);
		// }

		ArrayList<FileInfo> listtemp = new ArrayList<FileInfo>();
		listtemp.addAll(dataList);
		FileInfo tempFile = new FileInfo();
		tempFile.fileName = "更多";
		tempFile.extra = true;
		listtemp.add(tempFile);

		// List<FileInfo> tempAdd = list.subList(6, 10);
		ArrayList<FileInfo> listtempAdd = new ArrayList<FileInfo>();
		// listtempAdd.addAll(tempAdd);

		listtempAdd.add(tempFile);
		adapterPlay.addList(listtemp);
		adapterAdd.addList(listtempAdd);
		gridViewPlay.setAdapter(adapterPlay);
		gridViewAdd.setAdapter(adapterAdd);
		gridViewPlay.setOnItemClickListener(checkClick);
		gridViewAdd.setOnItemClickListener(checkClick);
		adapterPlay.notifyDataSetChanged();
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	public static class favAdapter extends BaseAdapter {
		public static final String TAG = "favAdapter";
		private ArrayList<FileInfo> mList = new ArrayList<FileInfo>();
		private final Context mContext;
		private FileIconHelper mHelper;

		public favAdapter(Activity activity, FileIconHelper _mFileIconHelper) {
			mContext = activity;
			inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mHelper = _mFileIconHelper;
		}

		LayoutInflater inflater;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.fav_item, null);
				holder = new ViewHolder();
				holder.fileImage = (ImageView) convertView
						.findViewById(R.id.fav_image);
				holder.fileImageFrame = (ImageView) convertView
						.findViewById(R.id.fav_image_frame);
				holder.name = (TextView) convertView
						.findViewById(R.id.fav_name);
				holder.videoTag = (ImageView) convertView
						.findViewById(R.id.file_video_tag);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			FileInfo file = mList.get(position);
			if (file != null) {
				holder.name.setText(file.fileName);
				if (file.extra) {
					// holder.fileImage.setImageResource(R.drawable.empty_icon);
				} else {
					// mHelper.setIcon(file, holder.fileImage,
					// holder.fileImageFrame, holder.videoTag, null);
				}

			}

			return convertView;
		}

		public static class ViewHolder {
			ImageView fileImageFrame;
			ImageView fileImage;
			ImageView videoTag;
			TextView name;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mList != null) {
				return mList.size();
			} else
				return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList == null ? null : mList.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		public void addList(ArrayList<FileInfo> mList) {
			if (this.mList != null && mList != null && !mList.isEmpty()) {
				this.mList.addAll(mList);
			}
		}
	}

	private OnItemClickListener checkClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			FileInfo file = (FileInfo) arg0.getAdapter().getItem(arg2);
			if (file.extra) {
				Intent intent = new Intent(mActivity, FavListActivity.class);
				startActivity(intent);
			} else {

				Log.i(LOG_TAG, "--item click--");
			}
		}

	};
}
