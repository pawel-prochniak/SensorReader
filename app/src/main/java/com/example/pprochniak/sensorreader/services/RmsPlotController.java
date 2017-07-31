package com.example.pprochniak.sensorreader.services;

import android.util.Log;

import com.example.pprochniak.sensorreader.calculation.RootMeanSquare;
import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;

import java.util.HashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.example.pprochniak.sensorreader.services.SignalProcessor.*;

/**
 * Created by Henny on 2017-07-22.
 */

public class RmsPlotController implements CharacteristicController {
    private static final String TAG = "RmsPlotController";

    private HashMap<String, HashMap<String, RootMeanSquare>> deviceRmsMap = new HashMap<>();

    private SingleBarGraph xBar, yBar, zBar;
    private int rmsSize = 64;

    public RmsPlotController(SingleBarGraph xBar, SingleBarGraph yBar, SingleBarGraph zBar) {
        this.xBar = xBar;
        this.yBar = yBar;
        this.zBar = zBar;

        getSharedPrefSettings();
    }

    private void getSharedPrefSettings() {
        SharedPreferencesController controller = new SharedPreferencesController(xBar.getContext());
        rmsSize = controller.getRmsSampleSize();
    }

    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        xBar.setTitle("RMS X");
        yBar.setTitle("RMS Y");
        zBar.setTitle("RMS Z");

        xBar.setGraphColor(colorArray[0]);
        yBar.setGraphColor(colorArray[1]);
        zBar.setGraphColor(colorArray[2]);

        addRmsForDevice(deviceAddress);
    }

    @Override
    public void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis) {
        HashMap<String, RootMeanSquare> axisToRmsMap;
        RootMeanSquare rms;
        if (deviceRmsMap.containsKey(deviceAddress)) axisToRmsMap = deviceRmsMap.get(deviceAddress);
        else {
            Log.e(TAG, "addValue: no rms map for device found");
            return;
        }
        rms = axisToRmsMap.get(axis);

        Observable.just(rms.putAndCalculate(val))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((rmsVal) -> {
                    switch (axis) {
                        case X:
                            xBar.setValue(rmsVal);
                            break;
                        case Y:
                            yBar.setValue(rmsVal);
                            break;
                        case Z:
                            zBar.setValue(rmsVal);
                            break;
                    }
                });
    }

    private void addRmsForDevice(String deviceAddress) {
        HashMap<String, RootMeanSquare> axisToRmsMap = new HashMap<>();
        axisToRmsMap.put(X, new RootMeanSquare(rmsSize));
        axisToRmsMap.put(Y, new RootMeanSquare(rmsSize));
        axisToRmsMap.put(Z, new RootMeanSquare(rmsSize));

        deviceRmsMap.put(deviceAddress, axisToRmsMap);
    }
}
