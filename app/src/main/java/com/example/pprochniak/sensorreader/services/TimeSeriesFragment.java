package com.example.pprochniak.sensorreader.services;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.R;

import com.jjoe64.graphview.GraphView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.time_series_fragment)
public class TimeSeriesFragment extends Fragment {
    private static final String TAG = "TimeSeriesFragment";

    public static boolean isInFragment = false;

    @Bean SignalProcessor signalProcessor;
    private boolean attachedToSignalProcessor = false;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.receiving_speed) TextView receivingSpeedView;
    @ViewById(R.id.save_log_button) Button saveLogsButton;
    @ViewById(R.id.clear_graph_button) Button clearGraphButton;


    @AfterViews
    void afterViews() {
        saveLogsButton.setOnClickListener((v) -> signalProcessor.saveLogs());
        clearGraphButton.setOnClickListener((v) -> signalProcessor.clearGraph());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Registering mServiceDiscovery");
        initializeSignalProcessor();
        isInFragment = true;
    }

    @Override
    public void onPause() {
        signalProcessor.clearControllers();
        attachedToSignalProcessor = false;
        isInFragment = false;

        super.onPause();
    }

    private void initializeSignalProcessor() {
        if (!attachedToSignalProcessor) {
            signalProcessor.attachGraphsFragment(this);
            attachedToSignalProcessor = true;
        }
    }

}
