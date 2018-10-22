package com.example.yurko.news;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.example.yurko.news.data.NewsContract.NewsEntry;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yurko.news.data.NewsContract;
import com.example.yurko.news.data.Util;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NewsListFragment extends Fragment {

    private static final String LOG_TAG = "testing";

    private RecyclerView mRecyclerView;
    private ProgressBar mProcessBar;
    private CursorRecyclerViewAdapter mNewsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsUpdater mNewsUpdater;

    private static final int CURSOR_LOADER_ID = 2;
    private static final String CATEGORY = "category";

    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    NewsEntry._ID,
                    NewsEntry.COLUMN_AUTHOR,
                    NewsEntry.COLUMN_DATE,
                    NewsEntry.COLUMN_DESCRIPTION,
                    NewsEntry.COLUMN_SOURCE,
                    NewsEntry.COLUMN_TITLE,
                    NewsEntry.COLUMN_URL,
                    NewsEntry.COLUMN_URLTOIMAGE,
                    NewsEntry.COLUMN_CATEGORY,
                    NewsEntry.COLUMN_ISREAD};

            CursorLoader loader = null;
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String category = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);

            loader = new CursorLoader(getContext(),
                    Uri.withAppendedPath(NewsEntry.CONTENT_BY_CAT, category),
                    projection, null, null, NewsEntry.COLUMN_DATE + " DESC");
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            //Log.i(LOG_TAG, "onLoadFinished");
            if (data == null || !data.moveToFirst()) {

            } else {
                mNewsAdapter.changeCursor(data);
                mNewsAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mNewsAdapter.swapCursor(null);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.newslist_fragment, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNewsAdapter = new CursorRecyclerViewAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mNewsAdapter);

        mProcessBar = (ProgressBar) view.findViewById(R.id.loading_spinner);
        mProcessBar.setVisibility(View.GONE);
        Log.i(LOG_TAG, "NewsListFragment onCreateView");

        mNewsUpdater = new NewsUpdater(getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                updateNewsList();

            }
        });

        getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mCursorLoaderCallbacks);

        return view;
    }

    private class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitle;
        private TextView mSource;
        private TextView mDate;
        //private TextView mCategory;
        private ImageView mItemImageView;

        public NewsHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSource = (TextView) itemView.findViewById(R.id.source);
            mDate = (TextView) itemView.findViewById(R.id.date);
            // mCategory = (TextView) itemView.findViewById(R.id.category);
            mItemImageView = (ImageView) itemView.findViewById(R.id.image_view);

            itemView.setOnClickListener(this);
        }

        public void bind(Cursor cursor) {
            mTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_TITLE)));
            int isRead = cursor.getInt(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_ISREAD));
            if (isRead == 0) {
                mTitle.setTextColor(getActivity().getResources().getColor(R.color.notRead));
            } else {
                mTitle.setTextColor(getActivity().getResources().getColor(R.color.alreadyRead));
            }
            mSource.setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_SOURCE)));

            String strDate = cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_DATE));

            // mCategory.setText(cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.NewsEntry.COLUMN_CATEGORY)));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            DateFormat dateFormatNew = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
            Date convertedDate = new Date();
            try {
                convertedDate = dateFormat.parse(strDate);

            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parsing datetime failed", e);
            }
            mDate.setText(dateFormatNew.format(convertedDate));
        }

        @Override
        public void onClick(View view) {
            int lastClickedIndex = this.getAdapterPosition();
            long itemID = mNewsAdapter.getItemId(lastClickedIndex);
            ContentValues values = new ContentValues();
            values.put(NewsContract.NewsEntry.COLUMN_ISREAD, 1);
            getActivity().getContentResolver().update(
                    Uri.withAppendedPath(NewsContract.NewsEntry.CONTENT_URI, String.valueOf(itemID))
                    , values, null, null);
            Intent intent = NewsPagerActivity.newIntent(getActivity(), itemID);
            startActivity(intent);
        }
    }

    private class CursorRecyclerViewAdapter extends RecyclerView.Adapter<NewsHolder> {

        private Context mContext;

        private Cursor mCursor;

        private boolean mDataValid;

        private int mRowIdColumn;

        private int mRowImageColumn;

        private DataSetObserver mDataSetObserver;

        public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
            mContext = context;
            mCursor = cursor;
            mDataValid = cursor != null;
            mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;

            mDataSetObserver = new NotifyingDataSetObserver();
            if (mCursor != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
        }

        public Cursor getCursor() {
            return mCursor;
        }

        public boolean isDataValid() {
            return mCursor != null;
        }

        @Override
        public int getItemCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIdColumn);
            }
            return 0;
        }

        public String getImageURL(int position) {
            if (isDataValid() && mCursor != null && mCursor.moveToPosition(position)) {
                mRowImageColumn = mCursor.getColumnIndex(NewsEntry.COLUMN_URLTOIMAGE);
                String temp = mCursor.getString(mRowImageColumn);
                return temp;
            }
            return null;
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(true);
        }


        @Override
        public NewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new NewsHolder(inflater, parent);
        }

        public void onBindViewHolder(NewsHolder viewHolder, Cursor cursor) {
            viewHolder.bind(cursor);
        }


        @Override
        public void onBindViewHolder(NewsHolder viewHolder, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            onBindViewHolder(viewHolder, mCursor);
            if (mCursor != null) {


                Picasso.get().
                        load(this.getImageURL(position))
                        .error(R.drawable.no_image_available)
                        .into(viewHolder.mItemImageView)
                ;
            }
        }

        /**
         * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
         * closed.
         */
        public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        }

        /**
         * Swap in a new Cursor, returning the old Cursor.  Unlike
         * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
         * closed.
         */
        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            final Cursor oldCursor = mCursor;
            if (oldCursor != null && mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
            mCursor = newCursor;
            if (mCursor != null) {
                if (mDataSetObserver != null) {
                    mCursor.registerDataSetObserver(mDataSetObserver);
                }
                mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
                mDataValid = true;
                notifyDataSetChanged();
            } else {
                mRowIdColumn = -1;
                mDataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
            return oldCursor;
        }

        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                mDataValid = true;
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                mDataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
        }
    }

    private void updateNewsList() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mProcessBar.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_LONG).show();
            return;
        }
