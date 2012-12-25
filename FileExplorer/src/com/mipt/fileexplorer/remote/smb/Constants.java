package com.mipt.fileexplorer.remote.smb;

import java.text.SimpleDateFormat;


public class Constants {
	public final static String TEST_IP = "192.168.51.44"; 
	
	public final static String DATE_FORMAT = "yy-MM-dd HH:mm:ss";
	public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
	
	public final static int GUI_STOP_NOTIFIER = 1;
	public final static int GUI_THREADING_NOTIIER = 2;
	
	public final static int REMOTE_PORT = 137;
	public final static int OUT_TIME_UNIT = 100;
}
