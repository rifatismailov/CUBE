package com.example.folder.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.folder.ReturnOpen;

import java.io.ByteArrayOutputStream;

public class ConversionImage extends AsyncTask<Bitmap, Integer, byte[]> {

    private Context context;
    private ReturnOpen returnOpen;
    String directory;
    private ProgressFile progressFile;
    int width;
    int height;

    public ConversionImage(Context context, ReturnOpen returnOpen, String directory) {
        this.context = context;
        this.returnOpen = returnOpen;
        this.directory = directory;
        progressFile = new ProgressFile(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressFile.show();
    }

    @Override
    protected byte[] doInBackground(Bitmap... bitmaps) {
        byte[] byteArray = new byte[0];
        progressFile.setMessage("Конвертація зображення...");

        try {

            if (bitmaps.length == 0) {
            }


            Bitmap bitmap = bitmaps[0];
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();

            final int totalPixels = bitmap.getWidth() * bitmap.getHeight();
            int processedPixels = 0;

            for (int y = 0; y < bitmap.getHeight(); y++) {
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    int pixel = bitmap.getPixel(x, y);
                    // Ваш код для обработки пикселей и конвертации в байты
                    // Обновление прогресса
                    processedPixels++;
                    int progress = (int) ((processedPixels / (float) totalPixels) * 100);
                    progressFile.setProgress(progress);

                    // Добавление байтов в массив
                    byte red = (byte) ((pixel >> 16) & 0xFF);
                    byte green = (byte) ((pixel >> 8) & 0xFF);
                    byte blue = (byte) (pixel & 0xFF);
                    stream.write(red);
                    stream.write(green);
                    stream.write(blue);
                }
            }
            byteArray = stream.toByteArray();
            // folder.openFile(byteArray, bitmap.getWidth(), bitmap.getHeight());
            // Your background code that may throw exceptions
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArray;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressFile.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(byte[] byteArray) {
        progressFile.dismiss();

        if (byteArray != null) {
            returnOpen.openFile(byteArray, width, height);
            Log.d("ImageConversion", "Конвертація зображення. розмір массива байтів: " + byteArray.length + " байт");
            Toast.makeText(context, "Конвертация завершена", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("ImageConversion", "Помилка при конвертації зображення.");
            Toast.makeText(context, "Помилка при конвертації зображення", Toast.LENGTH_SHORT).show();
        }
    }
}