package com.example.folder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.folder.dialogwindows.Open;

import java.io.ByteArrayOutputStream;

public class FileConversion extends Thread{
    String directory;
    Open open;

    public FileConversion(Open open,String directory){
        this.open=open;
        this.directory=directory;
    }
    @Override
    public void run() {
        Bitmap bMap = BitmapFactory.decodeFile(directory);
        convertStan(bMap);
        //convertWithProgress(bMap);
    }
    protected void convertStan(Bitmap bitmaps){
        int width = bitmaps.getWidth();
        int height = bitmaps.getHeight();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmaps.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        open.openFile(byteArray, width, height);
    }
    protected void convertWithProgress(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int totalPixels = bitmap.getWidth() * bitmap.getHeight();
        int processedPixels = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                // Ваш код для обработки пикселей

                // Добавление байтов в массив
                byte red = (byte) ((pixel >> 16) & 0xFF);
                byte green = (byte) ((pixel >> 8) & 0xFF);
                byte blue = (byte) (pixel & 0xFF);
                stream.write(red);
                stream.write(green);
                stream.write(blue);

                // Обновление процента выполнения
                processedPixels++;
                int progress = (int) ((processedPixels / (float) totalPixels) * 100);

                // Отправляем сообщение для обновления UI
            }
        }

        // Получение массива байтов после конвертации
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();
        open.openFile(byteArray, width, height);
    }



}
