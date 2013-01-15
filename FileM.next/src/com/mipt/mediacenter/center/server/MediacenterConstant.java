package com.mipt.mediacenter.center.server;

/**
 * 
 * @author fang
 * 
 */
public class MediacenterConstant {
	public static final String LOCAL_SDCARD_PATH = "/mnt/sdcard";
	public static final String LAST_PLYA_TIEM = "last_play_time";
	public static final String LAST_ADD_TIEM = "last_add_time";
	public static final String INTENT_EXTRA = "path_extra";
	public static final String INTENT_FIRSTTIME = "intent_first";
	public static final String INTENT_PATH = "intent_path";
	public static final String INTENT_EXTRA_DLAN = "intent_dlan";
	public static final String INTENT_TYPE_VIEW = "intent_data_type";
	public static final int MESSAGE_ADD = 1;
	public static final int MESSAGE_REMOVE = 2;
	public static final int ACTIVITYR_RESULT_CODE = 10;
	public static final String ALBUM_IMG_SMALL = "small";
	public static class IntentFlags {
		public static final String TAG_ID = "tag_id";
		public static final String BACK_BUTTON_ID = "back_id";
		public static final int FAV_ID = 1;
		public static final int MUSIC_ID = 2;
		public static final int PIC_ID = 3;
		public static final int VIDEO_ID = 4;
	}

	public static class FileViewType {
		public static final int VIEW_DIR = 1;
		public static final int VIEW_FILE = 2;
		public static final int VIEW_GENRE = 3;
		public static final int VIEW_ALBUM = 4;
		public static final int VIEW_ARTIST = 5;
		public static final int VIEW_MSUIC = 6;
	}

	public interface DlnaConstant {
		String URI = "content://mipt.ott_setting/conf";
		String CONDITION = "confgroup = \"ott_device_info\" and name = \"ott_device_dlna_name\" ";
	}
}
