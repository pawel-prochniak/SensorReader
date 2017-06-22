package com.example.pprochniak.sensorreader.GATT.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import com.example.pprochniak.sensorreader.GATT.GattOperationBundle;

/**
 * Created by henny on 22.06.2017
 */

public abstract class GattOperation {

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10000;
    private BluetoothDevice mDevice;
    private GattOperationBundle mBundle;

    public GattOperation(BluetoothDevice device) {
        mDevice = device;
    }

    public abstract void execute(BluetoothGatt bluetoothGatt);

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getTimoutInMillis() {
        return DEFAULT_TIMEOUT_IN_MILLIS;
    }

    public abstract boolean hasAvailableCompletionCallback();

    public GattOperationBundle getBundle() {
        return mBundle;
    }

    public void setBundle(GattOperationBundle bundle) {
        mBundle = bundle;
    }
}
