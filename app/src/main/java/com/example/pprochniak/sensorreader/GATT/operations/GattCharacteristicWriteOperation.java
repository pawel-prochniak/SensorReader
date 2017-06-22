package com.example.pprochniak.sensorreader.GATT.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/**
 * Created by henny on 22.06.2017
 */

public class GattCharacteristicWriteOperation extends GattOperation {
    private static final String TAG = GattCharacteristicWriteOperation.class.getSimpleName();
    private final UUID mService;
    private final UUID mCharacteristic;
    private final byte[] mValue;

    public GattCharacteristicWriteOperation(BluetoothDevice device, UUID service, UUID characteristic, byte[] value) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mValue = value;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Log.d(TAG, "writing to " + mCharacteristic);
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        characteristic.setValue(mValue);
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }
}
