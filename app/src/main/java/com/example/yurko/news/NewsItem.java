package com.example.yurko.news;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

public class NewsItem implements Comparable<NewsItem>{
    public NewsItem(String title, String source, String url, String urlToImage, String description,Date date) {
        mTitle = title;
        mSource = source;
        mUrl = url;
        mDescription = description;
        mUrlToImage = urlToImage;
        mId = UUID.randomUUID();
        mDate = date;
    }

    private String mAuthor;
    private UUID mId;
    private String mTitle;
    private String mDescription;
    private String mUrl;
    private String mUrlToImage;
    private String mSource;
    private Date mDate;

    public Date getDate() {
        return mDate;
    }

    public String getSource() {
        return mSource;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUrlToImage() {
        return mUrlToImage;
    }



    public UUID getId() {
        return mId;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NewsItem) {
            NewsItem newsItem = (NewsItem) obj;
            boolean res = this.mUrl.equals(newsItem.mUrl);
            return res;
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull NewsItem obj) {
        if (obj != null){
            Date publishDate =obj.getDate();
        /* For Ascending order*/
            return publishDate.compareTo(this.mDate);
        }
        return 0;
    }
}
