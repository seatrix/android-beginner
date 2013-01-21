package com.mipt.mediacenter.center.server;

import java.io.Serializable;
import java.util.ArrayList;

import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;

/**
 * 
 * @author fang
 * 
 */
public class FileInfo implements Serializable {
	public final static int TYPE_VIDEO = 1;
	public final static int TYPE_MUSIC = 2;
	public final static int TYPE_PIC = 3;
	private static final long serialVersionUID = 1L;
	public int fileType;
	public String fileName;

	public String fileId;

	public String filePath;

	public long fileSize;

	public boolean isDir;

	public int count;

	public long modifiedDate;

	public boolean isHidden;

	public long dbId; // id in the database, if is from database use for get pic

	// public long fileDate;

	public String imgPath;

	public FileCategory mCategory;

	public boolean extra;

	public boolean selected;

	public String albumName; // for music

	public String mediaName;
	public String artist;// for music

	public int duration;// for music video

	public String genreName; // for music

	public ArrayList<FileInfo> childs;

	// @Override
	// public boolean equals(Object object) {
	// // TODO: Warning - this method won't work in the case the componentId
	// // fields are not set
	// if (!(object instanceof FileInfo)) {
	// return false;
	// }
	// FileInfo other = (FileInfo) object;
	// if ((this.filePath == null && other.filePath != null)
	// || (this.filePath != null && !this.filePath
	// .equals(other.filePath))) {
	// return false;
	// }
	// return true;
	// }
	//
	// @Override
	// public int hashCode() {
	// int hash = 0;
	// hash += (filePath != null ? filePath.hashCode() : 0);
	// return hash;
	// }
}
