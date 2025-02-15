package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.example.web_socket_service.socket.Envelope;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * MessageMainManager - клас для управління операціями над повідомленнями в базі даних SQLite.
 * Використовується під час отримання повідомлень коли користувач не виконує чат з контактам тоб то
 * активного месенджуваня нема з контактом від якого прийшло повідомлення
 * Цей клас надає методи для додавання, оновлення, видалення та отримання повідомлень з бази даних.
 */
public class MessageMainManager {
    private SQLiteDatabase database;

    // Table and column definitions
    public static final String TABLE_MESSAGES_MAIN = "messages_main";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_OPERATION = "operation";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";
    public static final String COLUMN_TIMESTAMP = "timestamp";


    /**
     * Конструктор для ініціалізації MessageMainManager екземпляром SQLiteDatabase.
     *
     * База даних @param Екземпляр бази даних для керування контактами.
     */
    public MessageMainManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Метод для зберігання повідомлень
     * @param envelope саме повідомлення
     */
    public void setMessage(Envelope envelope,String time) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, envelope.getMessageId());
            values.put(COLUMN_SENDER, envelope.getSenderId());
            values.put(COLUMN_OPERATION, envelope.getOperation());
            values.put(COLUMN_ENCRYPTED_DATA, envelope.toJson().toString());
            values.put(COLUMN_TIMESTAMP, time);
            long result = database.insertWithOnConflict(TABLE_MESSAGES_MAIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.e("MessageMainManager", "Insert result: " + result);

        } catch (Exception e) {
            Log.e("MessageMainManager", "Error inserting message: " + e.getMessage());
        }
    }

    /**
     * Метод для отримання кількості повідомлень за контактом
     * @param senderId відправник
     * @param operation операція за яким прийшло повідомлення
     *                  підрахунок робимо коли повідомлення має operation message або file
     */
    public int getMessageCountBySenderAndOperation(String senderId, String operation) {
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES_MAIN +
                    " WHERE " + COLUMN_SENDER + " = ? AND " + COLUMN_OPERATION + " = ?";
            cursor = database.rawQuery(query, new String[]{senderId, operation});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            Log.e("MessageMainManager", "Count result: " + count);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error counting messages: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * Метод для отримання повідомлень за контактом
     * @param senderId відправник
     *                 повідомлення отримується від старшого до молодшого
     */
    public HashMap<String, Envelope> getMessagesByReceiverId(String senderId) {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // Використовуємо LinkedHashMap для збереження порядку
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_MAIN +
                    " WHERE " + COLUMN_SENDER + " = ?" +
                    " ORDER BY " + COLUMN_TIMESTAMP + " ASC"; // Сортування за зростанням часу
            cursor = database.rawQuery(selectQuery, new String[]{senderId});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        String messageId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String jsonMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                        JSONObject jsonObject = new JSONObject(jsonMessage);
                        Envelope envelope = new Envelope(jsonObject);
                        messages.put(messageId, envelope);
                    } catch (Exception e) {
                        Log.e("MessageMainManager", "Error parsing message: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error executing query: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return messages;
    }

    /**
     * Метод для видалення повідомлень
     * @param messageId ідентифікаційним номер повідомлення який буде видалений
     *
     */
    public void deleteMessageById(String messageId) {
        try {
            int deletedRows = database.delete(TABLE_MESSAGES_MAIN, COLUMN_ID + " = ?", new String[]{messageId});
            Log.e("MessageMainManager", "Deleted rows: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting message: " + e.getMessage());
        }
    }

}
