package com.example.yurko.news;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.example.yurko.news.data.Util;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            setPreferenceSummary(sharedPreferences,preference.getKey());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        setPreferenceSummary(sharedPreferences, key);
        if(key.equals(getResources().getString(R.string.pref_key_auto_update))){
            Util.setSchedule(getContext(),false);
        }
        if(key.equals("update_interval")){
            Util.setSchedule(getContext(),true);
        }
    }

    private void setPreferenceSummary(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(preference instanceof SwitchPreferenceCompat){
            Boolean value = sharedPreferences.getBoolean(key,false);
            if(value == true){
                preference.setSummary(R.string.auto_update_enabled);
            }else{
                preference.setSummary(R.string.auto_update_disabled);
            }
        }
        if(preference instanceof ListPreference){
            String interval = sharedPreferences.getString("update_interval", "12 hours");
            if(interval.equals("3600000")){
                interval = "1 hour";
            }
            else if(interval.equals("10800000")){
                interval = "3 hours";
            }
            else if(interval.equals("21600000")){
                interval = "6 hours";
            }
            else if(interval.equals("43200000")){
                interval = "12 hours";
            }
            preference.setSummary(interval);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
