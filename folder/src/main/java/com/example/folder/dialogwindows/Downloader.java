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

public class Downloader implements FileHandler {

    Context context;
    Activity activity;
    FileOMG fileOMG;
    Folder folder;
    String fileName;
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
        this.activity=(Activity) context;
        this.fileOMG=(FileOMG)context;
        this.position = position;
        this.messageId=messageId;
    }


    @Override
    public void setProgress(int progress) {
        // Оновлюємо прогрес у головному потоці
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //infoFile.setText(progress);
                   fileOMG.setProgressShow(messageId,progress);
                }
            });
        }

    }

    @Override
    public void showDirectory(String analogDir) {

    }

    @Override
    public void closeDialog() {

    }

    @Override
    public void onFinish() {
        try {

            // Use a Handler to delay the execution without blocking the UI thread
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FileDetect fileDetect = new FileDetect();
                    folder.updateItem(position, directory + "/" + fileName, fileDetect.getFileHash(directory + "/" + fileName, "SHA-256"));


                }
            }, 1); // Delay of 100 milliseconds
        } catch (Exception e) {
            // Handle any exceptions here
        }
    }
}
