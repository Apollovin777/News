package com.example.yurko.news;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.example.yurko.news.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UPDNews extends JobIntentService {

    private static final String LOG_TAG = NewsUpdater.class.getSimpleName();
    private static final String API_KEY = "50e74e09cc214f87abd4c2cc543c3edb";
    private static final String NEWSAPI_BASE_URL = "http://newsapi.org/v2/top-headlines";
    private static final String COUNTRY = "country";
    private static final String UKRAINE = "ua";
    private static final String CATEGORY = "category";

    public static final String BUSINESS = "business";
    public static final String ENTERTAINMENT = "entertainment";
    public static final String HEALTH = "health";
    public static final String SCIENCE = "science";
    public static final String SPORTS = "sports";
    public static final String TECHNOLOGY = "technology";
    public static final String TOP_NEWS = "top_news";
    public static final String ALL_NEWS = "all_news";

    public static final String[] CATEGORIES = {TOP_NEWS,SPORTS,SCIENCE,HEALTH,ENTERTAINMENT,BUSINESS,ALL_NEWS};

    private int mInsertedNewsCount;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        synchronized (this){
            Log.i("testing","UPDNews");

            String filename = "myfile";
            Date date = new Date();
            String fileContents = "Last job run: " + date.toString();
            FileOutputStream outputStream;
            try {
                outputStream = openFileOutput(filename, getBaseContext().MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            int newItentsCount = updateNews();
            if(newItentsCount > 0) {
                Intent resultIntent = new Intent(this, NewsListActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

// Create Notification
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean isNotificationEnabled = preferences.getBoolean(getResources().
                        getString(R.string.pref_key_notification_show), true);

                if (isNotificationEnabled) {
                    NotificationCompat.Builder builder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("News")
                                    .setContentText("News list is updated.")
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true);

                    Notification notification = builder.build();

// Show Notification
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1, notification);
                }
            }

            stopSelf();
            Log.i("testing","UPDNews_End");
        }
    }


    public int updateNews(){

        mInsertedNewsCount = 0;
        List<String> categories = new ArrayList<>();
        categories.add(BUSINESS);
        categories.add(ENTERTAINMENT);
        categories.add(SCIENCE);
        categories.add(SPORTS);
        //categories.add(TECHNOLOGY);
        categories.add(HEALTH);
        categories.add(TOP_NEWS);
        for (String cat: categories
                ) {
            fetchNews(cat);
        }
        return mInsertedNewsCount;
    }

    private void fetchNews(String category) {
        URL url = buildRequestURL(category);
        String jsonResponse = makeHttpRequest(url);
        storeInDB(category,jsonResponse);
    }

    private URL buildRequestURL(String category) {
        Uri baseUri = Uri.parse(NEWSAPI_BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(COUNTRY, UKRAINE);

        if (category != TOP_NEWS){
            uriBuilder.appendQueryParameter(CATEGORY, category);
        }

        URL url = null;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            android.util.Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private String makeHttpRequest(URL url) {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-api-key", API_KEY);
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = urlConnection.getInputStream();
                jsonResponse = readFromStream(stream);
            } else {
                android.util.Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            android.util.Log.i(LOG_TAG, e.getMessage());
            return jsonResponse;
        } finally {
            urlConnection.disconnect();
        }

        return jsonResponse;
    }

    private void storeInDB(String category,String jsonResponse) {

        try {
            JSONObject rootObject = new JSONObject(jsonResponse);
            JSONArray articlesArray = rootObject.getJSONArray("articles");
            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject newsItem = articlesArray.getJSONObject(i);
                String title = newsItem.getString("title");
                String author = newsItem.getString("author");
                String source = newsItem.getJSONObject("source").getString("name");
                String newsUrl = newsItem.getString("url");
                String urlToImage = newsItem.getString("urlToImage");
                String description = newsItem.getString("description");
                String date = newsItem.getString("publishedAt");

                ContentValues values = new ContentValues();
                values.put(NewsContract.NewsEntry.COLUMN_TITLE, title);
                values.put(NewsContract.NewsEntry.COLUMN_AUTHOR, author);
                values.put(NewsContract.NewsEntry.COLUMN_SOURCE, source);
                values.put(NewsContract.NewsEntry.COLUMN_URL, newsUrl);
                values.put(NewsContract.NewsEntry.COLUMN_URLTOIMAGE, urlToImage);
                values.put(NewsContract.NewsEntry.COLUMN_DESCRIPTION, description);
                values.put(NewsContract.NewsEntry.COLUMN_DATE, date);
                values.put(NewsContract.NewsEntry.COLUMN_CATEGORY, category);
                values.put(NewsContract.NewsEntry.COLUMN_ISREAD,0);
                ContentResolver resolver = getBaseContext().getContentResolver();
                Uri uri = resolver.insert(NewsContract.NewsEntry.CONTENT_URI, values);
                long id = ContentUris.parseId(uri);
                if(id != 0){
                    mInsertedNewsCount++;
                }

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        String result = "";
        if (inputStream == null) {
            return result;
        }
        InputStreamReader ir = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(ir);

        StringBuilder builder = new StringBuilder();

        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        return builder.toString();
    }
}
