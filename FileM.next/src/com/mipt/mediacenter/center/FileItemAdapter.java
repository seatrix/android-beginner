package com.mipt.mediacenter.center;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileIconHelper;
import com.mipt.mediacenter.center.file.FileOperatorEvent;
import com.mipt.mediacenter.center.file.FileOperatorEvent.Model;
import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class FileItemAdapter extends BaseAdapter {
	public static final String TAG = "FileItemAdapter";
	//public static boolean SELECT_FLAG = false;
	
	private ArrayList<FileInfo> mList = new ArrayList<FileInfo>();
	private final Activity mContext;
	private FileIconHelper mHelper;
	private Handler mHandler;
	private int messId;

	public FileItemAdapter(Activity activity,
            ArrayList<FileInfo> fileList){
		mContext = activity;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHelper = new FileIconHelper(activity);
		messId = DirViewFragment.MESSAGE_SETINFO;
		
		DirViewFragment f1 = (DirViewFragment) activity.getFragmentManager()
                .findFragmentByTag(DirViewFragment.TAG);
		mHandler = new DirViewFragment.mHandler(f1, this);
		mList = fileList;
	}

	public FileItemAdapter(Activity activity, FileIconHelper _mFileIconHelper,
			ArrayList<FileInfo> fileList, Handler _handler, int _messId) {
		mContext = activity;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHelper = _mFileIconHelper;
		mList = fileList;
		mHandler = _handler;
		messId = _messId;
	}

	public ArrayList<FileInfo> getDataList() {
		return mList;
	}

	public void appendDataList(boolean append, ArrayList<FileInfo> newData) {
		if (append) {
			mList.addAll(newData);
		} else {
			mList.clear();
			mList.addAll(newData);
		}
	}

	LayoutInflater inflater;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.cm_file_item, null);
			holder = new ViewHolder();
			holder.fileImage = (ImageView) convertView.findViewById(R.id.file_image);
			
			holder.fileImageFrame = (ImageView) convertView
					.findViewById(R.id.file_image_frame);
			
			holder.name = (TextView) convertView.findViewById(R.id.file_name);
			holder.childCnt = (TextView) convertView
					.findViewById(R.id.file_child_cnt);
			holder.videoTag = (ImageView) convertView.findViewById(R.id.file_video_tag);
			holder.musicImage = (ImageView) convertView.findViewById(R.id.music_image);
			holder.dirImage = (ImageView) convertView.findViewById(R.id.dir_image);
			
			holder.selectBox = (CheckBox)convertView.findViewById(R.id.select);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (mList.get(position) != null) {
			FileInfo file = mList.get(position);
			holder.name.setText(file.fileName);
			
			Model m = FileOperatorEvent.getModel(mContext);
			if(! m.equals(Model.SELECT_MODEL)){
			    holder.selectBox.setVisibility(View.GONE);
			}else{
			    holder.selectBox.setVisibility(View.VISIBLE);
			}
			if (file.extra) {
			    Log.i(TAG, "file.extra:" + file.extra);
				// holder.fileImage.setImageResource(R.drawable.empty_icon);
			} else {
				if (file.isDir) {
				    Log.i(TAG, "FileItemAdapter.getView, is dir.....");
					holder.videoTag.setVisibility(View.GONE);
					holder.musicImage.setVisibility(View.GONE);
					holder.fileImage.setVisibility(View.GONE);
					holder.fileImageFrame.setVisibility(View.GONE);

					String cntTag = mContext.getString(R.string.cm_file_cnt);
					holder.childCnt.setText(file.count + cntTag);
					holder.childCnt.setVisibility(View.VISIBLE);
					holder.dirImage.setImageResource(R.drawable.cm_folder);
					holder.dirImage.setVisibility(View.VISIBLE);
				} else {
					holder.dirImage.setVisibility(View.GONE);
					holder.childCnt.setVisibility(View.GONE);
					holder.fileImageFrame.setVisibility(View.VISIBLE);
					mHelper.setIcon(file, holder.fileImage,
							holder.fileImageFrame, holder.videoTag,
							holder.musicImage);
				}
			}
		}
		final int currentPos = position;
		convertView.setOnHoverListener(new View.OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
			    Log.i(TAG, "OnHoverListener......");

				if (MotionEvent.ACTION_HOVER_ENTER == event.getAction()) {
					if (mHandler != null) {
						Message message = mHandler.obtainMessage(messId, this);
						message.arg1 = currentPos;
						mHandler.removeMessages(messId);
						mHandler.sendMessage(message);
					}
					return true;
				} else if (MotionEvent.ACTION_HOVER_EXIT == event.getAction()) {
					return true;
				}
				return false;
			}
		});

		return convertView;
	}

	public static class ViewHolder {
		ImageView fileImageFrame;
		ImageView fileImage;
		ImageView musicImage;
		TextView name;
		TextView childCnt;
		ImageView videoTag;
		ImageView dirImage;
		CheckBox selectBox;
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
	public FileInfo getItem(int position) {
		return mList == null || mList.isEmpty() ? null : mList.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
}