//        if (mNewsUpdater != null) {
//            mNewsUpdater = null;
//            mNewsUpdater = new NewsUpdater(getContext());
//        }
//        mNewsUpdater.execute();
        Intent intent = new Intent(getActivity(),UPDNews.class);
        getActivity().startService(intent);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);

        Util.setSchedule(getContext(),false);

        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
        MenuItem item = menu.findItem(R.id.category_spinner);
        final Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cat_spinner_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);

        spinner.setAdapter(adapter);

        String[] arrayCategories = getActivity().getResources().getStringArray(R.array.cat_spinner_array);

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String savedText = sPref.getString(CATEGORY, NewsUpdater.TOP_NEWS);

        for (int i = 0; i < arrayCategories.length; i++) {
            if (NewsUpdater.CATEGORIES[i].equals(savedText)) {
                spinner.setSelection(i);
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(LOG_TAG, "OnSpinnerItemClick");
                Log.i(LOG_TAG, String.valueOf(i) + " " + String.valueOf(l));

                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sPref.edit();
                editor.putString(CATEGORY, NewsUpdater.CATEGORIES[i]);
                editor.commit();

                mNewsAdapter.swapCursor(null);
                getActivity().getSupportLoaderManager().restartLoader(CURSOR_LOADER_ID, null, mCursorLoaderCallbacks);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings:
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.refresh_news:
                updateNewsList();
                break;
            case R.id.delete_all:
                final ContentResolver resolver = getActivity().getContentResolver();

                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmation")
                        .setMessage("Do you really want to delete all news?")
                        .setIcon(android.R.drawable.alert_light_frame)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int count = resolver.delete(NewsContract.NewsEntry.CONTENT_URI, null, null);
                                Toast.makeText(getActivity(), count + " news deleted", Toast.LENGTH_SHORT).show();
                                mNewsAdapter.swapCursor(null);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.ViewLog:
                Intent log = new Intent(getActivity(), com.example.yurko.news.Log.class);
                startActivity(log);
                break;
        }
        return true;
    }


}
