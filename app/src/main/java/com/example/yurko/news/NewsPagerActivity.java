package com.example.yurko.news;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import java.util.UUID;

public class NewsPagerActivity extends AppCompatActivity {

    private static final String EXTRA_NEWSITEM_ID =
            "com.example.yurko.news.newsitem_id";

    private ViewPager mViewPager;
    private List<NewsItem> mNewsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_pager);

        mViewPager = findViewById(R.id.news_view_pager);
        mNewsItems = NewsLab.get(this).getNews();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                NewsItem newsItem = mNewsItems.get(position);
                return NewsDetailsFragment.newInstance(newsItem.getId());
            }

            @Override
            public int getCount() {
                return mNewsItems.size();
            }
        });

        UUID newsItemId = (UUID) getIntent().getSerializableExtra(EXTRA_NEWSITEM_ID);

        for (int i = 0; i < mNewsItems.size(); i++) {
            if (mNewsItems.get(i).getId().equals(newsItemId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, NewsPagerActivity.class);
        intent.putExtra(EXTRA_NEWSITEM_ID, crimeId);
        return intent;
    }
}
