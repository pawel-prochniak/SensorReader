package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Henny on 2017-03-29.
 */

@EBean(scope = EBean.Scope.Singleton)
public class GattController {
    private ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();


    private List<BluetoothGattCharacteristic> mGattCharacteristics;
    private BluetoothGattCharacteristic mBluetoothgattcharacteristic;
    private BluetoothGattDescriptor mBluetoothGattDescriptor;

    /**
     * getter method for Blue tooth GATT characteristic
     *
     * @return {@link BluetoothGattCharacteristic}
     */
    public BluetoothGattCharacteristic getBluetoothgattcharacteristic() {
        return mBluetoothgattcharacteristic;
    }

    /**
     * setter method for Blue tooth GATT characteristics
     *
     * @param bluetoothgattcharacteristic
     */
    public void setBluetoothgattcharacteristic(
            BluetoothGattCharacteristic bluetoothgattcharacteristic) {
        this.mBluetoothgattcharacteristic = bluetoothgattcharacteristic;
    }

    /**
     * getter method for Blue tooth GATT characteristic
     *
     * @return {@link BluetoothGattCharacteristic}
     */
    public BluetoothGattDescriptor getBluetoothgattDescriptor() {
        return mBluetoothGattDescriptor;
    }

    /**
     * setter method for Blue tooth GATT Descriptor
     *
     * @param bluetoothGattDescriptor
     */
    public void setBluetoothgattdescriptor(
            BluetoothGattDescriptor bluetoothGattDescriptor) {
        this.mBluetoothGattDescriptor = bluetoothGattDescriptor;
    }

    /**
     * getter method for blue tooth GATT Characteristic list
     *
     * @return {@link List<BluetoothGattCharacteristic>}
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristics() {
        return mGattCharacteristics;
    }

    /**
     * setter method for blue tooth GATT Characteristic list
     *
     * @param gattCharacteristics
     */
    public void setGattCharacteristics(
            List<BluetoothGattCharacteristic> gattCharacteristics) {
        this.mGattCharacteristics = gattCharacteristics;
    }

    public ArrayList<HashMap<String, BluetoothGattService>> getGattServiceMasterData() {
        return mGattServiceMasterData;
    }

    public void setGattServiceMasterData(
            ArrayList<HashMap<String, BluetoothGattService>> gattServiceMasterData) {
        this.mGattServiceMasterData = gattServiceMasterData;

    }
}
