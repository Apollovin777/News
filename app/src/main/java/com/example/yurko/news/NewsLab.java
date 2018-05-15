package com.example.yurko.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.yurko.news.data.NewsContract;
import com.example.yurko.news.data.NewsContract.NewsEntry;
import com.example.yurko.news.data.NewsDbHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NewsLab {

    private static final String LOG_TAG = NewsLab.class.getSimpleName();
    private static NewsLab sNewsLab;
    private ArrayList<NewsItem> mNews;
    private Context mContext;

    public static NewsLab get(Context context) {
        if (sNewsLab == null) {
            sNewsLab = new NewsLab(context);
        }
        return sNewsLab;
    }

    private NewsLab(Context context) {
        mNews = new ArrayList<>();
        mContext = context;
        fillListFromDB();
    }

    public ArrayList<NewsItem> getNews() {
        return mNews;
    }

    public void setNews(ArrayList<NewsItem> news) {
        mNews = news;
    }

    public NewsItem getNewsItem(UUID id) {
        for (NewsItem newsItem : mNews) {
            if (newsItem.getId().equals(id)) {
                return newsItem;
            }
        }
        return null;
    }

    public void addNewItems(List<NewsItem> list) {
        for (NewsItem newsItem : list) {
            if (!mNews.contains(newsItem)) {
                mNews.add(newsItem);
                insertNewsItemIntoDB(newsItem);
            }
        }
        Collections.sort(mNews);
    }

    private void insertNewsItemIntoDB(NewsItem item) {
        NewsDbHelper dbHelper = new NewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NewsContract.NewsEntry.COLUMN_AUTHOR, item.getAuthor());
        values.put(NewsContract.NewsEntry.COLUMN_DATE, item.getDate().toString());
        values.put(NewsContract.NewsEntry.COLUMN_DESCRIPTION, item.getDescription());
        values.put(NewsContract.NewsEntry.COLUMN_SOURCE, item.getSource());
        values.put(NewsContract.NewsEntry.COLUMN_TITLE, item.getTitle());
        values.put(NewsContract.NewsEntry.COLUMN_URL, item.getUrl());
        values.put(NewsContract.NewsEntry.COLUMN_URLTOIMAGE, item.getUrlToImage());

        db.insert(NewsContract.NewsEntry.TABLE_NAME, null, values);
    }

    private void fillListFromDB(){
        NewsDbHelper dbHelper = new NewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
            NewsEntry.COLUMN_AUTHOR,
            NewsEntry.COLUMN_DATE,
                NewsEntry.COLUMN_DESCRIPTION,
                NewsEntry.COLUMN_SOURCE,
                NewsEntry.COLUMN_TITLE,
                NewsEntry.COLUMN_URL,
                NewsEntry.COLUMN_URLTOIMAGE};

        Cursor cursor = db.query(
                NewsContract.NewsEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        if (!cursor.moveToFirst()){
            return;
        }
        Date date;
        //Thu May 10 18:44:00 CAT 2018
        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_TITLE));
            String source = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_SOURCE));
            String newsUrl = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_URL));
            String urlToImage = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_URLTOIMAGE));
            String description = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_DESCRIPTION));
            String strDate = cursor.getString(cursor.getColumnIndex(NewsEntry.COLUMN_DATE));
            try {
                date = dateFormat.parse(strDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parsing datetime failed", e);
                date = null;
            }
            NewsItem objectNewsItem = new NewsItem(title,source,newsUrl, urlToImage, description,date);
            mNews.add(objectNewsItem);
        }

    }

}
