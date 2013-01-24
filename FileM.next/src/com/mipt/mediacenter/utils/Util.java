package com.mipt.mediacenter.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Audio.AudioColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mipt.mediacenter.center.file.FileCategoryHelper;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.ErrorThrowable;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.HttpManager;
import com.mipt.mediacenter.center.server.MediacenterConstant;

/**
 * 
 * @author fang
 * 
 */
public class Util {
	private static final String TAG = "Util";
	public static final String LAST_TIME = "last_time";
	public static final String LAST_VIEW_TYPE = "last_view";

	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static boolean containsPath(String path1, String path2) {
		String path = path2;
		while (path != null) {
			if (path.equalsIgnoreCase(path1))
				return true;
			path = new File(path).getParent();
		}

		return false;
	}
	/**
	 * construct file path.
	 * */
	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator)) {
			return path1 + path2;
		} else {
			return path1 + File.separator + path2;
		}
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static String getDLANTempPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/.mediacenter/";
	}

	public static FileInfo GetFileInfo(String filePath) {
		File lFile = new File(filePath);
		if (!lFile.exists())
			return null;

		FileInfo lFileInfo = new FileInfo();
		// lFileInfo.canRead = lFile.canRead();
		// lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = Util.getNameFromFilepath(filePath);
		lFileInfo.modifiedDate = lFile.lastModified();
		lFileInfo.isDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		return lFileInfo;
	}

	public static FileInfo GetFileInfo(File f, FilenameFilter filter,
			boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		// lFileInfo.canRead = lFile.canRead();
		// lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.modifiedDate = lFile.lastModified();
		lFileInfo.isDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		if (lFileInfo.isDir) {
/*			int lCount = 0;
			File[] files = lFile.listFiles(filter);
			if (files == null) {
				return null;
			}
			for (File child : files) {
				if (!child.isHidden() || showHidden) {
					lCount++;
				}
			}
			lFileInfo.count = lCount;
*/
		} else {
			lFileInfo.fileSize = lFile.length();

		}
		return lFileInfo;
	}

	public static Drawable getApkIcon(Context context, String path) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			try {
				return pm.getApplicationIcon(appInfo);
			} catch (OutOfMemoryError e) {
				Log.e(TAG, e.toString());
			}
		}
		return null;
	}

	public static boolean isHidden(String filePath) {
		boolean isHidden = false;
		if (filePath.contains(".thumbnails")
				|| filePath.contains(".mediacenter")) {
			isHidden = true;
		}
		return isHidden;
	}

	public static String getExtFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	public static String getNameFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return "";
	}

	public static String getPathFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	public static String getType(String filePath) {
		int dotPosition = filePath.lastIndexOf('.');
		if (dotPosition == -1)
			return "*/*";

		String ext = filePath.substring(dotPosition + 1, filePath.length())
				.toLowerCase();

		return ext;
	}

	public static String getTypeUpperCase(String filePath) {
		int dotPosition = filePath.lastIndexOf('.');
		if (dotPosition == -1)
			return "*/*";

		String ext = filePath.substring(dotPosition + 1, filePath.length())
				.toUpperCase();

		return ext;
	}

	public static String getNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	// does not include sd card folder
	private static String[] SysFileDirs = new String[] { "miren_browser/imagecaches" };
	
	/**
	 * <li>1. do not show hidden file.
	 * <li>2. do not show start with point file. 
	 * */
	public static boolean shouldShowFile(String path) {
		return shouldShowFile(new File(path));
	}

	public static boolean shouldShowFile(File file) {
		if (file.isHidden())
			return false;

		if (file.getName().startsWith("."))
			return false;

		String sdFolder = getSdDirectory();
		for (String s : SysFileDirs) {
			if (file.getPath().startsWith(makePath(sdFolder, s)))
				return false;
		}

		return true;
	}

	public static boolean setText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	public static boolean setText(View view, int id, int text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	// comma separated number
	public static String convertNumber(long number) {
		return String.format("%,d", number);
	}

	// storage, G M K B
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	public static class SDCardInfo {
		public long total;
		public long free;
		public long used;
		public String path;
	}

	public static SDCardInfo getSDCardInfo() {
		String sDcString = android.os.Environment.getExternalStorageState();
		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File pathFile = android.os.Environment
					.getExternalStorageDirectory();
			SDCardInfo info = getSDCardInfo(pathFile);
			return info;
		}

		return null;
	}

	public static SDCardInfo getSDCardInfo(File _file) {
		File pathFile = _file;
		try {
			android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
			long nTotalBlocks = statfs.getBlockCount();
			long nBlocSize = statfs.getBlockSize();
			long nFreeBlock = statfs.getFreeBlocks();
			SDCardInfo info = new SDCardInfo();
			info.total = nTotalBlocks * nBlocSize;
			long nAvailaBlock = statfs.getAvailableBlocks();
			info.free = nAvailaBlock * nBlocSize;
			info.used = info.total - info.free;
			info.path = pathFile.getPath();
			return info;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
		}

		return null;
	}

	public static String formatDateString(Context context, long time) {
		DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat
				.getTimeFormat(context);
		Date date = new Date(time);
		return dateFormat.format(date) + " " + timeFormat.format(date);
	}

	public static String formatDateString(long time) {
		// String fromat = "MM/dd/yyyy";
		String fromat = "yyyy.MM.dd";
		SimpleDateFormat sdf = new SimpleDateFormat(fromat);
		Date dt = new Date(time);
		return sdf.format(dt);
	}

	public static String getLastTime(Context ctx, String key) {
		SharedPreferences settings = ctx.getSharedPreferences(LAST_TIME, 0);
		long lastCheck = settings.getLong(key, 0);
		return formatDateString(ctx, lastCheck);
	}

	public static long getLastTimeLong(Context ctx, String key) {
		SharedPreferences settings = ctx.getSharedPreferences(LAST_TIME, 0);
		long lastCheck = settings.getLong(key, 0);
		return lastCheck;
	}

	public static void saveLastTime(Context ctx, String key) {
		long lastCheck = System.currentTimeMillis();
		SharedPreferences settings = ctx.getSharedPreferences(LAST_TIME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, lastCheck);
		editor.commit();
	}

	public static void putLastType(final Context ctx, String key, int value) {
		SharedPreferences settings = ctx
				.getSharedPreferences(LAST_VIEW_TYPE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static int getLastType(Context ctx, String key) {
		SharedPreferences settings = ctx
				.getSharedPreferences(LAST_VIEW_TYPE, 0);
		int lastCheck = settings.getInt(key,
				MediacenterConstant.FileViewType.VIEW_DIR);
		return lastCheck;
	}

	private static boolean isHasByPath(String path,
			final ArrayList<FileInfo> list) {
		boolean isHas = false;
		if (path == null || "".equals(path.trim()) || list == null
				|| list.isEmpty()) {
			return isHas;
		}
		for (FileInfo fi : list) {
			if (path.equals(fi.filePath)) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
	}

	public static DeviceInfo isRemoveDevice(String path,
			final List<DeviceInfo> newDevices,
			final List<DeviceInfo> oldDevices) {
		if (newDevices == null || newDevices.isEmpty()) {
			if (oldDevices != null && !oldDevices.isEmpty()) {
				return oldDevices.get(oldDevices.size() - 1);
			} else {
				return null;
			}

		}
		DeviceInfo removeDevice = null;
		if (TextUtils.isEmpty(path)) {
			for (DeviceInfo di : oldDevices) {
				for (DeviceInfo odi : newDevices) {
					if (!TextUtils.isEmpty(odi.devPath)
							&& !TextUtils.isEmpty(di.devPath)
							&& !di.devPath.equals(odi.devPath)) {
						removeDevice = di;
					}
				}
			}
		} else {
			for (DeviceInfo di : oldDevices) {
				if (!TextUtils.isEmpty(di.devPath) && di.devPath.equals(path)) {
					removeDevice = di;
					break;
				}
			}
		}

		return removeDevice;
	}

	public static DeviceInfo isNewDevice(
			final List<DeviceInfo> newDevices,
			final List<DeviceInfo> oldDevices, String path) {
		if (newDevices == null || newDevices.isEmpty()) {
			return null;
		}
		if (oldDevices == null || oldDevices.isEmpty()) {
			return newDevices.get(newDevices.size() - 1);
		}
		DeviceInfo newDevice = null;
		if (TextUtils.isEmpty(path)) {
		for (DeviceInfo di : newDevices) {
			for (DeviceInfo odi : oldDevices) {
				if (!di.devPath.equals(odi.devPath)) {
						newDevice = di;
					}
				}
			}
		} else {
			for (DeviceInfo di : newDevices) {
				if (di.type == DeviceInfo.TYPE_USB
						&& path.indexOf(di.devPath) == 0) {
					newDevice = di;
				}
			}
		}
		return newDevice;
	}

	public static String getLocalDeviceName(Context cxt) {
		String name = null;
		try {
			Uri uri = Uri.parse(MediacenterConstant.DlnaConstant.URI);
			ContentResolver cr = cxt.getContentResolver();
			Cursor c = cr.query(uri, null,
					MediacenterConstant.DlnaConstant.CONDITION, null, null);
			if (c != null) {
				c.moveToFirst();
				name = c.getString(c.getColumnIndexOrThrow("value"));
				c.close();
			} else {
			}
		} catch (Exception e) {
		}
		return name;
	}

	public static void runOnUiThread(Activity mActivity, Runnable r) {
		if (mActivity != null) {
			mActivity.runOnUiThread(r);
		}

	}

	public static String handlePath(final String path) {
		String pathRe = path;
		if (!TextUtils.isEmpty(pathRe) && pathRe.contains("/mnt/")
				&& pathRe.indexOf("/mnt/") == 0) {
			pathRe = pathRe.substring(("/mnt/").length());
			if (pathRe.contains("/") && pathRe.indexOf("sdcard") != 0) {
				String[] str = pathRe.split("/");
				if ("A6".equals(android.os.Build.MODEL)) {
				pathRe = pathRe.substring(pathRe.indexOf("/"));
				} else {
					if (str.length > 1 && str[1].contains("_")) {
						pathRe = pathRe.substring(pathRe.indexOf("/"));
					}
				}
			}
		}
		if (pathRe.indexOf("/") != 0) {
			pathRe = "/" + pathRe;
		}
		return pathRe;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	private static Cursor getCursor(final Context cxt, String filePath) {
		String path = null;

		Cursor c = cxt.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (c.moveToFirst()) {
			do {
				path = c.getString(c.getColumnIndexOrThrow(MediaColumns.DATA));
				if (path.equals(filePath)) {
					break;
				}
			} while (c.moveToNext());
		}

		return c;
	}

	private static String getAlbumArt(final Context cxt, int album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = cxt.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		return album_art;
	}

	public static Bitmap getMusicImage(final Context cxt, String path) {
		Cursor currentCursor = getCursor(cxt, path);
		int album_id = currentCursor.getInt(currentCursor
				.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
		String albumArt = getAlbumArt(cxt, album_id);
		return BitmapFactory.decodeFile(albumArt);

	}

	public static ArrayList<FileInfo> getMusicFileByPath(String path) {
		return getList(path, FileCategoryHelper.FileCategory.Music,
		        FileCategoryHelper.FileCategory.Music.ordinal());

	}

	public static ArrayList<FileInfo> getVideoFileByPath(String path) {
		return getList(path, FileCategoryHelper.FileCategory.Video,
		        FileCategoryHelper.FileCategory.Video.ordinal());

	}

	public static ArrayList<FileInfo> getPicFileByPath(String path) {

		return getList(path, FileCategoryHelper.FileCategory.Picture,
		        FileCategoryHelper.FileCategory.Picture.ordinal());

	}

	private static ArrayList<FileInfo> getList(String path,
			FileCategoryHelper.FileCategory fileExts, int fileType) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return null;
		}

		File[] listFiles = file.listFiles();
		if (listFiles == null) {
			return null;
		}
		ArrayList<FileInfo> retList = new ArrayList<FileInfo>();
		FileInfo fi = null;
		for (File child : listFiles) {
			// do not show selected file if in move state
			String absolutePath = child.getAbsolutePath();
			if (!child.isDirectory() && Util.shouldShowFile(absolutePath)) {
				fi = new FileInfo();
				fi.fileName = child.getName();
				fi.modifiedDate = child.lastModified();
				fi.filePath = child.getAbsolutePath();
				if (fileType != 0) {
					fi.fileType = fileType;
				}
				fi.fileSize = child.length();
				retList.add(fi);
			}
		}
		return retList;

	}

	public static Bitmap getMusicThumbnail(String filePath, String devId) {
		if (filePath.indexOf("http") >= 0 && !TextUtils.isEmpty(devId)) {
			return getDlnaThumbnail(filePath, devId);
		}
		return createAlbumThumbnail(filePath);
	}

	public static Bitmap createAlbumThumbnail(String filePath) {
		if (filePath.indexOf("http") == 0) {
			return null;
		}
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			byte[] art = retriever.getEmbeddedPicture();
			if (art.length > 0) {
				bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
			}
		} catch (IllegalArgumentException ex) {
			bitmap = null;
		} catch (RuntimeException ex) {
			bitmap = null;
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up. } } return bitmap; }
			}
		}
		return bitmap;
	}

	// gizp bitmap
	public static Bitmap extractMiniThumb(Bitmap source, boolean recycle,
			int w, int y) {
		if (source == null) {
			return null;
		}
		int width = w;
		int height = y;
		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = width / (float) source.getWidth();
		} else {
			scale = height / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap miniThumbnail = transform(matrix, source, width, height, true,
				recycle);
		return miniThumbnail;
	}

	public static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp, boolean recycle) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);
			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();
		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;
		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}
		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}
		if (recycle && b1 != source) {
			source.recycle();
		}
		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);
		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);
		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}
		return b2;
	}

	private static final int MICRO_KIND = Images.Thumbnails.FULL_SCREEN_KIND;

	public static Bitmap getImageThumbnail(final Context mContext, long id) {
		return Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
				id, Images.Thumbnails.MINI_KIND, null);
	}

	public static Bitmap getVideoThumbnail(final Context mContext, long id) {
		return Video.Thumbnails.getThumbnail(mContext.getContentResolver(), id,
				Images.Thumbnails.MINI_KIND, null);
	}

	public static Bitmap createImageThumbnail(String filePath) {
		if (filePath.indexOf("http") >= 0) {
			return null;
		}
		Bitmap bt = null;
		try {
			bt = ThumbnailUtils.createImageThumbnail(filePath, MICRO_KIND);
		} catch (Exception ex) {
			bt = null;
		}
		return bt;
	}

	public static Bitmap getImageThumbnail(String filePath, String devId) {
		if (filePath.indexOf("http") >= 0 && !TextUtils.isEmpty(devId)) {
			return getDlnaThumbnail(filePath, devId);
		}
		return ThumbnailUtils.createImageThumbnail(filePath, MICRO_KIND);
	}

	public static Bitmap createVideoThumbnail(String filePath) {
		if (filePath.indexOf("http") >= 0) {
			return null;
		}
		Bitmap bt = null;
		try {
			bt = ThumbnailUtils.createVideoThumbnail(filePath, MICRO_KIND);
		} catch (Exception ex) {
			bt = null;
		}
		return bt;
	}

	public static Bitmap createVideoThumbnail(String filePath, long atTime) {
		if (filePath.indexOf("http") == 0) {
			return null;
		}
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			if (atTime == 0) {
				String dration = retriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				if (!TextUtils.isEmpty(dration)) {
					atTime = (Integer.parseInt(dration) / 10) * 1000000;
				}
			}
			bitmap = retriever.getFrameAtTime(atTime);
		} catch (IllegalArgumentException ex) {
			bitmap = null;
		} catch (RuntimeException ex) {
			bitmap = null;
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up. } } return bitmap; }
			}
		}
		return bitmap;
	}

	public static Bitmap getVideoThumbnail(String filePath, String devId) {
		if (filePath.indexOf("http") >= 0 && !TextUtils.isEmpty(devId)) {
			return getDlnaThumbnail(filePath, devId);
		}
		return ThumbnailUtils.createVideoThumbnail(filePath, MICRO_KIND);
	}

	public static long getDbId(final Context mContext, String path,
			boolean isVideo) {
		String volumeName = "external";
		Uri uri = isVideo ? Video.Media.getContentUri(volumeName)
				: Images.Media.getContentUri(volumeName);
		String selection = MediaColumns.DATA + "=?";
		String[] selectionArgs = new String[] { path };

		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA };

		Cursor c = mContext.getContentResolver().query(uri, columns, selection,
				selectionArgs, null);
		if (c == null) {
			return 0;
		}
		long id = 0;
		if (c.moveToNext()) {
			id = c.getLong(0);
		}
		c.close();
		return id;
	}

	public static Bitmap getDlnaThumbnail(String path, String devId) {
		File file = getDlnaFile(path, devId);
		if (file != null && file.exists()) {
			return BitmapFactory.decodeFile(file.getAbsolutePath());
		}
		return null;

	}

	public static File getDlnaFile(String path, String devId) {
		String picPath = Util.getDLANTempPath() + devId;
		File file = new File(picPath, getLastName(path));
		if (file.exists()) {
			return file;
		}
		return file;
	}

	public static File getDlnaTempFile(String path, String devId) {
		String picPath = Util.getDLANTempPath() + devId;
		File file = new File(picPath, getLastName(path) + "temp");
		if (file.exists() && file.length() > 0) {
			file.delete();
			return file;
		}
		return file;
	}
	public static String getLastName(final String path) {
		String temp = path;
		int pos = temp.lastIndexOf("/");
		if (pos >= 0) {
			temp = temp.substring(pos + 1, temp.length());
		}
		return temp;
	}

	public static Bitmap getDlanThumbnail(String path, String devName) {
		if (TextUtils.isEmpty(path) || TextUtils.isEmpty(devName)) {
			return null;
		}
		return getDlanThumbnail(path, devName, 121, 9, false);
	}

	public static Bitmap getDlanThumbnail(String path, String devName, int w,
			int y, boolean save) {
		Bitmap bimap = getDlnaThumbnail(path, devName);
		if (bimap != null) {
			return bimap;
		}
		if (!save) {
			return null;
		}
		File file = Util.getDlnaFile(path, devName);
		if (file != null && file.exists()) {
			file.delete();
		}
		InputStream is = null;
		try {
			is = HttpManager.doGetReturnInputStream(path, null);
		} catch (ErrorThrowable e) {
			// TODO Auto-generated catch block
			return null;
		}
		if (is == null) {
			return null;
		}
		File isFile = Util.getDlnaTempFile(path, devName);
		try {
			isFile = mkFilePath(isFile);
			saveInputStream2File(isFile, is);
		} catch (IOException e1) {
			return null;
		}
		if (!isFile.exists()) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(isFile.getAbsolutePath(), opts);

		opts.inSampleSize = computeSampleSize(opts, -1, 128 * 128);
		opts.inJustDecodeBounds = false;

		try {
			File newFile = mkFilePath(file);
			if (newFile.exists()) {
				try {
					Bitmap b = BitmapFactory.decodeFile(
							isFile.getAbsolutePath(), opts);
					if (b != null) {
						final Bitmap newBitmap = Util.extractMiniThumb(b, true,
								w, y);
						b.recycle();
						// final Bitmap savaBitmap = newBitmap;
						saveImage2File(file, newBitmap);
						if (isFile.exists()) {
							isFile.delete();
						}
						return newBitmap;
					} else {
						return b;
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}

		return null;

	}

	private static File mkFilePath(File file) throws IOException {
		File lFile = file;
		if (!lFile.exists()) {
			// make parent dirs if necessary
			File dir = new File(lFile.getParent());
			if (!dir.exists()) {
				if (dir.mkdirs()) {
				}
			}
			// make file
			try {
				if (lFile.createNewFile()) {
				} else {
				}
			} catch (Exception ex) {
			}
		}
		return lFile;
	}

	private static boolean saveImage2File(File file, Bitmap bitmap) {
		if (bitmap == null) {
			return false;
		}
		File coverFile = file;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(coverFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (FileNotFoundException e) {
			return false;
		} finally {
			Util.closeStream(out);
		}
		return true;
	}
	
	private static File saveInputStream2File(File file, InputStream inStream) {
		int byteread = 0;
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(file);
			byte[] buffer = new byte[3000];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
		} catch (Exception e) {
			return null;
		} finally {
			if (fs != null) {
				Util.closeStream(fs);
			}
		}
		return file;
	}

	public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	public static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	public static String converString(String str) {
		if (!TextUtils.isEmpty(str)) {
			try {
				String charset = getEncoding(str);
				str = new String(str.getBytes(charset), "GBK");
				return str;
			} catch (UnsupportedEncodingException e) {
			}
		}
		return null;
	}
	public static String getEncoding(String str) {
		if (TextUtils.isEmpty(str)) {
			return "UTF-8";
		}
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "UTF-8";
	}
	public static FileInfo getMusicInfo(FileInfo _fi) {
		if (_fi.filePath == null) {
			return null;
		}
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(_fi.filePath);
		} catch (Exception e) {
			mmr.release();
			return null;
		}
		_fi.mediaName = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		String str = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		if (!TextUtils.isEmpty(str)) {
			_fi.duration = Integer.valueOf(str);
		}
		_fi.albumName = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
		_fi.artist = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		_fi.mediaName = Util.converString(_fi.mediaName);
		_fi.albumName = Util.converString(_fi.albumName);
		_fi.artist = Util.converString(_fi.artist);
		String gener = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
		if (!TextUtils.isEmpty(gener)) {
			gener = gener.replace("(", "").replace(")", "").replace("[", "")
					.replace("]", "");
			if (Util.isNumeric(gener)) {
				int pos = Integer.parseInt(gener);
				if (pos < ID3_GENRES.length) {
					_fi.genreName = ID3_GENRES[Integer.parseInt(gener)];
				}
			} else {
				_fi.genreName = gener;
			}
		}
		mmr.release();
		return _fi;
	}
	public static FileInfo getVideoInfo(FileInfo _fi) {
		if (_fi.filePath == null) {
			return null;
		}
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(_fi.filePath);
			_fi.mediaName = mmr
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			_fi.mediaName = Util.converString(_fi.mediaName);
			String str = mmr
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (!TextUtils.isEmpty(str)) {
				_fi.duration = Integer.valueOf(str);
			}
		} catch (Exception e) {
			return _fi;
		} finally {
			mmr.release();
		}
		return _fi;
	}
	private static final String[] ID3_GENRES = {
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
			"Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
			"Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
			"Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
			"Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk",
			"Fusion", "Trance", "Classical", "Instrumental", "Acid", "House",
			"Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass",
			"Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
			"Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
			"Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
			"Christian Rap", "Pop/Funk", "Jungle",
			"Native American",
			"Cabaret",
			"New Wave",
			"Psychadelic",
			"Rave",
			"Showtunes",
			"Trailer",
			"Lo-Fi",
			"Tribal",
			"Acid Punk",
			"Acid Jazz",
			"Polka",
			"Retro",
			"Musical",
			"Rock & Roll",
			"Hard Rock",
			"Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
			"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
			"Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
			"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
			"Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
			"Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella",
			"Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House",
			"Hardcore", "Terror", "Indie", "Britpop", "Negerpunk",
			"Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
			"Black Metal", "Crossover", "Contemporary Christian",
			"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime",
			"JPop", "Synthpop"
	};
}
