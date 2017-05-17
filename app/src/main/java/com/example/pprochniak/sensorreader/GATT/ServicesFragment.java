package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.GATT.adapters.ServiceListItem;
import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Henny on 2017-03-29.
 */

@EFragment
public class ServicesFragment extends Fragment {
    private static final String TAG = "ServicesFragment";

    public static boolean isInFragment = false;

    private static final String LIST_UUID = "UUID";
    // Stops scanning after 2 seconds.
    private static final long DELAY_PERIOD = 500;
    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattdbServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();

    @Bean GattController gattController;
    private ServicesDelegatesAdapter recyclerAdapter;
    private BroadcastReceiver gattListener;
    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();
    int seriesSwitch = 0;
    int xCounter = 0;

    // View bindings
    @BindView(R.id.services_not_found) TextView servicesNotFound;
    @BindView(R.id.services_recycler_view) RecyclerView servicesRecyclerView;
    @BindView(R.id.graph) GraphView graphView;

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            int receivedValue;
            LineGraphSeries<DataPoint> series;
            switch (seriesSwitch) {
                case 0:
                    series = series1;
                    break;
                case 1:
                    series = series2;
                    break;
                default:
                    return;
            }

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (extras.containsKey(Constants.EXTRA_SENSOR_VALUE) && xCounter < 1000) {
                    receivedValue = extras.getInt(Constants.EXTRA_SENSOR_VALUE);
                    Log.d(TAG, "onReceive: appending point: "+xCounter+","+receivedValue);
                    series.appendData(new DataPoint(xCounter++, receivedValue), true, 1000);
                } else {
                    Log.d(TAG, "onReceive: adding series to graph");
                    graphView.addSeries(series);
                    xCounter = 0;
                }
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.services_fragment, container, false);
        ButterKnife.bind(this, rootView);
        setAdapter();
        setSeriesProperties();
        Logger.d(TAG, "Created views of ServicesFragment");
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(() -> {
            Logger.e("Discover service called");
            if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED)
                BluetoothLeService.discoverServices();
        }, DELAY_PERIOD);
        return rootView;
    }

    private void setSeriesProperties() {
        series2.setBackgroundColor(R.color.colorAccent);
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
        getActivity().unregisterReceiver(mServiceDiscoveryListener);
        getActivity().unregisterReceiver(mGattUpdateListener);
        if (gattListener != null) {
            getActivity().unregisterReceiver(gattListener);
        }
        super.onPause();
    }

    private void setAdapter() {
        recyclerAdapter = new ServicesDelegatesAdapter();
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        servicesRecyclerView.setAdapter(recyclerAdapter);
        gattListener = recyclerAdapter.getGattUpdateReceiver();
    }

    private void showNoServiceDiscoverAlert() {
        servicesNotFound.setVisibility(View.VISIBLE);
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
        if (recyclerAdapter != null) recyclerAdapter.clearList();

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
        getActivity().registerReceiver(mGattUpdateListener, Utils.makeGattUpdateIntentFilter());
    }

}
