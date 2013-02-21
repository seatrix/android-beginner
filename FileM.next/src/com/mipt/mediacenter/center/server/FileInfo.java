package com.mipt.mediacenter.center.server;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;

/**
 * 
 * @author fang
 * 
 */
public class FileInfo implements Serializable, Parcelable {
    private static final long serialVersionUID = 7745013311291995080L;
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
	public ArrayList<FileInfo> childs;

    @Override
    public String toString() {
        return "FileInfo [fileType=" + fileType + ", fileName=" + fileName + ", fileId=" + fileId
                + ", filePath=" + filePath + ", fileSize=" + fileSize + ", isDir=" + isDir
                + ", count=" + count + ", modifiedDate=" + modifiedDate + ", isHidden=" + isHidden
                + ", dbId=" + dbId + ", imgPath=" + imgPath + ", mCategory=" + mCategory
                + ", extra=" + extra + ", selected=" + selected + ", albumName=" + albumName
                + ", childs=" + (childs !=null ? childs.size() : 0) + "]";
    }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {

        @Override
        public FileInfo createFromParcel(Parcel source) {
            String[] arr = source.readStringArray();
            FileInfo info = new FileInfo();
            info.fileName = arr[0];
            info.filePath = arr[1];
            
            boolean[] bools = new boolean[1];
            source.readBooleanArray(bools);
            info.isDir = bools[0];
            return info;
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
    
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{fileName, filePath});
        dest.writeBooleanArray(new boolean[]{isDir});
    }
}
