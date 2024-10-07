package com.example.folder.file;

import com.example.folder.file.progress.ProgressRequestBody;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FileUploader {

    private DoHandler doHandler;

    public FileUploader(DoHandler doHandler) {
        this.doHandler = doHandler;
    }

    private static final String SERVER_URL = "http://192.168.193.183:8020/api/files/upload"; // Змініть IP на ваш

    public void uploadFile(File file) {

        OkHttpClient client = new OkHttpClient();

        // Створюємо ProgressRequestBody для моніторингу прогресу
        ProgressRequestBody fileBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                doHandler.setProgress("Завантаження: " + percentage + "%");
                if(percentage==100)doHandler.closeDialog();
            }

            @Override
            public void onError() {
                doHandler.setProgress("Помилка під час завантаження");
            }

            @Override
            public void onFinish() {
                doHandler.setProgress("Завантаження завершено");
            }
        });

        // Створюємо MultipartBody, додаючи файл та інші частини, якщо необхідно
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody) // додаємо файл
                .build();

        // Створюємо запит
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(requestBody)
                .build();

        // Викликаємо запит на сервер
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Помилка запиту");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Помилка відповіді сервера");
                } else {
                    System.out.println("Відповідь сервера: " + response.body().string());
                }
            }
        });
    }
}