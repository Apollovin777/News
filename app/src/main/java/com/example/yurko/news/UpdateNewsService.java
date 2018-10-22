package com.example.yurko.news;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.Date;


public class UpdateNewsService extends IntentService {
    public UpdateNewsService() {
        super("UpdateNewsService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        synchronized (this){
            Log.i("testing","UpdateNewsService");

            String filename = "myfile";
            Date date = new Date();
            String fileContents = "Last job run: " + date.toString();
            FileOutputStream outputStream;
            try {
                outputStream = openFileOutput(filename, getBaseContext().MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateNews();
            Log.i("testing","UpdateNewsService_End");
        }
    }

    private void updateNews(){
        NewsUpdater updater = new NewsUpdater(this);
        int newItentsCount = updater.updateNews();
        if(newItentsCount > 0) {
            Intent resultIntent = new Intent(this, NewsListActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

// Create Notification
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean isNotificationEnabled = preferences.getBoolean(getResources().
                    getString(R.string.pref_key_notification_show), true);

            if (isNotificationEnabled) {
                NotificationCompat.Builder builder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("News")
                                .setContentText("News list is updated.")
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true);

                Notification notification = builder.build();

// Show Notification
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
            }
        }

        stopSelf();

    }
}
