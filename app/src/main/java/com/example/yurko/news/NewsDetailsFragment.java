package com.example.yurko.news;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yurko.news.data.NewsContract;
import com.example.yurko.news.data.NewsProvider;

import java.util.UUID;

public class NewsDetailsFragment extends Fragment  {

    private static final String LOG_TAG = NewsDetailsFragment.class.getSimpleName();
    public static final String ARG_NEWSITEM_ID = "com.example.yurko.news.newsitemid";
    private ImageView mImageView;
    private TextView mTitle;
    private TextView mDesc;
    private TextView mLink;
    private long mNewsItemId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewsItemId = getArguments().getLong(ARG_NEWSITEM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newsdetails_fragment, container,false);
        if (mNewsItemId > 0) {
            Uri contentUri = ContentUris.withAppendedId(NewsContract.NewsEntry.CONTENT_URI, mNewsItemId);
            final Cursor cursor = getActivity().getContentResolver().query(contentUri, null, null, null, null);
            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    mImageView = (ImageView)view.findViewById(R.id.news_image);
                    mTitle = (TextView)view.findViewById(R.id.news_title);
                    mDesc = (TextView)view.findViewById(R.id.news_description);
                    mLink = (TextView)view.findViewById(R.id.news_link);

                    mTitle.setText(cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_TITLE)));
                    mDesc.setText(cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_DESCRIPTION)));
                    mLink.setText(R.string.readNews);

                    mLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_URL))));
                            startActivity(intent);
                        }
                    });
                }
            }
        }

        return view;
    }

    public static NewsDetailsFragment newInstance(long id) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_NEWSITEM_ID, id);
        NewsDetailsFragment fragment = new NewsDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

}
