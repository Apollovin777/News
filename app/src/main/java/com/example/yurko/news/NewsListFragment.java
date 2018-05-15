package com.example.yurko.news;

import android.content.ContentValues;
import android.content.Context;
import com.example.yurko.news.data.NewsContract.NewsEntry;

import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yurko.news.data.NewsContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "testing";

    private static final String IMAGE_URL = "image_url";
    private static final String TITLE = "title";
    private static final String DESC = "desc";
    private static final String NEWSItem_URL = "newsItem_url";
    private ListView mListView;
    private ProgressBar mProcessBar;
    private NewsCursorAdapter mNewsCursorAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyList;
    private static final String NEWSAPI_REQUEST_URL = "http://newsapi.org/v2/top-headlines";

    private static final int JSON_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID = 2;

    private LoaderManager.LoaderCallbacks<List<NewsItem>> mJsonDataLoaderListener
            = new LoaderManager.LoaderCallbacks<List<NewsItem>>() {
        @Override
        public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "NewsListFragment onCreateLoader");
            Uri baseUri = Uri.parse(NEWSAPI_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("country", "ua");
            uriBuilder.appendQueryParameter("pageSize", "30");

            return new NewsLoader(getContext(), uriBuilder.toString());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<NewsItem>> loader, List<NewsItem> data) {
            Log.i(LOG_TAG, "NewsListFragment onLoadFinished");
            if (data != null && !data.isEmpty()) {
                addNewItems(data);
            }
            mProcessBar.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
            Log.i(LOG_TAG, "NewsListFragment onLoaderReset");
        }
    };

    public void addNewItems(List<NewsItem> list) {
        for (NewsItem newsItem : list) {
            insertNewsItemIntoDB(newsItem);
        }
    }

    private void insertNewsItemIntoDB(NewsItem item) {
        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.getDefault());
        ContentValues values = new ContentValues();
        values.put(NewsEntry.COLUMN_AUTHOR, item.getAuthor());
        values.put(NewsEntry.COLUMN_DATE, dateFormat.format(item.getDate()));
        values.put(NewsEntry.COLUMN_DESCRIPTION, item.getDescription());
        values.put(NewsEntry.COLUMN_SOURCE, item.getSource());
        values.put(NewsEntry.COLUMN_TITLE, item.getTitle());
        values.put(NewsEntry.COLUMN_URL, item.getUrl());
        values.put(NewsEntry.COLUMN_URLTOIMAGE, item.getUrlToImage());

        getActivity().getContentResolver().insert(NewsEntry.CONTENT_URI, values);
    }

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
                    NewsEntry.COLUMN_URLTOIMAGE};

            CursorLoader loader = new CursorLoader(getContext(), NewsEntry.CONTENT_URI,
                    projection, null, null, NewsEntry.COLUMN_DATE + " DESC");
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.i(LOG_TAG, "onLoadFinished");
            if (data == null || !data.moveToFirst()){
                mEmptyList.setText(R.string.no_news);
            }
            else {
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

        mEmptyList = (TextView)view.findViewById(R.id.empty);
        mListView.setEmptyView(mEmptyList);

        mProcessBar = (ProgressBar) view.findViewById(R.id.loading_spinner);
        mProcessBar.setVisibility(View.GONE);
        Log.i(LOG_TAG, "NewsListFragment onCreateView");

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                getActivity().getSupportLoaderManager().initLoader(JSON_LOADER_ID, null, mJsonDataLoaderListener);
                Loader loader = getActivity().getSupportLoaderManager().getLoader(JSON_LOADER_ID);
                loader.startLoading();
                //updateNewsList(true);
            }
        });

        getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mCursorLoaderCallbacks);
        getActivity().getSupportLoaderManager().initLoader(JSON_LOADER_ID, null, mJsonDataLoaderListener);

        return view;
    }

    private void updateNewsList(boolean updateOnRequest) {
        mEmptyList.setText("");

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader loader = loaderManager.getLoader(JSON_LOADER_ID);

        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mProcessBar.setVisibility(View.GONE);
            mEmptyList.setText(R.string.no_connection);
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(),R.string.no_connection,Toast.LENGTH_LONG).show();
            return;
        }

        if (updateOnRequest) {
            initAndStartLoader(loaderManager,loader);
            return;
        }
    }

    private void initAndStartLoader(LoaderManager loaderManager, Loader loader) {
        if (loader == null) {
            loaderManager.initLoader(JSON_LOADER_ID, null, mJsonDataLoaderListener);
            loader = loaderManager.getLoader(JSON_LOADER_ID);
        }
        loader.startLoading();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "NewsListFragment onStart");
        //updateNewsList(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "NewsListFragment onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "NewsListFragment onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "NewsListFragment onResume");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(LOG_TAG, "NewsListFragment onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "NewsListFragment onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "NewsListFragment onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOG_TAG, "NewsListFragment onDetach");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = NewsPagerActivity.newIntent(getActivity(), id);
        startActivity(intent);
    }

    //
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        NewsItem item = (NewsItem)mNewsCursorAdapter.getItem(position);
//        Intent intent = NewsPagerActivity.newIntent(getActivity(), item.getId());
//        startActivity(intent);
//    }

}
