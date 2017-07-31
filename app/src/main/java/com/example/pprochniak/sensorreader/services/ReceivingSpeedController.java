package com.example.pprochniak.sensorreader.services;

import android.content.Context;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;

import static com.example.pprochniak.sensorreader.services.SignalProcessor.X;

/**
 * Created by Henny on 2017-07-23.
 */

public class ReceivingSpeedController implements CharacteristicController {
    private int receivingSampleSize;
    private int xSamplesCounter = 0;
    private long timeStamp;
    private String firstDeviceAddress;
    private Context context;
    private TextView textView;
    private boolean calculated;

    public ReceivingSpeedController(Context context, TextView textView) {
        this.context = context;
        this.textView = textView;
        getSharedPrefsSettings();
    }

    private void getSharedPrefsSettings() {
        SharedPreferencesController sharedPrefs = new SharedPreferencesController(context);
        receivingSampleSize = sharedPrefs.getCollectedSampleSize();
        if (receivingSampleSize % 2 != 0) {
            receivingSampleSize = (receivingSampleSize + 1) / 2;
        } else {
            receivingSampleSize = receivingSampleSize / 2;
        }
    }


    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        if (xSamplesCounter == 0) {
            firstDeviceAddress = deviceAddress;
            timeStamp = System.currentTimeMillis();
        }
    }

    @Override
    public void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis) {
        if (axis.equals(X)) {
            handleReceivingRate(deviceAddress);
        }
    }

    private void setReceivingSpeed(float speed) {
        String str = String.valueOf(speed);
        textView.setText(str);
        calculated = true;
    }

    private void handleReceivingRate(String deviceAddress) {
        if (deviceAddress.equals(firstDeviceAddress)) {
            if (xSamplesCounter < receivingSampleSize) xSamplesCounter++;
            else if (!calculated && xSamplesCounter == receivingSampleSize) {
                float msDiff = (System.currentTimeMillis() - timeStamp);
                setReceivingSpeed(xSamplesCounter * 1000 / msDiff);
            }
        }
    }
}
