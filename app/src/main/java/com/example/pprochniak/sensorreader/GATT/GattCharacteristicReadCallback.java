package com.example.pprochniak.sensorreader.GATT;

/**
 * Created by henny on 22.06.2017
 */

public interface GattCharacteristicReadCallback {
    void call(byte[] characteristic);
}