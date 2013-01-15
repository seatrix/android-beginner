/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
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

	public static FileSortHelper getInstance() {
		if (instance == null)
			instance = new FileSortHelper();
		return instance;
	}

	private boolean mFileFirst;

	private HashMap<SortMethod, Comparator> mComparatorList = new HashMap<SortMethod, Comparator>();

	private FileSortHelper() {
		sMethod = SortMethod.name;
		mComparatorList.put(SortMethod.name, cmpName);
		mComparatorList.put(SortMethod.size, cmpSize);
		mComparatorList.put(SortMethod.date, cmpDate);
		mComparatorList.put(SortMethod.type, cmpType);
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

	public Comparator getComparator() {
		return mComparatorList.get(sMethod);
	}

	public Comparator getComparator(SortMethod _sMethod) {
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

	private Comparator cmpName = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return object1.fileName.compareToIgnoreCase(object2.fileName);
		}
	};

	private Comparator cmpSize = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object1.fileSize - object2.fileSize);
		}
	};

	private Comparator cmpDate = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object2.modifiedDate - object1.modifiedDate);
		}
	};

	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}

	private Comparator cmpType = new FileComparator() {
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
