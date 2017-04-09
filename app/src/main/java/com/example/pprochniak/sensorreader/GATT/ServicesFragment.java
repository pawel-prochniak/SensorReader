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

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

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
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceFindMeData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceProximityData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceSensorHubData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattdbServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();

    @Bean GattController gattController;
    private ServicesDelegatesAdapter recyclerAdapter;
    private BroadcastReceiver gattListener;
    private Timer mTimer;

    // View bindings
    @BindView(R.id.services_not_found) TextView servicesNotFound;
    @BindView(R.id.services_recycler_view) RecyclerView servicesRecyclerView;

    private final BroadcastReceiver mServiceDiscoveryListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                Logger.e("Service discovered");
                if (mTimer != null)
                    mTimer.cancel();
                prepareGattServices(BluetoothLeService.getSupportedGattServices());

                /*
                / Changes the MTU size to 512 in case LOLLIPOP and above devices
                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    BluetoothLeService.exchangeGattMtu(512);
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL
                    .equals(action)) {
                if (mTimer != null)
                    mTimer.cancel();
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
        Logger.d(TAG, "Created views of ServicesFragment");
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(() -> {
            Logger.e("Discover service called");
            if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED)
                BluetoothLeService.discoverServices();
        }, DELAY_PERIOD);
        return rootView;
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
        boolean mFindmeSet = false;
        boolean mProximitySet = false;
        boolean mGattSet = false;
        if (gattServices == null)
            return;
        // Clear all array list before entering values.
        mGattServiceData.clear();
        mGattServiceFindMeData.clear();
        mGattServiceMasterData.clear();
        if (recyclerAdapter != null) recyclerAdapter.clearList();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, BluetoothGattService> currentServiceData = new HashMap<String, BluetoothGattService>();
            UUID uuid = gattService.getUuid();
            // Optimization code for FindMe Profile
            if (uuid.equals(UUIDDatabase.UUID_IMMEDIATE_ALERT_SERVICE)) {
                currentServiceData.put(LIST_UUID, gattService);
                mGattServiceMasterData.add(currentServiceData);
                if (!mGattServiceFindMeData.contains(currentServiceData)) {
                    mGattServiceFindMeData.add(currentServiceData);
                }
                if (!mFindmeSet) {
                    mFindmeSet = true;
                    mGattServiceData.add(currentServiceData);
                }

            }
            // Optimization code for Proximity Profile
            else if (uuid.equals(UUIDDatabase.UUID_LINK_LOSS_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_TRANSMISSION_POWER_SERVICE)) {
                currentServiceData.put(LIST_UUID, gattService);
                mGattServiceMasterData.add(currentServiceData);
                if (!mGattServiceProximityData.contains(currentServiceData)) {
                    mGattServiceProximityData.add(currentServiceData);
                }
                if (!mProximitySet) {
                    mProximitySet = true;
                    mGattServiceData.add(currentServiceData);
                }

            }// Optimization code for GATTDB
            else if (uuid.equals(UUIDDatabase.UUID_GENERIC_ACCESS_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_GENERIC_ATTRIBUTE_SERVICE)) {
                currentServiceData.put(LIST_UUID, gattService);
                mGattdbServiceData.add(currentServiceData);
                if (!mGattSet) {
                    mGattSet = true;
                    mGattServiceData.add(currentServiceData);
                }

            } //Optimization code for HID
            else if (uuid.equals(UUIDDatabase.UUID_HID_SERVICE)) {
                /**
                 * Special handling for KITKAT devices
                 */
                if (android.os.Build.VERSION.SDK_INT < 21) {
                    Logger.e("Kitkat RDK device found");
                    List<BluetoothGattCharacteristic> allCharacteristics =
                            gattService.getCharacteristics();
                    List<BluetoothGattCharacteristic> RDKCharacteristics = new
                            ArrayList<BluetoothGattCharacteristic>();
                    List<BluetoothGattDescriptor> RDKDescriptors = new
                            ArrayList<BluetoothGattDescriptor>();


                    //Find all Report characteristics
                    for (BluetoothGattCharacteristic characteristic : allCharacteristics) {
                        if (characteristic.getUuid().equals(UUIDDatabase.UUID_REP0RT)) {
                            RDKCharacteristics.add(characteristic);
                        }
                    }

                    //Find all Report descriptors
                    for (BluetoothGattCharacteristic rdkcharacteristic : RDKCharacteristics) {
                        List<BluetoothGattDescriptor> descriptors = rdkcharacteristic.
                                getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descriptors) {
                            RDKDescriptors.add(descriptor);
                        }
                    }
                    /**
                     * Wait for all  descriptors to receive
                     */
                    if (RDKDescriptors.size() == RDKCharacteristics.size() * 2) {

                        for (int pos = 0, descPos = 0; descPos < RDKCharacteristics.size(); pos++, descPos++) {
                            BluetoothGattCharacteristic rdkcharacteristic =
                                    RDKCharacteristics.get(descPos);
                            //Mapping the characteristic and descriptors
                            Logger.e("Pos-->" + pos);
                            Logger.e("Pos+1-->" + (pos + 1));
                            BluetoothGattDescriptor clientdescriptor = RDKDescriptors.get(pos);
                            BluetoothGattDescriptor reportdescriptor = RDKDescriptors.get(pos + 1);
                            if (!rdkcharacteristic.getDescriptors().contains(clientdescriptor))
                                rdkcharacteristic.addDescriptor(clientdescriptor);
                            if (!rdkcharacteristic.getDescriptors().contains(reportdescriptor))
                                rdkcharacteristic.addDescriptor(reportdescriptor);
                            pos++;
                        }
                    }
                    currentServiceData.put(LIST_UUID, gattService);
                    mGattServiceMasterData.add(currentServiceData);
                    mGattServiceData.add(currentServiceData);
                } else {
                    currentServiceData.put(LIST_UUID, gattService);
                    mGattServiceMasterData.add(currentServiceData);
                    mGattServiceData.add(currentServiceData);
                }

            } else {
                currentServiceData.put(LIST_UUID, gattService);
                mGattServiceMasterData.add(currentServiceData);
                mGattServiceData.add(currentServiceData);
            }

        }
        gattController.setGattServiceMasterData(mGattServiceMasterData);
        if (mGattdbServiceData.size() > 0) {
            displayAllGattData();
        } else {
            showNoServiceDiscoverAlert();
        }
    }

    private void displayAllGattData() {
        recyclerAdapter.notifyServiceListChanged();
    }

}
