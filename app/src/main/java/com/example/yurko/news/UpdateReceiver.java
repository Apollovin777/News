package com.example.yurko.news;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {

    private String LOG_TAG = UpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(LOG_TAG,"onReceive");
        context.startService(new Intent(context, UpdateNewsService.class));
    }
}
