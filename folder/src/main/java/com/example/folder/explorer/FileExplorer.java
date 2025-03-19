package com.example.folder.explorer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.folder.FileData;
import com.example.folder.R;
import com.example.folder.file.Folder;
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
public class FileExplorer extends Dialog implements AdapterView.OnItemClickListener {
    private static final String ALGORITHM = "AES";

    private final Context context;
    private final List<Explorer> arrayList = new ArrayList<>();
    private final Folder folder;
    private final String DIR = Environment.getExternalStorageDirectory().toString();
    private final String messageId;
    private ExplorerAdapter explorerAdapter;
    private ListView listView;
    private String directory;
    private ImageButton back;
    private String fileName;
    private final String senderKey;
    private final String serverUrl;

    /**
     * Конструктор класу Open.
     *
     * @param context   Контекст, у якому працює діалог.
     * @param senderKey Ключ відправника.
     */
    public FileExplorer(Context context, String serverUrl, String senderKey) {
        super(context);
        this.directory = DIR;
        this.context = context;
        this.serverUrl=serverUrl;
        this.senderKey = senderKey;
        this.folder = (Folder) context;
        messageId = UUID.randomUUID().toString();
    }

    /**
     * Відображення діалогового вікна.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_open);

        try {
            // Налаштування для повноекранного режиму.
            Window window = getWindow();
            if (window != null) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
                // Робимо фон прозорим
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            listView = findViewById(R.id.OpenListView);
            listView.setOnItemClickListener(this);

            back = findViewById(R.id.back);
            back.setOnClickListener(v -> navigateBack());

            displayDirectoryContents(directory);
        } catch (Resources.NotFoundException e) {
            Toast.makeText(context, "Resource not found: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
        @Override
    public void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
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
            if(directory.equals(DIR)){
                dismiss();
            }
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
            Log.e("FileExplorer", "Невідомий тип: " + selectedPath);
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
                Log.e("FileExplorer", "Помилка завантаження файлу", e);
            }
        }).start();
    }


    /**
     * Закриття діалогового вікна та оновлення інформації у батьківському компоненті.
     * @param encFile зашифрований файл
     */
    public void onFinish(String encFile) {
        folder.addFile(messageId, directory + "/" + fileName, encFile, FileData.getFileHash(directory + "/" + fileName, "SHA-256"));
        dismiss();
    }
}
