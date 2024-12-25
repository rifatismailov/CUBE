package com.example.folder.download;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.example.folder.Folder;
import com.example.folder.file.FileDetect;
import com.example.folder.file.FileOMG;

import java.io.File;
import java.net.URL;

public class Downloader implements FileDownload.DownloadHandler {

    private final Context context;
    private final FileOMG fileOMG;
    private final Folder folder;
    private final String fileName;
    private final String directory;
    private final int position;
    private final String messageId;

    public Downloader(Context context, URL url, int position, String messageId) {
        File externalDir = new File(context.getExternalFilesDir(null), "cube");
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
        }
        fileName = getFileNameFromUrl(url.toString());
        new FileDownload(this).downloadFile(url, new File(externalDir + "/" + fileName));
        this.directory = externalDir.getAbsolutePath();
        this.context = context;
        folder = (Folder) context;
        this.fileOMG = (FileOMG) context;
        this.position = position;
        this.messageId = messageId;
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

    @Override
    public void setProgress(int progress) {
        // Оновлюємо прогрес у головному потоці
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId, progress, ""));
        }

    }

    @Override
    public void showDetails(String info) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(messageId, 0, info));
        }
    }


    @Override
    public void onFinish() {
        try {
            new Handler().postDelayed(() -> {
                FileDetect fileDetect = new FileDetect();
                folder.updateItem(position, directory + "/" + fileName, fileDetect.getFileHash(directory + "/" + fileName, "SHA-256"));
            }, 1);
        } catch (Exception e) {
            // Handle any exceptions here
        }
    }
}
