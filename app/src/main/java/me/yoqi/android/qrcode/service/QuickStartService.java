package me.yoqi.android.qrcode.service;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import me.yoqi.android.qrcode.CaptureActivity;
import me.yoqi.android.utils.SPUtils;
import me.yoqi.android.utils.ui.SimplexToast;

/**
 * 下拉快速启动
 *
 * @author liuyuqi.gov@msn.cn
 * @createTime 2020-10-14
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickStartService extends TileService {
    Tile tile;
    SPUtils spUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        spUtils = SPUtils.getInstance(this);
    }

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
        tile = getQsTile();
        int state = tile.getState();
        if (state == Tile.STATE_INACTIVE) {
            tile.setState(Tile.STATE_ACTIVE);
            spUtils.putBoolean("flag_qrcode", true);
        } else {
            spUtils.putBoolean("flag_qrcode", false);
        }
        tile.updateTile();

        SimplexToast.show(this, state + "");
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(intent);
    }

    /**
     * 打开下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
     */
    @Override
    public void onStartListening() {
        super.onStartListening();
        tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean flag = spUtils.getBoolean("flag_qrcode", false); // 是否启动
        tile.setState(flag ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}