package com.example.pprochniak.sensorreader.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;

import com.jjoe64.graphview.GraphView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.example.pprochniak.sensorreader.services.TimeSeriesPlotController.X;
import static com.example.pprochniak.sensorreader.services.TimeSeriesPlotController.Y;
import static com.example.pprochniak.sensorreader.services.TimeSeriesPlotController.Z;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.graphs_fragment)
public class GraphsFragment extends Fragment {
    private static final String TAG = "GraphsFragment";

    public static boolean isInFragment = false;

    private static final long DELAY_PERIOD = 500;
    static HashMap<String, List<BluetoothGattService>> mGattServiceData = new HashMap<>();

    // Plot settings
    private TimeSeriesPlotController timeSeriesPlotController;
    private RmsPlotController rmsPlotController;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.x_speed) TextView xSpeedView;
    @ViewById(R.id.y_speed) TextView ySpeedView;
    @ViewById(R.id.z_speed) TextView zSpeedView;
    @ViewById(R.id.x_bar_graph) BarGraph xBarGraph;

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                timeSeriesPlotController.receiveValueAndAppendPoint(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                processServiceDiscovery(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL.equals(action)) {
                showNoServiceDiscoverAlert();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                displayDisconnectToast(extras);
            }
        }
    };

    @AfterViews public void afterViews() {
        initializePlotController();
    }

    private void initializePlotController() {
        timeSeriesPlotController = new TimeSeriesPlotController(this);
        timeSeriesPlotController.setGraphProperties();
        xBarGraph.setTitle("RMS X");
    }

    void setXRms(float val) {
        xBarGraph.setValue(val);
    }

    void setReceivingSpeed(float speed, @TimeSeriesPlotController.AXIS String axis) {
        String str = String.valueOf(speed);
        TextView textView;
        switch (axis) {
            default:
            case X:
                textView = xSpeedView;
                break;
            case Y:
                textView = ySpeedView;
                break;
            case Z:
                textView = zSpeedView;
        }
        textView.setText(str);
    }


    @Override
    public void onResume() {
        super.onResume();
        Logger.d("Registering mServiceDiscovery");
        subscribeToGattUpdates();
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(() -> {
            Logger.d("Discover service called");
            BluetoothLeService.discoverAllServices();
        }, DELAY_PERIOD);
        isInFragment = true;
    }

    @Override
    public void onPause() {
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
        List<BluetoothGattService> services = BluetoothLeService.getSupportedGattServices(deviceAddress);
        setNotificationsEnabled(deviceAddress, services);
        if (!mGattServiceData.containsKey(deviceAddress)) mGattServiceData.put(deviceAddress, services);
        timeSeriesPlotController.addDevice(deviceAddress);

        /*
        / Changes the MTU size to 512 in case LOLLIPOP and above devices
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeService.exchangeGattMtu(deviceAddress, 512);
        }
    }


    /**
     * Prepare GATTServices data.
     *
     * @param gattServices
     */
    private void setNotificationsEnabled(String deviceAddress, List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            UUID uuid = gattService.getUuid();
            if (UUIDDatabase.UUID_SENSOR_READ_SERVICE.equals(uuid)) {
                // Auto set notify as TRUE
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    if (Utils.checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                        BluetoothLeService.setCharacteristicNotification(deviceAddress,gattCharacteristic, true);
                    } else {
                        Log.d(TAG, "setNotificationsEnabled: no notify characteristic available");
                    }
                }
            }
        }
    }

    private void subscribeToGattUpdates() {
        getActivity().registerReceiver(mGattUpdateListener, Utils.makeGattUpdateIntentFilter());
    }



}
