package com.example.pprochniak.sensorreader.GATT.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.UUID;


/**
 * Created by henny on 22.06.2017
 */

public class GattDescriptorWriteOperation extends GattOperation {
    private static final String TAG = GattDescriptorWriteOperation.class.getSimpleName();
    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;

    public GattDescriptorWriteOperation(BluetoothDevice device, UUID service, UUID characteristic, UUID descriptor) {
        super(device);
        mService = service;
        mCharacteristic = characteristic;
        mDescriptor = descriptor;
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Log.d(TAG, "Writing to " + mDescriptor);
        BluetoothGattDescriptor descriptor = gatt.getService(mService).getCharacteristic(mCharacteristic).getDescriptor(mDescriptor);
        gatt.writeDescriptor(descriptor);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }
}
