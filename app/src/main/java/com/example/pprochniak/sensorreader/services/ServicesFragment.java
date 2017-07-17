package com.example.pprochniak.sensorreader.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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
import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.services_fragment)
public class ServicesFragment extends Fragment {
    private static final String TAG = "ServicesFragment";

    public static boolean isInFragment = false;

    private static final String X_MAP = "X";
    private static final String Y_MAP = "Y";
    private static final String Z_MAP = "Z";

    private static final long DELAY_PERIOD = 500;
    private int SERIES_LENGTH; // how many data points will be collected for each axis
    static HashMap<String, List<BluetoothGattService>> mGattServiceData = new HashMap<>();
    HashMap<String, HashMap<String, LineGraphSeries<DataPoint>>> mapOfSeries = new HashMap<>();
    private SharedPreferencesController sharedPreferencesController;
    long xTimestamp, yTimestamp, zTimestamp;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.x_speed) TextView xSpeedView;
    @ViewById(R.id.y_speed) TextView ySpeedView;
    @ViewById(R.id.z_speed) TextView zSpeedView;

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                receiveValueAndAppendPoint(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                processServiceDiscovery(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL
                    .equals(action)) {
                showNoServiceDiscoverAlert();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                displayDisconnectToast(extras);
            }
        }
    };

    @AfterViews public void afterViews() {
        sharedPreferencesController = new SharedPreferencesController(getContext());
        SERIES_LENGTH = sharedPreferencesController.getCollectedSampleSize();
        setGraphProperties();
    }


    private void setGraphProperties() {
        graphView.getGridLabelRenderer().setLabelVerticalWidth(120);
        graphView.getLegendRenderer().setWidth(80);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(SERIES_LENGTH);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void addSeriesForDevice(HashMap<String, LineGraphSeries<DataPoint>> map) {
        LineGraphSeries<DataPoint> xSeries = map.get(X_MAP);
        LineGraphSeries<DataPoint> ySeries = map.get(Y_MAP);
        LineGraphSeries<DataPoint> zSeries = map.get(Z_MAP);
        xSeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        ySeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        zSeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        xSeries.setTitle("X");
        ySeries.setTitle("Y");
        zSeries.setTitle("Z");
        ySeries.setColor(Color.rgb(255,0,0));
        zSeries.setColor(Color.rgb(0,255,0));
        graphView.addSeries(xSeries);
        graphView.addSeries(ySeries);
        graphView.addSeries(zSeries);

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

    private void receiveValueAndAppendPoint(Bundle extras) {
        float receivedValue;
        double counter;
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        LineGraphSeries<DataPoint> series;
        HashMap<String, LineGraphSeries<DataPoint>> deviceSeriesMap = mapOfSeries.get(deviceAddress);

        if (deviceSeriesMap == null) {
            return;
        }

        if (extras.containsKey(Constants.EXTRA_ACC_X_VALUE)) {
            series = deviceSeriesMap.get("X");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_X_VALUE);
//            if (counter < 10) {
//                Log.d(TAG, "Appending x: ("+counter+", "+receivedValue+")");
//            }
            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) xTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: x series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - xTimestamp);
                    xSpeedView.setText(String.valueOf(speed));
                }
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Y_VALUE)) {
            series = deviceSeriesMap.get("Y");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Y_VALUE);

            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) yTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: y series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - yTimestamp);
                    ySpeedView.setText(String.valueOf(speed));
                }
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Z_VALUE)) {
            series = deviceSeriesMap.get("Z");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Z_VALUE);

            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) zTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: z series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - zTimestamp);
                    zSpeedView.setText(String.valueOf(speed));
                }
            }
        }
        else {
            Log.d(TAG, "No known characteristic read");
            return; // unknown data received
        }

        if (counter <= SERIES_LENGTH) {
            series.appendData(new DataPoint(counter, receivedValue), true, SERIES_LENGTH);
        }

    }

    private void processServiceDiscovery(Bundle extras) {
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        Log.d(TAG, "Service discovered from device "+deviceAddress);
        List<BluetoothGattService> services = BluetoothLeService.getSupportedGattServices(deviceAddress);
        setNotificationsEnabled(deviceAddress, services);
        if (!mGattServiceData.containsKey(deviceAddress)) mGattServiceData.put(deviceAddress, services);
        if (!mapOfSeries.containsKey(deviceAddress)) {
            mapOfSeries.put(deviceAddress, getNewXYZSeries());
            addSeriesForDevice(mapOfSeries.get(deviceAddress));
        }

        /*
        / Changes the MTU size to 512 in case LOLLIPOP and above devices
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeService.exchangeGattMtu(deviceAddress, 512);
        }
    }

    private HashMap<String, LineGraphSeries<DataPoint>> getNewXYZSeries() {
        HashMap<String, LineGraphSeries<DataPoint>> seriesMap = new HashMap<>(3);
        seriesMap.put(X_MAP, new LineGraphSeries<>());
        seriesMap.put(Y_MAP, new LineGraphSeries<>());
        seriesMap.put(Z_MAP, new LineGraphSeries<>());
        return seriesMap;
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
