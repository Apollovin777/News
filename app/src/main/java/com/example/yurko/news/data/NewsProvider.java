package com.example.yurko.news.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class NewsProvider extends ContentProvider {
    private static final String LOG_TAG = NewsProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final String COL_ID = "_id";


    private static final int NEWS = 100;
    private static final int NEWS_ID = 101;

    private NewsDbHelper mDbHelper;

    static {
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY,NewsContract.PATH_NEWS,NEWS);
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY,NewsContract.PATH_NEWS+"/#",NEWS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new NewsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                cursor = database.query(NewsContract.NewsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case NEWS_ID:
                selection = NewsContract.NewsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(NewsContract.NewsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = 0;
        String[] checkProjection = {NewsContract.NewsEntry._ID, NewsContract.NewsEntry.COLUMN_URL};
        String[] checkArgs = {values.getAsString(NewsContract.NewsEntry.COLUMN_URL)};
        switch (match) {
            case NEWS:
                Cursor checkSet = database.query(NewsContract.NewsEntry.TABLE_NAME,
                        checkProjection,
                        NewsContract.NewsEntry.COLUMN_URL + "=?",
                        checkArgs,null,null,null);
                if (!checkSet.moveToFirst()) {
                    id = database.insert(NewsContract.NewsEntry.TABLE_NAME, null, values);
                }
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
