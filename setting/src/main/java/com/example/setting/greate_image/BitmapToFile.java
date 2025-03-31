package com.example.setting.greate_image;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Клас `BitmapToFile` призначений для збереження зображень у форматі Bitmap до файлів на пристрої.
 * Він надає метод для збереження Bitmap як файл у вказаній директорії.
 */
public class BitmapToFile {

    /**
     * Зберігає об'єкт Bitmap як файл з розширенням ".png" у вказаній директорії.
     * Зображення зберігається у форматі PNG з максимальною якістю.
     *
     * @param bitmap   Об'єкт Bitmap, який потрібно зберегти.
     * @param fileName Назва файлу без розширення. До назви буде додано ".png".
     * @param directory Директорія, в якій потрібно зберегти файл.
     * @return Назва збереженого файлу з розширенням ".png".
     */
    public String saveBitmapToFile(Bitmap bitmap, String fileName, File directory) {
        // Додаємо розширення ".png" до назви файлу
        fileName = fileName + ".png";

        // Створюємо файл у вказаній директорії
        File file = new File(directory, fileName);
        FileOutputStream outputStream = null;

        try {
            // Створюємо потік для запису в файл
            outputStream = new FileOutputStream(file);

            // Зберігаємо bitmap у форматі PNG з максимальною якістю (100)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            // Очищаємо потік
            outputStream.flush();
        } catch (IOException e) {
            // Виводимо інформацію про помилку, якщо виникла проблема з записом
            e.printStackTrace();
        } finally {
            // Закриваємо потік, якщо він відкритий
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Повертаємо назву файлу
        return fileName;
    }
}
