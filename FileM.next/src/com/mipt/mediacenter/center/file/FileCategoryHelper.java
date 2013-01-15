package com.mipt.mediacenter.center.file;

import java.io.FilenameFilter;
import java.util.HashMap;

import android.content.Context;

/**
 * 
 * @author fang
 * 
 */
public class FileCategoryHelper {

	private static final String LOG_TAG = "FileCategoryHelper";

	public enum FileCategory {
		All, Music, Video, Picture, Custom, Other
	}

	public static String[] VIDEO_EXTS = new String[] { "mp4", "wmv", "mpeg",
			"m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf", "rmvb", "flv", "swf",
			"f4v", "avi", "mkv", "mpg", "ts", "m2ts","mov" };

	public static String[] PICTURE_EXTS = new String[] { "jpg", "jpeg", "gif",
			"png", "bmp", "wbmp" };

	public static String[] AUDIO_EXTS = new String[] { "wav", "au", "mp3",
			"wma", "aac", "ape", "ogg" };

	public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();

	static {
		filters.put(FileCategory.Picture, new FilenameExtFilter(PICTURE_EXTS));
		filters.put(FileCategory.Video, new FilenameExtFilter(VIDEO_EXTS));
		filters.put(FileCategory.Music, new FilenameExtFilter(AUDIO_EXTS));

	}

	private FileCategory mCategory;

	private Context mContext;

	public FileCategoryHelper(Context context) {
		mContext = context;
		mCategory = FileCategory.All;
	}

	public FileCategory getCurCategory() {
		return mCategory;
	}

	public void setCurCategory(FileCategory c) {
		mCategory = c;
	}

	public void setCustomCategory(String[] exts) {
		mCategory = FileCategory.Custom;
		if (filters.containsKey(FileCategory.Custom)) {
			filters.remove(FileCategory.Custom);
		}

		filters.put(FileCategory.Custom, new FilenameExtFilter(exts));
	}

	public FilenameFilter getFilter() {
		return filters.get(mCategory);
	}

	private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

	public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
		return mCategoryInfo;
	}

	public CategoryInfo getCategoryInfo(FileCategory fc) {
		if (mCategoryInfo.containsKey(fc)) {
			return mCategoryInfo.get(fc);
		} else {
			CategoryInfo info = new CategoryInfo();
			mCategoryInfo.put(fc, info);
			return info;
		}
	}

	public class CategoryInfo {
		public long count;

		public long size;
	}

	public static FileCategory getCategoryFromPath(String path) {
		int dotPosition = path.lastIndexOf('.');
		if (dotPosition == -1)
			return FileCategory.Other;

		String ext = path.substring(dotPosition + 1, path.length());

		if (matchExts(ext, VIDEO_EXTS)) {
			return FileCategory.Video;
		}

		if (matchExts(ext, PICTURE_EXTS)) {
			return FileCategory.Picture;
		}

		if (matchExts(ext, AUDIO_EXTS)) {
			return FileCategory.Music;
		}

		return FileCategory.Other;
	}

	public static boolean matchExts(String ext, String[] exts) {
		for (String ex : exts) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	public static boolean matchVideoExts(String ext) {
		for (String ex : VIDEO_EXTS) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	public static boolean matchMusicExts(String ext) {
		for (String ex : AUDIO_EXTS) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	public static boolean matchExts(String ext) {
		boolean isMatch = matchVideoExts(ext);
		if (!isMatch) {
			isMatch = matchMusicExts(ext);
		}
		if (!isMatch) {
			isMatch = matchPicExts(ext);
		}
		return isMatch;
	}

	public static boolean matchPicExts(String ext) {
		for (String ex : PICTURE_EXTS) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}
}
