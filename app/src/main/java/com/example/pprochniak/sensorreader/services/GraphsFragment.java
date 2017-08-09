package com.example.pprochniak.sensorreader.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.Utils;

import com.jjoe64.graphview.GraphView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.graphs_fragment)
public class GraphsFragment extends Fragment {
    private static final String TAG = "GraphsFragment";

    public static boolean isInFragment = false;

    @Bean SignalProcessor signalProcessor;
    private boolean attachedToSignalProcessor = false;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.receiving_speed) TextView receivingSpeedView;
    @ViewById(R.id.x_bar_graph) SingleBarGraph xSingleBarGraph;
    @ViewById(R.id.y_bar_graph) SingleBarGraph ySingleBarGraph;
    @ViewById(R.id.z_bar_graph) SingleBarGraph zSingleBarGraph;
    @ViewById(R.id.peak_to_peak_layout) LinearLayout peakToPeakLayout;

    private void initializeSignalProcessor() {
        if (!attachedToSignalProcessor) {
            signalProcessor.attachGraphsFragment(this);
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
        signalProcessor.saveLogs();
        isInFragment = false;

        super.onPause();
    }

}
