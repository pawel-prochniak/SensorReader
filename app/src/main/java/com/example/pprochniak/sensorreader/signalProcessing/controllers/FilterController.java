package com.example.pprochniak.sensorreader.signalProcessing.controllers;

import android.content.Context;
import android.util.Log;

import com.example.pprochniak.sensorreader.calculation.FRF;
import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;
import com.example.pprochniak.sensorreader.utils.FileIO;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Henny on 2017-08-13.
 */

public class FilterController implements CharacteristicController {
    private static final String TAG = "FilterController";
    private HashMap<String, HashMap<String, FRF>> deviceMap = new HashMap<>();

    private FilterReceiver receiver;
    private double[] weights_x, weights_y, weights_z;
    private static final double[] EMPTY_WEIGHTS = {1};

    public FilterController(Context context, FilterReceiver receiver) {
        getWeights(context);
        this.receiver = receiver;
    }

    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        if (deviceMap.containsKey(deviceAddress)) return;
        deviceMap.put(deviceAddress, getAxisToFRFMap());
    }

    @Override
    public void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis) {
        HashMap<String, FRF> axisToFilter = deviceMap.get(deviceAddress);

        if (axisToFilter == null) {
            Log.e(TAG, "addValue: no axis to filter map found");
            return;
        }

        FRF filter = axisToFilter.get(axis);
        if (filter == null) {
            Log.e(TAG, "addValue: no filter for axis found");
            return;
        }
        receiver.receiveFilterValue(deviceAddress, filter.putAndCalculate(val), axis);
    }

    private HashMap<String, FRF> getAxisToFRFMap() {
        HashMap<String, FRF> axisToFRF = new HashMap<>();
        axisToFRF.put(SignalProcessor.X, new FRF(weights_x));
        axisToFRF.put(SignalProcessor.Y, new FRF(weights_y));
        axisToFRF.put(SignalProcessor.Z, new FRF(weights_z));
        return axisToFRF;
    }

    private void getWeights(Context context) {
        weights_x = getSingleAxisWeights(context, SignalProcessor.X);
        weights_y = getSingleAxisWeights(context, SignalProcessor.Y);
        weights_z = getSingleAxisWeights(context, SignalProcessor.Z);
    }

    private double[] getSingleAxisWeights(Context context, @SignalProcessor.AXIS String axis){
        double[] weights;
        try {
            weights = FileIO.getWeightsFromFile(context, axis);
        } catch (ClassCastException | IOException e) {
            weights = EMPTY_WEIGHTS;
        }
        return weights;
    }



    public interface FilterReceiver {
        void receiveFilterValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis);
    }
}
