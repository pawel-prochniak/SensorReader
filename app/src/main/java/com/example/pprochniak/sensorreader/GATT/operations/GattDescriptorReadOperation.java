package com.example.pprochniak.sensorreader.GATT.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.example.pprochniak.sensorreader.GATT.GattDescriptorReadCallback;

import java.util.UUID;

/**
 * Created by henny on 22.06.2017
 */

public class GattDescriptorReadOperation extends GattOperation {
    private static final String TAG = GattDescriptorReadOperation.class.getSimpleName();
    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;
    private final GattDescriptorReadCallback mCallback;

    public GattDescriptorReadOperation(BluetoothDevice device, UUID service, UUID characteristic, UUID descriptor, GattDescriptorReadCallback callback) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mDescriptor = descriptor;
        mCallback = callback;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Log.d(TAG, "Reading from " + mDescriptor);
        BluetoothGattDescriptor descriptor = gatt.getService(mService).getCharacteristic(mCharacteristic).getDescriptor(mDescriptor);
        gatt.readDescriptor(descriptor);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattDescriptor descriptor) {
        mCallback.call(descriptor.getValue());
    }
}
