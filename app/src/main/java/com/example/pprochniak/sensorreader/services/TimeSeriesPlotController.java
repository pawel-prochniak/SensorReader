package com.example.pprochniak.sensorreader.services;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.util.Log;

import com.example.pprochniak.sensorreader.calculation.RootMeanSquare;
import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;


/**
 * Created by Henny on 2017-07-20.
 */

public class TimeSeriesPlotController {
    private static final String TAG = "TimeSeriesPlotControlle";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({X, Y, Z})
    public @interface AXIS {}
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    HashMap<String, HashMap<String, LineGraphSeries<DataPoint>>> mapOfSeries = new HashMap<>();

    long xTimestamp, yTimestamp, zTimestamp;

    private int SERIES_LENGTH; // how many data points will be collected for each axis
    private boolean realTimePlotting;
    private boolean continuousPlotting;
    private int baseColors[] = {Color.GREEN, Color.BLUE, Color.RED, Color.CYAN, Color.MAGENTA};

    private RootMeanSquare xRMS;

    private GraphsFragment fragment;
    private GraphView graphView;

    public TimeSeriesPlotController(GraphsFragment fragment) {
        this.fragment = fragment;
        this.graphView = fragment.graphView;
        getSavedProperties();
    }

    private void getSavedProperties() {
        SharedPreferencesController sharedPreferencesController = new SharedPreferencesController(graphView.getContext());
        SERIES_LENGTH = sharedPreferencesController.getCollectedSampleSize();
        realTimePlotting = sharedPreferencesController.getRealTimePlotting();
        continuousPlotting = sharedPreferencesController.getContinuousPlotting();
    }

    void addDevice(String deviceAddress) {
        if (!mapOfSeries.containsKey(deviceAddress)) {
            mapOfSeries.put(deviceAddress, addXYZSeriesForDevice(deviceAddress));
            xRMS = new RootMeanSquare(24);
        } else {
            Log.d(TAG, "addDevice: series for device already added");
        }
    }


    private HashMap<String, LineGraphSeries<DataPoint>> addXYZSeriesForDevice(String deviceAddress) {
        Log.d(TAG, "addXYZSeriesForDevice: "+deviceAddress);
        HashMap<String, LineGraphSeries<DataPoint>> seriesMap = new HashMap<>(3);
        seriesMap.put(X, new LineGraphSeries<>());
        seriesMap.put(Y, new LineGraphSeries<>());
        seriesMap.put(Z, new LineGraphSeries<>());

        int baseColor = baseColors[mapOfSeries.size() % baseColors.length];

        LineGraphSeries<DataPoint> xSeries = seriesMap.get(X);
        LineGraphSeries<DataPoint> ySeries = seriesMap.get(Y);
        LineGraphSeries<DataPoint> zSeries = seriesMap.get(Z);
        xSeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        ySeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        zSeries.appendData(new DataPoint(-1.0,0), false, SERIES_LENGTH);
        String formattedAddress = deviceAddress.substring(0, 4) + "...";
        xSeries.setTitle("X "+formattedAddress);
        ySeries.setTitle("Y "+formattedAddress);
        zSeries.setTitle("Z "+formattedAddress);
        int[] colorAccents = getAnalogousColors(baseColor);
        xSeries.setColor(colorAccents[0]);
        ySeries.setColor(colorAccents[1]);
        zSeries.setColor(colorAccents[2]);

        if (realTimePlotting) {
            graphView.addSeries(xSeries);
            graphView.addSeries(ySeries);
            graphView.addSeries(zSeries);
        }

        return seriesMap;
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

    void setGraphProperties() {
        graphView.getGridLabelRenderer().setLabelVerticalWidth(120);
        graphView.getLegendRenderer().setWidth(360);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(SERIES_LENGTH);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    void receiveValueAndAppendPoint(Bundle extras) {
        float receivedValue;
        double counter;
        String deviceAddress = extras.getString(Constants.DEVICE_ADDRESS);
        LineGraphSeries<DataPoint> series;
        HashMap<String, LineGraphSeries<DataPoint>> deviceSeriesMap = mapOfSeries.get(deviceAddress);

        if (deviceSeriesMap == null) {
            Log.e(TAG, "receiveValueAndAppendPoint: no device series found");
            return;
        }

        if (extras.containsKey(Constants.EXTRA_ACC_X_VALUE)) {
            series = deviceSeriesMap.get("X");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_X_VALUE);
            fragment.setXRms(xRMS.putAndCalculate(receivedValue));
            if (counter < 10) {
                Log.d(TAG, "Appending x: ("+counter+", "+receivedValue+")");
            }
            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) xTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: x series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - xTimestamp);
                    fragment.setReceivingSpeed(speed, X);
                }
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Y_VALUE)) {
            series = deviceSeriesMap.get("Y");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Y_VALUE);

            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) yTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: y series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - yTimestamp);
                    fragment.setReceivingSpeed(speed, Y);
                }
            }
        }
        else if (extras.containsKey(Constants.EXTRA_ACC_Z_VALUE)) {
            series = deviceSeriesMap.get("Z");
            counter = series.getHighestValueX() + 1.0d;
            receivedValue = extras.getFloat(Constants.EXTRA_ACC_Z_VALUE);

            if (counter <= SERIES_LENGTH) {
                if (counter - 1.0d == 0) zTimestamp = System.currentTimeMillis();
                if (counter == SERIES_LENGTH) {
                    Log.d(TAG, "receiveValueAndAppendPoint: z series finished");
                    float speed = SERIES_LENGTH * 1000 / (System.currentTimeMillis() - zTimestamp);
                    fragment.setReceivingSpeed(speed, Z);
                }
            }
        }
        else {
            Log.d(TAG, "No known characteristic read");
            return; // unknown data received
        }

        if (continuousPlotting) {
            series.appendData(new DataPoint(counter, receivedValue), true, SERIES_LENGTH);
        } else {
            if (counter <= SERIES_LENGTH) {
                series.appendData(new DataPoint(counter, receivedValue), true, SERIES_LENGTH);
            } else {
                if (!realTimePlotting) graphView.addSeries(series);
            }
        }

    }


}
