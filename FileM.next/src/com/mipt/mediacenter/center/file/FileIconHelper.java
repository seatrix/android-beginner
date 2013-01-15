package com.mipt.mediacenter.center.file;

import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.file.FileIconLoader.IconLoadFinishListener;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;

/**
 * 
 * @author fang
 * 
 */
public class FileIconHelper implements IconLoadFinishListener {

	private static final String LOG_TAG = "FileIconHelper";

	private static HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

	private FileIconLoader mIconLoader;

	public FileIconHelper(Context context) {
		mIconLoader = new FileIconLoader(context, this);
	}

	public static int getDlanDaultIcon(int type) {
		int res = R.drawable.cm_default_pic;
		if (type == FileInfo.TYPE_MUSIC) {
			res = R.drawable.cm_default_music_in;
		} else if (type == FileInfo.TYPE_VIDEO) {
			res = R.drawable.cm_default_video;
		}
		return res;
	}

	public static int getFileDaultIcon(FileCategory fc) {
		int res = R.drawable.cm_default_pic;
		if (fc == FileCategory.Music) {
			res = R.drawable.cm_default_music_in;
		} else if (fc == FileCategory.Video) {
			res = R.drawable.cm_default_video;
		}
		return res;
	}

	public void setDlanIcon(FileInfo fileInfo, ImageView fileImage,
			String devName, ImageView videoTag, ImageView fileImageFrame,
			ImageView musicFrame) {
		fileImage.setVisibility(View.VISIBLE);
		musicFrame.setVisibility(View.GONE);
		String filePath = fileInfo.imgPath;
		FileCategory fc = FileCategory.Video;
		int id = getDlanDaultIcon(fileInfo.fileType);
		fileImageFrame.setImageResource(R.drawable.cm_img_bg);
		fileImageFrame.setVisibility(View.VISIBLE);
		if (fileInfo.fileType == FileInfo.TYPE_PIC) {
			if (TextUtils.isEmpty(filePath)) {
				filePath = fileInfo.filePath;
			}
			fc = FileCategory.Picture;
		} else if (fileInfo.fileType == FileInfo.TYPE_MUSIC) {
			fileImage.setVisibility(View.GONE);
			musicFrame.setVisibility(View.VISIBLE);
			musicFrame.setImageResource(id);
			fileImageFrame.setImageResource(R.drawable.cm_default_music_bg);
			fc = FileCategory.Music;
		}
		boolean set = false;
		fileImage.setImageResource(id);
		mIconLoader.cancelRequest(fileImage);
		if (!TextUtils.isEmpty(filePath)) {
			if (fileInfo.fileType == FileInfo.TYPE_MUSIC) {
				set = mIconLoader.loadDlanIcon(musicFrame, filePath, devName,
						fc);
			} else {
				set = mIconLoader
						.loadDlanIcon(fileImage, filePath, devName, fc);
			}
			if (!set) {
				fileImage.setImageResource(id);
				imageFrames.put(fileImage, fileImageFrame);
				musicFrame.setImageResource(id);
				set = true;

			}
		}
		if (fileInfo.fileType == FileInfo.TYPE_VIDEO) {
			videoTag.setVisibility(View.VISIBLE);
		} else {
			videoTag.setVisibility(View.GONE);
		}
		if (!set) {
			fileImage.setImageResource(id);
			musicFrame.setImageResource(R.drawable.cm_default_music_in);
		}

	}

	public void setMusicIcon(FileInfo fileInfo, ImageView fileImage,
			ImageView fileImageFrame, int res) {
		String filePath = MediacenterConstant.ALBUM_IMG_SMALL
				+ fileInfo.filePath;
		boolean set = false;
		fileImage.setImageResource(res);
		mIconLoader.cancelRequest(fileImage);
		set = mIconLoader.loadIcon(fileImage, filePath, 0, FileCategory.Music);
		if (!set) {
			fileImage.setImageResource(res);
			imageFrames.put(fileImage, fileImageFrame);
			set = true;
		}

	}

	public void setIcon(FileInfo fileInfo, ImageView fileImage,
			ImageView fileImageFrame, ImageView videoTag, ImageView musicFrame) {
		fileImage.setVisibility(View.VISIBLE);
		musicFrame.setVisibility(View.GONE);
		videoTag.setVisibility(View.GONE);
		fileImageFrame.setImageResource(R.drawable.cm_img_bg);
		String filePath = fileInfo.filePath;
		long fileId = fileInfo.dbId;
		FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
		int resId = getFileDaultIcon(fc);
		if (fc == FileCategory.Music) {
			fileImage.setVisibility(View.GONE);
			musicFrame.setImageResource(resId);
			musicFrame.setVisibility(View.VISIBLE);
			fileImageFrame.setImageResource(R.drawable.cm_default_music_bg);
		}
		if (fc == FileCategory.Video) {
			videoTag.setVisibility(View.VISIBLE);
		}
		boolean set = false;
		fileImage.setImageResource(resId);
		mIconLoader.cancelRequest(fileImage);
		mIconLoader.cancelRequest(musicFrame);
		switch (fc) {
		case Music:
			set = mIconLoader.loadIcon(musicFrame, filePath, -2, fc);
			if (!set) {
				musicFrame.setImageResource(resId);
				imageFrames.put(musicFrame, fileImageFrame);
				set = true;
			}
			break;
		case Picture:
		case Video:
			set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
			if (!set) {
				fileImage.setImageResource(resId);
				imageFrames.put(fileImage, fileImageFrame);
				set = true;
			}
			break;
		default:
			set = true;
			break;
		}

		if (!set) {
			musicFrame.setImageResource(resId);
			fileImage.setImageResource(resId);
		}

	}

	@Override
	public void onIconLoadFinished(ImageView view) {
		ImageView frame = imageFrames.get(view);
		if (frame != null) {
			frame.setVisibility(View.VISIBLE);
			imageFrames.remove(view);
		}
	}

	public void stopLoad() {
		mIconLoader.stop();
	}
}
