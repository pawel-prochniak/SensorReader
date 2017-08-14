package com.example.pprochniak.sensorreader.deviceDiscovery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.GATT.GattController;
import com.example.pprochniak.sensorreader.signalProcessing.TimeSeriesFragment;
import com.example.pprochniak.sensorreader.signalProcessing.TimeSeriesFragment_;
import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.Utils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Henny on 2017-03-27.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@EFragment(R.layout.discover_device_fragment)
public class DevicesFragment extends Fragment {
    private static final String TAG = "DevicesFragment";

    public static boolean isInFragment = false;

    TimeSeriesFragment timeSeriesFragment;

    //
    // Scanning properties
    //

    private static final long SCAN_PERIOD_TIMEOUT = 5000;
    private Timer mScanTimer;
    private boolean mScanning;

    // Connection time out after 10 seconds.
    private static final long CONNECTION_TIMEOUT = 15000;
    private Timer mConnectTimer;
    private int mConnectionCounter = 0;

    //Delay Time out
    private static final long DELAY_PERIOD = 500;

    // Activity request constant
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean searchEnabled = false;
    private boolean scanningInProgress = false;

    //
    // Fragment-related properties
    //

    private DeviceListAdapter deviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanSettings scanSettings;
    private ArrayList<ScanFilter> scanFilters;

    private ProgressDialog progressDialog;

    @Bean GattController gattController;
    @Bean SignalProcessor signalProcessor;

    @ViewById(R.id.discover_device_scan_button) Button scanButton;
    @ViewById(R.id.discover_device_recycler_view) RecyclerView deviceListView;

    // Lifecycle methods

    @AfterInject
    public void afterInject() {
        timeSeriesFragment = new TimeSeriesFragment_();
    }

