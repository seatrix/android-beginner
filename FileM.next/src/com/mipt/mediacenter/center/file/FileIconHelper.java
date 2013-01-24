package com.mipt.mediacenter.center.file;

import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.file.FileIconLoader.IconLoadFinishListener;
import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class FileIconHelper implements IconLoadFinishListener {

	private static final String TAG = "FileIconHelper";

	private static HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

	private FileIconLoader mIconLoader;

	public FileIconHelper(Context context) {
		mIconLoader = new FileIconLoader(context, this);
	}
	//Music, Video, Picture, text, apk, zip, Other
	public static int getFileDaultIcon(FileCategory fc) {
	    int res = -1;
	    switch (fc) {
            case Music:
                res = R.drawable.cm_default_music_in;
                break;
            case Video:
                res = R.drawable.cm_default_video;
                break;
            case Picture:
                res = R.drawable.cm_default_pic;
                break;
            case Text:
                res = R.drawable.cm_default_txt;
                break;
            case APK:
                res = R.drawable.cm_default_apk;
                break;
            case ZIP:
                res = R.drawable.cm_default_zip;
                break;
            default:
                res = R.drawable.cm_default_unknown;
                break;
        }
		return res;
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
