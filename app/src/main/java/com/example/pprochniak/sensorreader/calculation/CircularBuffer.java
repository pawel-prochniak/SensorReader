package com.example.pprochniak.sensorreader.calculation;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * Created by Henny on 2017-07-20.
 */

class CircularBuffer {
    private Float data[];
    private int head;
    private int tail;

    public CircularBuffer(Integer number) {
        data = new Float[number];
        head = 0;
        tail = 0;
    }

    public boolean store(Float value) {
        if (!bufferFull()) {
            data[tail++] = value;
            if (tail == data.length) {
                tail = 0;
            }
            return true;
        } else {
            throw new BufferOverflowException();
        }
    }

    public Float read() {
        if (head != tail) {
            float value = data[head++];
            if (head == data.length) {
                head = 0;
            }
            return value;
        } else {
            throw new BufferUnderflowException();
        }
    }

    public Float[] getValues() {
        return data;
    }

    public boolean bufferFull() {
        if (tail + 1 == head) {
            return true;
        }
        if (tail == (data.length - 1) && head == 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CircularBuffer: length: " + data.length + ", head: " + head+", tail: " + tail;
    }
}
