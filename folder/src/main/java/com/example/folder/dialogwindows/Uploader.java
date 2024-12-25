package com.example.folder.dialogwindows;

import android.app.Activity;
import android.content.Context;

import com.example.folder.file.FileOMG;
import com.example.folder.file.progress.ProgressRequestBody;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class Uploader {
    private FileOMG fileOMG;
    String messageId;
    Context context;
    private  String server_address; // Змініть IP на ваш
    public Uploader(Context context, String messageId, String server_address) {
        this.context=context;
        this.fileOMG=(FileOMG)context;
        this.messageId=messageId;
        this.server_address=server_address;
    }
    
    public void uploadFile(File file)throws InterruptedException {

        OkHttpClient client = new OkHttpClient();
        ProgressRequestBody fileBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage)  {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId,percentage,""));
                }
            }

            @Override
            public void onError() {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId,0,"ERROR: to sending"));
                }
            }

            @Override
            public void onFinish() {
            }
        });

        // Створюємо MultipartBody, додаючи файл та інші частини, якщо необхідно
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody) // додаємо файл
                .build();

        // Створюємо запит
        Request request = new Request.Builder()
                .url(server_address)
                .post(requestBody)
                .build();

        // Викликаємо запит на сервер
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId,0,"ERROR: "+e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId,0,"ERROR: server is not responding."));

                } else {
                    System.out.println("Відповідь сервера: " + response.body().string());
                }
            }
        });
    }
}