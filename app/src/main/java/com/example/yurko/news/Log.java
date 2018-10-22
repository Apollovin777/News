package com.example.yurko.news;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.yurko.news.data.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Log extends AppCompatActivity {

    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mTextView = findViewById(R.id.textView_log);

        JobScheduler jobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE );
        List<JobInfo> list = jobScheduler.getAllPendingJobs();
        StringBuilder builder = new StringBuilder();
        if(list.size()>0){
            for (JobInfo j:list
                 ) {
                builder.append("Job ID:" + j.getId());
                builder.append("\n");
                builder.append("Is periodic:" + j.isPeriodic());
                builder.append("\n");
                builder.append("Is persisted:" + j.isPersisted());
                builder.append("\n");
                builder.append("Service:" + j.getService());
                builder.append("\n");
                builder.append("Interval,sec:" + j.getIntervalMillis()/1000);
                builder.append("\n");

            }
            mTextView.setText(builder.toString());
        }
        else {
            mTextView.setText("There is no scheduled tasks.");
        }

        String filename = "myfile";

        try {
            InputStream inputStream = this.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                mTextView.append(stringBuilder.toString());
            }
        }
        catch (FileNotFoundException e) {
            mTextView.append("Job last run: " + "File not found");
        } catch (IOException e) {
            mTextView.append("Job last run: " + "Can not read file");
        }



    }
}
