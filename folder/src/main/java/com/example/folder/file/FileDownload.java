package com.example.folder.file;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.example.folder.file.progress.ProgressResponseBody;
import okhttp3.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Клас для завантаження файлів з підтримкою відображення прогресу.
 */
public class FileDownload {

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
     * Отримує ім'я файлу з URL.
     *
     * @param url URL файлу.
     * @return Ім'я файлу.
     */
    public String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
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
                                    // Оновлення прогресу на основному потоці
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        handler.setProgress(percentage);
                                        if (percentage == 100) handler.onFinish();
                                    });
                                }

                                @Override
                                public void onError() {
                                    // Оновлення статусу на основному потоці
                                    new Handler(Looper.getMainLooper()).post(() -> handler.showDetails("Error during download"));
                                }

                                @Override
                                public void onFinish() {
                                    // Оновлення статусу на основному потоці
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
                // Помилка запиту
                new Handler(Looper.getMainLooper()).post(() -> handler.showDetails("Request error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    handler.showDetails("Server response error");
                    return;
                }

                // Зберігаємо завантажений файл
                try {
                    assert response.body() != null;
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                        byte[] buffer = new byte[2048];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        // Виклик onFinish на основному потоці
                        new Handler(Looper.getMainLooper()).post(handler::onFinish);
                    }
                } catch (IOException e) {
                    handler.showDetails("Error saving file: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Інтерфейс для обробки подій завантаження файлів.
     */
    public interface DownloadHandler {
        /**
         * Встановлює прогрес завантаження.
         *
         * @param progress Прогрес у відсотках.
         */
        void setProgress(int progress);

        /**
         * Відображає деталі завантаження.
         *
         * @param info Інформація для відображення.
         */
        void showDetails(String info);

        /**
         * Викликається після завершення завантаження.
         */
        void onFinish();
    }
}
