package com.example.pprochniak.sensorreader.calculation;

import java.nio.BufferUnderflowException;
import java.util.Arrays;

/**
 * Created by Henny on 2017-07-23.
 */

public class Queue {
    private int qMaxSize;// max queue size
    private int fp = 0;  // front pointer
    private int rp = 0;  // rear pointer
    private int qs = 0;  // size of queue
    private float[] q;    // actual queue

    public Queue(int size) {
        qMaxSize = size;
        fp = 0;
        rp = 0;
        qs = 0;
        q = new float[qMaxSize];
    }

    public float delete() {
        if (!isEmpty()) {
            qs--;
            fp = (fp + 1)%qMaxSize;
            return q[fp];
        }
        else {
            throw new BufferUnderflowException();
        }
    }

    public void insert(float c) {
        if (isFull()) {
            delete();
        } else {
            qs++;
            rp = (rp + 1)%qMaxSize;
            q[rp] = c;
        }
    }

    public boolean isEmpty() {
        return qs == 0;
    }

    public boolean isFull() {
        return qs == qMaxSize;
    }

    public float[] getAll() {
        return Arrays.copyOf(q, qs);
    }
}
