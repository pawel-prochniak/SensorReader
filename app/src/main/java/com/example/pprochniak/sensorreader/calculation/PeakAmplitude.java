package com.example.pprochniak.sensorreader.calculation;

/**
 * Created by Henny on 2017-07-24.
 */

public class PeakAmplitude {
    private Float max;
    private Float min;

    public boolean putNewValueAndCheckIfChanged(float newVal) {
        if (max == null || min == null) {
            if (max == null) {
                max = newVal;
            }
            if (min == null) {
                min = newVal;
            }
            return true;
        } else {
            if (newVal < min) {
                min = newVal;
                return true;
            } else if (newVal > max) {
                max = newVal;
                return true;
            } else {
                return false;
            }
        }
    }

    public float getPkAmplitude() {
        return (max - min)/2;
    }
}
