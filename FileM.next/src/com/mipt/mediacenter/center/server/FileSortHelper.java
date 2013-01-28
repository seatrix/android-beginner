/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 */

package com.mipt.mediacenter.center.server;

import java.util.Comparator;
import java.util.HashMap;

import com.mipt.mediacenter.utils.Util;

/**
 * 
 * @author fang
 * 
 */
public class FileSortHelper {
	public enum SortMethod {
		name, size, date, type
	}

	private SortMethod sMethod;
	private static FileSortHelper instance;
	private boolean mFileFirst;
	
	private HashMap<SortMethod, Comparator<FileInfo>> mComparatorList = new HashMap<SortMethod, Comparator<FileInfo>>();
	
	private FileSortHelper() {
	    sMethod = SortMethod.name;
	    mComparatorList.put(SortMethod.name, cmpName);
	    mComparatorList.put(SortMethod.size, cmpSize);
	    mComparatorList.put(SortMethod.date, cmpDate);
	    mComparatorList.put(SortMethod.type, cmpType);
	}

	public static FileSortHelper getInstance() {
		if (instance == null)
			instance = new FileSortHelper();
		return instance;
	}


	public void setSortMethod(SortMethod s) {
		sMethod = s;
	}

	public SortMethod getSortMethod() {
		return sMethod;
	}

	public void setFileFirst(boolean f) {
		mFileFirst = f;
	}

	public Comparator<FileInfo> getComparator() {
		return mComparatorList.get(sMethod);
	}

	public Comparator<FileInfo> getComparator(SortMethod _sMethod) {
		return mComparatorList.get(_sMethod);
	}

	private abstract class FileComparator implements Comparator<FileInfo> {

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			if (object1.isDir == object2.isDir) {
				return doCompare(object1, object2);
			}

			if (mFileFirst) {
				// the files are listed before the dirs
				return (object1.isDir ? 1 : -1);
			} else {
				// the dir-s are listed before the files
				return object1.isDir ? -1 : 1;
			}
		}

		protected abstract int doCompare(FileInfo object1, FileInfo object2);
	}

	private Comparator<FileInfo> cmpName = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return object1.fileName.compareToIgnoreCase(object2.fileName);
		}
	};

	private Comparator<FileInfo> cmpSize = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object1.fileSize - object2.fileSize);
		}
	};

	private Comparator<FileInfo> cmpDate = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object2.modifiedDate - object1.modifiedDate);
		}
	};

	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}

	private Comparator<FileInfo> cmpType = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			int result = Util.getExtFromFilename(object1.fileName)
					.compareToIgnoreCase(
							Util.getExtFromFilename(object2.fileName));
			if (result != 0)
				return result;

			return Util.getNameFromFilename(object1.fileName)
					.compareToIgnoreCase(
							Util.getNameFromFilename(object2.fileName));
		}
	};
}
