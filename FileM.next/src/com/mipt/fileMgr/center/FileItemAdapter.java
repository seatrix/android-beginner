package com.mipt.fileMgr.center;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.file.FileIconHelper;
import com.mipt.fileMgr.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class FileItemAdapter extends BaseAdapter {
	public static final String TAG = "FileItemAdapter";
	private ArrayList<FileInfo> mList = new ArrayList<FileInfo>();
	private final Context mContext;
	private FileIconHelper mHelper;
	private Handler mHandler;
	private int messId;

	public FileItemAdapter(Activity activity, FileIconHelper _mFileIconHelper,
			ArrayList<FileInfo> fileList) {
		mContext = activity;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHelper = _mFileIconHelper;
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
			holder.fileImage = (ImageView) convertView
					.findViewById(R.id.file_image);
			holder.fileImageFrame = (ImageView) convertView
					.findViewById(R.id.file_image_frame);
			holder.name = (TextView) convertView.findViewById(R.id.file_name);
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

		if (mList.get(position) != null) {
			FileInfo file = mList.get(position);
			holder.name.setText(replaceNameType(file.fileName));
			String cntTag = mContext.getString(R.string.cm_file_cnt);
			if (file.extra) {
				// holder.fileImage.setImageResource(R.drawable.empty_icon);
			} else {
				if (file.isDir) {
					holder.videoTag.setVisibility(View.GONE);
					holder.musicImage.setVisibility(View.GONE);
					holder.fileImage.setVisibility(View.GONE);
					holder.childCnt.setText(file.count + cntTag);
					holder.childCnt.setVisibility(View.VISIBLE);
					holder.fileImageFrame.setVisibility(View.GONE);
					holder.dirImage.setImageResource(R.drawable.cm_folder);
					holder.dirImage.setVisibility(View.VISIBLE);
					
				} else {
					holder.dirImage.setVisibility(View.GONE);
					holder.fileImageFrame.setVisibility(View.VISIBLE);
					holder.childCnt.setVisibility(View.GONE);
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
	// TODO Auto-generated method stub
	public FileInfo getItem(int position) {
		return mList == null || mList.isEmpty() ? null : mList.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	private String replaceNameType(String name) {
		int pos = name.lastIndexOf(".");
		if (pos > 0) {
			return name.substring(0, pos);
		}
		return name;
	}

}
