package com.example.cube.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cube.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";
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
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";
    private static final String COLUMN_FILE_SIZE="file_size";
    private static final String COLUMN_TYPE_FILE="type_file";
    private static final String COLUMN_FILE_HASH="hash";
    private static final String COLUMN_DATE_CREATE="data_create";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_ACCOUNT_TABLE = "CREATE TABLE " + TABLE_ACCOUNT + " (" +
                    COLUMN_ENCRYPTED_DATA + " TEXT)";
            db.execSQL(CREATE_ACCOUNT_TABLE);
            Log.e("DatabaseHelper", "Table account created.");

            String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_ID + " TEXT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_RECEIVER + " TEXT, " +
                    COLUMN_MESSAGE + " TEXT, " +
                    COLUMN_SELECTED_URL + " TEXT, " +
                    COLUMN_FILE_NAME+ " TEXT, " +
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
                    COLUMN_TIMESTAMP + " TEXT)";
            db.execSQL(CREATE_MESSAGES_TABLE);
            Log.e("DatabaseHelper", "Table messages created.");

            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_ENCRYPTED_DATA + " TEXT)";
            db.execSQL(CREATE_CONTACTS_TABLE);
            Log.e("DatabaseHelper", "Table contacts created.");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
}
