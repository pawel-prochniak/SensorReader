package com.example.pprochniak.sensorreader.settings;

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
    private static final String REAL_TIME_PLOTTING = "REAL_TIME_PLOTTING";
    private static final String CONTINUOUS_PLOTTING = "CONTINUOUS_PLOTTING";
    private static final String RMS_SAMPLE_SIZE = "RMS_SAMPLE_SIZE";
    private static final String RMS_FILTERING = "RMS_FILTERING";
    private static final String TIME_SERIES_FILTERING = "TIME_SERIES_FILTERING";

    public SharedPreferencesController(Context context) {
        this.context = context;
    }

    public int getCollectedSampleSize() {
        return getSharedPrefs().getInt(COLLECTED_SAMPLE_SIZE_KEY, COLLECTED_SAMPLE_SIZE_DEFAULT);
    }

    public void saveCollectedSampleSize(int size) {
        editor().putInt(COLLECTED_SAMPLE_SIZE_KEY, size).apply();
    }

    public boolean getRealTimePlotting() {
       return getSharedPrefs().getBoolean(REAL_TIME_PLOTTING, true);
    }

    public void saveRealTimePlotting(boolean isEnabled) {
        editor().putBoolean(REAL_TIME_PLOTTING, isEnabled).apply();
    }

    public boolean getContinuousPlotting() {
        return getSharedPrefs().getBoolean(CONTINUOUS_PLOTTING, false);
    }

    public void saveContinuousPlotting(boolean isEnabled) {
        editor().putBoolean(CONTINUOUS_PLOTTING, isEnabled).apply();
    }

    public int getRmsSampleSize() {
        return getSharedPrefs().getInt(RMS_SAMPLE_SIZE, 8);
    }

    public void saveRmsSampleSize(int sampleSize) {
        editor().putInt(RMS_SAMPLE_SIZE, sampleSize).apply();
    }

    public boolean getRmsFilteringFlag() {
        return getSharedPrefs().getBoolean(RMS_FILTERING, false);
    }

    public void saveRmsFilteringFlag(boolean enable) {
        editor().putBoolean(RMS_FILTERING, enable).apply();
    }

    public boolean getTimeSeriesFilteringFlag() {
        return getSharedPrefs().getBoolean(TIME_SERIES_FILTERING, false);
    }

    public void saveTimeSeriesFilteringFlag(boolean enable) {
        editor().putBoolean(TIME_SERIES_FILTERING, enable).apply();
    }

    private SharedPreferences.Editor editor() {
        return getSharedPrefs().edit();
    }

    private SharedPreferences getSharedPrefs() {
        return context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }


}
