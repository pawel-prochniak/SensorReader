package com.example.pprochniak.sensorreader.GATT.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.example.pprochniak.sensorreader.GATT.GattCharacteristicReadCallback;

import java.util.UUID;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by henny on 22.06.2017
 */

public class GattCharacteristicReadOperation extends GattOperation {
    private static final String TAG = GattCharacteristicReadOperation.class.getSimpleName();

    private final UUID mService;
    private final UUID mCharacteristic;
    private final GattCharacteristicReadCallback mCallback;

    public GattCharacteristicReadOperation(BluetoothDevice device, UUID service, UUID characteristic, GattCharacteristicReadCallback callback) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mCallback = callback;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Log.d(TAG, "writing to " + mCharacteristic);
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        gatt.readCharacteristic(characteristic);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattCharacteristic characteristic) {
        mCallback.call(characteristic.getValue());
    }
}