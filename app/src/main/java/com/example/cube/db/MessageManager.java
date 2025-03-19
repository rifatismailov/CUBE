package com.example.cube.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.cube.chat.message.Message;
import com.example.cube.control.Check;
import com.example.cube.control.Side;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageManager - клас для управління операціями над повідомленнями в базі даних SQLite.
 * <p>
 * Цей клас надає методи для додавання, оновлення, видалення та отримання повідомлень з бази даних.
 */
public class MessageManager {

    private final SQLiteDatabase database;

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
     * Constructor of the MessageManager class.
     *
     * @param database SQLiteDatabase object used to connect to the database.
     */
    public MessageManager(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Clears the message table.
     */
    public void clearMessagesTable() {
        database.execSQL("DELETE FROM " + TABLE_MESSAGES);
    }

    /**
     * Adds a new message to the database.
     *
     * @param message Message object containing information about the message.
     */
    public void addMessage(Message message) {
        ContentValues values = new ContentValues();

        // Add shared fields
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());

// Determine the message type
        switch (message.getCheck()) {
            case Message: // For a text message
                break;

            case File: // For a message with a file
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

            case Image: // For a message with a picture
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
        // there is duplication and if there is, we simply overwrite it
        database.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        //database.insert(TABLE_MESSAGES, null, values);
    }

    /**
     * Deletes messages by recipient ID.
     *
     * @param receiverId The recipient ID.
     */
    public void deleteMessagesByReceiverId(String receiverId) {
        database.delete(TABLE_MESSAGES, COLUMN_RECEIVER + " = ?", new String[]{receiverId});
    }

    /**
     * Updates messages in the database.
     *
     * @param message A Message object containing the updated data.
     */
    public void updateMessage(Message message) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSenderId());
        values.put(COLUMN_RECEIVER, message.getReceiverId());
        values.put(COLUMN_MESSAGE, message.getMessage());

        switch (message.getCheck()) {
            case Message:
                break;

            case File: // For a message with a file
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

            case Image: // For a message with a picture
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

        database.update(TABLE_MESSAGES, values, COLUMN_MESSAGE_ID + " = ?", new String[]{message.getMessageId()});
    }

    /**
     * Gets a list of messages by recipient ID.
     *
     * @param receiverId The recipient ID.
     * @return A list of Message objects.
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
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //database.close();
        return messages;
    }
    /**
     * Gets the last message by recipient ID.
     *
     * @param receiverId The recipient ID.
     * @return The last message or null if there are no messages.
     */
    public Message getLastMessageByReceiverId(String receiverId) {
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
                messages.add(message);
            } while (cursor.moveToNext());
        }
        assert cursor != null;
        cursor.close();
        return messages.get(messages.size()-1);
    }

}
