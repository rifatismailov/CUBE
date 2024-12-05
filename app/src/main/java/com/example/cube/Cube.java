package com.example.cube;

import android.content.Context;
import android.util.Log;

import com.example.cube.contact.UserData;
import com.example.folder.file.FileDetect;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class Cube {
    private Map<String, UserData> contacts = new HashMap<>();  // Контакти користувачів
    Context context;
    private static final String CONTACTS_FILE_NAME = "contacts.cube";
    File externalDir;

    public Cube(Context context) {
        this.context = context;
        externalDir = new File(context.getExternalFilesDir(null), "cube");
    }

    public Map<String, UserData> getContacts(SecretKey secretKey) {
        try {
            contacts = (Map<String, UserData>) new FileDetect().loadFromFile(externalDir + "/"+CONTACTS_FILE_NAME, secretKey);
        } catch (Exception e) {
            Log.e("Cube", e.toString());
        }
        return contacts;
    }
    // Зберігаємо контакти у файл з шифруванням
    public void setContacts(Map<String, UserData> contacts ,SecretKey secretKey){
        try {
            new FileDetect().saveToFile(contacts, externalDir + "/"+CONTACTS_FILE_NAME, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
