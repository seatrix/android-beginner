package com.mipt.mediacenter.center.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.MimeUtils;
import com.mipt.mediacenter.utils.ToastFactory;

/**
 * @author fang
 */
public class IntentBuilder {
	private static final String TAG = "IntentBuilder";

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
		Log.i(TAG, "-----IntentBuilder-filePath:" + filePath);
		//final int duration = lFileInfo.duration;
		if (lFileInfo.fileType == 0) {
		     int dotPosition = lFileInfo.filePath.lastIndexOf('.');
		     if (dotPosition == -1)
		            lFileInfo.fileType = FileCategoryHelper.FileCategory.Other.ordinal();
		     
		     String ext = filePath.substring(dotPosition + 1, filePath.length())
		                .toLowerCase(Locale.ENGLISH);
		     lFileInfo.fileType = FileCategoryHelper.EXT_TO_TYPE.get(ext).ordinal();
		}

		String appPackage = "com.mipt.mediacenter";
		if (lFileInfo.fileType == FileCategory.Picture.ordinal()) {		    
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.picture.view.MainActivity", isDlan,
                    title, devId);

		} else if (lFileInfo.fileType == FileCategory.Video.ordinal()) {
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.video.activity.VideoActivity", isDlan,
                    title, devId);

		} else if (lFileInfo.fileType == FileCategory.Music.ordinal()) {
            doActionExternal(context, filePath, appPackage,
                    "com.mipt.mediacenter.music.ui.MusicPlayerActivity", isDlan,
                    title, devId);
		}else if(lFileInfo.fileType == FileCategory.APK.ordinal()){
		    Log.i(TAG, "open text file, path:" + lFileInfo);
		    Intent intent = new Intent();
            intent.setClassName("com.android.packageinstaller",
                    "com.android.packageinstaller.PackageInstallerActivity");
            intent.setDataAndType(Uri.fromFile(new File(lFileInfo.filePath)), "application/vnd.android.package-archive");
            context.startActivity(intent);
		}else if(lFileInfo.fileType == FileCategory.Text.ordinal()){
		    Log.i(TAG, "open text file, path:" + lFileInfo);
		    doAction(context, lFileInfo, "text/plain");
		}else if(lFileInfo.fileType == FileCategory.ZIP.ordinal()){
		    Log.i(TAG, "open text file, path:" + lFileInfo);
		    doAction(context, lFileInfo, "application/zip");
        }else{
            Log.i(TAG, "unknown type:" + lFileInfo.fileType);
        }
	}
	
	private static void doAction(Context context, FileInfo info, String mime){
	    Intent intent = new Intent();
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setDataAndType(Uri.fromFile(new File(info.filePath)), mime);
	    context.startActivity(intent);
	}

    private static void doActionExternal(Activity context, final String filePath,
            final String appPakage, final String classTo, final boolean isDlna,
            final String title, String devId) {

        Intent intent = new Intent();
        intent.setClassName(appPakage, classTo);
        intent.putExtra("isLocal", false);
        if (isDlna) {
            intent.putExtra(MediacenterConstant.INTENT_EXTRA, filePath);
            //intent.putExtra(MediacenterConstant.INTENT_EXTRA_DLAN, duration);
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
				.toLowerCase(Locale.ENGLISH);
		String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
		if (ext.equals("mtz")) {
			mimeType = "application/miui-mtz";
		}

		return mimeType != null ? mimeType : "*/*";
	}
}
