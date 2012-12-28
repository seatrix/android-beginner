
package com.explorer.ftp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**database operations to help
 *CNcomment: 数据库操作帮助
 * 
 * @author qian_wei
 */
public class DBHelper extends SQLiteOpenHelper {

    // database name
    public static final String DATABASE_NAME = "server.db";

    // database version
    public static final int DATABASE_VERSION = 1;

    // creat database
    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // add database data
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table ftp (_id integer primary key autoincrement,"
                + "ip varchar(100),port varchar(100),name varchar(100),pwd varchar(100),flag integer,nick varchar(100))";
        db.execSQL(sql);
        sql = "create table samba (_id integer primary key autoincrement,"
                + "server_ip varchar(100),nick_name varchar(100), work_path varchar(300),"
                + "account varchar(100),password varchar(100))";
        db.execSQL(sql);
    }

    // triggered when the database changes
    //CNcomment: 数据库变化时触发
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists ftp";
        db.execSQL(sql);
        sql = "drop table if exists samba";
        db.execSQL(sql);
        onCreate(db);
    }
}
