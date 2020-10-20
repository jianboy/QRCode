package me.yoqi.android.qrcode.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_history);
        mContext = this;
        initView();
    }

    private void initView() {
        lvHistory = findViewById(R.id.lv_history);
        scanHistoryDao = new ScanHistoryDao(mContext);

        // 获取所有 content
        String[] contents = scanHistoryDao.getContents();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.lv_content, contents);
        lvHistory.setAdapter(adapter);
    }

}