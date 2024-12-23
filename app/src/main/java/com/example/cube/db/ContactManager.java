package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.cube.contact.UserData;
import com.example.cube.encryption.Encryption;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

/**
 * The ContactManager class manages CRUD operations for user contacts stored in an SQLite database.
 * It supports encryption and decryption of contact data using a SecretKey.
 */
public class ContactManager {

    private final SQLiteDatabase database;

    // Table and column definitions
    public static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";

    /**
     * Constructor to initialize ContactManager with an SQLiteDatabase instance.
     *
     * @param database The database instance to manage contacts.
     */
    public ContactManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Clears all entries from the contacts table.
     * Useful for resetting or purging the contact data.
     */
    public void clearMessagesTable() {
        database.execSQL("DELETE FROM " + TABLE_CONTACTS);
    }

    /**
     * Retrieves all contacts from the database, decrypting their data using the provided SecretKey.
     *
     * @param secretKey The key used to decrypt contact data.
     * @return A map of contact IDs to UserData objects.
     */
    public Map<String, UserData> getContacts(SecretKey secretKey) {
        Map<String, UserData> contacts = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_CONTACTS, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String encryptedData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));

                // Decrypt the contact data
                String decryptedJson = Encryption.AES.decryptCBCdb(encryptedData, secretKey);
                UserData userData = new UserData(new JSONObject(decryptedJson));

                // Assign default name if none exists
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

    /**
     * Inserts or updates multiple contacts in the database.
     * The data is encrypted using the provided SecretKey before storage.
     *
     * @param contacts  A map of contact IDs to UserData objects.
     * @param secretKey The key used to encrypt contact data.
     */
    public void setContacts(Map<String, UserData> contacts, SecretKey secretKey) {
        try {
            for (Map.Entry<String, UserData> entry : contacts.entrySet()) {
                String id = entry.getKey();

                // Convert UserData to JSON and encrypt it
                String json = entry.getValue().toJson().toString();
                String encryptedJson = Encryption.AES.encryptCBCdb(json, secretKey);

                // Prepare data for insertion or update
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, id);
                values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

                // Use conflict resolution to replace existing entries
                database.insertWithOnConflict(TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                Log.e("createContact", "createContact ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("createContact", "createContact "+e);

        }
    }

    /**
     * Updates a specific contact in the database.
     * The updated data is encrypted using the provided SecretKey.
     *
     * @param userData  The updated UserData object.
     * @param secretKey The key used to encrypt contact data.
     */
    public void updateContact(UserData userData, SecretKey secretKey) {
        try {
            String id = userData.getId();

            // Convert UserData to JSON and encrypt it
            String json = userData.toJson().toString();
            String encryptedJson = Encryption.AES.encryptCBCdb(json, secretKey);

            // Prepare data for update
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

            // Define the update condition
            String whereClause = COLUMN_ID + " = ?";
            String[] whereArgs = {id};

            // Perform the update
            database.update(TABLE_CONTACTS, values, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
