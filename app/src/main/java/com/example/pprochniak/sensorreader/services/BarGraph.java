package com.example.pprochniak.sensorreader.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Henny on 2017-07-22.
 */

public class BarGraph extends LinearLayout {
    private TextView titleView, valueView;
    private GraphView graphView;
    private BarGraphSeries<DataPoint> series;

    private static final double BAR_POSITION = 0.0;

    public BarGraph(Context context) {
        super(context);
        initializeViews(context);
    }

    public BarGraph(Context context, @Nullable AttributeSet attrs) {
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

        graphView = (GraphView) this
                .findViewById(R.id.bar_graph);

        series = new BarGraphSeries<>();
        series.appendData(new DataPoint(0.0, 0.0), false, 1);

        graphView.addSeries(series);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        titleView = (TextView) this
                .findViewById(R.id.bar_title);

        valueView =(TextView) this
                .findViewById(R.id.bar_value);
    }

    public void setTitle(String title) {
        this.titleView.setText(title);
    }

    public void setValue(Float value) {
        this.valueView.setText(String.valueOf(value));
        series.appendData(new DataPoint(BAR_POSITION, value), false, 1);
    }

    public void setGraphColor(int color) {
        series.setColor(color);
    }


}
