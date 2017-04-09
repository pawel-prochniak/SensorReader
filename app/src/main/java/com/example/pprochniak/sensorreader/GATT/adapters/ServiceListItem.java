package com.example.pprochniak.sensorreader.GATT.adapters;

import android.bluetooth.BluetoothGattService;
import android.content.Intent;

/**
 * Created by Henny on 2017-04-02.
 */

public interface ServiceListItem {
    void bind(BluetoothGattService gattService, int instanceId);
    void updateItem(Intent gattBroadcastIntent);
}
