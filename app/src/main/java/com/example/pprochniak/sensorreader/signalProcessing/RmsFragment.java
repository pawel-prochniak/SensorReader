package com.example.pprochniak.sensorreader.signalProcessing;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.pprochniak.sensorreader.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Henny on 2017-08-12.
 */
@EFragment(R.layout.rms_fragment)
public class RmsFragment extends Fragment {
    private static final String TAG = "RmsFragment";

    private boolean isInFragment = false;

    @Bean SignalProcessor signalProcessor;
    boolean attachedToSignalProcessor = false;

    @ViewById(R.id.x_bar_graph) SingleBarGraph xSingleBarGraph;
    @ViewById(R.id.y_bar_graph) SingleBarGraph ySingleBarGraph;
    @ViewById(R.id.z_bar_graph) SingleBarGraph zSingleBarGraph;
    @ViewById(R.id.peak_to_peak_layout) LinearLayout peakToPeakLayout;
    @ViewById(R.id.save_log_button) Button saveLogsButton;

    @AfterViews
    void afterViews() {
        saveLogsButton.setOnClickListener((v) -> signalProcessor.saveLogs());
    }

    private void initializeSignalProcessor() {
        if (!attachedToSignalProcessor) {
            signalProcessor.attachRmsFragment(this);
            attachedToSignalProcessor = true;
        }
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

}
