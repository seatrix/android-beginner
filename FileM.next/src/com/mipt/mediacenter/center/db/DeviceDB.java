package com.mipt.mediacenter.center.db;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.FileObserver;
import android.util.Log;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.R.string;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.FileInfo;

/**
 * 
 * @author fang
 * @version $Id: 2013-02-25 09:26:01Z slieer $ 
 * 
 */
public class DeviceDB {
    private static final String TAG = "DeviceDatabaseImpl";
	private static final String DB_NAME = "fileMgr.db";
	private static final String TABLE_FILE = "file";
	private Context mActivity;

	public DeviceDB(Context activity) {
		this.mActivity = activity;
		create();
	}

	private void create() {
		SQLiteDatabase db = getDb();
		// create tables if necessary
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_FILE
				+ " (" 
				+ "file_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "file_name VARCHAR,"
				+ "file_path VARCHAR unique,"
				+ "file_date LONG,"
				+ "file_size VARCHAR,"
				+ "add_date VARCHAR, "
				+ "file_type INTEGER,"
				+ "img_url VARCHAR"
				//+ "db_id VARCHAR," 
				//+ "show_type INTEGER"
				+ ");");

		db.close();
	}

	private SQLiteDatabase getDb() {
		return mActivity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
	}

	public void addFile(FileInfo... files) {
		SQLiteDatabase db = getDb();
		for(FileInfo fileInfo : files){
		    Log.i(TAG, "add file to db, " + fileInfo.toString());
		    
		    ContentValues values = new ContentValues();
		    values.putAll(DeviceDBUtils.deconstruct(fileInfo));
		    try{
		        db.insert(TABLE_FILE, null, values);
		    }catch(SQLiteConstraintException e){
		        Log.e(TAG, e.getMessage());
		    }
		}
		db.close();
	}

	public void deleteFile(String filePath) {
		SQLiteDatabase db = getDb();
//		String sql = "delete from file where file_path = '" + filePath + "'";
//		db.rawQuery(sql, null);
		db.delete(TABLE_FILE, "file_path = ?", new String[]{filePath});
		db.close();
	}
	
	public List<DeviceInfo> listSmbPath(){
	    SQLiteDatabase db = getDb();
	    String sql = "select file_path from file where file_path like 'smb%'";
	    Cursor c = db.rawQuery(sql, null);
	    c.moveToFirst();
	    List<DeviceInfo> result = new ArrayList<DeviceInfo>();
	    if(c.getCount() > 0){
	        while (! c.isAfterLast()){
	            String path = c.getString(0);
	            DeviceInfo info = new DeviceInfo(null, mActivity.getString(R.string.smb_share_device), path, null, null, DeviceInfo.TYPE_CIFS, true, R.drawable.lan_device_icon);
	            result.add(info);
	            c.moveToNext(); 
	        }
	    }
	    db.close();
	    return result.size() > 0 ? result : null;
	}
}
