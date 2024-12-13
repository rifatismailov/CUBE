package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cube.contact.UserData;
import com.example.cube.encryption.Encryption;
import org.json.JSONObject;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

public class ContactManager {

    private final SQLiteDatabase database;
    private static final String TABLE_NAME = "Contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";

    public ContactManager(SQLiteDatabase database) {
        this.database = database;
    }
    public void clearMessagesTable() {
        database.execSQL("DELETE FROM " + TABLE_NAME);
    }

    // Get all contacts from the database
    public Map<String, UserData> getContacts(SecretKey secretKey) {
        Map<String, UserData> contacts = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String encryptedData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                String decryptedJson = Encryption.AES.decryptCBCdb(encryptedData, secretKey);
                UserData userData = new UserData(new JSONObject(decryptedJson));

                // Додаємо дефолтне ім'я, якщо відсутнє
                if (userData.getName() == null || userData.getName().isEmpty()) {
                    userData.setName("No name");
                }
                contacts.put(id, userData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contacts;
    }


    // Set (add or update) contacts in the database
    public void setContacts(Map<String, UserData> contacts, SecretKey secretKey) {
        try {
            for (Map.Entry<String, UserData> entry : contacts.entrySet()) {
                String id = entry.getKey();

                String json = entry.getValue().toJson().toString();
                String encryptedJson = Encryption.AES.encryptCBCdb(json, secretKey);

                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, id);
                values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

                database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateContact(UserData userData, SecretKey secretKey) {
        try {
            String id=userData.getId();
            String json = userData.toJson().toString();
            String encryptedJson = Encryption.AES.encryptCBCdb(json, secretKey);
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

            // Умови для оновлення
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {id};

            // Оновлення
            database.update(TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
