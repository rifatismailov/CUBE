package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.cube.chat.message.Message;
import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MessageManager - клас для управлжіння операціями над повідомленнями в базі даних SQLite.
 * <p>
 * Цей клас надає методи для додавання, оновлення, видалення та отримання повідомлень з бази даних.
 */
public class MessageManager {

    private SQLiteDatabase database;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_RECEIVER = "receiver";
    private static final String COLUMN_MESSAGE = "message_text";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_SIDE = "side";
    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_CHECK = "check_";
    private static final String COLUMN_SELECTED_URL = "selected_url";
    public static final String COLUMN_FILE_NAME = "file_name";

    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_IMAGE_WIDTH = "image_width";
    private static final String COLUMN_IMAGE_HEIGHT = "image_height";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_TYPE_FILE = "type_file";
    private static final String COLUMN_FILE_HASH = "hash";
    private static final String COLUMN_DATE_CREATE = "data_create";

    /**
     * Конструктор класу MessageManager.
     *
     * @param database SQLiteDatabase об’єкт, який використовується для зв’язку з базою даних.
     */
    public MessageManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Очищає таблицю повідомлень.
     */
    public void clearMessagesTable() {
        database.execSQL("DELETE FROM " + TABLE_MESSAGES);
    }

    /**
     * Додає нове повідомлення до бази даних.
     *
     * @param message Об’єкт Message, який містить інформацію про повідомлення.
     */
    public void addMessage(Message message) {
        ContentValues values = new ContentValues();

        // Додавання спільних полів
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());

        // Визначення типу повідомлення
        switch (message.getCheck()) {
            case Message: // Для текстового повідомлення
                break;

            case File: // Для повідомлення з файлом
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                    values.put(COLUMN_FILE_NAME, message.getFileName());
                    values.put(COLUMN_FILE_SIZE, message.getFileSize());
                    values.put(COLUMN_TYPE_FILE, message.getTypeFile());
                    values.put(COLUMN_FILE_HASH, message.getHas());
                    values.put(COLUMN_DATE_CREATE, message.getDataCreate());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage());
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            case Image: // Для повідомлення із зображенням
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                    values.put(COLUMN_FILE_NAME, message.getFileName());
                    values.put(COLUMN_FILE_SIZE, message.getFileSize());
                    values.put(COLUMN_TYPE_FILE, message.getTypeFile());
                    values.put(COLUMN_FILE_HASH, message.getHas());
                    values.put(COLUMN_DATE_CREATE, message.getDataCreate());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage());
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown message type: " + message.getCheck());
        }

        values.put(COLUMN_SIDE, message.getSide().toString());
        values.put(COLUMN_CHECK, message.getCheck().toString());
        values.put(COLUMN_STATUS, message.getMessageStatus());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());

        database.insert(TABLE_MESSAGES, null, values);
    }

    /**
     * Видаляє повідомлення за ID отримувача.
     *
     * @param receiverId ID отримувача.
     * @return Кількість видалених записів.
     */
    public int deleteMessagesByReceiverId(String receiverId) {
        return database.delete(TABLE_MESSAGES, COLUMN_RECEIVER + " = ?", new String[]{receiverId});
    }

    /**
     * Оновлює повідомлення в базі даних.
     *
     * @param message Об’єкт Message, який містить оновлені дані.
     * @return Кількість оновлених записів.
     */
    public int updateMessage(Message message) {
        ContentValues values = new ContentValues();
        Log.e("Listener", "Id " + message.getMessageId());
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());

        switch (message.getCheck()) {
            case Message:
                break;

            case File: // Для повідомлення з файлом
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                    values.put(COLUMN_FILE_NAME, message.getFileName());
                    values.put(COLUMN_FILE_SIZE, message.getFileSize());
                    values.put(COLUMN_TYPE_FILE, message.getTypeFile());
                    values.put(COLUMN_FILE_HASH, message.getHas());
                    values.put(COLUMN_DATE_CREATE, message.getDataCreate());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage());
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            case Image: // Для повідомлення із зображенням
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                    values.put(COLUMN_FILE_NAME, message.getFileName());
                    values.put(COLUMN_FILE_SIZE, message.getFileSize());
                    values.put(COLUMN_TYPE_FILE, message.getTypeFile());
                    values.put(COLUMN_FILE_HASH, message.getHas());
                    values.put(COLUMN_DATE_CREATE, message.getDataCreate());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage());
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown message type: " + message.getCheck());
        }

        values.put(COLUMN_SIDE, message.getSide().toString());
        values.put(COLUMN_CHECK, message.getCheck().toString());
        values.put(COLUMN_STATUS, message.getMessageStatus());

        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        Log.e("Listener", "Time  " + message.getTimestamp());

        return database.update(TABLE_MESSAGES, values, COLUMN_MESSAGE_ID + " = ?", new String[]{message.getMessageId()});
    }

    /**
     * Отримує список повідомлень за ID отримувача.
     *
     * @param receiverId ID отримувача.
     * @return Список об’єктів Message.
     */
    public List<Message> getMessagesByReceiverId(String receiverId) {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_RECEIVER + " = ?";

        Cursor cursor = database.rawQuery(selectQuery, new String[]{receiverId});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
                message.setReceiverId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER)));
                message.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                String urlString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_URL));
                String filename = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_NAME));
                String fileSize = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_SIZE));
                String fileType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_FILE));
                String fileHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_HASH));
                String fileDateCreate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_CREATE));

                if (urlString != null && !urlString.isEmpty()) {
                    message.setUrl(Uri.parse(urlString));
                    message.setFileName(filename);
                    message.setFileSize(fileSize);
                    message.setTypeFile(fileType);
                    message.setHas(fileHash);
                    message.setDataCreate(fileDateCreate);

                }
                message.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
                message.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
                message.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
                message.setSide(Side.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIDE))));
                message.setCheck(Check.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECK))));
                message.setMessageStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                message.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                Log.e("Listener", "Time read " + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //database.close();
        return messages;
    }

}
