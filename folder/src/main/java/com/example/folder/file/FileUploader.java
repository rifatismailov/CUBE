package com.example.folder.file;

import android.app.Activity;
import android.content.Context;

import com.example.folder.file.progress.ProgressRequestBody;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FileUploader {

    private FileHandler fileHandler;
    private FileOMG fileOMG;
    String messageId;
    Context context;
    public FileUploader(FileHandler fileHandler, Context context, String messageId) {
        this.fileHandler = fileHandler;
        this.context=context;
        this.fileOMG=(FileOMG)context;
        this.messageId=messageId;
    }

    private static final String SERVER_URL = "http://192.168.1.237:8020/api/files/upload"; // Змініть IP на ваш

    public void uploadFile(File file)throws InterruptedException {

        OkHttpClient client = new OkHttpClient();
        //Thread.sleep(1000);

        //fileHandler.closeDialog();
        // Створюємо ProgressRequestBody для моніторингу прогресу
        ProgressRequestBody fileBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage)  {
               // fileHandler.setProgress("Завантаження: " + percentage + "%");
                // Оновлюємо прогрес у головному потоці
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileOMG.setProgressShow(messageId,percentage);
                        }
                    });
                }

               // if(percentage==100) fileHandler.closeDialog();
            }

            @Override
            public void onError() {
              //  fileHandler.setProgress("Помилка під час завантаження");
            }

            @Override
            public void onFinish() {
           //     fileHandler.setProgress("Завантаження завершено");
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