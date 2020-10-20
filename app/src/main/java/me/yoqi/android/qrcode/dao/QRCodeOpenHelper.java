package me.yoqi.android.qrcode.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import me.yoqi.android.qrcode.model.Config;

/**
 * 数据库操作类
 */
public class QRCodeOpenHelper extends SQLiteOpenHelper {
    public String tag = "QRCodeOpenHelper";

    /**
     * 首次创建表
     *
     * @see SQLiteOpenHelper#onCreate(SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(tag, "onCreate");
        String sql1 = "CREATE TABLE User ( _ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, pwd TEXT NOT NULL)";
        String sql2 = "CREATE TABLE scanHistory ( _ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, content TEXT NOT NULL, " +
                "created TEXT NOT NULL, isUrl INTEGER DEFAULT 0 )";
        db.execSQL(sql1);
        db.execSQL(sql2);
    }

    /**
     * 构造方法创建一个数据库 Creates a new instance of OrderOpenHelper.
     *
     * @param context 上下文对象
     */
    public QRCodeOpenHelper(Context context) {
        super(context, Config.DATA_BASE, null, 3);
        Log.i(tag, "OrderOpenHelper");
    }

    /**
     * 升级数据库会调用这个方法
     *
     * @param db         数据库对象
     * @param oldVersion 老版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(tag, "onUpgrade");
    }
}
