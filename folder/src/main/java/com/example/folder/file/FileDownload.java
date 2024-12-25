package com.example.folder.file;

import android.os.Handler;
import android.os.Looper;

import com.example.folder.file.progress.ProgressResponseBody;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

public class FileDownload {
    private static final Logger LOGGER = Logger.getLogger(FileDownload.class.getName());
    private final FileHandler fileHandler;

    public FileDownload(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

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
                                        fileHandler.setProgress(percentage);
                                        LOGGER.info("Завантаження: " + percentage + "%");
                                        if (percentage == 100) fileHandler.onFinish();
                                    });
                                }

                                @Override
                                public void onError() {
                                    // Оновлення статусу на основному потоці
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                       // fileHandler.setProgress("Помилка під час завантаження");
                                        LOGGER.severe("Помилка під час завантаження");
                                    });
                                }

                                @Override
                                public void onFinish() {
                                    // Оновлення статусу на основному потоці
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                       // fileHandler.setProgress("Завантаження завершено");
                                        LOGGER.info("Завантаження завершено");
                                        fileHandler.showDirectory(null);
                                    });
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
                LOGGER.severe("Помилка запиту: " + e.getMessage());
                // Оновлення статусу на основному потоці у випадку помилки
                new Handler(Looper.getMainLooper()).post(() -> {
                   // fileHandler.setProgress("Помилка запиту");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    LOGGER.severe("Помилка відповіді сервера");
                    return;
                }

                // Зберігаємо завантажений файл
                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    LOGGER.info("Файл збережено: " + destinationFile.getAbsolutePath());
                    // Виклик onFinish на основному потоці
                    new Handler(Looper.getMainLooper()).post(() -> {
                        fileHandler.onFinish();
                    });
                } catch (IOException e) {
                    LOGGER.severe("Помилка запису файлу: " + e.getMessage());
                }
            }
        });
    }
}
