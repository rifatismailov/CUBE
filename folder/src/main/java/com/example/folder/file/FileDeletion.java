package com.example.folder.file;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class FileDeletion {

    public static boolean deleteFile(Context context, String fileName) {
        // Отримуємо зовнішню директорію додатку
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            // Створюємо об'єкт файлу, який потрібно видалити
            File fileToDelete = new File(externalDir, fileName);

            if (fileToDelete.exists()) {
                // Видаляємо файл
                boolean deleted = fileToDelete.delete();
                if (deleted) {
                    Log.d("FileDeletion", "Файл " + fileName + " було успішно видалено");
                    return true;
                } else {
                    Log.e("FileDeletion", "Не вдалося видалити файл " + fileName);
                }
            } else {
                Log.e("FileDeletion", "Файл " + fileName + " не існує");
            }
        } else {
            Log.e("FileDeletion", "Зовнішня директорія недоступна");
        }
        return false;
    }
}
