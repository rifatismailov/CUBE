package com.example.folder.dialogwindows;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.folder.Folder;
import com.example.folder.R;
import com.example.folder.file.FileHandler;
import com.example.folder.file.FileDetect;
import com.example.folder.file.FileDownload;
import com.example.folder.file.FileOMG;
import com.example.folder.file.FileUploader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Даний  @AlertDialog відображає папки та файли
 * при натисканні на які фалй конвертується в массив байтів
 * за допомогою методів @convertImage
 *
 * @convertVideo
 * @convertFile За задумкою всі ці методи мають окремі класи  як @ConvertImage
 * @ConvertFile яка будуть конвертувати файли
 */
public class Open implements AdapterView.OnItemClickListener, FileHandler {

    private AlertDialog alertDialog;
    private final Context context;
    private final List<Search> arrayList = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private ListView listView;
    private String directory;
    private ImageButton back;
    private final Folder folder;
    private TextView infoFile;
    public final String DIR = Environment.getExternalStorageDirectory().toString();
    public Activity activity;
    public int position;
    public String fileName;
    public String messageId;
    public Open(Context context) {
        this.directory = DIR;
        this.context = context;
        activity = (Activity) context;
        folder = (Folder) context;
        DialogShow();
    }





    private void DialogShow() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        final View linearlayout = activity.getLayoutInflater().inflate(R.layout.dialog_open, null);
        dialog.setView(linearlayout);
        alertDialog = dialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        listView = linearlayout.findViewById(R.id.OpenListView);
        listView.setOnItemClickListener(this);
        back = linearlayout.findViewById(R.id.back);
        back.setOnClickListener(v -> back());
        infoFile = linearlayout.findViewById(R.id.infoFile);
        showDirectory(directory);
        dialog.create();
    }

    public String[] arrayDir(String directory) {
        File dir = new File(directory);
        return dir.list();
    }

    /**
     * Проводник
     */
    @Override
    public void showDirectory(String analogDir) {

        arrayList.clear();
        String[] sDirList;
        if (analogDir == null) {
            sDirList = arrayDir(directory);
        } else {
            sDirList = arrayDir(analogDir);
        }
        for (String s : sDirList) {
            File file = new File(analogDir + "/" + s);
            if (!file.isFile()) {
                arrayList.add(new Search(s, "time", "about", R.drawable.ic_folder, false));
            } else {
                arrayList.add(new Search(s, "time", "about", R.drawable.ic_file_other, false));
            }
        }
        searchAdapter = new SearchAdapter(context, R.layout.iteam_row, arrayList);
        listView.setAdapter(searchAdapter);
    }



    private void back() {
        try {
            StringBuilder newDirectory = new StringBuilder();
            String[] parts = directory.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                newDirectory.append("/").append(parts[i]);
            }
            if (newDirectory.length() < DIR.length()) {
                showDirectory(DIR);
                directory = DIR;
            } else {
                if (newDirectory.length() > DIR.length()) {
                    showDirectory(newDirectory.toString());
                    directory = newDirectory.toString();
                } else showDirectory(DIR);
            }
        } catch (Exception e) {
            directory = DIR;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Оновлення шляху
        String selectedPath = directory + "/" + Objects.requireNonNull(searchAdapter.getItem(position)).getNumber();
        File selectedFile = new File(selectedPath);
        messageId = UUID.randomUUID().toString();

        // Перевірка, чи це файл або папка
        if (selectedFile.isFile()) {
           String SERVER_URL = "http://192.168.1.237:8020/api/files/upload"; // Змініть IP на ваш
            // Якщо це файл, викликаємо uploadFile
            FileUploader fileUploader = new FileUploader(this, context,messageId,SERVER_URL);
            File file = new File(selectedPath);
            try {
                fileUploader.uploadFile(file);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            directory = selectedPath;
            closeDialog();
        } else if (selectedFile.isDirectory()) {
            directory = selectedPath;
            showDirectory(directory);
        } else {
            // Непередбачений випадок
            Log.e("onItemClick", "Не вдалося визначити тип: " + selectedPath);
        }

    }


    public void closeDialog() {
        FileDetect fileDetect = new FileDetect();
        alertDialog.cancel();
        folder.addFile(messageId,directory, fileDetect.getFileHash(directory, "SHA-256"));
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void setProgress(int progress) {
        // Оновлюємо прогрес у головному потоці
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> infoFile.setText(progress+" %"));
        }

    }

    public void onFinish() {
        try {
            if (!activity.isFinishing()) { // Check if Activity is still valid
                activity.runOnUiThread(() -> {
                    alertDialog.show(); // Show dialog on the UI thread
                });
            }

            new Handler().postDelayed(() -> {
                FileDetect fileDetect = new FileDetect();
                folder.updateItem(position, directory + "/" + fileName, fileDetect.getFileHash(directory + "/" + fileName, "SHA-256"));
                activity.runOnUiThread(() -> {
                    alertDialog.cancel();
                    listView.clearFocus();
                });
            }, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
