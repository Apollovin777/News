package com.example.yurko.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.UUID;

public class NewsListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new NewsListFragment();
    }
}
