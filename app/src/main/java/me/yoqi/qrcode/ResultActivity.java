package me.yoqi.qrcode;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import me.yoqi.qrcode.utils.StringUtils;

public class ResultActivity extends AppCompatActivity {

    private EditText et_result;
    private Button btn_url;
    ClipboardManager myClipboard;
    String text;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        initView();
        Intent intent = getIntent();
        text = intent.getStringExtra("text");
        et_result.setText(text);
        if (StringUtils.isHttpUrl(text)) {
            btn_url.setVisibility(View.VISIBLE);
        } else {
            btn_url.setVisibility(View.GONE);
        }

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
    }

    private void initView() {
        et_result = findViewById(R.id.et_result);
        btn_url = findViewById(R.id.btn_url);
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
