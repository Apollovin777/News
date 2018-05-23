package com.example.yurko.news;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.yurko.news.data.NewsContract;


public class NewsPagerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = NewsPagerActivity.class.getSimpleName();
    private static final String EXTRA_NEWSITEM_ID =
            "com.example.yurko.news.newsitem_id";
    private static final String CATEGORY = "category";

    private ViewPager mViewPager;
    private CursorPagerAdapter mCursorPagerAdapter;
    private long mNewsID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_pager);

        mNewsID = getIntent().getLongExtra(EXTRA_NEWSITEM_ID, 0);

        mViewPager = (ViewPager) findViewById(R.id.news_view_pager);
        mCursorPagerAdapter = new CursorPagerAdapter(getSupportFragmentManager(), null);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    public static Intent newIntent(Context packageContext, long newsID) {
        Intent intent = new Intent(packageContext, NewsPagerActivity.class);
        intent.putExtra(EXTRA_NEWSITEM_ID, newsID);
        return intent;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        String category = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);

        CursorLoader loader = new CursorLoader(this,
                Uri.withAppendedPath(NewsContract.NewsEntry.CONTENT_BY_CAT,category),
                new String[]{NewsContract.NewsEntry._ID},
                null,
                null,
                NewsContract.NewsEntry.COLUMN_DATE + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, " testing NewsPagerActivity onLoadFinished");

            mViewPager.setAdapter(mCursorPagerAdapter);
            mCursorPagerAdapter.swapCursor(data);
            if (data != null) {
                if (data.getCount() > 0) {
                    while (data.moveToNext()) {
                        if (data.getLong(data.getColumnIndex(NewsContract.NewsEntry._ID)) == mNewsID)
                            mViewPager.setCurrentItem(data.getPosition());
                    }
                }
            }

            getSupportLoaderManager().getLoader(0).abandon();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorPagerAdapter.swapCursor(null);
    }
}
