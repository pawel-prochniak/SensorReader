package com.example.pprochniak.sensorreader.services;

import android.util.Log;
import android.widget.LinearLayout;

import com.example.pprochniak.sensorreader.calculation.PeakToPeak;

import java.util.HashMap;

/**
 * Created by Henny on 2017-07-24.
 */

public class PeakToPeakController implements CharacteristicController {
    private static final String TAG = "PeakToPeakController";

    private HashMap<String, HashMap<String, PeakToPeak>> deviceToPeakValues = new HashMap<>();
    private HashMap<String, PeakToPeakView> deviceToViewMap = new HashMap<>();

    private LinearLayout viewToPopulate;

    public PeakToPeakController(LinearLayout viewToPopulate) {
        this.viewToPopulate = viewToPopulate;
    }

    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        deviceToPeakValues.put(deviceAddress, getAxisToPeakToPeakMap());
        addViewForDevice(deviceAddress);
    }

    @Override
    public void addValue(String deviceAddress, float val, @PlotController.AXIS String axis) {
        HashMap<String, PeakToPeak> axisToPeakToPeak = deviceToPeakValues.get(deviceAddress);
        if (axisToPeakToPeak == null) {
            Log.e(TAG, "addValue: no map for this device found");
            return;
        }
        PeakToPeak pk2pk = axisToPeakToPeak.get(axis);
        if (pk2pk.putNewValueAndCheckIfChanged(val)) {
            float newVal = pk2pk.getPk2Pk();
            String newValText = String.valueOf(newVal);
            PeakToPeakView view = deviceToViewMap.get(deviceAddress);
            view.setValue(newValText, axis);
        }

    }

    private HashMap<String, PeakToPeak> getAxisToPeakToPeakMap() {
        HashMap<String, PeakToPeak> axisToPkPk = new HashMap<>();
        axisToPkPk.put(PlotController.X, new PeakToPeak());
        axisToPkPk.put(PlotController.Y, new PeakToPeak());
        axisToPkPk.put(PlotController.Z, new PeakToPeak());
        return axisToPkPk;
    }

    private void addViewForDevice(String deviceAddress) {
        Log.d(TAG, "addViewForDevice: ");
        PeakToPeakView pk2pkView = new PeakToPeakView(viewToPopulate.getContext());
        pk2pkView.requestLayout();
        viewToPopulate.addView(pk2pkView);
        deviceToViewMap.put(deviceAddress, pk2pkView);
    }
}
