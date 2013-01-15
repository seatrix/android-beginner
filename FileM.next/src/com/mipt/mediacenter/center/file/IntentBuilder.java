package com.mipt.mediacenter.center.file;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.MainActivity;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.MimeUtils;
import com.mipt.mediacenter.utils.ToastFactory;

/**
 * 
 * @author fang
 * 
 */
public class IntentBuilder {
	private static final String LOG_TAG = "IntentBuilder";

	public static void viewFile(Activity context, final FileInfo lFileInfo,
			final ArrayList<FileInfo> files, boolean isDlan, String devId) {
		final String title = lFileInfo.fileName;
		final String filePath = lFileInfo.filePath;
		if (!isDlan) {
			File file = new File(filePath);
			if (!file.exists()) {
				ToastFactory
						.getInstance()
						.getToast(context,
								context.getString(R.string.current_sd_remove))
						.show();
				context.finish();
				return;
			}
		}
		Log.i(LOG_TAG, "-----IntentBuilder-filePath:" + filePath);
		final int duration = lFileInfo.duration;
		if (lFileInfo.fileType == 0) {
			getFileType(lFileInfo);
		}
		// final String type = getMimeType(filePath);
		// String selectType = "audio/*";
		Intent intent = new Intent();
		String appPackage = "com.mipt.mediacenter";
		
		if (lFileInfo.fileType == FileInfo.TYPE_PIC) {		    
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.picture.view.MainActivity", isDlan, duration,
                    title, devId);

		} else if (lFileInfo.fileType == FileInfo.TYPE_VIDEO) {
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.video.activity.VideoActivity", isDlan, duration,
                    title, devId);

		} else if (lFileInfo.fileType == FileInfo.TYPE_MUSIC) {
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.music.ui.MusicPlayerActivity", isDlan, duration,
                    title, devId);

		}

	}

	private static void doAction(Activity context, final String filePath,
			final Class classTo, final boolean isDlna, final int duration,
			final String title, String devId) {
		// Log.i(LOG_TAG, "---filePath---:" +
		// filePath+"--size:"+MediaCenterApplication.getInstance().getDataSize());
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		//
		// }
		// }).start();
		Intent intent = new Intent(context, classTo);
		intent.putExtra("isLocal", true);
		if (isDlna) {
			intent.putExtra(MediacenterConstant.INTENT_EXTRA, filePath);
			intent.putExtra(MediacenterConstant.INTENT_EXTRA_DLAN, duration);
			intent.putExtra("title", title);
			intent.putExtra("devId", devId);
		} else {
			intent.setData(Uri.fromFile(new File(filePath)));
		}
		context.startActivityForResult(intent,
				MediacenterConstant.ACTIVITYR_RESULT_CODE);
	}

    private static void doActionExternal(Activity context, final String filePath,
            final String appPakage, final String classTo, final boolean isDlna, final int duration,
            final String title, String devId) {

        Intent intent = new Intent();
        intent.setClassName(appPakage, classTo);
        intent.putExtra("isLocal", true);
        if (isDlna) {
            intent.putExtra(MediacenterConstant.INTENT_EXTRA, filePath);
            intent.putExtra(MediacenterConstant.INTENT_EXTRA_DLAN, duration);
            intent.putExtra("title", title);
            intent.putExtra("devId", devId);
        } else {
            intent.setData(Uri.fromFile(new File(filePath)));
        }
        context.startActivityForResult(intent,
                MediacenterConstant.ACTIVITYR_RESULT_CODE);
    }	
	
	public static Intent buildSendFile(ArrayList<FileInfo> files) {
		ArrayList<Uri> uris = new ArrayList<Uri>();

		String mimeType = "*/*";
		for (FileInfo file : files) {
			if (file.isDir)
				continue;
			File fileIn = new File(file.filePath);
			mimeType = getMimeType(file.fileName);
			Uri u = Uri.fromFile(fileIn);
			uris.add(u);
		}

		if (uris.size() == 0)
			return null;

		boolean multiple = uris.size() > 1;
		Intent intent = new Intent(
				multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
						: android.content.Intent.ACTION_SEND);

		if (multiple) {
			intent.setType("*/*");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			intent.setType(mimeType);
			intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
		}

		return intent;
	}

	private static String getMimeType(String filePath) {
		int dotPosition = filePath.lastIndexOf('.');
		if (dotPosition == -1)
			return "*/*";

		String ext = filePath.substring(dotPosition + 1, filePath.length())
				.toLowerCase();
		String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
		if (ext.equals("mtz")) {
			mimeType = "application/miui-mtz";
		}

		return mimeType != null ? mimeType : "*/*";
	}

	private static FileInfo getFileType(FileInfo lFileInfo) {
		String filePath = lFileInfo.filePath;
		int dotPosition = filePath.lastIndexOf('.');
		if (dotPosition == -1)
			return lFileInfo;

		String ext = filePath.substring(dotPosition + 1, filePath.length())
				.toLowerCase();
		if (FileCategoryHelper.matchVideoExts(ext)) {
			lFileInfo.fileType = FileInfo.TYPE_VIDEO;
		} else if (FileCategoryHelper.matchMusicExts(ext)) {
			lFileInfo.fileType = FileInfo.TYPE_MUSIC;
		} else if (FileCategoryHelper.matchPicExts(ext)) {
			lFileInfo.fileType = FileInfo.TYPE_PIC;
		}
		return lFileInfo;
	}
}
