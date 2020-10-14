package me.yoqi.qrcode.service;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import me.yoqi.qrcode.CaptureActivity;

/**
 * 下拉快速启动
 *
 * @author liuyuqi.gov@msn.cn
 * @createTime 2020-10-14
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickStartService extends TileService {

    /**
     * 当用户从Edit栏添加到快速设置中调用
     */
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        //添加成功
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        //移除成功
    }

    @Override
    public void onClick() {
        super.onClick();
        int state = getQsTile().getState();
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(intent);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }
}