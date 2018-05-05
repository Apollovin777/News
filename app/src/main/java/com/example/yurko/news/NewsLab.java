package com.example.yurko.news;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewsLab {

    private static NewsLab sNewsLab;

    private List<NewsItem> mNews;

    public static NewsLab get(Context context) {
        if (sNewsLab == null) {
            sNewsLab = new NewsLab(context);
        }
        return sNewsLab;
    }

    private NewsLab(Context context) {
        mNews = new ArrayList<>();
    }

    public List<NewsItem> getNews() {
        return mNews;
    }

    public void setNews(List<NewsItem> news) {
        mNews = news;
    }

    public NewsItem getNewsItem(UUID id) {
        for (NewsItem newsItem : mNews) {
            if (newsItem.getId().equals(id)) {
                return newsItem;
            }
        }
        return null;
    }
}
