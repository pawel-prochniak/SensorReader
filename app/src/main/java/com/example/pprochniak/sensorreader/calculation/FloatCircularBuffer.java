package com.example.pprochniak.sensorreader.calculation;

import android.util.Log;

import java.nio.BufferUnderflowException;

/**
 * Created by Henny on 2017-07-20.
 */

public class FloatCircularBuffer {
//    private float[] buffer;
//    private int size;
//
//    public FloatCircularBuffer(int n) {
//        size = n;
//        buffer = new float[n];
//    }
//
//    public float getRms() {
//        float sum = 0;
//        float currentVal;
//        int headToTails = head - tail;
//        int numSamples = headToTails < size ? headToTails : size;
//        if (numSamples < 1) throw new BufferUnderflowException();
//        for (int i = 0; i < numSamples; i++) {
//            currentVal = buffer[i];
//            sum += currentVal * currentVal;
//        }
//        Log.d("FloatCircularBuffer", toString());
//        return (float) Math.sqrt(sum/numSamples);
//    }
}
