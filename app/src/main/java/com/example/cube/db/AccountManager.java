package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.cube.encryption.Encryption;

import org.json.JSONObject;

import javax.crypto.SecretKey;

public class AccountManager {

    private final SQLiteDatabase database;

    // Table and column definitions
    public static final String TABLE_ACCOUNT = "account";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";

    /**
     * Constructor to initialize ContactManager with an SQLiteDatabase instance.
     *
     * @param database The database instance to manage contacts.
     */
    public AccountManager(SQLiteDatabase database) {
        this.database = database;
    }


    /**
     * Retrieves all contacts from the database, decrypting their data using the provided SecretKey.
     *
     * @param secretKey The key used to decrypt contact data.
     * @return A map of contact IDs to UserData objects.
     *
     * Перевірка існування таблиці: Викликаємо метод isTableExists, щоб перевірити, чи існує таблиця account.
     * Отримання даних: Використовуємо database.query, щоб отримати зашифровані дані з таблиці. Якщо курсор не пустий, читаємо дані.
     * Дешифрування даних: Зчитуємо зашифровані дані з курсора, дешифруємо їх і створюємо об'єкт JSONObject.
     * Закриття курсора: Після завершення роботи з курсором, закриваємо його для звільнення ресурсів.
     */
    public JSONObject getAccount(SecretKey secretKey) {
        if (isTableExists(database, TABLE_ACCOUNT)) {
            Log.e("DatabaseHelper", "TABLE_ACCOUNT exists");
        } else {
            Log.e("DatabaseHelper", "TABLE_ACCOUNT does not exist");
        }

        JSONObject account = null;
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_ACCOUNT, new String[]{COLUMN_ENCRYPTED_DATA}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                Log.e("DatabaseHelper", "Found data in TABLE_ACCOUNT");
                do {
                    String encryptedData = cursor.getString(cursor.getColumnIndex(COLUMN_ENCRYPTED_DATA));
                    Log.e("DatabaseHelper", "Encrypted Data retrieved: " + encryptedData);

                    // Decrypt the contact data
                    String decryptedJson = Encryption.AES.decryptCBCdb(encryptedData, secretKey);
                    account = new JSONObject(decryptedJson);
                } while (cursor.moveToNext());
            } else {
                Log.e("DatabaseHelper", "No data found in TABLE_ACCOUNT");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "account error. " + e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return account;
    }



    /**
     * Inserts or updates multiple contacts in the database.
     * The data is encrypted using the provided SecretKey before storage.
     *
     * @param jsonObject to Account objects.
     * @param secretKey  The key used to encrypt contact data.
     * Початок транзакції: Використовуємо db.beginTransaction() для початку транзакції. Це допомагає забезпечити, що всі операції всередині блоку try виконуються як одна атомарна операція.
     * Шифрування даних: Конвертуємо jsonObject у строку JSON і шифруємо її, використовуючи заданий секретний ключ.
     * Підготовка даних для вставки: Створюємо об'єкт ContentValues та додаємо зашифровані дані до нього.
     * Вставка або заміна даних: Використовуємо метод db.replace() для вставки або заміни даних у таблиці account. Якщо операція не вдалася, rowId дорівнюватиме -1.
     * Завершення транзакції: Використовуємо db.setTransactionSuccessful(), щоб вказати, що всі операції успішні, а потім db.endTransaction(), щоб завершити транзакцію.
     */
    public void setAccount(JSONObject jsonObject, SecretKey secretKey) {
        SQLiteDatabase db = database;
        db.beginTransaction();
        try {
            // Convert UserData to JSON and encrypt it
            String encryptedJson = Encryption.AES.encryptCBCdb(jsonObject.toString(), secretKey);
            Log.e("DatabaseHelper", "Encrypted JSON: " + encryptedJson);

            // Prepare data for insertion or update
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

            long rowId = db.replace(TABLE_ACCOUNT, null, values);
            if (rowId == -1) {
                Log.e("DatabaseHelper", "Failed to insert data into TABLE_ACCOUNT.");
            } else {
                Log.e("DatabaseHelper", "Data inserted into TABLE_ACCOUNT with row id: " + rowId);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DatabaseHelper", "Account creation error: " + e);
        } finally {
            db.endTransaction();
            Log.e("DatabaseHelper", "Transaction ended.");
        }
    }



    public boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[] { tableName }
        );
        boolean tableExists = (cursor.getCount() > 0);
        cursor.close();
        return tableExists;
    }

    /**
     * Updates a specific contact in the database.
     * The updated data is encrypted using the provided SecretKey.
     *
     * @param jsonObject to Account objects.
     * @param secretKey  The key used to encrypt contact data.
     */
    public void updateContact(JSONObject jsonObject, SecretKey secretKey) {
        try {
            // Convert UserData to JSON and encrypt it
            String encryptedJson = Encryption.AES.encryptCBCdb(jsonObject.toString(), secretKey);
            // Prepare data for insertion or update
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENCRYPTED_DATA, encryptedJson);

            // Perform the update
            database.update(TABLE_ACCOUNT, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}