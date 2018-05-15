package com.example.yurko.news.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NewsContract {

    public static final String CONTENT_AUTHORITY = "com.example.yurko.news";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NEWS = "news";


    private NewsContract() {
    }

    public static final class NewsEntry implements BaseColumns {
        public final static String TABLE_NAME = "newsitem";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NEWS);

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_AUTHOR = "author";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_DESCRIPTION = "description";
        public final static String COLUMN_URL = "url";
        public final static String COLUMN_URLTOIMAGE = "urltoimage";
        public final static String COLUMN_SOURCE = "source";
        public final static String COLUMN_DATE = "date";
    }
}
//    private String mAuthor;
//    private UUID mId;
//    private String mTitle;
//    private String mDescription;
//    private String mUrl;
//    private String mUrlToImage;
//    private String mSource;
//    private Date mDate;