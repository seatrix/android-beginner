package com.mipt.mediacenter.center.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * 
 */
public class DatabaseImpl implements DatabaseIfc {
	private static final String DB_NAME = "mediacenter.db";
	private static final String TABLE_FILE = "file";
	private Context mActivity;

	public DatabaseImpl(Context activity) {
		this.mActivity = activity;
		create();
	}

	/**
	 * Initializes database and tables
	 */
	private void create() {
		SQLiteDatabase db = getDb();
		// create tables if necessary
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_FILE
				+ " (file_id VARCHAR UNIQUE,file_name VARCHAR,file_path VARCHAR,"
				+ " file_date LONG, file_size VARCHAR, add_date VARCHAR, file_type INTEGER,"
				+ " img_url VARCHAR,db_id VARCHAR,show_type INTEGER);");

		db.close();
	}

	private SQLiteDatabase getDb() {
		return mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE,
				null);
	}

	private String queryForFile(String name, SQLiteDatabase db) {
		String fileName = null;
		String[] columns = { "file_name" };
		String[] selectionArgs = { name };
		Cursor query = db.query(TABLE_FILE, columns, "file_name = ?",
				selectionArgs, null, null, null);
		if (query != null && query.getCount() > 0) {
			int columnIndex = query.getColumnIndex("file_name");
			query.moveToFirst();
			fileName = "" + query.getString(columnIndex);
		}
		query.close();
		return fileName;
	}

	@Override
	public void addFile(FileInfo fileInfo) {
		SQLiteDatabase db = getDb();
		ContentValues values = new ContentValues();
		values.putAll(new FileBuilder().deconstruct(fileInfo));
		String[] whereArgs = { fileInfo.fileName };
		int row_count = db.update(TABLE_FILE, values, "file_name=?", whereArgs);
		if (row_count == 0) {
			db.insert(TABLE_FILE, null, values);
		}
		db.close();
	}

	@Override
	public void DeleteFile(FileInfo fileInfo) {
		SQLiteDatabase db = getDb();
		String[] whereArgs = { "" + fileInfo.fileName };
		db.delete(TABLE_FILE, "file_name = ?", whereArgs);
		db.close();
	}

	@Override
	public ArrayList<FileInfo> getFiles(int type, long time, int size) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getDb();
		ArrayList<FileInfo> returnFile = new ArrayList<FileInfo>();
		StringBuffer orderby = new StringBuffer(10);
		orderby.append("add_date").append(" DESC");
		String[] whereArgs = { "" + type, "" + time };
		String limit = null;
		if (size != 0) {
			limit = size + "";
		}
		Cursor query = db.query(TABLE_FILE, null, null, null, null, null,
				orderby.toString(), limit);
		if (query != null) {
			query.moveToFirst();
			while (!query.isAfterLast()) {
				FileInfo file = new FileBuilder().build(query);
				returnFile.add(file);
				query.moveToNext();
			}
			query.close();
		}
		db.close();
		return returnFile;
	}
}
