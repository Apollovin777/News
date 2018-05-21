package com.example.yurko.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.UUID;

public class NewsListActivity extends SingleFragmentActivity {

    private static final String LOG_TAG = NewsListActivity.class.getSimpleName();

    @Override
    protected Fragment createFragment() {
        return new NewsListFragment();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG,"NewsListActivity onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"NewsListActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"NewsListActivity onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG,"NewsListActivity onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG,"NewsListActivity onPause");
    }
}
