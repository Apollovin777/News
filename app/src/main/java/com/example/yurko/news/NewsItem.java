package com.example.yurko.news;

import java.util.Date;
import java.util.UUID;

public class NewsItem {
    public NewsItem(String title, String source, String url, String urlToImage, String description) {
        mTitle = title;
        mSource = source;
        mUrl = url;
        mDescription = description;
        mUrlToImage = urlToImage;
        mId = UUID.randomUUID();
    }

    private String mAuthor;
    private UUID mId;
    private String mTitle;
    private String mDescription;
    private String mUrl;
    private String mUrlToImage;
    private Date mPublishedAt;
    private String mSource;

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

    public Date getPublishedAt() {
        return mPublishedAt;
    }

    public UUID getId() {
        return mId;
    }
}
