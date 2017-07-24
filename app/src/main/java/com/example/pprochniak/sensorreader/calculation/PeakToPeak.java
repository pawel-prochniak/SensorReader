package com.example.pprochniak.sensorreader.calculation;

/**
 * Created by Henny on 2017-07-24.
 */

public class PeakToPeak {
    private float max;
    private float min;

    public boolean putNewValueAndCheckIfChanged(float newVal) {
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

    public float getPk2Pk() {
        return max - min;
    }
}
