package com.example.yurko.news;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<NewsItem>> {

    private static final String LOG_TAG = "testing";

    private static final String IMAGE_URL = "image_url";
    private static final String TITLE = "title";
    private static final String DESC = "desc";
    private static final String NEWSItem_URL = "newsItem_url";
    private ListView mListView;
    private ProgressBar mProcessBar;
    private NewsAdapter mAdapter;
    private ConstraintLayout mLayout;
    private Animation slideUpAnimation, slideDownAnimation;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyList;
    private static final String NEWSAPI_REQUEST_URL = "http://newsapi.org/v2/top-headlines";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.newslist_fragment, container, false);
        ArrayList<NewsItem> list = new ArrayList<>();

        mLayout = view.findViewById(R.id.constraint_layout);

        mListView = view.findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);

        mAdapter = new NewsAdapter(getContext(), list);
        mListView.setAdapter(mAdapter);

        mEmptyList = view.findViewById(R.id.empty);
        mListView.setEmptyView(mEmptyList);

        mProcessBar = view.findViewById(R.id.loading_spinner);
        mProcessBar.setVisibility(View.GONE);
        Log.i(LOG_TAG, "NewsListFragment onCreateView");

        slideUpAnimation = AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_up_animation);

        slideDownAnimation = AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_down_animation);

        mSwipeRefreshLayout = view.findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                updateNewsList(true);
            }
        });
        return view;
    }

    private void updateNewsList(boolean updateOnRequest) {
        mEmptyList.setText("");

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader loader = loaderManager.getLoader(0);
        NewsLab newsLab = NewsLab.get(getContext());
        if(!newsLab.getNews().isEmpty()){
            mAdapter.addAll(newsLab.getNews());
        }

        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mProcessBar.setVisibility(View.GONE);
            mEmptyList.setText(R.string.no_connection);
            mSwipeRefreshLayout.setRefreshing(false);
            if(!newsLab.getNews().isEmpty()){
                Toast.makeText(getActivity(),R.string.no_connection,Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (updateOnRequest) {
            initAndStartLoader(loaderManager,loader);
            return;
        }

        if (!newsLab.getNews().isEmpty()) {
            loaderManager.destroyLoader(0);
        } else {
            mProcessBar.setVisibility(View.VISIBLE);
            initAndStartLoader(loaderManager,loader);
        }
    }

    private void initAndStartLoader(LoaderManager loaderManager,Loader loader) {
        if (loader == null) {
            loaderManager.initLoader(0, null, this);
            loader = loaderManager.getLoader(0);
        }
        loader.startLoading();
    }

//    public void startSlideUpAnimation(View view) {
//        mLayout.startAnimation(slideUpAnimation);
//    }
//
//    public void startSlideDownAnimation(View view) {
//        mLayout.startAnimation(slideDownAnimation);
//    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsItem item = mAdapter.getNewsItem(position);
        Intent intent = NewsPagerActivity.newIntent(getActivity(), item.getId());
        startActivity(intent);
    }

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
            NewsLab newsLab = NewsLab.get(getContext());
            newsLab.setNews(data);
            mAdapter.addAll(newsLab.getNews());

        }
        else{
            mEmptyList.setText(R.string.no_news);
        }

        mProcessBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
        Log.i(LOG_TAG, "NewsListFragment onLoaderReset");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "NewsListFragment onStart");
        updateNewsList(false);
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
}
