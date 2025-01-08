package com.example.image_account;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapToFile {


    public String saveBitmapToFile(Bitmap bitmap, String fileName, File directory) {
        fileName = fileName + ".png";
        // Створюємо директорію для збереження зображень


        // Створюємо файл у вказаній директорії
        File file = new File(directory, fileName);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            // Зберігаємо бітмап як PNG або JPEG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileName;
    }

}
