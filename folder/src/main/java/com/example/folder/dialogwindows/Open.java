package com.example.folder.dialogwindows;

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
    private static final Logger LOGGER = Logger.getLogger(Open.class.getName());

    AlertDialog alertDialog;
    Context context;
    private List<Search> arrayList = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private ListView listView;
    String directory;
    ImageButton back;
    Folder folder;
    TextView infoFile;
    public final String DIR = Environment.getExternalStorageDirectory().toString();
    Activity activity;
    int position;
    String fileName;
    FileOMG fileOMG;
    String messageId;
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
        for (int i = 0; i < sDirList.length; i++) {
            File file = new File(analogDir + "/" + sDirList[i]);
            if (!file.isFile()) {
                arrayList.add(new Search(sDirList[i], "time", "about", R.drawable.ic_folder, false));
            } else {
                arrayList.add(new Search(sDirList[i], "time", "about", R.drawable.ic_file_other, false));
            }
        }
        searchAdapter = new SearchAdapter(context, R.layout.iteam_row, arrayList);
        listView.setAdapter(searchAdapter);
    }



    private void back() {
        try {
            String newDirectory = "";
            String[] parts = directory.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                newDirectory = newDirectory + "/" + parts[i];
            }
            if (newDirectory.length() < DIR.length()) {
                showDirectory(DIR);
                directory = DIR;
            } else {
                if (newDirectory.length() > DIR.length()) {
                    showDirectory(newDirectory);
                    directory = newDirectory;
                } else showDirectory(DIR);
            }
        } catch (Exception e) {
            directory = DIR;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Оновлення шляху
        String selectedPath = directory + "/" + searchAdapter.getItem(position).getNumber();
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
            // Якщо це папка, показуємо її вміст
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
    @Override
    public void setProgress(int progress) {
        // Оновлюємо прогрес у головному потоці
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoFile.setText(progress+" %");
                }
            });
        }

    }

    public void onFinish() {
        try {
            if (!activity.isFinishing()) { // Check if Activity is still valid
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.show(); // Show dialog on the UI thread
                    }
                });
            }

            // Use a Handler to delay the execution without blocking the UI thread
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FileDetect fileDetect = new FileDetect();
                    folder.updateItem(position, directory + "/" + fileName, fileDetect.getFileHash(directory + "/" + fileName, "SHA-256"));
                    LOGGER.severe("Завершено завантаження");

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.cancel(); // Cancel dialog on the UI thread
                            listView.clearFocus(); // Clear focus on the UI thread
                        }
                    });
                }
            }, 1); // Delay of 100 milliseconds
        } catch (Exception e) {
            // Handle any exceptions here
            LOGGER.severe("Error in onFinish: " + e.getMessage());
        }
    }


}
