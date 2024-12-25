package com.example.folder.dialogwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.folder.Folder;
import com.example.folder.R;
import com.example.folder.file.FileDetect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Клас Open відповідає за відображення діалогового вікна, яке дозволяє
 * користувачеві переглядати файли та директорії, а також завантажувати файли на сервер.
 */
public class Open implements AdapterView.OnItemClickListener {

    private AlertDialog alertDialog;
    private final Context context;
    private final List<Search> arrayList = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private ListView listView;
    private String directory;
    private ImageButton back;
    private final Folder folder;
    private TextView infoFile;

    public static final String DIR = Environment.getExternalStorageDirectory().toString();
    private final Activity activity;
    private String fileName;
    private String messageId;

    /**
     * Конструктор класу Open.
     * @param context Контекст, у якому працює діалог.
     */
    public Open(Context context) {
        this.directory = DIR;
        this.context = context;
        this.activity = (Activity) context;
        this.folder = (Folder) context;
        messageId = UUID.randomUUID().toString();
        showDialog();
    }

    /**
     * Відображення діалогового вікна.
     */
    private void showDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View layout = activity.getLayoutInflater().inflate(R.layout.dialog_open, null);
        dialog.setView(layout);
        alertDialog = dialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        listView = layout.findViewById(R.id.OpenListView);
        listView.setOnItemClickListener(this);

        back = layout.findViewById(R.id.back);
        back.setOnClickListener(v -> navigateBack());

        infoFile = layout.findViewById(R.id.infoFile);
        displayDirectoryContents(directory);
    }

    /**
     * Отримання списку файлів і папок у директорії.
     * @param directory Шлях до директорії.
     * @return Масив назв файлів і папок.
     */
    public String[] getDirectoryContents(String directory) {
        File dir = new File(directory);
        return dir.list();
    }

    /**
     * Відображення вмісту директорії.
     * @param path Шлях до директорії.
     */
    public void displayDirectoryContents(String path) {
        arrayList.clear();
        String[] dirContents = getDirectoryContents(path);

        if (dirContents != null) {
            for (String item : dirContents) {
                File file = new File(path + "/" + item);
                if (file.isDirectory()) {
                    arrayList.add(new Search(item, "time", "about", R.drawable.ic_folder, false));
                } else {
                    arrayList.add(new Search(item, "time", "about", R.drawable.ic_file_other, false));
                }
            }
        }

        searchAdapter = new SearchAdapter(context, R.layout.iteam_row, arrayList);
        listView.setAdapter(searchAdapter);
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
     * @param parent Батьківський адаптер.
     * @param view Натиснутий елемент.
     * @param position Позиція елемента.
     * @param id ID елемента.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedPath = directory + "/" + Objects.requireNonNull(searchAdapter.getItem(position)).getNumber();
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
     * @param file Файл для завантаження.
     */
    private void uploadFile(File file) {
        fileName=file.getName();
        String serverUrl = "http://192.168.1.237:8020/api/files/upload";
        Uploader uploader = new Uploader( context, messageId, serverUrl);

        try {
            uploader.uploadFile(file);
            onFinish();
        } catch (InterruptedException e) {
            Log.e("uploadFile", "Помилка завантаження файлу", e);
        }
    }


    /**
     * Закриття діалогового вікна та оновлення інформації у батьківському компоненті.
     */
    public void onFinish() {
        FileDetect fileDetect = new FileDetect();
        folder.addFile(messageId,directory+"/"+fileName, fileDetect.getFileHash(directory+"/"+fileName, "SHA-256"));
        alertDialog.cancel();
    }
}
