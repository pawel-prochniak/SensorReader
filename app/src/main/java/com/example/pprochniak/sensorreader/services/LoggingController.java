package com.example.pprochniak.sensorreader.services;

import android.content.Context;
import android.util.Log;

import com.example.pprochniak.sensorreader.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Henny on 2017-07-25.
 */

public class LoggingController implements CharacteristicController {
    private static final String TAG = "LoggingController";

    private Context context;
    private HashMap<String, File> deviceToFileMap = new HashMap<>();
    private HashMap<String, HashMap<String, List<Float>>> deviceToAxisDataMap = new HashMap<>();

    public LoggingController(Context context) {
        this.context = context;
    }

    @Override
    public void addDevice(String deviceAddress, int[] colorArray) {
        createFileMapForDevice(deviceAddress);
        createAxisDataListsForDevice(deviceAddress);
    }

    @Override
    public void addValue(String deviceAddress, float val, @SignalProcessor.AXIS String axis) {
        addValueToAxisData(deviceAddress, axis, val);
    }

    public void saveLogs() {
        Set<String> devices = deviceToFileMap.keySet();
        for (String deviceAddress : devices) {
            File file = deviceToFileMap.get(deviceAddress);
            String log = getLogMessage(deviceAddress);
            Logger.saveMessageToFile(context, file, log);
        }
    }

    private String getLogMessage(String deviceAddress) {
        StringBuilder logBuilder = new StringBuilder();
        HashMap<String, List<Float>> axisLogs = deviceToAxisDataMap.get(deviceAddress);
        if (axisLogs == null) {
            Log.e(TAG, "getLogMessage: no logs for device found");
            return null;
        }
        for (String key : axisLogs.keySet()) {
            logBuilder.append(key);
            logBuilder.append("  ");
            List<Float> floatList = axisLogs.get(key);
            for (Float val : floatList) {
                logBuilder.append(String.valueOf(val));
                logBuilder.append("  ");
            }
        }
        return logBuilder.toString();
    }

    private void addValueToAxisData(String deviceAddress, @SignalProcessor.AXIS String axis, float val) {
        HashMap<String, List<Float>> deviceAxisData = deviceToAxisDataMap.get(deviceAddress);
        if (deviceAxisData == null) {
            Log.e(TAG, "addValueToAxisData: device not added to controller");
            return;
        }
        deviceAxisData.get(axis).add(val);
    }

    private void createAxisDataListsForDevice(String deviceAddress) {
        HashMap<String, List<Float>> axisToDataMap = new HashMap<>();

        axisToDataMap.put(SignalProcessor.X, new ArrayList<>());
        axisToDataMap.put(SignalProcessor.Y, new ArrayList<>());
        axisToDataMap.put(SignalProcessor.Z, new ArrayList<>());

        deviceToAxisDataMap.put(deviceAddress, axisToDataMap);
    }

    private void createFileMapForDevice(String deviceAddress) {
        File deviceLogFile = Logger.createSignalLogFile(context, deviceAddress);
        deviceToFileMap.put(deviceAddress, deviceLogFile);
    }
}
