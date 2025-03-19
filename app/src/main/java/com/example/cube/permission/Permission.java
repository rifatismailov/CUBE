package com.example.cube.permission;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Клас для роботи з дозволами на доступ до файлової системи.
 */
public class Permission {
    private static final int PERMISSION_REQUEST_CODE = 1; // Код запиту дозволу
    private final Activity activity;

    /**
     * Конструктор, який перевіряє, чи є у додатка необхідні дозволи,
     * та запитує їх у разі відсутності.
     *
     * @param activity Активність, у якій виконується перевірка дозволів.
     */
    public Permission(Activity activity) {
        this.activity = activity;
        if (!checkPermission()) {
            requestPermission();
        }
    }

    /**
     * Перевіряє, чи надано необхідні дозволи для роботи з файлами.
     *
     * @return true, якщо дозволи надано, інакше false.
     */
    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Запитує у користувача дозвіл на доступ до файлової системи.
     */
    public void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // Запит дозволу для Android 11 (API 30) і вище
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", activity.getApplicationContext().getPackageName())));
                activity.startActivityForResult(intent, 2296);
            } catch (Exception e) {
                // Альтернативний спосіб запиту дозволу
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, 2296);
            }
        } else {
            // Запит дозволу для версій Android нижче 11
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
}
