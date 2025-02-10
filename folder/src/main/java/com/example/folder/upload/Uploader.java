package com.example.folder.upload;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.folder.file.FileOMG;
import com.example.folder.file.progress.ProgressRequestBody;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class Uploader {
    private FileOMG fileOMG;
    String positionId;
    Context context;
    private final String server_address; // Змініть IP на ваш

    public Uploader(Context context, String positionId, String server_address) {
        this.context = context;
        this.fileOMG = (FileOMG) context;
        this.positionId = positionId;
        this.server_address = server_address;
        Log.e("Uploader", "PositionId: " + positionId);

    }

    public void uploadFile(File file) throws InterruptedException {
        Log.e("Uploader", "uploadFile " + file);

        OkHttpClient client = new OkHttpClient();
        ProgressRequestBody fileBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
//                        if (percentage >100) {
//                            fileOMG.endProgress(positionId, "end");
//                        }
                        fileOMG.setProgressShow(positionId, percentage, "");
                    });
                }
            }

            @Override
            public void onError() {

                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "ERROR:Помилка-відправки"));
                }
            }

            @Override
            public void onFinish() {
            }
        });

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(server_address)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (context instanceof Activity) {
                    Log.e("Uploader", "Проблема інтернет-з'єднання:" + e);

                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "ERROR:Помилка-відправки"));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "ERROR:Помилка-відправки"));
                } else {

                    ((Activity) context).runOnUiThread(() -> {
                        try {
                            fileOMG.setProgressShow(positionId, 100, "Файл успішно завантажено");
                            fileOMG.endProgress(positionId, "end");
                            Log.e("Uploader", "Файл успішно завантажено: " + response.body().string());
                        } catch (Exception e) {
                            Log.e("Uploader", "ERROR: при обробці відповіді сервера: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
}
