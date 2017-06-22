package com.example.pprochniak.sensorreader.GATT;

/**
 * Created by henny on 22.06.2017
 */

public interface GattDescriptorReadCallback {
    void call(byte[] value);
}