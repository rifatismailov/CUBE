package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.example.cube.contact.ContactData;
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
    private final SecretKey secretKey;

    /**
     * Constructor to initialize ContactManager with an SQLiteDatabase instance.
     *
     * @param database The database instance to manage contacts.
     * @param secretKey The key used to decrypt contact data.
     */
    public ContactManager(SQLiteDatabase database,SecretKey secretKey) {
        this.database = database;
        this.secretKey=secretKey;
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
     * @return A map of contact IDs to UserData objects.
     */
    public Map<String, ContactData> getContacts() {
        Map<String, ContactData> contacts = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_CONTACTS, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String encryptedData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));

                // Decrypt the contact data
                String decryptedJson = Encryption.AES.decryptCBCdb(encryptedData, secretKey);
                ContactData contactData = new ContactData(new JSONObject(decryptedJson));

                // Assign default name if none exists
                if (contactData.getName() == null || contactData.getName().isEmpty()) {
                    contactData.setName("No name");
                }

                contacts.put(id, contactData);
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


    public Pair<Boolean, String> getContactById(String id) {
        Cursor cursor = null;
        String decryptedJson = null;
        boolean found = false;

        try {
            cursor = database.query(
                    TABLE_CONTACTS,
                    new String[]{COLUMN_ENCRYPTED_DATA},
                    COLUMN_ID + " = ?",
                    new String[]{id},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                String encryptedData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                decryptedJson = Encryption.AES.decryptCBCdb(encryptedData, secretKey);
                found = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new Pair<>(found, decryptedJson);
    }

    /**
     * Inserts or updates multiple contacts in the database.
     * The data is encrypted using the provided SecretKey before storage.
     *
     * @param contacts  A map of contact IDs to UserData objects.
     */
    public void setContacts(Map<String, ContactData> contacts) {
        try {
            for (Map.Entry<String, ContactData> entry : contacts.entrySet()) {
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
            Log.e("createContact", "createContact " + e);

        }
    }

    /**
     * Updates a specific contact in the database.
     * The updated data is encrypted using the provided SecretKey.
     *
     * @param contactData The updated UserData object.
     * @param secretKey   The key used to encrypt contact data.
     */
    public void updateContact(ContactData contactData, SecretKey secretKey) {
        try {
            String id = contactData.getId();

            // Convert UserData to JSON and encrypt it
            String json = contactData.toJson().toString();
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

    public void deleteAll() {
        try {
            int deletedRows = database.delete(TABLE_CONTACTS, null, null);
            Log.e("MessageMainManager", "All messages deleted. Rows affected: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting all messages: " + e.getMessage());
        }
    }
}
