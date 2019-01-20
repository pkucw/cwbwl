package com.example.dell.cwbwl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDB extends SQLiteOpenHelper {
    public final static String TABLE_NAME_RECORD = "record";

    public final static String RECORD_ID = "_id";
    public final static String RECORD_TITLE = "title_name";
    public final static String RECORD_BODY = "text_body";
    public final static String RECORD_TIME = "create_time";
    public final static String NOTICE_TIME ="notice_time";


    public MyDB(Context context) {
        super(context, "test.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME_RECORD+" ("+RECORD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                RECORD_TITLE+" VARCHAR(30)," +
                RECORD_BODY+" TEXT," +
                RECORD_TIME+" DATETIME NOT NULL," +
                NOTICE_TIME+" DATETIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}