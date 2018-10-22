package com.example.yurko.news;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class UpdateJobService extends JobService {



    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("testing","onStartJob");
        this.startService(new Intent(this, UPDNews.class));
        //jobFinished(params, false);
        //NewsUpdater updater = new NewsUpdater(this);
        //updater.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i("testing","onStopJob");
        return true;
    }
}
