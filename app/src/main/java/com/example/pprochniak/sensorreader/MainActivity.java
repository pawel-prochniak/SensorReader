package com.example.pprochniak.sensorreader;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.deviceDiscovery.DevicesFragment;
import com.example.pprochniak.sensorreader.services.SignalProcessor;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.DrawerController;
import com.example.pprochniak.sensorreader.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static boolean isAppInBackground = false;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_STORAGE = 2;
    private static final int PERMISSIONS_MULTIPLE = 3;

    @ViewById(R.id.drawer_layout) DrawerLayout drawerLayout;
    @ViewById(R.id.drawer) ListView drawerList;
    @Bean DrawerController drawerController;
    @Bean SignalProcessor signalProcessor;

    @AfterViews
    public void afterViews() {
        setupDrawer();

        // start BLE service
        Intent gattServiceIntent = new Intent(getApplicationContext(),
                BluetoothLeService.class);
        startService(gattServiceIntent);

        // set start fragment
        drawerController.setFragment(DrawerController.DEVICES_FRAGMENT);

        checkPermissions();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isAppInBackground = false;
        subscribeToGattUpdates();
    }

    @Override
    protected void onPause() {
        getIntent().setData(null);
        // Getting the current active fragment
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.main_container);
        if (currentFragment instanceof DevicesFragment) {
            Intent gattServiceIntent = new Intent(getApplicationContext(),
                    BluetoothLeService.class);
            stopService(gattServiceIntent);
        }
        unsubscribeFromGattUpdates();
        isAppInBackground = true;
        super.onPause();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "newIntent");
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location permission (needed for BLE)
            boolean locationPermissionGranted = this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            boolean writePermissionGranted = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
            if (!locationPermissionGranted && !writePermissionGranted) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_MULTIPLE);
            } else if (!writePermissionGranted && locationPermissionGranted) {
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            } else {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    private void setupDrawer() {
        drawerController.setupDrawer(drawerLayout, drawerList);
    }

    private final BroadcastReceiver mGattUpdateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                signalProcessor.receiveValueAndAppendPoint(extras);
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                processDeviceConnection(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                processServiceDiscovery(extras);
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL.equals(action)) {
                processUnsuccessfulConnection(extras);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                displayDisconnectToast(extras);
            }

        }
    };

    private void displayDisconnectToast(Bundle extras) {
        Resources res = getResources();
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        String formattedDisconnectMsg = res.getString(R.string.device_disconnected, deviceAddress);
        Toast.makeText(this, formattedDisconnectMsg, Toast.LENGTH_LONG).show();
    }

    private void processDeviceConnection(Bundle extras) {
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        Log.d(TAG, "processDeviceConnection: device: "+deviceAddress);
        if (deviceAddress == null) {
            processUnsuccessfulConnection(extras);
            return;
        }
        signalProcessor.connectToAllServices();
    }

    private void processServiceDiscovery(Bundle extras) {
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        Log.d(TAG, "Service discovered from device " + deviceAddress);
        if (deviceAddress == null) {
            processUnsuccessfulConnection(extras);
            return;
        }
        signalProcessor.addDevice(deviceAddress);
        BluetoothLeService.subscribeToSensorNotifications(deviceAddress);

        /*
        / Changes the MTU size to 512 in case LOLLIPOP and above devices
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeService.exchangeGattMtu(deviceAddress, 512);
        }
    }

    private void processUnsuccessfulConnection(Bundle extras) {
        Log.d(TAG, "processUnsuccessfulConnection: ");
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        BluetoothLeService.disconnect(deviceAddress);
    }

    private void subscribeToGattUpdates() {
        registerReceiver(mGattUpdateListener, Utils.makeGattUpdateIntentFilter());
    }

    private void unsubscribeFromGattUpdates() {
        unregisterReceiver(mGattUpdateListener);
    }


}
