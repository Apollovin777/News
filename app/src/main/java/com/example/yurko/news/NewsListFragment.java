package com.example.yurko.news;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import com.example.yurko.news.data.NewsContract.NewsEntry;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yurko.news.data.NewsContract;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NewsListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "testing";

    private ListView mListView;
    private ProgressBar mProcessBar;
    private NewsCursorAdapter mNewsCursorAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyList;
    private NewsUpdater mNewsUpdater;

    private static final int CURSOR_LOADER_ID = 2;
    private static final String CATEGORY = "category";

    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    NewsEntry._ID,
                    NewsEntry.COLUMN_AUTHOR,
                    NewsEntry.COLUMN_DATE,
                    NewsEntry.COLUMN_DESCRIPTION,
                    NewsEntry.COLUMN_SOURCE,
                    NewsEntry.COLUMN_TITLE,
                    NewsEntry.COLUMN_URL,
                    NewsEntry.COLUMN_URLTOIMAGE,
                    NewsEntry.COLUMN_CATEGORY,
                    NewsEntry.COLUMN_ISREAD};

            CursorLoader loader = null;
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String category = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);


            //Log.i(LOG_TAG + "Callbacks",category);
                    loader = new CursorLoader(getContext(),
                            Uri.withAppendedPath(NewsEntry.CONTENT_BY_CAT,category),
                            projection, null,null, NewsEntry.COLUMN_DATE + " DESC");
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.i(LOG_TAG, "onLoadFinished");
            if (data == null || !data.moveToFirst()) {
                mEmptyList.setText(R.string.no_news);
            } else {
                mNewsCursorAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mNewsCursorAdapter.swapCursor(null);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.newslist_fragment, container, false);

        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);

        mNewsCursorAdapter = new NewsCursorAdapter(getActivity(), null);
        mListView.setAdapter(mNewsCursorAdapter);


        mEmptyList = (TextView) view.findViewById(R.id.empty);
        mListView.setEmptyView(mEmptyList);

        mProcessBar = (ProgressBar) view.findViewById(R.id.loading_spinner);
        mProcessBar.setVisibility(View.GONE);
        Log.i(LOG_TAG, "NewsListFragment onCreateView");

        mNewsUpdater = new NewsUpdater(getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                updateNewsList();
            }
        });

        getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mCursorLoaderCallbacks);


        updateNewsList();

        return view;
    }

    private void updateNewsList() {
        mEmptyList.setText("");

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mProcessBar.setVisibility(View.GONE);
            mEmptyList.setText(R.string.no_connection);
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_LONG).show();
            return;
        }
        if (mNewsUpdater != null){
            mNewsUpdater = null;
            mNewsUpdater = new NewsUpdater(getContext());
        }
        mNewsUpdater.execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = NewsPagerActivity.newIntent(getActivity(), id);
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
        MenuItem item = menu.findItem(R.id.category_spinner);
        final Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cat_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String[] arrayCategories = getActivity().getResources().getStringArray(R.array.cat_spinner_array);

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String savedText = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);

        for(int i = 0; i < arrayCategories.length;i++){
            if (NewsUpdater.CATEGORIES[i].equals(savedText)){
                spinner.setSelection(i);
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(LOG_TAG,"OnSpinnerItemClick");
                Log.i(LOG_TAG,String.valueOf(i)+ " " + String.valueOf(l));

                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sPref.edit();
                editor.putString(CATEGORY, NewsUpdater.CATEGORIES[i]);
                editor.commit();

                mNewsCursorAdapter.swapCursor(null);
                getActivity().getSupportLoaderManager().restartLoader(CURSOR_LOADER_ID, null, mCursorLoaderCallbacks);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }


}
