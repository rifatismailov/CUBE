package com.example.folder.download;

import javax.crypto.SecretKey;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.example.folder.file.FileDetect;
import com.example.folder.file.FileOMG;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileDecryption {
    private static final String ALGORITHM = "AES";
    private static final int BUFFER_SIZE = 4096; // 4KB buffer size
    private final FileOMG fileOMG;
    private final DecryptionHandle decryptionHandle;
    private final String positionId;
    private final Context context;
    private String decryptedFileName;
    private File encryptedFile;
    private SecretKey secretKey;

    public FileDecryption(DecryptionHandle decryptionHandle, Context context, String positionId) {
        this.context = context;
        this.fileOMG = (FileOMG) context;
        this.decryptionHandle = decryptionHandle;
        this.positionId = positionId;
    }

    public String getDecFile(File encryptedFile, SecretKey secretKey) throws Exception {
        this.encryptedFile = encryptedFile;
        this.secretKey = secretKey;
        decryptedFileName = generateDecryptedFileName(encryptedFile);
        return decryptedFileName;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void fileDecryption() throws Exception {
        Log.d("FileDecryption", "[decrypted FileName] " + decryptedFileName);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = encryptedFile.length();
        long processedBytes = 0;

        try (InputStream fis = Files.newInputStream(encryptedFile.toPath());
             CipherInputStream cis = new CipherInputStream(fis, cipher);
             OutputStream fos = Files.newOutputStream(Paths.get(decryptedFileName))) {

            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                processedBytes += bytesRead;

                int progress = (int) ((processedBytes / (double) totalBytes) * 100);
                if (context instanceof Activity) {
                    int finalProgress = progress;
                    ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, finalProgress, ""));
                }
            }

            // Перевірка, чи всі байти були оброблені
            if (processedBytes < totalBytes) {
                // Зчитуємо останні байти
                int remainingBytes = (int) (totalBytes - processedBytes);
                if (remainingBytes > 0) {
                    bytesRead = cis.read(buffer, 0, remainingBytes);
                    if (bytesRead != -1) {
                        fos.write(buffer, 0, bytesRead);
                        processedBytes += bytesRead;
                    }

                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 100, "Дешифрування завершено"));
                    }
                    decryptionHandle.stopDecryption();
                } else {
                    Log.e("Downloader", "[ Помилка ] Не всі байти були оброблені. Processed bytes: " + processedBytes + " / Total bytes: " + totalBytes);
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "Помилка дешифрування"));
                    }
                }
            }

        } catch (IndexOutOfBoundsException e) {
            Log.e("Downloader", "[decryptedFileName] " + decryptedFileName + " [ Помилка дешифрування ] " + e.getMessage());
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "Помилка дешифрування"));
            }
        } catch (Exception e) {
            Log.e("Downloader", "[decryptedFileName] " + decryptedFileName + " [ Помилка дешифрування ] " + e);
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> fileOMG.setProgressShow(positionId, 0, "Помилка дешифрування"));
            }
        }
    }

    public String generateDecryptedFileName(File encryptedFile) throws Exception {
        String originalExtension = decrypt(getFileExtension(encryptedFile), secretKey);
        return encryptedFile.getParent() + File.separator + "dec-" + UUID.randomUUID().toString() +
                (originalExtension.isEmpty() ? "" : "." + originalExtension);
    }

    public String decrypt(String input, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = decodeFromCustomBase(input);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private byte[] decodeFromCustomBase(String data) {
        int len = data.length();
        byte[] decoded = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            decoded[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                    + Character.digit(data.charAt(i + 1), 16));
        }
        return decoded;
    }

    public String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    interface DecryptionHandle {
        void stopDecryption();
    }
}
