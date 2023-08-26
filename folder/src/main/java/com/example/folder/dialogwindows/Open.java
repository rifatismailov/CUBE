package com.example.folder.dialogwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.folder.Folder;
import com.example.folder.R;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Open implements AdapterView.OnItemClickListener {
    AlertDialog alertDialog;
    Context context;
    private List<Search> arrayList = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private ListView listView;
    String directory;
    ImageButton sendFile;
    ImageButton delete;
    ImageButton back;
    ImageButton cancel;
    Folder folder;
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
        sendFile = linearlayout.findViewById(R.id.SendFile);
        delete = linearlayout.findViewById(R.id.deleteFile);
        back = linearlayout.findViewById(R.id.back);
        cancel = linearlayout.findViewById(R.id.cancel);
        sendFile.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        cancel.setOnClickListener(v -> {
            /**Закрываем Диалог*/
            alertDialog.cancel();
        });
        sendFile.setOnClickListener(v -> {
            /**Отпровляем фаилы и скрываем кнопки*/
            sendFile.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            alertDialog.cancel();
        });
        delete.setOnClickListener(v -> {
            sendFile.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);

        });
        back.setOnClickListener(v -> back());
        showDirectory(directory);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final CheckBox checkBox = view.findViewById(R.id.check);
            /**разблокировка checkBox*/
            checkBox.setVisibility(view.VISIBLE);
            if (searchAdapter.getItem(position).getCheck() == false) {
                checkBox.setChecked(true);
                searchAdapter.getItem(position).setCheck(true);
                /**При выделении проверяем на состояния checkBox ListView
                 * если оно ложное меняем его состояние на истинное
                 * потом меняем состояние кнопок*/
                sendFile.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
            } else {
                checkBox.setChecked(false);
                searchAdapter.getItem(position).setCheck(false);
                sendFile.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            }
            return true;
        });
        dialog.create();
    }


    public String[] arrayDir(String directory) {
        File dir = new File(directory);
        return dir.list();
    }

    public boolean checkFile(String file) {
        return new File(file).isFile();
    }

    /**
     * Проводник
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDirectory(String analogDir) {
        // Toast.makeText(context, "" + analogDir, Toast.LENGTH_LONG).show();

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

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    Bitmap bMap = BitmapFactory.decodeFile(directory);
                    int width = bMap.getWidth();
                    int height = bMap.getHeight();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    folder.openFile(byteArray, width, height);
                    alertDialog.cancel();

                    //folder.openFile(directory);

                }
            });


        } else {
            showDirectory(directory + "/");
        }
    }
}
