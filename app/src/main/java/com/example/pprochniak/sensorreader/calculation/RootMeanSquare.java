package com.example.pprochniak.sensorreader.calculation;


import android.util.Log;

/**
 * Created by Henny on 2017-07-20.
 */

public class RootMeanSquare {
    private static final String TAG = "RootMeanSquare";

    private Queue queue;

    public RootMeanSquare(int bufferSize) {
        this.queue = new Queue(bufferSize);
    }

    public float putAndCalculate(float val) {
        float squareSum = 0;
        queue.insert(val);
        float vals[] = queue.getAll();
        for (float single : vals) {
            squareSum += single*single;
        }
        float squareAve = squareSum / vals.length;
        return (float) Math.sqrt(squareAve);
    }

}
