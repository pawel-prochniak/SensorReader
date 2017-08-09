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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.graphs_fragment)
public class GraphsFragment extends Fragment {
    private static final String TAG = "GraphsFragment";

    public static boolean isInFragment = false;

    // Plot settings
    private SignalProcessor signalProcessor;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.receiving_speed) TextView receivingSpeedView;
    @ViewById(R.id.x_bar_graph) SingleBarGraph xSingleBarGraph;
    @ViewById(R.id.y_bar_graph) SingleBarGraph ySingleBarGraph;
    @ViewById(R.id.z_bar_graph) SingleBarGraph zSingleBarGraph;
    @ViewById(R.id.peak_to_peak_layout) LinearLayout peakToPeakLayout;

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                signalProcessor.receiveValueAndAppendPoint(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                processServiceDiscovery(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL.equals(action)) {
                showNoServiceDiscoverAlert();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                displayDisconnectToast(extras);
            }
        }
    };

    private void initializeSignalProcessor() {
        if (signalProcessor == null) signalProcessor = new SignalProcessor(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Registering mServiceDiscovery");
        initializeSignalProcessor();
        subscribeToGattUpdates();
        signalProcessor.connectToAllServices();
        isInFragment = true;
    }

    @Override
    public void onPause() {
        signalProcessor.saveLogs();
        isInFragment = false;
        getActivity().unregisterReceiver(mGattUpdateListener);
        super.onPause();
    }

    private void showNoServiceDiscoverAlert() {
        servicesNotFound.setVisibility(View.VISIBLE);
    }

    private void displayDisconnectToast(Bundle extras) {
        Resources res = getResources();
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        String formattedDisconnectMsg = res.getString(R.string.device_disconnected, deviceAddress);
        Toast.makeText(getContext(), formattedDisconnectMsg, Toast.LENGTH_LONG).show();
    }

    private void processServiceDiscovery(Bundle extras) {
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        Log.d(TAG, "Service discovered from device "+deviceAddress);
        BluetoothLeService.subscribeToSensorNotifications(deviceAddress);
        signalProcessor.addDevice(deviceAddress);

        /*
        / Changes the MTU size to 512 in case LOLLIPOP and above devices
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeService.exchangeGattMtu(deviceAddress, 512);
        }
    }

    private void subscribeToGattUpdates() {
        getActivity().registerReceiver(mGattUpdateListener, Utils.makeGattUpdateIntentFilter());
    }

}
