package com.example.database_cube;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cube.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_MESSAGES_MAIN = "messages_main";
    public static final String TABLE_MESSAGES_SERVICE = "messages_service";
    public static final String TABLE_CONTACTS = "contacts";
    public static final String TABLE_ACCOUNT = "account";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_MESSAGE = "message_text";
    public static final String COLUMN_SELECTED_URL = "selected_url";
    public static final String COLUMN_FILE_NAME = "file_name";

    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_IMAGE_WIDTH = "image_width";
    public static final String COLUMN_IMAGE_HEIGHT = "image_height";
    public static final String COLUMN_SIDE = "side";
    public static final String COLUMN_CHECK = "check_";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIMESTAMP = "timestamp";  // Тип INTEGER
    public static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_TYPE_FILE = "type_file";
    private static final String COLUMN_FILE_HASH = "hash";
    private static final String COLUMN_DATE_CREATE = "data_create";
    public static final String COLUMN_OPERATION = "operation";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Створення таблиці account
            String CREATE_ACCOUNT_TABLE = "CREATE TABLE " + TABLE_ACCOUNT + " (" +
                    COLUMN_ENCRYPTED_DATA + " TEXT)";
            db.execSQL(CREATE_ACCOUNT_TABLE);
            Log.e("DatabaseHelper", "Table account created.");

            // Створення таблиці messages
            String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_ID + " TEXT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_RECEIVER + " TEXT, " +
                    COLUMN_MESSAGE + " TEXT, " +
                    COLUMN_SELECTED_URL + " TEXT, " +
                    COLUMN_FILE_NAME + " TEXT, " +
                    COLUMN_IMAGE + " BLOB, " +
                    COLUMN_IMAGE_WIDTH + " INTEGER, " +
                    COLUMN_IMAGE_HEIGHT + " INTEGER, " +
                    COLUMN_FILE_SIZE + " TEXT, " +
                    COLUMN_TYPE_FILE + " TEXT, " +
                    COLUMN_FILE_HASH + " TEXT, " +
                    COLUMN_DATE_CREATE + " TEXT, " +
                    COLUMN_SIDE + " TEXT, " +
                    COLUMN_CHECK + " TEXT, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER)";
            db.execSQL(CREATE_MESSAGES_TABLE);
            Log.e("DatabaseHelper", "Table messages created.");

            // Створення таблиці contacts
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_ENCRYPTED_DATA + " TEXT)";
            db.execSQL(CREATE_CONTACTS_TABLE);
            Log.e("DatabaseHelper", "Table contacts created.");

            // Створення таблиці messages_main
            String CREATE_MESSAGES_MAIN_TABLE = "CREATE TABLE " + TABLE_MESSAGES_MAIN + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_OPERATION + " TEXT, " +
                    COLUMN_ENCRYPTED_DATA + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER)";
            db.execSQL(CREATE_MESSAGES_MAIN_TABLE);
            Log.e("DatabaseHelper", "Table Message main created.");

            // Створення таблиці messages_service
            String CREATE_MESSAGES_SERVICE_TABLE = "CREATE TABLE " + TABLE_MESSAGES_SERVICE + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_OPERATION + " TEXT, " +
                    COLUMN_ENCRYPTED_DATA + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER)";
            db.execSQL(CREATE_MESSAGES_SERVICE_TABLE);
            Log.e("DatabaseHelper", "Table Message service created.");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {  // Перевірка для того, щоб виконати це лише при оновленні
            // Створення нової таблиці з оновленою схемою
            String CREATE_NEW_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "_new (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_ID + " TEXT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_RECEIVER + " TEXT, " +
                    COLUMN_MESSAGE + " TEXT, " +
                    COLUMN_SELECTED_URL + " TEXT, " +
                    COLUMN_FILE_NAME + " TEXT, " +
                    COLUMN_IMAGE + " BLOB, " +
                    COLUMN_IMAGE_WIDTH + " INTEGER, " +
                    COLUMN_IMAGE_HEIGHT + " INTEGER, " +
                    COLUMN_FILE_SIZE + " TEXT, " +
                    COLUMN_TYPE_FILE + " TEXT, " +
                    COLUMN_FILE_HASH + " TEXT, " +
                    COLUMN_DATE_CREATE + " TEXT, " +
                    COLUMN_SIDE + " TEXT, " +
                    COLUMN_CHECK + " TEXT, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER)";
            db.execSQL(CREATE_NEW_MESSAGES_TABLE);

            // Копіювання даних з старої таблиці в нову
            String COPY_DATA_QUERY = "INSERT INTO " + TABLE_MESSAGES + "_new SELECT * FROM " + TABLE_MESSAGES;
            db.execSQL(COPY_DATA_QUERY);

            // Видалення старої таблиці
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

            // Перейменування нової таблиці на стару назву
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + "_new RENAME TO " + TABLE_MESSAGES);
        }
    }

    // Метод для вставки нового повідомлення
    public long insertMessage(SQLiteDatabase db, String messageId, String sender, String receiver,
                              String messageText, String selectedUrl, String fileName, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_ID, messageId);
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_RECEIVER, receiver);
        values.put(COLUMN_MESSAGE, messageText);
        values.put(COLUMN_SELECTED_URL, selectedUrl);
        values.put(COLUMN_FILE_NAME, fileName);
        values.put(COLUMN_TIMESTAMP, timestamp);

        return db.insert(TABLE_MESSAGES, null, values);
    }

    // Метод для отримання останнього часу повідомлення
    public long getLastMessageTimestamp(SQLiteDatabase db) {
        String query = "SELECT " + COLUMN_TIMESTAMP + " FROM " + TABLE_MESSAGES +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
            cursor.close();
            return timestamp;
        }
        return 0;
    }
}
