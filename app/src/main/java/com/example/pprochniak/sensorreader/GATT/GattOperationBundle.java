package com.example.pprochniak.sensorreader.GATT;

import com.example.pprochniak.sensorreader.GATT.operations.GattOperation;

import java.util.ArrayList;

/**
 * Created by henny on 22.06.2017
 */

public class GattOperationBundle {
    final ArrayList<GattOperation> operations;

    public GattOperationBundle() {
        operations = new ArrayList<>();
    }

    public void addOperation(GattOperation operation) {
        operations.add(operation);
        operation.setBundle(this);
    }

    public ArrayList<GattOperation> getOperations() {
        return operations;
    }
}
