package com.example.yurko.news;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.UUID;

public class NewsDetailsFragment extends Fragment  {

    private static final String LOG_TAG = NewsDetailsFragment.class.getSimpleName();
    private static final String ARG_NEWSITEM_ID = "com.example.yurko.news.newsitemid";
    private ImageView mImageView;
    private TextView mTitle;
    private TextView mDesc;
    private TextView mLink;
    private NewsItem mNewsItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID newsItemId = (UUID) getArguments().getSerializable(ARG_NEWSITEM_ID);
        mNewsItem = NewsLab.get(getActivity()).getNewsItem(newsItemId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newsdetails_fragment, container,false);

        mImageView = view.findViewById(R.id.news_image);
        mTitle = view.findViewById(R.id.news_title);
        mDesc = view.findViewById(R.id.news_description);
        mLink = view.findViewById(R.id.news_link);

        mTitle.setText(mNewsItem.getTitle());
        mDesc.setText(mNewsItem.getDescription());
        mLink.setText(R.string.readNews);

        return view;
    }

    public static NewsDetailsFragment newInstance(UUID id) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_NEWSITEM_ID, id);
        NewsDetailsFragment fragment = new NewsDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

}
