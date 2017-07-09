package com.example.pprochniak.sensorreader.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Henny on 2017-07-09.
 */

public class SharedPreferencesController {
    private Context context;

    private static final String SHARED_PREFS_KEY = "SENSOR_READER_PREFS";
    private static final int COLLECTED_SAMPLE_SIZE_DEFAULT = 512;
    private static final String COLLECTED_SAMPLE_SIZE_KEY = "COLLECTED_SAMPLE_SIZE";

    public SharedPreferencesController(Context context) {
        this.context = context;
    }

    public int getCollectedSampleSize() {
        return getSharedPrefs().getInt(COLLECTED_SAMPLE_SIZE_KEY, COLLECTED_SAMPLE_SIZE_DEFAULT);
    }

    public void saveCollectedSampleSize(int size) {
        SharedPreferences.Editor editor = getSharedPrefs().edit();
        editor.putInt(COLLECTED_SAMPLE_SIZE_KEY, size).apply();
    }

    private SharedPreferences getSharedPrefs() {
        return context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }
}
