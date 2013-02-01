package com.mipt.mediacenter.center.db;

import java.util.ArrayList;

import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public interface DeviceDatabaseIfc {
	public void addFile(FileInfo  fileInfo);
	public void DeleteFile(FileInfo  fileInfo);	
	public ArrayList<FileInfo> getFiles(int type,long time,int size);	

}