    @AfterViews
    public void afterViews() {
        checkBleSupportAndInitialize();
        setRecyclerWithAdapter();
//        Logger.createDataLoggerFile(getActivity());
        scanButton.setOnClickListener((view) -> {
            if (!mScanning) {
                // Prepare list view and initiate scanning
                scanLeDevice(true);
                mScanning = true;
                searchEnabled = false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isInFragment = true;
        if (checkBluetoothStatus()) {
            if (Build.VERSION.SDK_INT >= 21) {
                scanFilters = new ArrayList<>();
                scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            }
        }
        Log.e(TAG, "Registering receiver in Profile scannng");
        getActivity().registerReceiver(mGattConnectReceiver,
                Utils.makeGattUpdateIntentFilter());

    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mGattConnectReceiver);
        if (scanningInProgress) {
            if (Build.VERSION.SDK_INT < 21) bluetoothAdapter.stopLeScan(mLeScanCallback);
            else bleScanner.stopScan(scanCallbackAPI21);
        }
        Log.d(TAG, "Unregistered gatt receiver");
        isInFragment = false;
        if (progressDialog != null) progressDialog = null;
        super.onPause();
    }

    private void setRecyclerWithAdapter() {
        deviceListAdapter = new DeviceListAdapter(this);
        deviceListView.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceListView.setAdapter(deviceListAdapter);

        Set<Map.Entry<String, BluetoothGatt>> bleGattConnected = BluetoothLeService.getConnectedGattServices().entrySet();
        Log.d(TAG, "setRecyclerWithAdapter: found " + bleGattConnected.size() + " connected devices");
        for (Map.Entry<String, BluetoothGatt> entry : bleGattConnected) {
            deviceListAdapter.addDevice(entry.getValue().getDevice());
            deviceListAdapter.addConnectedDevice(entry.getKey());
        }
    }

    private void checkBleSupportAndInitialize() {
        // Use this check to determine whether BLE is supported on the device.
        if (!getActivity().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.device_ble_not_supported,
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= 21) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }


        if (bluetoothAdapter == null) {
            // Device does not support Blue tooth
            Toast.makeText(getActivity(),
                    R.string.device_bluetooth_not_supported, Toast.LENGTH_SHORT)
                    .show();
            getActivity().finish();
        }
    }

    private void showProgressDialog(String msg) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    /**
     * Method to scan BLE Devices. The status of the scan will be detected in
     * the BluetoothAdapter.LeScanCallback
     *
     * @param enable
     */
    void scanLeDevice(final boolean enable) {
        if (bluetoothAdapter == null || bleScanner == null) checkBleSupportAndInitialize();
        if (isInFragment) {
            if (enable) {
                if (!scanningInProgress) {
                    Log.d(TAG, "Starting BLE scan");
                    scanningInProgress = true;
                    if (Build.VERSION.SDK_INT < 21) bluetoothAdapter.startLeScan(mLeScanCallback);
                    else bleScanner.startScan(scanFilters, scanSettings, scanCallbackAPI21);
                    scanButton.post(() -> scanButton.setText(R.string.device_discovery_scanning));
                }
            } else {
                Log.d(TAG, "Stopping BLE scan");
                scanningInProgress = false;
                if (Build.VERSION.SDK_INT < 21) bluetoothAdapter.stopLeScan(mLeScanCallback);
                else bleScanner.stopScan(scanCallbackAPI21);
                if (scanButton != null)
                    scanButton.post(() -> scanButton.setText(R.string.device_discovery_scan));
            }
        }

    }

    public void connectDevice(String deviceAddress, boolean isFirstConnect) {
        showProgressDialog("Connecting to device");

        // Get the connection status of the device
        if (!BluetoothLeService.getConnectedGattServices().containsKey(deviceAddress)) {
            Log.v(TAG, "BLE DISCONNECTED STATE");
            // Disconnected,so connect
            BluetoothLeService.connect(deviceAddress, getActivity());
        } else {
            // Connecting to some devices,so disconnect and then connect
            disconnectDevice(deviceAddress);
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(() -> {
                BluetoothLeService.connect(deviceAddress, getActivity());
            }, DELAY_PERIOD);
        }

        startConnectTimer(deviceAddress);
        mConnectionCounter++;

    }

    public void disconnectDevice(String deviceAddress) {
        BluetoothLeService.disconnect(deviceAddress);
    }

    /**
     * Scanning timer. Stops scanning after {@value SCAN_PERIOD_TIMEOUT} [ms].
     */
    public void startScanTimer() {
        mScanTimer = new Timer();
        mScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
                scanLeDevice(false);
            }
        }, SCAN_PERIOD_TIMEOUT);
    }

    /**
     * Connect Timer
     */
    private void startConnectTimer(String deviceAddress) {
        mConnectTimer = new Timer();
        mConnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, "CONNECTION TIME OUT");
                disconnectDevice(deviceAddress);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(),
                                R.string.profile_cannot_connect_message,
                                Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                        if (deviceListAdapter != null) {
                            deviceListAdapter.clear();
                            deviceListAdapter.notifyDataSetChanged();
                        }
                        scanLeDevice(true);
                        mScanning = true;
                    });
                }

            }
        }, CONNECTION_TIMEOUT);
    }

    public boolean checkBluetoothStatus() {
        /**
         * Ensures Blue tooth is enabled on the device. If Blue tooth is not
         * currently enabled, fire an intent to display a dialog asking the user
         * to grant permission to enable it.
         */
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    /**
     * BroadcastReceiver for receiving the GATT communication status
     */
    private final BroadcastReceiver mGattConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mConnectionCounter--;
            if (mConnectionCounter < 1) hideProgressDialog();

            final String action = intent.getAction();
            // Status received when connected to GATT Server
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                if (mScanning) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }


                String deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS);

                deviceListAdapter.addConnectedDevice(deviceAddress);

                Log.d(TAG, "Connected to GATT service");
                Toast.makeText(context, "Connected to GATT service", Toast.LENGTH_LONG).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                /**
                 * Disconnect event.When the connect timer is ON,Reconnect the device
                 * else show disconnect message
                 */
                String deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS);
                deviceListAdapter.removeConnectedDevice(deviceAddress);

                String disconnectMsg = getResources().getString(R.string.device_disconnected, deviceAddress);

                Toast.makeText(getActivity(), disconnectMsg, Toast.LENGTH_SHORT).show();
            }

            if (mConnectTimer != null) {
                mConnectTimer.cancel();
            }
        }
    };

    /**
     * Call back for BLE Scan
     * This call back is called when a BLE device is found near by.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            (device, rssi, scanRecord) -> addFoundBluetoothDevice(device, rssi);

    private ScanCallback scanCallbackAPI21 = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null && result.getDevice() != null)
                addFoundBluetoothDevice(result.getDevice(), result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "API21 scan callback - batch: " + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with errorCode: " + errorCode);
        }
    };

    private void addFoundBluetoothDevice(BluetoothDevice device, int rssi) {
        Activity mActivity = getActivity();
        Log.d(TAG, "Found device: " + device.getName() + ", address: " + device.getAddress() + ", RSSI: " + rssi);
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                        if (!searchEnabled) {
                            deviceListAdapter.addDevice(device, rssi);
                            try {
                                deviceListAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (deviceListAdapter.deviceList.size() == 1) {
                                startScanTimer();
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable BlueTooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            getActivity().finish();
        } else {
            // Check which request we're responding to
            if (requestCode == REQUEST_ENABLE_BT) {

                // Make sure the request was successful
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(
                                    R.string.device_bluetooth_on),
                            Toast.LENGTH_SHORT).show();
                    scanLeDevice(true);
                } else {
                    getActivity().finish();
                }
            }
        }
    }


}
