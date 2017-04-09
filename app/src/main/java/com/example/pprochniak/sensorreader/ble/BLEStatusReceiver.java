package com.example.pprochniak.sensorreader.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.GATT.ServicesFragment;
import com.example.pprochniak.sensorreader.MainActivity;
import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.deviceDiscovery.DiscoverFragment;
import com.example.pprochniak.sensorreader.utils.Logger;

/**
 * Created by Henny on 2017-04-05.
 */

public class BLEStatusReceiver extends BroadcastReceiver {
    static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Logger.e("onReceive--" + MainActivity.isAppInBackground);
            if (!MainActivity.isAppInBackground) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.alert_message_bluetooth_disconnect),
                        Toast.LENGTH_SHORT).show();

                if (!DiscoverFragment.isInFragment &&
                        !ServicesFragment.isInFragment&&!MainActivity.isAppInBackground) {
                    Logger.e("Not in PSF and SCF");
                    if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_DISCONNECTED) {
                        Toast.makeText(context,
                                context.getResources().getString(R.string.alert_message_bluetooth_disconnect),
                                Toast.LENGTH_SHORT).show();
                        Intent homePage = new Intent(context, MainActivity.class);
                        homePage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(homePage);
                    }
                }
            }
        }
    }
}
