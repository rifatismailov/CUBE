package com.example.folder.dialogwindows;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.example.folder.Folder;
import com.example.folder.file.FileDetect;
import com.example.folder.file.FileDownload;
import com.example.folder.file.FileHandler;
import com.example.folder.file.FileOMG;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class Downloader implements FileDownload.DownloadHandler {

    private final Context context;
    private final Activity activity;
    private final FileOMG fileOMG;
    private final Folder folder;
    private final String fileName;
    private final String directory;
    int position;
    String messageId;

    public Downloader(Context context, URL url, int position, String messageId) {
        File externalDir = new File(context.getExternalFilesDir(null), "cube");
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
        }
        fileName = new FileDownload(this).getFileNameFromUrl(url.toString());
        new FileDownload(this).downloadFile(url, new File(externalDir + "/" + fileName));
        this.directory = externalDir.getAbsolutePath();
        this.context = context;
        folder = (Folder) context;
        this.activity = (Activity) context;
        this.fileOMG = (FileOMG) context;
        this.position = position;
        this.messageId = messageId;
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
