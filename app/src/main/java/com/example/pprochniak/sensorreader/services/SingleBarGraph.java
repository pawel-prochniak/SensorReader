package com.example.pprochniak.sensorreader.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Henny on 2017-07-22.
 */

public class SingleBarGraph extends LinearLayout {
    private TextView titleView, valueView;
    private GraphView graphView;
    private BarGraphSeries<DataPoint> series;

    private static final double BAR_POSITION = 0.5;

    public SingleBarGraph(Context context) {
        super(context);
        initializeViews(context);
    }

    public SingleBarGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.single_bar, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        graphView = (GraphView) this.findViewById(R.id.bar_graph);
        titleView = (TextView) this.findViewById(R.id.bar_title);
        valueView =(TextView) this.findViewById(R.id.bar_value);

        setGraphProperties();
    }

    private void setGraphProperties() {
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(1);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        series = new BarGraphSeries<>();
        series.setDataWidth(0.7);

        graphView.addSeries(series);
    }

    public void setTitle(String title) {
        this.titleView.setText(title);
    }

    public void setValue(Float value) {
        String valStr = String.format("%.3f", value);
        this.valueView.setText(valStr);
        series.appendData(new DataPoint(BAR_POSITION, value), false, 1);
    }

    public void setGraphColor(int color) {
        series.setColor(color);
    }

}
