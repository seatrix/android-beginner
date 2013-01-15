package com.mipt.mediacenter.center.db;

import java.util.ArrayList;

import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public interface DatabaseIfc {
	public static final int RECENT_PLAY = 1;
	public static final int RECENT_ADD = 2;
	public static final int TYPE_VIDEO = 1;
	public static final int TYPE_MUSIC = 2;
	public static final int TYPE_PIC = 3;
	public void addFile(FileInfo  fileInfo);
	public void DeleteFile(FileInfo  fileInfo);	
	public ArrayList<FileInfo> getFiles(int type,long time,int size);	

}
