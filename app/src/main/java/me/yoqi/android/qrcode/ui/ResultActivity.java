package me.yoqi.android.qrcode.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.yoqi.android.qrcode.R;
import me.yoqi.android.qrcode.dao.ScanHistoryDao;
import me.yoqi.android.qrcode.model.ScanHistory;

/**
 * 扫描结果页面
 */
public class ResultActivity extends AppCompatActivity {

    ClipboardManager myClipboard;
    String text;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        mContext = this;
        applySystemBarInsets();
        initView();
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.ll_result);
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
        EditText etResult = findViewById(R.id.et_result);
        Button btnUrl = findViewById(R.id.btn_url);

        ScanHistory scanHistory = new ScanHistory();
        Intent intent = getIntent();
        text = intent != null ? intent.getStringExtra("text") : null;
        if (text == null) {
            text = "";
        }
        etResult.setText(text);

        if (isHttpUrl(text)) {
            btnUrl.setVisibility(View.VISIBLE);
            scanHistory.setIsUrl(1);
        } else {
            btnUrl.setVisibility(View.GONE);
        }

        // 剪切板
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (myClipboard != null) {
            ClipData myClip = ClipData.newPlainText("text", text);
            myClipboard.setPrimaryClip(myClip);
        }

        // 保存数据库
        if (!text.isEmpty()) {
            ScanHistoryDao scanHistoryDao = new ScanHistoryDao(mContext);
            scanHistory.setCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            scanHistory.setContent(text);
            scanHistoryDao.addHistory(scanHistory);
        }

        btnUrl.setOnClickListener(v -> {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
            startActivity(viewIntent);
        });
    }

    private static boolean isHttpUrl(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        return (lower.startsWith("http://") || lower.startsWith("https://"))
                && Patterns.WEB_URL.matcher(trimmed).matches();
    }
}
