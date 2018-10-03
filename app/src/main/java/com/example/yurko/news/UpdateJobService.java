package com.example.yurko.news;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class UpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        this.startService(new Intent(this, UpdateNewsService.class));
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
