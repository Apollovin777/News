package com.example.yurko.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    public static final String LOG_TAG = "testing";

    private String mUrl;

    public NewsLoader(@NonNull Context context,String url) {
        super(context);
        mUrl = url;
    }

    @Nullable
    @Override
    public List<NewsItem> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e){
            Log.i(LOG_TAG,e.getMessage());
        }

        List<NewsItem> news = Utilities.fetchNews(mUrl);
        return news;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        Log.i(LOG_TAG, "NewsLoader onForceLoad");
    }

    @Override
    protected void onAbandon() {
       Log.i(LOG_TAG, "NewsLoader onAbandon");
    }

    @Override
    protected void onReset() {
        super.onReset();
       Log.i(LOG_TAG, "NewsLoader onReset");
    }

    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
        Log.i(LOG_TAG, "NewsLoader onStartLoading");
    }
}
