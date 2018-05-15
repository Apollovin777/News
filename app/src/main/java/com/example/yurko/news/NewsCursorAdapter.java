package com.example.yurko.news;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yurko.news.data.NewsContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = NewsCursorAdapter.class.getSimpleName();

    public NewsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View listItemView = view;
        TextView title = (TextView) listItemView.findViewById(R.id.title);
        TextView source = (TextView)listItemView.findViewById(R.id.source);
        TextView date = (TextView)listItemView.findViewById(R.id.date);

        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_TITLE)));
        source.setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_SOURCE)));

        String strDate = cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_DATE));

            DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.getDefault());
            DateFormat dateFormatNew = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
            Date convertedDate = new Date();
            try {
                convertedDate = dateFormat.parse(strDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parsing datetime failed", e);
            }
            date.setText(dateFormatNew.format(convertedDate));

    }
}