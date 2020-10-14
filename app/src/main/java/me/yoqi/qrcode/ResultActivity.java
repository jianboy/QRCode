package me.yoqi.qrcode;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private EditText et_result;
    ClipboardManager myClipboard;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        initView();
        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        et_result.setText(text);

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
    }

    private void initView() {
        et_result = (EditText) findViewById(R.id.et_result);
    }
}
