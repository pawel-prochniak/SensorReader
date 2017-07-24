package com.example.pprochniak.sensorreader.services;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.util.Log;

import com.example.pprochniak.sensorreader.utils.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henny on 2017-07-23.
 */

public class PlotController {
    private static final String TAG = "PlotController";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({X, Y, Z})
    public @interface AXIS {}

    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    private int baseColors[] = {Color.GREEN, Color.BLUE, Color.RED, Color.CYAN, Color.MAGENTA};

    private List<String> devices = new ArrayList<>();


    private TimeSeriesPlotController timeSeriesPlotController;
    private RmsPlotController rmsPlotController;
    private ReceivingSpeedController speedController;
    private PeakToPeakController peakToPeakController;

    private List<CharacteristicController> activePlotControllers = new ArrayList<>();

    private GraphsFragment fragment;

    public PlotController(GraphsFragment fragment) {
        this.fragment = fragment;
        timeSeriesPlotController = new TimeSeriesPlotController(fragment.graphView);
        rmsPlotController = new RmsPlotController(fragment.xSingleBarGraph, fragment.ySingleBarGraph, fragment.zSingleBarGraph);
        speedController = new ReceivingSpeedController(fragment.getContext(), fragment.receivingSpeedView);
        peakToPeakController = new PeakToPeakController(fragment.peakToPeakLayout);
        activePlotControllers.add(timeSeriesPlotController);
        activePlotControllers.add(rmsPlotController);
        activePlotControllers.add(speedController);
        activePlotControllers.add(peakToPeakController);
    }


    public void receiveValueAndAppendPoint(Bundle extras) {
        float receivedValue;
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        String axis;

        if (extras.containsKey(Constants.EXTRA_ACC_X_VALUE)) {
            axis = PlotController.X;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_X_VALUE);
        } else if (extras.containsKey(Constants.EXTRA_ACC_Y_VALUE)) {
            axis = PlotController.Y;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Y_VALUE);
        } else if (extras.containsKey(Constants.EXTRA_ACC_Z_VALUE)) {
            axis = PlotController.Z;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Z_VALUE);
        } else {
            Log.e(TAG, "receiveValueAndAppendPoint: unknown axis");
            return;
        }

        addValueToAllActivePlots(deviceAddress, receivedValue, axis);
    }


    public void addDevice(String deviceAddress) {
        devices.add(deviceAddress);
        int baseColor = baseColors[devices.size() % baseColors.length];
        int[] graphColors = getAnalogousColors(baseColor);
        addDeviceToAllActivePlots(deviceAddress, graphColors);
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


}
