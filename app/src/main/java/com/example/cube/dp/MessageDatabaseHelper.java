package com.example.cube.dp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cube.chat.message.Message;
import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cube.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
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
    private Context context;
    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE_ID + " TEXT, " +
                COLUMN_SENDER + " TEXT, " +
                COLUMN_RECEIVER + " TEXT, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_SELECTED_URL + " TEXT, " +
                COLUMN_IMAGE + " BLOB, " +
                COLUMN_IMAGE_WIDTH + " INTEGER, " +
                COLUMN_IMAGE_HEIGHT + " INTEGER, " +
                COLUMN_SIDE + " TEXT, " +
                COLUMN_CHECK + " TEXT, " +
                COLUMN_STATUS + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT)";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
    public void clearMessagesTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);
    }

    public void addMessage(SQLiteDatabase db, Message message) {
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
        values.put(COLUMN_STATUS, message.getMessageStatus()); // Конвертуйте Check у String, якщо це enum
        values.put(COLUMN_TIMESTAMP, new Date().getTime());
        // Вставка даних у таблицю
        db.insert("messages", null, values);
    }
    public int deleteMessagesByReceiverId(SQLiteDatabase db, String receiverId) {
        // Видалення записів, де ReceiverId збігається
        return db.delete(TABLE_MESSAGES, COLUMN_RECEIVER + " = ?", new String[]{receiverId});
    }

    public int updateMessage(SQLiteDatabase db, Message message) {
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
        return db.update(TABLE_MESSAGES, values, COLUMN_MESSAGE_ID + " = ?", new String[]{message.getMessageId()});
    }
    public List<Message> getMessagesByReceiverId(String receiverId) {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_RECEIVER + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{receiverId});

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
                message.setReceiverId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER)));
                message.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                message.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_URL)));
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
        db.close();
        return messages;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
//                Message message = new Message();
//                message.setId(cursor.getInt(0));
//                message.setSender(cursor.getString(1));
//                message.setReceiver(cursor.getString(2));
//                message.setMessage(cursor.getString(3));
//                message.setTimestamp(cursor.getString(4));
//
//                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messages;
    }
}
