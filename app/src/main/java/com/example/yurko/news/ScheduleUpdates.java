package com.example.yurko.news;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class ScheduleUpdates {

    public static void setSchedule(Context context){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isAutoUpdatesEnable = sharedPref.getBoolean
                (context.getResources().getString(R.string.pref_key_auto_update), false);
        Log.i("testing", isAutoUpdatesEnable.toString());
        if (isAutoUpdatesEnable) {
            enableScheduleUpdate(context);
        }else {
            disableScheduleUpdate(context);
        }
    }

    private static void enableScheduleUpdate(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, 0);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 9);

                alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                         1000*60*60*180, pendingIntent);
                Log.i("testing", "alarm enabled");
            }

    }

    private static void disableScheduleUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
        Log.i("testing", "alarm disabled");
    }
}
