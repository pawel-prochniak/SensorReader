package com.example.pprochniak.sensorreader.services;

/**
 * Created by Henny on 2017-07-23.
 */

public interface CharacteristicController {
    void addDevice(String deviceAddress, int[] colorArray);
    void addValue(String deviceAddress, float val, @PlotController.AXIS String axis);
}
