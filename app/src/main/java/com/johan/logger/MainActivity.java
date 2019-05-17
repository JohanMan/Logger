package com.johan.logger;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.johan.android.logger.DailyLogger;
import com.johan.android.logger.Logger;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements PermissionHelper.OnPermissionCallback {

    private EditText contentView;
    private TextView logView;

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = (EditText) findViewById(R.id.content_view);
        logView = (TextView) findViewById(R.id.log_view);
        PermissionHelper.requestPermission(this, PERMISSIONS, this);
    }

    @Override
    protected void onDestroy() {
        DailyLogger.stopLog();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.handlePermissionResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionAccept(int requestCode, String... permissions) {
        String logPath = Environment.getExternalStorageDirectory() + "/Logger/";
        DailyLogger.startLog(logPath);
    }

    @Override
    public void onPermissionRefuse(int requestCode, String... permission) {

    }

    public void write(View view) {
        String content = contentView.getText().toString();
        DailyLogger.writeLog("MainActivity", content + "\n");
        contentView.setText("");
    }

    public void read(View view) {
        String log = DailyLogger.readLog();
        logView.setText(log);
    }

}
