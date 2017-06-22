package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by henny on 22.06.2017
 */

public interface CharacteristicChangeListener {
    public void onCharacteristicChanged(String deviceAddress, BluetoothGattCharacteristic characteristic);
}
