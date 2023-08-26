package com.example.folder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.folder.dialogwindows.Open;

import java.io.ByteArrayOutputStream;

public class FileConversion extends Thread{
    String directory;
    Open open;
    public FileConversion(Open open,String directory){
        this.open=open;
        this.directory=directory;
    }
    @Override
    public void run() {
        Bitmap bMap = BitmapFactory.decodeFile(directory);
        int width = bMap.getWidth();
        int height = bMap.getHeight();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        open.openFile(byteArray, width, height);
    }
}
