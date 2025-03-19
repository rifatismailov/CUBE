package com.example.folder.upload;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.folder.FileData;
import com.example.folder.file.FileOMG;
import com.example.folder.file.progress.ProgressRequestBody;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

/**
 * Клас Uploader відповідає за завантаження файлів на сервер.
 */
public class Uploader {
    private final FileOMG fileOMG;
    private final String positionId;
    private final Context context;
    private final String server_address; // Адреса сервера

    /**
     * Конструктор класу Uploader.
     *
     * @param context        Контекст додатку.
     * @param positionId     Ідентифікатор позиції для оновлення прогресу.
     * @param server_address Адреса сервера, на який буде завантажено файл.
     */
    public Uploader(Context context, String positionId, String server_address) {
        this.context = context;
        this.fileOMG = (FileOMG) context;
        this.positionId = positionId;
        this.server_address = server_address;
    }

    /**
     * Метод для завантаження файлу на сервер.
     *
     * @param file Файл, який потрібно завантажити.
     */
    public void uploadFile(File file) {
        OkHttpClient client = new OkHttpClient();

        // Обгортка файлу для відстеження прогресу завантаження
        ProgressRequestBody fileBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                setProgress(positionId, percentage, "");
            }

            @Override
            public void onError(String e) {
                setProgress(positionId, 0, "ERROR:" + e);
            }

            @Override
            public void onFinish() {
                // Викликається після завершення завантаження
            }
        });

        // Формування запиту з файлом
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(server_address)
                .post(requestBody)
                .build();

        // Виконання HTTP-запиту
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setProgress(positionId, 0, "ERROR:Sending error");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    setProgress(positionId, 0, "ERROR:Sending error");
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        try {
                            fileOMG.setProgressShow(positionId, 100, "File successfully uploaded");
                            fileOMG.endProgress(positionId, "end");
                            FileData.deleteFile(context,file.getAbsolutePath());
                        } catch (Exception e) {
                            Log.e("Uploader", "An error occurred while processing the server response.: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void setProgress(String positionId, int progress, String info) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, progress, info));
        }
    }
}
