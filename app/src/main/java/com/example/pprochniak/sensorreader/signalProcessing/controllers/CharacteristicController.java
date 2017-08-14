package com.example.pprochniak.sensorreader.signalProcessing.controllers;

import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;

/**
 * Created by Henny on 2017-07-23.
 */

public interface CharacteristicController {
    void addDevice(String deviceAddress, int[] colorArray);
    void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis);
}
