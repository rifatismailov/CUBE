package com.example.folder.dialogwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.folder.file.ConversionImage;
import com.example.folder.ReturnOpen;
import com.example.folder.Folder;
import com.example.folder.R;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Даний  @AlertDialog відображає папки та файли
 * при натисканні на які фалй конвертується в массив байтів
 * за допомогою методів @convertImage
 *
 * @convertVideo
 * @convertFile За задумкою всі ці методи мають окремі класи  як @ConvertImage
 * @ConvertFile яка будуть конвертуавти файли
 */
public class Open implements AdapterView.OnItemClickListener, ReturnOpen {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Open(Context context) {
        this.directory = DIR;
        this.context = context;
        activity = (Activity) context;

        folder = (Folder) context;
        DialogShow();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Open(Context context, String directory) {
        this.directory = directory;
        this.context = context;
        activity = (Activity) context;

        folder = (Folder) context;
        DialogShow();
    }

    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DialogShow() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        final View linearlayout = activity.getLayoutInflater().inflate(R.layout.dialog_open, null);
        dialog.setView(linearlayout);
        alertDialog = dialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    public boolean checkFile(String file) {
        return new File(file).isFile();
    }

    public void openFile(byte[] byteArray, int width, int height) {
        folder.openFile(byteArray, width, height);
    }

    @Override
    public void showInfo(String info) {

    }

    /**
     * Проводник
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDirectory(String analogDir) {

        arrayList.clear();
        final String[] sDirList = arrayDir(analogDir);
        for (int i = 0; i < sDirList.length; i++) {
            String file = analogDir + sDirList[i];
            if (checkFile(file)) {
                arrayList.add(new Search(sDirList[i], "time", "about", R.drawable.ic_file_other, false));
            } else {
                arrayList.add(new Search(sDirList[i], "time", "about", R.drawable.ic_folder, false));
            }
        }
        searchAdapter = new SearchAdapter(context, R.layout.iteam_row, arrayList);
        listView.setAdapter(searchAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        directory = directory + "/" + searchAdapter.getItem(position).getNumber();
        if (checkFile(directory)) {
            infoFile.setText("Please waite some time");
            convertImage(directory);
            alertDialog.cancel();

        } else {
            showDirectory(directory + "/");
        }
    }

    private void convertImage(String directory) {
        Bitmap yourBitmap = BitmapFactory.decodeFile(directory);
        // Запуск AsyncTask для конвертации
        ConversionImage task = new ConversionImage(context, this, directory);
        task.execute(yourBitmap);
    }


}
