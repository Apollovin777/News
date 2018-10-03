package com.example.yurko.news.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.example.yurko.news.R;
import com.example.yurko.news.UpdateJobService;

public class Util {

    private static int JOB_ID = 7;

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
        ComponentName serviceComponent = new ComponentName(context, UpdateJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setPeriodic(1000*60*60*3);
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    private static void disableScheduleUpdate(Context context){
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE );
        jobScheduler.cancel(JOB_ID);
    }


}
