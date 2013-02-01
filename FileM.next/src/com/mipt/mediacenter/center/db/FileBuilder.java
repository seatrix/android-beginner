package com.mipt.mediacenter.center.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.mipt.mediacenter.center.file.FileCategoryHelper;
import com.mipt.mediacenter.center.file.FileCategoryHelper.FileCategory;
import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class FileBuilder extends DatabaseBuilder<FileInfo> {

	@Override
	public FileInfo build(Cursor query) {
		int columnFileId = query.getColumnIndex("file_id");
		int columnFileName = query.getColumnIndex("file_name");
		int columnFilePath = query.getColumnIndex("file_path");
		int columnFileDate = query.getColumnIndex("file_date");
		int columnFileSize = query.getColumnIndex("file_size");
		int columnFileImg = query.getColumnIndex("img_url");
		int columnFileType = query.getColumnIndex("file_type");
		FileInfo file = new FileInfo();
		String fileSize = query.getString(columnFileSize);
		if (fileSize != null) {
			file.fileSize = new Long(fileSize);
		}
		file.fileName = query.getString(columnFileName);
		file.modifiedDate = query.getLong(columnFileDate);
		file.imgPath = query.getString(columnFileImg);
		file.filePath = query.getString(columnFilePath);
		//int type = query.getInt(columnFileType);
		
		FileCategory cat = FileCategoryHelper.getCategoryFromPath(file.fileName);
		file.mCategory = cat;
		return file;
	}

	@Override
	public ContentValues deconstruct(FileInfo file) {
		ContentValues values = new ContentValues();
		values.put("file_size", file.fileSize + "");
		values.put("file_name", file.fileName);
		values.put("file_path", file.filePath);
		values.put("file_date", file.modifiedDate);
		values.put("img_url", file.imgPath);
		return values;
	}

}
