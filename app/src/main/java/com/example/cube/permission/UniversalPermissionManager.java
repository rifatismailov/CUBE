package com.example.cube.permission;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UniversalPermissionManager {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 2296;
    private final Activity activity;

    public UniversalPermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readPermission = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED &&
                    writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", activity.getApplicationContext().getPackageName())));
                activity.startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    public void handleRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Дозвіл отримано, можна виконувати дії
            } else {
                // Дозвіл не отримано, показати повідомлення користувачу
            }
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Дозвіл отримано, можна виконувати дії
                } else {
                    // Дозвіл не отримано, показати повідомлення користувачу
                }
            }
        }
    }
}
