package com.example.pprochniak.sensorreader.services;

import android.util.Log;

import com.example.pprochniak.sensorreader.calculation.RootMeanSquare;
import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;

import java.util.HashMap;

import static com.example.pprochniak.sensorreader.services.PlotController.*;

/**
 * Created by Henny on 2017-07-22.
 */

public class RmsPlotController implements GraphPlotController {
    private static final String TAG = "RmsPlotController";

    private HashMap<String, HashMap<String, RootMeanSquare>> deviceRmsMap = new HashMap<>();

    private BarGraph xBar, yBar, zBar;
    private int rmsSize = 64;

    public RmsPlotController(BarGraph xBar, BarGraph yBar, BarGraph zBar) {
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
    public void addValue(String deviceAddress, float val, @PlotController.AXIS String axis) {
        HashMap<String, RootMeanSquare> axisToRmsMap;
        RootMeanSquare rms;
        if (deviceRmsMap.containsKey(deviceAddress)) axisToRmsMap = deviceRmsMap.get(deviceAddress);
        else {
            Log.e(TAG, "addValue: no rms map for device found");
            return;
        }
        rms = axisToRmsMap.get(axis);
        switch (axis) {
            case X:
                xBar.setValue(rms.putAndCalculate(val));
                break;
            case Y:
                yBar.setValue(rms.putAndCalculate(val));
                break;
            case Z:
                zBar.setValue(rms.putAndCalculate(val));
                break;
        }
    }

    private void addRmsForDevice(String deviceAddress) {
        HashMap<String, RootMeanSquare> axisToRmsMap = new HashMap<>();
        axisToRmsMap.put(X, new RootMeanSquare(rmsSize));
        axisToRmsMap.put(Y, new RootMeanSquare(rmsSize));
        axisToRmsMap.put(Z, new RootMeanSquare(rmsSize));

        deviceRmsMap.put(deviceAddress, axisToRmsMap);
    }
}
