package com.mipt.mediacenter.center.file;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * @author fang
 * 
 */
public class FileCategoryHelper {
	private static final String TAG = "FileCategoryHelper";

	public enum FileCategory {
		Music, Video, Picture, Text, APK, ZIP, Other
	}

	public final static String[] PICTURE_EXTS = new String[] { "jpg", "jpeg", "gif",
	    "png", "bmp", "wbmp", "jfif", "tiff" };
	
	public final static String[] VIDEO_EXTS = new String[] { "mp4", "wmv", "mpeg",
			"m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf", "rm", "rmvb", "flv",
			"swf", "f4v", "avi", "mkv", "mpg", "ts", "m2ts", "mov", "3dm",
			"divx", "webm", "tp", "m4b", "ios", "trp" };

	public final static String[] AUDIO_EXTS = new String[] { "mp3", "ogg", "wav",
			"wma", "m4a", "ape", "dts", "flac", "mp1", "mp2", "aac", "midi",
			"mid", "mp5", "mpga", "mpa", "m4p", "amr", "m4r" };

	public final static String[] ZIP_EXTS = new String[]{
	    "zip", "rar","iso", "gz","tar","bz"
	};
	
	public final static String[] TEXT_EXTS = new String[]{
        "txt","ini","properties","log","text","asc","diff","srt"
    };

	private Map<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();
	public Map<FileCategory, CategoryInfo> getCategoryInfos() {
		return mCategoryInfo;
	}

	public class CategoryInfo {
		public long count;
		public long size;
	}

    private FileCategory mCategory;

    public FileCategory getCurCategory() {
        return mCategory;
    }

    public void setCurCategory(FileCategory c) {
        mCategory = c;
    }	
	
    public static final Map<String, FileCategory> EXT_TO_TYPE = new HashMap<String, FileCategory>(100);
    static{
        for(String ext : FileCategoryHelper.PICTURE_EXTS){
            EXT_TO_TYPE.put(ext, FileCategory.Picture);            
        }
        
        for(String ext : FileCategoryHelper.VIDEO_EXTS){
            EXT_TO_TYPE.put(ext, FileCategory.Video);            
        }

        for(String ext : FileCategoryHelper.AUDIO_EXTS){
            EXT_TO_TYPE.put(ext, FileCategory.Music);            
        }

        for(String ext : FileCategoryHelper.TEXT_EXTS){
            EXT_TO_TYPE.put(ext, FileCategory.Text);            
        }

        for(String ext : FileCategoryHelper.ZIP_EXTS){
            EXT_TO_TYPE.put(ext, FileCategory.ZIP);            
        }
        EXT_TO_TYPE.put("apk", FileCategory.APK);
    }
    
	public static FileCategory getCategoryFromPath(String path) {
		int dotPosition = path.lastIndexOf('.');
		if (dotPosition == -1)
			return FileCategory.Other;

		String ext = path.substring(dotPosition + 1, path.length());
		FileCategory type = EXT_TO_TYPE.get(ext.toLowerCase(Locale.ENGLISH));
		return type != null ? type : FileCategory.Other;
	}

}
