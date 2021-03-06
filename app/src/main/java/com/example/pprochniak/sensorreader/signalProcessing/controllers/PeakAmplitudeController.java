package com.example.pprochniak.sensorreader.signalProcessing.controllers;

import android.util.Log;
import android.widget.LinearLayout;

import com.example.pprochniak.sensorreader.calculation.PeakAmplitude;
import com.example.pprochniak.sensorreader.signalProcessing.PeakAmplitudeView;
import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;

import java.util.HashMap;

/**
 * Created by Henny on 2017-07-24.
 */

public class PeakAmplitudeController implements CharacteristicController {
    private static final String TAG = "PeakAmplitudeController";

    private HashMap<String, HashMap<String, PeakAmplitude>> deviceToPeakValues = new HashMap<>();
    private HashMap<String, PeakAmplitudeView> deviceToViewMap = new HashMap<>();

    private LinearLayout viewToPopulate;

    public PeakAmplitudeController(LinearLayout viewToPopulate) {
        this.viewToPopulate = viewToPopulate;
    }

    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        deviceToPeakValues.put(deviceAddress, getAxisToPeakToPeakMap());
        addViewForDevice(deviceAddress);
    }

    @Override
    public void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis) {
        HashMap<String, PeakAmplitude> axisToPeakToPeak = deviceToPeakValues.get(deviceAddress);
        if (axisToPeakToPeak == null) {
            Log.e(TAG, "addValue: no map for this device found");
            return;
        }
        PeakAmplitude pk2pk = axisToPeakToPeak.get(axis);
        if (pk2pk.putNewValueAndCheckIfChanged(val)) {
            float newVal = pk2pk.getPkAmplitude();
            String newValText = String.format("%.3f", newVal);
            PeakAmplitudeView view = deviceToViewMap.get(deviceAddress);
            view.setValue(newValText, axis);
        }

    }

    private HashMap<String, PeakAmplitude> getAxisToPeakToPeakMap() {
        HashMap<String, PeakAmplitude> axisToPkPk = new HashMap<>();
        axisToPkPk.put(SignalProcessor.X, new PeakAmplitude());
        axisToPkPk.put(SignalProcessor.Y, new PeakAmplitude());
        axisToPkPk.put(SignalProcessor.Z, new PeakAmplitude());
        return axisToPkPk;
    }

    private void addViewForDevice(String deviceAddress) {
        Log.d(TAG, "addViewForDevice: ");
        PeakAmplitudeView pk2pkView = new PeakAmplitudeView(viewToPopulate.getContext());
        pk2pkView.requestLayout();
        viewToPopulate.addView(pk2pkView);
        deviceToViewMap.put(deviceAddress, pk2pkView);
    }
}
