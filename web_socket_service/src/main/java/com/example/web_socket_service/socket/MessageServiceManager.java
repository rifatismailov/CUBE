package com.example.web_socket_service.socket;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * MessageMainManager - клас для управління операціями над повідомленнями в базі даних SQLite.
 * Використовується під час отримання повідомлень коли користувач не виконує чат з контактам тоб то
 * активного месенджуваня нема з контактом від якого прийшло повідомлення
 * Цей клас надає методи для додавання, оновлення, видалення та отримання повідомлень з бази даних.
 */
public class MessageServiceManager {
    private SQLiteDatabase database;

    public static final String TABLE_MESSAGES_SERVICE = "messages_service";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_OPERATION = "operation";
    private static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";
    public static final String COLUMN_TIMESTAMP = "timestamp";


    /**
     * Конструктор для ініціалізації MessageMainManager екземпляром SQLiteDatabase.
     * <p>
     * База даних @param Екземпляр бази даних для керування контактами.
     */
    public MessageServiceManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Метод для зберігання повідомлень
     *
     * @param envelope саме повідомлення
     */
    public void setMessage(Envelope envelope) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, envelope.getMessageId());
            values.put(COLUMN_SENDER, envelope.getSenderId());
            values.put(COLUMN_OPERATION, envelope.getOperation());
            values.put(COLUMN_ENCRYPTED_DATA, envelope.toJson().toString());
            values.put(COLUMN_TIMESTAMP, envelope.getTime());
            long result = database.insertWithOnConflict(TABLE_MESSAGES_SERVICE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.e("MessageMainManager", "Insert result: " + result);

        } catch (Exception e) {
            Log.e("MessageMainManager", "Error inserting message: " + e.getMessage());
        }
    }
    public Envelope getMessageById(String messageId) {
        Envelope envelope = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_SERVICE +
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
     * Метод для зберігання повідомлень
     *
     * @param envelope саме повідомлення
     */
    public void setMessage(Envelope envelope, String operation) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, envelope.getMessageId());
            values.put(COLUMN_SENDER, envelope.getSenderId());
            values.put(COLUMN_OPERATION, operation); //сюди буде додаватися назва операції send що значить на відправу
            values.put(COLUMN_ENCRYPTED_DATA, envelope.toJson().toString());
            values.put(COLUMN_TIMESTAMP, envelope.getTime());
            // Перевіряємо, чи вже є запис із таким ID
            Cursor cursor = database.query(TABLE_MESSAGES_SERVICE,
                    new String[]{COLUMN_ID},
                    COLUMN_ID + " = ?",
                    new String[]{envelope.getMessageId()},
                    null, null, null);

            boolean exists = cursor.getCount() > 0;
            cursor.close();

            if (exists) {
                // Якщо запис є, оновлюємо його
                int rowsUpdated = database.update(TABLE_MESSAGES_SERVICE, values, COLUMN_ID + " = ?", new String[]{envelope.getMessageId()});
                Log.i("MessageMainManager", "Message updated: " + envelope.getMessageId() + " (" + rowsUpdated + " rows affected)");
            } else {
                // Якщо запису немає, додаємо новий
                values.put(COLUMN_ID, envelope.getMessageId());
                long result = database.insert(TABLE_MESSAGES_SERVICE, null, values);
                Log.i("MessageMainManager", "New message inserted: " + result);
            }

        } catch (Exception e) {
            Log.e("MessageMainManager", "Error inserting/updating message: " + e.getMessage());
        }
    }

    /**
     * Метод для отримання кількості повідомлень за контактом
     *
     * @param operation операція за яким прийшло повідомлення
     *                  підрахунок робимо коли повідомлення має operation message або file
     */
    public int getMessageCountByOperation(String operation) {
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES_SERVICE +
                    " WHERE " + COLUMN_OPERATION + " = ?";
            cursor = database.rawQuery(query, new String[]{operation});

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0); // Отримуємо значення COUNT(*)
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
     */
    public HashMap<String, Envelope> getMessages() {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // Використовуємо LinkedHashMap для збереження порядку
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_MESSAGES_SERVICE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String messageId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String jsonMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED_DATA));
                JSONObject jsonObject = new JSONObject(jsonMessage);
                Envelope envelope = new Envelope(jsonObject);
                messages.put(messageId, envelope);
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
     * Метод для отримання повідомлень за контактом
     *
     * @param operation
     */
    public HashMap<String, Envelope> getMessagesByOperation(String operation) {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // Використовуємо LinkedHashMap для збереження порядку
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_SERVICE +
                    " WHERE " + COLUMN_OPERATION + " = ?" +
                    " ORDER BY " + COLUMN_TIMESTAMP + " ASC"; // Сортування за зростанням часу
            cursor = database.rawQuery(selectQuery, new String[]{operation});

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
    public HashMap<String, Envelope> getMessagesExceptOperation(String excludedOperation) {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // Використовуємо LinkedHashMap для збереження порядку
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_SERVICE +
                    " WHERE " + COLUMN_OPERATION + " != ?" +
                    " ORDER BY " + COLUMN_TIMESTAMP + " ASC"; // Сортування за зростанням часу

            cursor = database.rawQuery(selectQuery, new String[]{excludedOperation});

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
     * Метод для отримання повідомлень за контактом
     *
     * @param senderId відправник
     *                 повідомлення отримується від старшого до молодшого
     */
    public HashMap<String, Envelope> getMessagesByReceiverId(String senderId) {
        HashMap<String, Envelope> messages = new LinkedHashMap<>(); // Використовуємо LinkedHashMap для збереження порядку
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MESSAGES_SERVICE +
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
     *
     * @param messageId ідентифікаційним номер повідомлення який буде видалений
     */
    public void deleteMessageById(String messageId) {
        try {
            int deletedRows = database.delete(TABLE_MESSAGES_SERVICE, COLUMN_ID + " = ?", new String[]{messageId});
            Log.e("MessageMainManager", "Deleted rows: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting message: " + e.getMessage());
        }
    }

    /**
     * Метод для видалення всіх повідомлень
     */
    public void deleteAllMessages() {
        try {
            int deletedRows = database.delete(TABLE_MESSAGES_SERVICE, null, null);
            Log.e("MessageMainManager", "All messages deleted. Rows affected: " + deletedRows);
        } catch (Exception e) {
            Log.e("MessageMainManager", "Error deleting all messages: " + e.getMessage());
        }
    }


}
