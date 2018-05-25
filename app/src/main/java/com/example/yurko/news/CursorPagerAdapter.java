package com.example.yurko.news;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.yurko.news.data.NewsContract;
import com.example.yurko.news.data.NewsProvider;



public class CursorPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public CursorPagerAdapter(FragmentManager fm, Cursor c) {
        super(fm);
        mCursor = c;
    }

    @Override
    public Fragment getItem(int position) {
        if (mCursor.moveToPosition(position)) {
            Bundle arguments = new Bundle();
            arguments.putLong(NewsDetailsFragment.ARG_NEWSITEM_ID, mCursor.getLong(mCursor.getColumnIndex(NewsProvider.COL_ID)));

            NewsDetailsFragment fragment = NewsDetailsFragment.newInstance(
                    mCursor.getLong(mCursor.getColumnIndex(NewsContract.NewsEntry._ID)));
            return fragment;
        }
        return null;
    }

    public long getItemId(int position){
        if (mCursor.moveToPosition(position)) {
            return mCursor.getLong(mCursor.getColumnIndex(NewsProvider.COL_ID));
        }
        return 0;
    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
