package me.yoqi.android.qrcode.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.yoqi.android.qrcode.model.Config;
import me.yoqi.android.qrcode.model.ScanHistory;

/**
 * 扫码记录 操作类
 *
 * @author liuyuqi.gov@msn.cn
 * @date 2020-10-20
 */
public class ScanHistoryDao {
    private QRCodeOpenHelper openHelper;

    public ScanHistoryDao(Context context) {
        openHelper = new QRCodeOpenHelper(context);
    }

    /**
     * 删除表
     */
    public void delete() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        db.delete(Config.TABLE_HISTORY, null, null);
        db.close();
    }

    /**
     * 删除一条数据 : DELETE FROM User WHERE _ID=1
     *
     * @param id qrcode id
     */
    public void delete(int id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        db.delete(Config.TABLE_HISTORY, "_ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 插入历史记录
     *
     * @param scanHistories 一条或多条
     */
    public void addHistorys(ArrayList<ScanHistory> scanHistories) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        for (ScanHistory scanHistory : scanHistories) {
            ContentValues values = new ContentValues();
            values.put("content", scanHistory.getContent());
            values.put("created", scanHistory.getCreated());
            values.put("isUrl", scanHistory.getIsUrl());
            db.insert(Config.TABLE_HISTORY, null, values);
        }
        db.close();
    }

    /**
     * 增加一条数据
     *
     * @param scanHistory
     */
    public void addHistory(ScanHistory scanHistory) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", scanHistory.getContent());
        values.put("created", scanHistory.getCreated());
        values.put("isUrl", scanHistory.getIsUrl());
        db.insert(Config.TABLE_HISTORY, null, values);
        db.close();
    }

    /**
     * 获取多条数据
     *
     * @return
     */
    public ArrayList<ScanHistory> getHistories() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        ArrayList<ScanHistory> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "select * from scanHistory order by _ID",
                null);// 查询获取数据

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ScanHistory scanHistory = new ScanHistory();
                scanHistory.setContent(cursor.getString(1));
                scanHistory.setCreated(cursor.getString(2));
                scanHistory.setIsUrl(cursor.getInt(3));
                list.add(scanHistory);
            }
        }
        db.close();
        cursor.close();
        return list;
    }

    /**
     * 获取所有扫码内容
     *
     * @return
     */
    public String[] getContents() {
        List<String> tmp = new ArrayList<String>();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select content from scanHistory order by _ID",
                null);// 查询获取数据

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                tmp.add(cursor.getString(0));
            }
        }
        db.close();
        cursor.close();
        return tmp.toArray(new String[0]);
    }

    /**
     * 通过id查询一条数据
     *
     * @param id
     * @return
     */
    public ScanHistory getHistory(int id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        ScanHistory scanHistory = new ScanHistory();
        Cursor cursor = db.query(Config.TABLE_HISTORY, new String[]{"_ID", "content",
                        "created", "isUrl"},
                null, new String[]{String.valueOf(id)}, null,
                null, "_ID");
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                scanHistory.setContent(cursor.getString(1));
                scanHistory.setCreated(cursor.getString(2));
                scanHistory.setIsUrl(cursor.getInt(3));
            }
        }
        db.close();
        cursor.close();
        return scanHistory;

    }

    /**
     * 更新数据
     *
     * @param isUrl 是否是链接
     */
    public void updateIsUrl(boolean isUrl, int id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("isurl", 0);
        db.update(Config.TABLE_HISTORY, values, "_ID=?", new String[]{String.valueOf(id)});
        db.close();
    }
}