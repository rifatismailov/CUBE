package com.example.folder.download;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.folder.file.progress.ProgressResponseBody;
import okhttp3.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Клас для завантаження файлів з підтримкою відображення прогресу.
 */
public class FileDownload implements FileDecryption.DecryptionHandle{

    private final DownloadHandler handler;

    /**
     * Конструктор класу FileDownload.
     *
     * @param handler Обробник подій завантаження.
     */
    public FileDownload(DownloadHandler handler) {
        this.handler = handler;
    }

    /**
     * Завантажує файл з вказаного URL у зазначену папку.
     *
     * @param fileUrl        URL файлу.
     * @param destinationFile Файл, куди потрібно зберегти завантаження.
     */
    public void downloadFile(URL fileUrl, File destinationFile) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), new ProgressResponseBody.DownloadCallbacks() {
                                @Override
                                public void onProgressUpdate(int percentage) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        handler.setProgress(percentage);
                                        if (percentage == 100) {
                                            onFinish();
                                        }
                                    });
                                }

                                @Override
                                public void onError(String e) {
                                    new Handler(Looper.getMainLooper()).post(() -> handler.showDetails("Error during download"));
                                }

                                @Override
                                public void onFinish() {
                                }
                            }))
                            .build();
                })
                .build();

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> handler.showDetails("Request error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    handler.showDetails("Server response error");
                    return;
                }

                try {
                    assert response.body() != null;
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                        byte[] buffer = new byte[2048];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        new Handler(Looper.getMainLooper()).post(handler::onFinish);
                    }
                    verifyFileIntegrity(destinationFile);
                } catch (IOException e) {
                    handler.showDetails("Error saving file: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Перевірка цілісності файлу за допомогою хешу SHA-256.
     *
     * @param file Файл для перевірки.
     */
    private void verifyFileIntegrity(File file) {
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192]; // Оптимальний розмір буфера
            int bytesRead;

            // Потокове обчислення хешу
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            // Перетворення хешу у шістнадцятковий рядок
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e("FileDownload", "Error calculating file hash: " + e.getMessage());
        }
    }


    @Override
    public void stopDecryption() {
        handler.onFinish();
    }

    /**
     * Інтерфейс для обробки подій завантаження файлів.
     */
    public interface DownloadHandler {
        void setProgress(int progress);
        void showDetails(String info);
        void onFinish();
    }
}
