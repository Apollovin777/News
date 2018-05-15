package com.example.yurko.news.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yurko.news.data.NewsContract.NewsEntry;

public class NewsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = NewsDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "news.db";
    private static final int DATABASE_VERSION = 2;


    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_NEWS_TABLE = "CREATE TABLE " + NewsEntry.TABLE_NAME + " ("
                + NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NewsEntry.COLUMN_AUTHOR + " TEXT, "
                + NewsEntry.COLUMN_TITLE + " TEXT, "
                + NewsEntry.COLUMN_DESCRIPTION + " TEXT, "
                + NewsEntry.COLUMN_URL + " TEXT, "
                + NewsEntry.COLUMN_URLTOIMAGE + " TEXT, "
                + NewsEntry.COLUMN_SOURCE + " TEXT, "
                + NewsEntry.COLUMN_DATE + " TEXT);";

        db.execSQL(SQL_CREATE_NEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("SQLite", "Update from version " + oldVersion + " to version " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + NewsEntry.TABLE_NAME);
        onCreate(db);
    }
}
