package com.example.cube.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cube.db";
    private static final int DATABASE_VERSION = 1;



    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_CONTACTS = "contacts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_SELECTED_URL = "selected_url";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_IMAGE_WIDTH = "image_width";
    public static final String COLUMN_IMAGE_HEIGHT = "image_height";
    public static final String COLUMN_SIDE = "side";
    public static final String COLUMN_CHECK = "check";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_ENCRYPTED_DATA = "encrypted_data";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_ENCRYPTED_DATA + " TEXT)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
}