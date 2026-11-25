package me.yoqi.android.qrcode.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.yoqi.android.qrcode.R;
import me.yoqi.android.qrcode.dao.ScanHistoryDao;

/**
 * 扫码历史记录
 *
 * @author liuyuqi.gov@msn.cn
 * @date 2020-10-20
 */
public class HistoryActivity extends AppCompatActivity {
    ScanHistoryDao scanHistoryDao;
    private Context mContext;
    ListView lvHistory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mContext = this;
        applySystemBarInsets();
        initView();
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.ll_history);
        if (root == null) {
            return;
        }
        final int padLeft = root.getPaddingLeft();
        final int padTop = root.getPaddingTop();
        final int padRight = root.getPaddingRight();
        final int padBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(padLeft + bars.left, padTop + bars.top, padRight + bars.right, padBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void initView() {
        lvHistory = findViewById(R.id.lv_history);
        scanHistoryDao = new ScanHistoryDao(mContext);

        // 获取所有 content
        String[] contents = scanHistoryDao.getContents();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.lv_content, contents);
        lvHistory.setAdapter(adapter);
    }
}
