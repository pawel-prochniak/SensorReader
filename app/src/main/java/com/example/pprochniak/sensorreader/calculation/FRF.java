package com.example.pprochniak.sensorreader.calculation;


/**
 * Created by Henny on 2017-08-13.
 */

public class FRF {
    private double[] weights;
    private Queue buffer;

    public FRF(double[] weights) {
        this.weights = weights;
        this.buffer = new Queue(weights.length);
    }

    public float putAndCalculate(float val) {
        buffer.insert(val);
        if (!buffer.isFull()) return val;
        float[] delayed = buffer.getAllSorted();
        float output = 0;
        for (int i = 0; i < delayed.length; i++) {
            output += weights[i] * delayed[i];
        }
        return output;
    }
}
