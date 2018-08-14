package com.example.yurko.news;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.yurko.news.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class NewsUpdater extends AsyncTask<Void,Void,Void>{

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

    private Context mContext;
    private int mInsertedNewsCount;

    public NewsUpdater(Context context) {
        mContext = context;
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
            Log.e(LOG_TAG, "Error with creating URL ", e);
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
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.i(LOG_TAG, e.getMessage());
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
                ContentResolver resolver = mContext.getContentResolver();
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

    @Override
    protected Void doInBackground(Void... voids) {
        updateNews();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(mContext,"News updated",Toast.LENGTH_SHORT).show();
    }
}
