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
 * MessageMainManager - a class for managing operations on messages in the SQLite database.
 * Used when receiving messages when the user is not chatting with contacts, i.e.
 * there is no active messaging with the contact from whom the message came
 * This class provides methods for adding, updating, deleting and retrieving messages from the database.
 */
public class MessageMainManager {
    private final SQLiteDatabase database;

    public static final String TABLE_MESSAGES_MAIN = "messages_main";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_OPERATION = "operation";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";
    public static final String COLUMN_TIMESTAMP = "timestamp";


    /**
     * Constructor to initialize MessageMainManager with SQLiteDatabase instance.
     *
     * @param database The database instance to manage contacts.
     */
    public MessageMainManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Method to store messages
     *
     * @param envelope the message itself
     */
    public void setMessage(Envelope envelope, String time) {
        try {
            Log.e("MessageMainManager", "Save or Update Message: " + envelope.getMessageId());

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, envelope.getMessageId());
            values.put(COLUMN_SENDER, envelope.getSenderId());
            values.put(COLUMN_OPERATION, envelope.getOperation());
            values.put(COLUMN_ENCRYPTED_DATA, envelope.toJson().toString());

            // Перевіряємо, чи є запис у базі
            Cursor cursor = database.query(TABLE_MESSAGES_MAIN, new String[]{COLUMN_TIMESTAMP}, COLUMN_ID + " = ?", new String[]{envelope.getMessageId()}, null, null, null);

            if (cursor.moveToFirst()) {
                String existingTime = cursor.getString(0);
                cursor.close();

                // Оновлюємо запис, залишаючи час незмінним
                values.put(COLUMN_TIMESTAMP, existingTime); // Використовуємо старий час
                int rowsUpdated = database.update(TABLE_MESSAGES_MAIN, values, COLUMN_ID + " = ?", new String[]{envelope.getMessageId()});
                Log.i("MessageMainManager", "Message updated: " + envelope.getMessageId());
            } else {
                cursor.close();
                // Додаємо новий запис із вказаним часом
                values.put(COLUMN_TIMESTAMP, time);
                long result = database.insert(TABLE_MESSAGES_MAIN, null, values);
                Log.i("MessageMainManager", "Message inserted: " + result);
            }

        } catch (Exception e) {
            Log.e("MessageMainManager", "Error inserting or updating message: " + e.getMessage());
        }
    }



    /**
     * Method for getting the number of messages by contact
     *
     * @param senderId  sender
     * @param operation operation by which the message arrived
     *                  we count when the message has the operation message or file
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
     * Method for receiving messages by contact
     *
     * @param senderId sender
     *                 message is received from oldest to youngest
     */
    public HashMap<String, Envelope> getMessagesByReceiverId(String senderId) {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // We use LinkedHashMap to preserve the order
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_MAIN +
                    " WHERE " + COLUMN_SENDER + " = ?" +
                    " ORDER BY " + COLUMN_TIMESTAMP + " ASC"; // Sort by increasing time
            cursor = database.rawQuery(selectQuery, new String[]{senderId});

            if (cursor.moveToFirst()) {
                do {
                    String messageId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String jsonMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                    JSONObject jsonObject = new JSONObject(jsonMessage);
                    Envelope envelope = new Envelope(jsonObject);
                    messages.put(messageId, envelope);
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

    public Envelope getMessageById(String messageId) {
        Envelope envelope = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_MAIN +
                    " WHERE " + COLUMN_ID + " = ?" +
                    " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1"; // Беремо останнє повідомлення

            cursor = database.rawQuery(selectQuery, new String[]{messageId});

            if (cursor.moveToFirst()) {
                String jsonMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                JSONObject jsonObject = new JSONObject(jsonMessage);
                envelope = new Envelope(jsonObject);
            }
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error executing query: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return envelope;
    }

    /**
     * Method for deleting messages
     *
     * @param messageId the identification number of the message to be deleted
     */
    public void deleteMessageById(String messageId) {
        try {
            int deletedRows = database.delete(TABLE_MESSAGES_MAIN, COLUMN_ID + " = ?", new String[]{messageId});
            Log.i("MessageMainManager", "Deleted rows: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting message: " + e.getMessage());
        }
    }

    /**
     * Method to delete all messages
     */
    public void deleteAllMessages() {
        try {
            int deletedRows = database.delete(TABLE_MESSAGES_MAIN, null, null);
            Log.i("MessageMainManager", "All messages deleted. Rows affected: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting all messages: " + e.getMessage());
        }
    }
}
