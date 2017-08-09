package com.example.pprochniak.sensorreader.services;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringDef;
import android.util.Log;

import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Constants;

import org.androidannotations.annotations.EBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Henny on 2017-07-23.
 */

@EBean(scope = EBean.Scope.Singleton)
public class SignalProcessor {
    private static final String TAG = "SignalProcessor";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({X, Y, Z})
    public @interface AXIS {
    }

    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    private static final long CONNECTION_DELAY_PERIOD = 100;

    private int baseColors[] = {Color.GREEN, Color.BLUE, Color.RED, Color.CYAN, Color.MAGENTA};

    private List<String> devices = new ArrayList<>();

    private TimeSeriesPlotController timeSeriesPlotController;
    private RmsPlotController rmsPlotController;
    private ReceivingSpeedController speedController;
    private PeakAmplitudeController peakAmplitudeController;
    private LoggingController loggingController;

    private List<CharacteristicController> activePlotControllers = new ArrayList<>();

    public void attachGraphsFragment(GraphsFragment fragment) {
        Log.d(TAG, "attachGraphsFragment");
        timeSeriesPlotController = new TimeSeriesPlotController(fragment.graphView);
        rmsPlotController = new RmsPlotController(fragment.xSingleBarGraph, fragment.ySingleBarGraph, fragment.zSingleBarGraph);
        speedController = new ReceivingSpeedController(fragment.getContext(), fragment.receivingSpeedView);
        peakAmplitudeController = new PeakAmplitudeController(fragment.peakToPeakLayout);
        loggingController = new LoggingController(fragment.getContext().getApplicationContext());
        activePlotControllers.add(timeSeriesPlotController);
        activePlotControllers.add(rmsPlotController);
        activePlotControllers.add(speedController);
        activePlotControllers.add(peakAmplitudeController);
        activePlotControllers.add(loggingController);
        initPlotsForDevices();
    }

    public void clearControllers() {
        activePlotControllers.clear();
    }

    public void connectToAllServices() {
        if (!checkIfAllServicesAreDiscovered()) {
            Log.d(TAG, "Not all devices' services are discovered");
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(BluetoothLeService::discoverAllServices,
                    CONNECTION_DELAY_PERIOD);
        }
    }


    public void receiveValueAndAppendPoint(Bundle extras) {
        float receivedValue;
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        String axis;

        if (extras.containsKey(Constants.EXTRA_ACC_X_VALUE)) {
            axis = SignalProcessor.X;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_X_VALUE);
        } else if (extras.containsKey(Constants.EXTRA_ACC_Y_VALUE)) {
            axis = SignalProcessor.Y;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Y_VALUE);
        } else if (extras.containsKey(Constants.EXTRA_ACC_Z_VALUE)) {
            axis = SignalProcessor.Z;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Z_VALUE);
        } else {
            Log.e(TAG, "receiveValueAndAppendPoint: unknown axis");
            return;
        }

        addValueToAllActivePlots(deviceAddress, receivedValue, axis);
    }

    public void addDevice(String deviceAddress) {
        devices.add(deviceAddress);
    }

    private void initPlotsForDevices() {
        for (String address : devices) {
            int baseColor = baseColors[devices.size() % baseColors.length];
            int[] graphColors = getAnalogousColors(baseColor);
            addDeviceToAllActivePlots(address, graphColors);
        }
    }

    private void addDeviceToAllActivePlots(String deviceAddress, int[] graphColors) {
        for (CharacteristicController controller : activePlotControllers) {
            controller.addDevice(deviceAddress, graphColors);
        }
    }

    private void addValueToAllActivePlots(String deviceAddress, float val, @AXIS String axis) {
        for (CharacteristicController controller : activePlotControllers) {
            controller.addValue(deviceAddress, val, axis);
        }
    }

    private boolean checkIfAllServicesAreDiscovered() {
        Set<String> bleServiceConnectedDevices = BluetoothLeService.getConnectedGattServices().keySet();
        for (String address : bleServiceConnectedDevices) {
            if (!devices.contains(address)) return false;
        }
        return true;
    }

    private int[] getAnalogousColors(int baseColor) {
        int colorArray[] = new int[3];
        float hsv[] = new float[3];
        Color.colorToHSV(baseColor, hsv);

        float h = hsv[0];
        if (h == 0) h = 360;
        float h1 = (h + 45) % 360;
        float h2 = (h - 45) % 360;
        float hsv1[] = {h1, hsv[1], hsv[2]};
        float hsv2[] = {h2, hsv[1], hsv[2]};

        colorArray[0] = baseColor;
        colorArray[1] = Color.HSVToColor(hsv1);
        colorArray[2] = Color.HSVToColor(hsv2);
        return colorArray;
    }

    public void saveLogs() {
        if (loggingController != null) {
            loggingController.saveLogs();
        }
    }


}
