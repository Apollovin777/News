package com.example.yurko.news;

import android.util.Log;
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

public class Utilities {

    private static final String LOG_TAG = Utilities.class.getSimpleName();
    private static final String API_KEY = "50e74e09cc214f87abd4c2cc543c3edb";

    public static List<NewsItem> fetchNews(String urlString) {
        URL url = createURL(urlString);
        String jsonResponse = makeHttpRequest(url);
        return extractNews(jsonResponse);
    }

    private static URL createURL(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-api-key",API_KEY);
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
        }
        catch (IOException e){
            Log.i(LOG_TAG,e.getMessage());
            return jsonResponse;
        }
        finally {
            urlConnection.disconnect();
        }

        return jsonResponse;
    }

    private static ArrayList<NewsItem> extractNews(String jsonResponse) {

        ArrayList<NewsItem> news = new ArrayList<>();
        //2018-05-06T17:05:24Z
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
        try {
            JSONObject rootObject = new JSONObject(jsonResponse);
            JSONArray articlesArray = rootObject.getJSONArray("articles");
            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject newsItem = articlesArray.getJSONObject(i);
                String title = newsItem .getString("title");

                String source = newsItem.getJSONObject("source").getString("name");
                String newsUrl = newsItem.getString("url");

                String urlToImage = newsItem.getString("urlToImage");

                String description = newsItem.getString("description");

                Date date = df.parse(newsItem.getString("publishedAt").replaceAll("Z$", "+0000"));

                NewsItem objectNewsItem = new NewsItem(title,source,newsUrl, urlToImage, description,date);
                news.add(objectNewsItem);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }
        catch (ParseException e){
            Log.e(LOG_TAG, "Problem parsing Date", e);
        }
        return news;
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
