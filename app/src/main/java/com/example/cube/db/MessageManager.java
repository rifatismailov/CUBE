package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.cube.chat.message.Message;
import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageManager {
    private SQLiteDatabase database;
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_SENDER = "senderId";
    private static final String COLUMN_RECEIVER = "receiverId";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_SIDE = "sider";
    private static final String COLUMN_MESSAGE_ID = "messagesId";
    private static final String COLUMN_CHECK = "checker";
    private static final String COLUMN_SELECTED_URL = "url";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_IMAGE_WIDTH = "width";
    private static final String COLUMN_IMAGE_HEIGHT = "height";
    private static final String COLUMN_STATUS = "status";



        public MessageManager(SQLiteDatabase database) {
            this.database = database;
        }

    public void clearMessagesTable() {
        database.execSQL("DELETE FROM " + TABLE_MESSAGES);
    }


    public void addMessage(Message message) {
        ContentValues values = new ContentValues();

        // Додати спільні поля
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());


        // Toast.makeText(context, ""+message.getUrl().toString(), Toast.LENGTH_SHORT).show();

        // Визначення типу повідомлення
        switch (message.getCheck()) {
            case Message: // Для текстового повідомлення
                break;

            case File: // Для повідомлення з файлом
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                }
                break;

            case Image: // Для повідомлення із зображенням
                if (message.getUrl() != null && !message.getUrl().toString().isEmpty()) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage()); // Зберігайте як BLOB
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown message type: " + message.getCheck());
        }
        values.put(COLUMN_SIDE, message.getSide().toString()); // Конвертуйте Side у String, якщо це enum
        values.put(COLUMN_CHECK, message.getCheck().toString()); // Конвертуйте Check у String, якщо це enum
        values.put(COLUMN_STATUS, message.getMessageStatus()); // Конвертуйте Check у String, якщо це enum
        values.put(COLUMN_TIMESTAMP, new Date().getTime());
        // Вставка даних у таблицю
        database.insert(TABLE_MESSAGES, null, values);
    }

    public int deleteMessagesByReceiverId( String receiverId) {
        // Видалення записів, де ReceiverId збігається
        return database.delete(TABLE_MESSAGES, COLUMN_RECEIVER + " = ?", new String[]{receiverId});
    }

    public int updateMessage(Message message) {
        ContentValues values = new ContentValues();
        // Додати спільні поля
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());

        // Визначення типу повідомлення
        switch (message.getCheck()) {
            case Message: // Для текстового повідомлення
                break;

            case File: // Для повідомлення з файлом
                if (message.getUrl() != null) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                }
                break;

            case Image: // Для повідомлення із зображенням
                if (message.getUrl() != null) {
                    values.put(COLUMN_SELECTED_URL, message.getUrl().toString());
                }
                if (message.getImage() != null) {
                    values.put(COLUMN_IMAGE, message.getImage()); // Зберігайте як BLOB
                    values.put(COLUMN_IMAGE_WIDTH, message.getImageWidth());
                    values.put(COLUMN_IMAGE_HEIGHT, message.getImageHeight());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown message type: " + message.getCheck());
        }
        values.put(COLUMN_SIDE, message.getSide().toString()); // Конвертуйте Side у String, якщо це enum
        values.put(COLUMN_CHECK, message.getCheck().toString()); // Конвертуйте Check у String, якщо це enum
        values.put(COLUMN_STATUS, message.getMessageStatus()); // Конвертуйте STATUS у String
        values.put(COLUMN_TIMESTAMP, new Date().getTime());
        // Виконання запиту на оновлення за messageId
        return database.update(TABLE_MESSAGES, values, COLUMN_MESSAGE_ID + " = ?", new String[]{message.getMessageId()});
    }

    public List<Message> getMessagesByReceiverId(String receiverId) {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_RECEIVER + " = ?";


        Cursor cursor = database.rawQuery(selectQuery, new String[]{receiverId});

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
                message.setReceiverId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER)));
                message.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                String urlString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_URL));
                if (urlString != null && !urlString.isEmpty()) {
                    message.setUrl(Uri.parse(urlString));
                }
                message.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
                message.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
                message.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
                message.setSide(Side.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIDE))));
                message.setCheck(Check.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECK))));
                message.setMessageStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        ////database.close();
        return messages;
    }

}
