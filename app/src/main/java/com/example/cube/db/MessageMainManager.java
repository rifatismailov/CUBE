package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.example.web_socket_service.socket.Envelope;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
     * Constructor to initialize MessageMainManager with an SQLiteDatabase instance.
     *
     * @param database The database instance to manage contacts.
     */
    public MessageMainManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * @param envelope
     */
    public void setMessage(Envelope envelope,String time) {
        try {
            Log.e("setMessage", "Attempting to insert message: " + envelope.toJson().toString());

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, envelope.getMessageId());
            values.put(COLUMN_SENDER, envelope.getSenderId());
            values.put(COLUMN_OPERATION, envelope.getOperation());
            values.put(COLUMN_ENCRYPTED_DATA, envelope.toJson().toString());
            values.put(COLUMN_TIMESTAMP, time);
            long result = database.insertWithOnConflict(TABLE_MESSAGES_MAIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.e("setMessage", "Insert result: " + result);

        } catch (Exception e) {
            Log.e("setMessage", "Error inserting message: " + e.getMessage());
        }
    }


    public int getMessageCountBySenderAndOperation(String senderId, String operation) {
        int count = 0;
        Cursor cursor = null;
        try {
            Log.e("getMessageCount", "Querying for senderId: " + senderId + " and operation: " + operation);

            String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES_MAIN +
                    " WHERE " + COLUMN_SENDER + " = ? AND " + COLUMN_OPERATION + " = ?";
            cursor = database.rawQuery(query, new String[]{senderId, operation});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            Log.e("getMessageCount", "Count result: " + count);
        } catch (Exception e) {
            Log.e("getMessageCount", "Error counting messages: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }



    public int getMessageCountBySenderId(String senderId) {
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES_MAIN + " WHERE " + COLUMN_SENDER + " = ?";
            cursor = database.rawQuery(query, new String[]{senderId});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0); // Отримуємо значення першого (і єдиного) стовпця
            }
            Log.e("getMessageCount", "Count result: " + count);

        } catch (Exception e) {
            Log.e("getMessageCount", "Error counting messages: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }


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
                        Log.e("getMessagesByReceiverId", "Error parsing message: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("getMessagesByReceiverId", "Error executing query: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return messages;
    }

    public void deleteMessageById(String messageId) {
        try {
            Log.e("IOService", "Deleted message of DB: " + messageId);

            int deletedRows = database.delete(TABLE_MESSAGES_MAIN, COLUMN_ID + " = ?", new String[]{messageId});
            Log.e("IOService", "Deleted rows: " + deletedRows);
        } catch (Exception e) {
            Log.e("IOService", "Error deleting message: " + e.getMessage());
        }
    }

}
