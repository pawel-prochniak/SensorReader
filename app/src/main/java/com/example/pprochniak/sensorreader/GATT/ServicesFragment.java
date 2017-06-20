package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.MainActivity;
import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment(R.layout.services_fragment)
public class ServicesFragment extends Fragment {
    private static final String TAG = "ServicesFragment";

    public static boolean isInFragment = false;
    private boolean registeredToUpdates = false;

    private static final String LIST_UUID = "UUID";
    private static final long DELAY_PERIOD = 500;
    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;
    private static final int SERIES_LENGTH = 500; // how many data points will be collected for each axis
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattdbServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();

    MainActivity mainActivity;
    @Bean GattController gattController;

    private ServicesDelegatesAdapter recyclerAdapter;
    private BroadcastReceiver gattListener;
    LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>();
    int xCounter = 0;
    int yCounter = 0;
    int zCounter = 0;
    long xTimestamp, yTimestamp, zTimestamp;

    boolean appendingCompleted = false;

    // View bindings
    @ViewById(R.id.services_not_found) TextView servicesNotFound;
    @ViewById(R.id.graph) GraphView graphView;
    @ViewById(R.id.x_speed) TextView xSpeedView;
    @ViewById(R.id.y_speed) TextView ySpeedView;
    @ViewById(R.id.z_speed) TextView zSpeedView;
    @ViewById(R.id.total_time) TextView totalTimeView;


    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                receiveValueAndAppendPoint(extras);
            }
        }
    };

    private final BroadcastReceiver mServiceDiscoveryListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                Logger.e("Service discovered");
                prepareGattServices(BluetoothLeService.getSupportedGattServices());

                /*
                / Changes the MTU size to 512 in case LOLLIPOP and above devices
                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    BluetoothLeService.exchangeGattMtu(512);
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL
                    .equals(action)) {
                showNoServiceDiscoverAlert();
            }
        }
    };

    @AfterViews
    public void afterViews(){
        mainActivity = (MainActivity) getActivity();
        setGraphProperties();
        Logger.d(TAG, "Created views of ServicesFragment");
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(() -> {
            Logger.e("Discover service called");
            if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED)
                BluetoothLeService.discoverServices();
        }, DELAY_PERIOD);
    }

    private void setGraphProperties() {
        graphView.getGridLabelRenderer().setLabelVerticalWidth(100);
        seriesX.setTitle("X");
        seriesY.setTitle("Y");
        seriesZ.setTitle("Z");
        seriesY.setColor(Color.rgb(255, 0, 0));
        seriesZ.setColor(Color.rgb(0, 255, 0));
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(SERIES_LENGTH);
        graphView.addSeries(seriesX);
        graphView.addSeries(seriesY);
        graphView.addSeries(seriesZ);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("Registering mServiceDiscovery");
        getActivity().registerReceiver(mServiceDiscoveryListener, Utils.makeGattUpdateIntentFilter());
        if (gattListener != null) {
            getActivity().registerReceiver(gattListener, Utils.makeGattUpdateIntentFilter());
        } else {
            Log.e(TAG, "Couldn't register gattListener, gatListener null!");
        }
        isInFragment = true;
    }

    @Override
    public void onPause() {
        isInFragment = false;
        unsubscribeBroadcastReceivers();
        super.onPause();
    }

    private void setAdapter() {
        recyclerAdapter = new ServicesDelegatesAdapter();
        gattListener = recyclerAdapter.getGattUpdateReceiver();
    }

    private void showNoServiceDiscoverAlert() {
        servicesNotFound.setVisibility(View.VISIBLE);
    }

    private void receiveValueAndAppendPoint(Bundle extras) {
        int receivedValue;
        int counter;
        LineGraphSeries<DataPoint> series;

        if (extras.containsKey(Constants.EXTRA_ACC_X_VALUE) && xCounter < SERIES_LENGTH) {
            if (xCounter == 0) xTimestamp = System.currentTimeMillis();
            series = seriesX;
            counter = xCounter++;
            receivedValue = extras.getInt(Constants.EXTRA_ACC_X_VALUE);
            if (xCounter == SERIES_LENGTH) {
                Log.d(TAG, "receiveValueAndAppendPoint: x series finished");
                float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - xTimestamp);
                xSpeedView.setText(String.valueOf(speed));
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Y_VALUE) && yCounter < SERIES_LENGTH) {
            if (yCounter == 0) yTimestamp = System.currentTimeMillis();
            series = seriesY;
            counter = yCounter++;
            receivedValue = extras.getInt(Constants.EXTRA_ACC_Y_VALUE);
            if (yCounter == SERIES_LENGTH) {
                Log.d(TAG, "receiveValueAndAppendPoint: y series finished");
                float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - yTimestamp);
                ySpeedView.setText(String.valueOf(speed));
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Z_VALUE) && zCounter < SERIES_LENGTH) {
            if (zCounter == 0) zTimestamp = System.currentTimeMillis();
            series = seriesZ;
            counter = zCounter++;
            receivedValue = extras.getInt(Constants.EXTRA_ACC_Z_VALUE);
            if (zCounter == SERIES_LENGTH) {
                Log.d(TAG, "receiveValueAndAppendPoint: z series finished");
                float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - zTimestamp);
                zSpeedView.setText(String.valueOf(speed));
            }
        }
        else return; // unknown data received

        // calculate total time if all series ended
        if (xCounter == SERIES_LENGTH && yCounter == SERIES_LENGTH && zCounter == SERIES_LENGTH) {
            long least;
            // Find earliest timestamp
            if (xTimestamp - yTimestamp < 0) {
                least = xTimestamp;
            } else least = yTimestamp;
            if (least - zTimestamp > 0) least = zTimestamp;

            long totalTime = (System.currentTimeMillis() - least) / 1000;
            Log.d(TAG, "receiveValueAndAppendPoint: total time: "+String.valueOf(totalTime));
            totalTimeView.setText(String.valueOf(totalTime));
        }

        if (counter <= SERIES_LENGTH) {
            series.appendData(new DataPoint(counter, receivedValue), true, SERIES_LENGTH);
        }

    }

    /**
     * Getting the GATT Services
     *
     * @param gattServices
     */
    private void prepareGattServices(List<BluetoothGattService> gattServices) {
        prepareData(gattServices);
    }

    /**
     * Prepare GATTServices data.
     *
     * @param gattServices
     */
    private void prepareData(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        // Clear all array list before entering values.
        mGattServiceData.clear();
        mGattServiceMasterData.clear();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, BluetoothGattService> currentServiceData = new HashMap<String, BluetoothGattService>();
            UUID uuid = gattService.getUuid();
            if (UUIDDatabase.UUID_SENSOR_READ_SERVICE.equals(uuid)) {
                // Auto set notify as TRUE
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    if (Utils.checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                        BluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    }
                }
            }

            currentServiceData.put(LIST_UUID, gattService);
            mGattServiceMasterData.add(currentServiceData);
            mGattServiceData.add(currentServiceData);

        }
        gattController.setGattServiceMasterData(mGattServiceMasterData);
        if (mGattServiceData.size() > 0) {
            subscribeToGattUpdates();
        } else {
            showNoServiceDiscoverAlert();
        }
    }


    private void displayAllGattData() {
        if (mGattdbServiceData.contains(UUIDDatabase.UUID_SENSOR_READ_SERVICE.toString()))
            recyclerAdapter.notifyServiceListChanged();
    }

    private void subscribeToGattUpdates() {
        registeredToUpdates = true;
        getActivity().registerReceiver(mGattUpdateListener, Utils.makeGattUpdateIntentFilter());
    }

    private void unsubscribeBroadcastReceivers() {
        getActivity().unregisterReceiver(mServiceDiscoveryListener);
        if (registeredToUpdates) {
            getActivity().unregisterReceiver(mGattUpdateListener);
            registeredToUpdates = false;
        }
        if (gattListener != null) {
            getActivity().unregisterReceiver(gattListener);
        }
    }

}
