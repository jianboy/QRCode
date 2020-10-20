package me.yoqi.android.qrcode.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.yoqi.android.qrcode.model.Config;

/**
 * 用户表操作类
 *
 * @author liuyuqi.gov@msn.cn
 * @date 2020-10-20
 */
public class UserDao {
    private QRCodeOpenHelper openHelper;
    private SQLiteDatabase db;

    public UserDao(Context context) {
        openHelper = new QRCodeOpenHelper(context);
        db = openHelper.getReadableDatabase();//数据库对象
    }

    public void addUser() {

    }

    public void addUsers() {

    }

    /**
     * 删除表
     */
    public void delete() {
        db.delete(Config.TABLE_USER, null, null);
        db.close();
    }

    /**
     * 通过id更新用户表
     *
     * @param id
     */
    public void update(int id) {
        // TODO: 2020-10-20
    }

}