package com.example.folder.dialogwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.folder.file.Folder;
import com.example.folder.R;
import com.example.folder.file.FileDetect;
import com.example.folder.upload.FileEncryption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Клас Open відповідає за відображення діалогового вікна, яке дозволяє
 * користувачеві переглядати файли та директорії, а також завантажувати файли на сервер.
 */
public class FileExplorer extends AlertDialog.Builder implements AdapterView.OnItemClickListener {
    private static final String ALGORITHM = "AES";

    private final Context context;
    private final List<Explorer> arrayList = new ArrayList<>();
    private final Folder folder;
    private final String DIR = Environment.getExternalStorageDirectory().toString();
    private final String messageId;
    private AlertDialog alertDialog;
    private ExplorerAdapter explorerAdapter;
    private ListView listView;
    private String directory;
    private ImageButton back;
    private String fileName;
    private String senderKey;
    private String serverUrl = "http://192.168.1.237:8020/api/files/upload";

    /**
     * Конструктор класу Open.
     *
     * @param context   Контекст, у якому працює діалог.
     * @param senderKey
     */
    public FileExplorer(Context context, String senderKey) {
        super(context);
        this.directory = DIR;
        this.context = context;
        this.senderKey = senderKey;
        this.folder = (Folder) context;
        messageId = UUID.randomUUID().toString();
        showDialog();
    }

    /**
     * Відображення діалогового вікна.
     */

    private void showDialog() {
        View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_open, null);
        setView(layout);
        alertDialog = show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        listView = layout.findViewById(R.id.OpenListView);
        listView.setOnItemClickListener(this);

        back = layout.findViewById(R.id.back);
        back.setOnClickListener(v -> navigateBack());

        displayDirectoryContents(directory);
    }

    /**
     * Отримання списку файлів і папок у директорії.
     *
     * @param directory Шлях до директорії.
     * @return Масив назв файлів і папок.
     */
    public String[] getDirectoryContents(String directory) {
        File dir = new File(directory);
        return dir.list();
    }

    /**
     * Відображення вмісту директорії.
     *
     * @param path Шлях до директорії.
     */
    public void displayDirectoryContents(String path) {
        arrayList.clear();
        String[] dirContents = getDirectoryContents(path);

        if (dirContents != null) {
            for (String item : dirContents) {
                File file = new File(path + "/" + item);
                if (file.isDirectory()) {
                    arrayList.add(new Explorer(item, "time", "about", R.drawable.ic_folder, false));
                } else {
                    arrayList.add(new Explorer(item, "time", "about", R.drawable.ic_file_other, false));
                }
            }
        }

        explorerAdapter = new ExplorerAdapter(context, R.layout.iteam_row, arrayList);
        listView.setAdapter(explorerAdapter);
    }

    /**
     * Навігація назад у файловій системі.
     */
    private void navigateBack() {
        File currentDir = new File(directory);
        String parentDir = currentDir.getParent();

        if (parentDir != null && parentDir.startsWith(DIR)) {
            directory = parentDir;
            displayDirectoryContents(directory);
        } else {
            directory = DIR;
            displayDirectoryContents(directory);
        }
    }

    /**
     * Обробка кліку на елемент списку.
     *
     * @param parent   Батьківський адаптер.
     * @param view     Натиснутий елемент.
     * @param position Позиція елемента.
     * @param id       ID елемента.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedPath = directory + "/" + Objects.requireNonNull(explorerAdapter.getItem(position)).getNumber();
        File selectedFile = new File(selectedPath);
        if (selectedFile.isFile()) {
            uploadFile(selectedFile);
        } else if (selectedFile.isDirectory()) {
            directory = selectedPath;
            displayDirectoryContents(directory);
        } else {
            Log.e("onItemClick", "Невідомий тип: " + selectedPath);
        }
    }

    /**
     * Завантаження файлу на сервер.
     *
     * @param file Файл для завантаження.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void uploadFile(File file) {
        fileName = file.getName();
        // Виконуємо шифрування у фоновому потоці
        new Thread(() -> {
            try {
                FileEncryption fileEncryption = new FileEncryption(context, messageId, serverUrl);
                SecretKey secretKey = new SecretKeySpec(senderKey.getBytes(), ALGORITHM);
                String encryptedFile = fileEncryption.getEncFile(file, secretKey);
                ((Activity) context).runOnUiThread(() -> onFinish(encryptedFile));
                // Шифруємо файл
                fileEncryption.fileEncryption();
            } catch (Exception e) {
                Log.e("uploadFile", "Помилка завантаження файлу", e);
            }
        }).start();
    }


    /**
     * Закриття діалогового вікна та оновлення інформації у батьківському компоненті.
     */

    public void onFinish(String encFile) {
        FileDetect fileDetect = new FileDetect();
        folder.addFile(messageId, directory + "/" + fileName, encFile, fileDetect.getFileHash(directory + "/" + fileName, "SHA-256"));
        alertDialog.cancel();
    }
}
