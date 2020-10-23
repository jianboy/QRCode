package me.yoqi.android.qrcode.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.yoqi.android.qrcode.R;
import me.yoqi.android.qrcode.dao.ScanHistoryDao;
import me.yoqi.android.qrcode.model.ScanHistory;
import me.yoqi.android.utils.StringUtils;

/**
 * 扫描结果页面
 */
public class ResultActivity extends AppCompatActivity {

    ClipboardManager myClipboard;
    String text;
    Context mContext;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        mContext = this;
        initView();
    }

    private void initView() {
        EditText et_result = findViewById(R.id.et_result);
        Button btn_url = findViewById(R.id.btn_url);

        ScanHistory scanHistory = new ScanHistory();
        Intent intent = getIntent();
        text = intent.getStringExtra("text");
        et_result.setText(text);

        if (StringUtils.isHttpUrl(text)) {
            btn_url.setVisibility(View.VISIBLE);
            scanHistory.setIsUrl(1);
        } else {
            btn_url.setVisibility(View.GONE);
        }
        //剪切板
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
        //保存数据库
        ScanHistoryDao scanHistoryDao = new ScanHistoryDao(mContext);
        scanHistory.setCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        scanHistory.setContent(text);
        scanHistoryDao.addHistory(scanHistory);

        btn_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(text));//Url 就是你要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent); //启动浏览器
            }
        });
    }
}
