package com.example.yurko.news.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.example.yurko.news.R;
import com.example.yurko.news.UPDNews;
import com.example.yurko.news.UpdateJobService;

import java.util.List;

public class Util {

    public static int JOB_ID = 7;

    public static void setSchedule(Context context,boolean reschedule){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isAutoUpdatesEnable = sharedPref.getBoolean
                (context.getResources().getString(R.string.pref_key_auto_update), false);
        Log.i("testing", isAutoUpdatesEnable.toString());
        if (isAutoUpdatesEnable) {
            enableScheduleUpdate(context,reschedule);
        }else {
            disableScheduleUpdate(context);
        }
    }

    private static void enableScheduleUpdate(Context context,boolean reschedule) {
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE );
        List<JobInfo> list = jobScheduler.getAllPendingJobs();
        if(list.size()==0 || reschedule) {
            ComponentName serviceComponent = new ComponentName(context, UpdateJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(context);
            String interval = sharedPref.getString("update_interval", "43200000");
            builder.setPeriodic(Integer.parseInt(interval));
            int test = jobScheduler.schedule(builder.build());
            Log.i("testing", "enableScheduleUpdate" + String.valueOf(test));
        }
    }


    private static void disableScheduleUpdate(Context context){
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE );
        jobScheduler.cancel(JOB_ID);
    }


}
