package com.example.yurko.news;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<NewsItem> {

    public NewsAdapter(@NonNull Context context, ArrayList<NewsItem> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }
        NewsItem newsItem = getItem(position);

        TextView title = (TextView) listItemView.findViewById(R.id.title);
        title.setText(newsItem.getTitle());

        TextView source = (TextView) listItemView.findViewById(R.id.source);
        source.setText(newsItem.getSource());

        return listItemView;
    }

    public NewsItem getNewsItem(int position){
        return getItem(position);
    }

}
