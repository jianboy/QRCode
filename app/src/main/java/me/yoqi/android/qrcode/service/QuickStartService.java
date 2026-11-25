package me.yoqi.android.qrcode.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import me.yoqi.android.qrcode.CaptureActivity;

/**
 * 下拉快速启动
 *
 * @author liuyuqi.gov@msn.cn
 * @createTime 2020-10-14
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickStartService extends TileService {
    private static final String PREFS_NAME = "qrcode_prefs";
    private static final String KEY_FLAG_QRCODE = "flag_qrcode";

    Tile tile;
    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * 当用户从Edit栏添加到快速设置中调用
     */
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        super.onClick();
        tile = getQsTile();
        if (tile != null) {
            int state = tile.getState();
            if (state == Tile.STATE_INACTIVE) {
                tile.setState(Tile.STATE_ACTIVE);
                prefs.edit().putBoolean(KEY_FLAG_QRCODE, true).apply();
            } else {
                prefs.edit().putBoolean(KEY_FLAG_QRCODE, false).apply();
            }
            tile.updateTile();
            Toast.makeText(this, String.valueOf(state), Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, CaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            startActivityAndCollapse(pendingIntent);
        } else {
            startActivityAndCollapse(intent);
        }
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
        boolean flag = prefs.getBoolean(KEY_FLAG_QRCODE, false); // 是否启动
        tile.setState(flag ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
