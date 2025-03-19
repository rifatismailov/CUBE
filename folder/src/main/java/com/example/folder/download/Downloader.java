package com.example.folder.download;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.folder.FileData;
import com.example.folder.file.Folder;
import com.example.folder.file.FileOMG;

import java.io.File;
import java.net.URL;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Клас Downloader є посередником між класом FileDownload та FileDecryption
 * спочатку запускає клас FileDownload
 * та очікуємо дій від FileDownload а саме завантаження файлу після запускаємо
 * клас FileDecryption робота якого полягає в дешифруванні файлу
 */
public class Downloader implements FileDownload.DownloadHandler, FileDecryption.DecryptionHandle {

    private final Context context;
    private final FileOMG fileOMG;
    private final Folder folder;
    private final String fileName;
    private String decryptedFileName;
    private final String directory;
    private final int position;
    private final String positionId;
    private String fileHash;
    private final DownloaderHandler downloaderHandler;

    /**
     * Основний конструктор класу  Downloader який приймає такі поля:
     *
     * @param context     Контекст основного активності
     * @param url         адреса де знаходиться файл
     * @param externalDir місце куди буде збережено файл
     * @param position    позиція у активності
     * @param positionId  ІД позиції у активності
     */
    public Downloader(Context context, URL url, File externalDir, int position, String positionId) {
        // Перевірка та створення директорії
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
            if (!mkdirs) {
                Log.e("Downloader", "Не вдалося створити директорію");
            }
        }

        fileName = getFileNameFromUrl(url.toString());
        new FileDownload(this).downloadFile(url, new File(externalDir + "/" + fileName));
        this.directory = externalDir.getAbsolutePath();
        this.context = context;
        folder = (Folder) context;
        downloaderHandler = (DownloaderHandler) context;
        this.fileOMG = (FileOMG) context;
        this.position = position;
        this.positionId = positionId;
    }

    /**
     * Основний конструктор класу  Downloader який приймає такі поля:
     *
     * @param context     Контекст основного активності
     * @param url         адреса де знаходиться файл
     * @param externalDir місце куди буде збережено файл
     * @param position    позиція у активності
     * @param positionId  ІД позиції у активності
     * @param fileHash    хеш сума файлу
     */
    public Downloader(Context context, URL url, File externalDir, int position, String positionId, String fileHash) {
        // Перевірка та створення директорії
        if (!externalDir.exists()) {
            boolean mkdirs = externalDir.mkdirs();
            if (!mkdirs) {
                Log.e("Downloader", "Не вдалося створити директорію");
            }
        }

        fileName = getFileNameFromUrl(url.toString());
        new FileDownload(this).downloadFile(url, new File(externalDir + "/" + fileName));
        this.directory = externalDir.getAbsolutePath();
        this.context = context;
        folder = (Folder) context;
        downloaderHandler = (DownloaderHandler) context;
        this.fileOMG = (FileOMG) context;
        this.position = position;
        this.positionId = positionId;
        this.fileHash = fileHash;
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
        setProgress(positionId, progress, "");
    }

    @Override
    public void showDetails(String info) {
        setProgress(positionId, 0, info);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onFinish() {
        try {
            if (downloaderHandler.getKey(positionId).getBytes() != null) {
                FileDecryption fileDecryption = new FileDecryption(this, context, positionId);
                SecretKey secretKey = new SecretKeySpec(downloaderHandler.getKey(positionId).getBytes(), "AES");
                decryptedFileName = fileDecryption.getDecFile(new File(directory + "/" + fileName), secretKey);
                fileDecryption.fileDecryption();
            } else {
                Log.e("Downloader", "відсутній ключ для розшифровці файлу");
            }
        } catch (Exception e) {
            Log.e("Downloader", "Помилка при розшифровці файлу", e);
        }
    }

    private void setProgress(String positionId, int progress, String info) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, progress, info));
        }
    }

    @Override
    public void stopDecryption() {
        try {
            new Handler().postDelayed(() -> {
                folder.updateItem(position, positionId, decryptedFileName, FileData.getFileHash(decryptedFileName, "SHA-256"));
            }, 1);
            //Видаляємо шифрований файл щоб не було накопичування
            FileData.deleteFile(context, directory + "/" + fileName);
        } catch (Exception e) {
            Log.e("Downloader", "Помилка при оновленні інформації про розшифрований файл", e);
        }
    }

    public interface DownloaderHandler {
        String getKey(String positionId);
    }
}
