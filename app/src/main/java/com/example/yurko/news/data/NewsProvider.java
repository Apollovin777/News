package com.example.yurko.news.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yurko.news.NewsUpdater;

public class NewsProvider extends ContentProvider {
    private static final String LOG_TAG = NewsProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final String COL_ID = "_id";
    private static final String CATEGORY = "category";


    private static final int NEWS = 100;
    private static final int NEWS_ID = 101;
    private static final int NEWS_CATEGORY = 102;


    private NewsDbHelper mDbHelper;

    static {
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY,NewsContract.PATH_NEWS,NEWS);
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY,NewsContract.PATH_NEWS+"/#",NEWS_ID);
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY,NewsContract.PATH_CAT+"/*",NEWS_CATEGORY);
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
            case NEWS_CATEGORY:
                selection = NewsContract.NewsEntry.COLUMN_CATEGORY + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                if (uri.getLastPathSegment().equals(NewsUpdater.ALL_NEWS)){
                    selection = null;
                    selectionArgs = null;
                }
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

        if(id != 0) {
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String category = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);
            getContext().getContentResolver().notifyChange(
                    Uri.withAppendedPath(NewsContract.NewsEntry.CONTENT_BY_CAT, category),
                    null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        int count = 0;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                count = database.delete(NewsContract.NewsEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case NEWS_ID:
                selection = NewsContract.NewsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                count = database.delete(NewsContract.NewsEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        selection = NewsContract.NewsEntry._ID + "=?";
        selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
        switch (match) {
            case NEWS_ID:
                rowsUpdated = database.update(NewsContract.NewsEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        if(rowsUpdated != 0){
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String category = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);

            getContext().getContentResolver().notifyChange(
                    Uri.withAppendedPath(NewsContract.NewsEntry.CONTENT_BY_CAT,category),
                    null);
        }
        return rowsUpdated;
    }
}
