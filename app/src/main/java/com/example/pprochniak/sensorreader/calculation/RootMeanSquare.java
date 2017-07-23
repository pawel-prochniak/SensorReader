package com.example.pprochniak.sensorreader.calculation;


import android.util.Log;

/**
 * Created by Henny on 2017-07-20.
 */

public class RootMeanSquare {
    private static final String TAG = "RootMeanSquare";

    private int size;
    private CircularBuffer buffer;
    private Queue queue;

    public RootMeanSquare(int bufferSize) {
        this.size = bufferSize;
        this.buffer = new CircularBuffer(bufferSize);
        this.queue = new Queue(bufferSize);
    }

//    public float putAndCalculate(float val) {
//        float squareAve = 0;
//        buffer.store(val);
//        if (!buffer.bufferFull()) squareAve = (val * val);
//        else {
//            Log.d(TAG, "putAndCalculate: " + buffer.toString());
//            Float[] data = buffer.getValues();
//            for (int i = 0; i < data.length; i++) {
//                Log.d(TAG, "putAndCalculate: reading data[" + i + "]: " + data[i]);
//                float t = data[i];
//                squareAve += t * t;
//            }
//            squareAve = squareAve / data.length;
//            buffer.read();
//        }
//
//        return (float) Math.sqrt(squareAve);
//    }

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
