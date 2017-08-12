package com.example.pprochniak.sensorreader.services;

import android.util.Log;

import com.example.pprochniak.sensorreader.settings.SharedPreferencesController;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.example.pprochniak.sensorreader.services.SignalProcessor.*;


/**
 * Created by Henny on 2017-07-20.
 */

public class TimeSeriesPlotController implements CharacteristicController {
    private static final String TAG = "TimeSeriesPlotControlle";

    private HashMap<String, HashMap<String, LineGraphSeries<DataPoint>>> mapOfSeries = new HashMap<>();
    private HashMap<String, int[]> colorMap = new HashMap<>();
    private HashMap<String, List<String>> completedSeries = new HashMap<>();

    private int SERIES_LENGTH; // how many data points will be collected for each axis
    private boolean realTimePlotting;
    private boolean continuousPlotting;
    private boolean disabledPlotting;

    private GraphView graphView;

    public TimeSeriesPlotController(GraphView graphView) {
        this.graphView = graphView;

        getSavedProperties();
        setGraphProperties();
    }

    private void getSavedProperties() {
        SharedPreferencesController sharedPreferencesController = new SharedPreferencesController(graphView.getContext());
        SERIES_LENGTH = sharedPreferencesController.getCollectedSampleSize();
        realTimePlotting = sharedPreferencesController.getRealTimePlotting();
        continuousPlotting = sharedPreferencesController.getContinuousPlotting();
    }

    @Override
    public void addDevice(String deviceAddress, int[] graphColors) {
        if (!mapOfSeries.containsKey(deviceAddress)) {
            putNewSeriesIntoMap(deviceAddress, graphColors);
        } else {
            Log.d(TAG, "addDevice: series for device already added");
        }
    }

    public void clearGraph() {
        disablePlot();
        graphView.removeAllSeries();
        Set<String> devices = mapOfSeries.keySet();
        for (String deviceAddress : devices) {
            putNewSeriesIntoMap(deviceAddress, colorMap.get(deviceAddress));
        }
        enablePlot();
    }

    public void enablePlot() {
        disabledPlotting = false;
    }

    public void disablePlot() {
        disabledPlotting = true;
    }

    private void putNewSeriesIntoMap(String deviceAddress, int[] graphColors) {
        mapOfSeries.put(deviceAddress, addXYZSeriesForDevice(deviceAddress, graphColors));
    }

    private HashMap<String, LineGraphSeries<DataPoint>> addXYZSeriesForDevice(String deviceAddress, int[] graphColors) {
        Log.d(TAG, "addXYZSeriesForDevice: "+deviceAddress);
        HashMap<String, LineGraphSeries<DataPoint>> seriesMap = new HashMap<>(3);
        seriesMap.put(X, new LineGraphSeries<>());
        seriesMap.put(Y, new LineGraphSeries<>());
        seriesMap.put(Z, new LineGraphSeries<>());

        colorMap.put(deviceAddress, graphColors);

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

        xSeries.setColor(graphColors[0]);
        ySeries.setColor(graphColors[1]);
        zSeries.setColor(graphColors[2]);

        if (realTimePlotting) {
            graphView.addSeries(xSeries);
            graphView.addSeries(ySeries);
            graphView.addSeries(zSeries);
        }

        return seriesMap;
    }

    void setGraphProperties() {
        graphView.getGridLabelRenderer().setLabelVerticalWidth(120);
        graphView.getLegendRenderer().setWidth(240);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(SERIES_LENGTH);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    @Override
    public void addValue(String deviceAddress, float val, @AXIS String axis) {
        if (disabledPlotting) return;
        double counter;
        HashMap<String, LineGraphSeries<DataPoint>> deviceSeriesMap = mapOfSeries.get(deviceAddress);
        if (deviceSeriesMap == null) {
            Log.e(TAG, "addValue: unknown device, no series found");
            return;
        }
        LineGraphSeries<DataPoint> series = deviceSeriesMap.get(axis);

        counter = series.getHighestValueX() + 1.0d;

        if (continuousPlotting || counter <= SERIES_LENGTH) {
            series.appendData(new DataPoint(counter, val), true, SERIES_LENGTH);
        }
        if (!realTimePlotting)  {
            if (!completedSeries.containsKey(deviceAddress)) {
                completedSeries.put(deviceAddress, new ArrayList<>());
            }
            if (!completedSeries.get(deviceAddress).contains(axis) && counter == SERIES_LENGTH) {
                graphView.addSeries(series);
                completedSeries.get(deviceAddress).add(axis);
            }
        }
    }

}
