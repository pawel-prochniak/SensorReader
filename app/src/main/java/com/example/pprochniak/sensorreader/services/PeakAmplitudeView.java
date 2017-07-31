package com.example.pprochniak.sensorreader.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.R;

import java.util.HashMap;

/**
 * Created by Henny on 2017-07-24.
 */

public class PeakAmplitudeView extends LinearLayout {
    private TextView xTextView, yTextView, zTextView;

    private HashMap<String, TextView> axisToViewMap = new HashMap<>();

    public PeakAmplitudeView(Context context) {
        super(context);
        initializeViews(context);
    }

    public PeakAmplitudeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.peak_amplitude_view, this);
        setGravity(Gravity.CENTER);

        xTextView = (TextView) this.findViewById(R.id.x_pk2pk);
        yTextView = (TextView) this.findViewById(R.id.y_pk2pk);
        zTextView =(TextView) this.findViewById(R.id.z_pk2pk);

        setUpAxisToViewMap();
    }


    private void setUpAxisToViewMap() {
        axisToViewMap.put(PlotController.X, xTextView);
        axisToViewMap.put(PlotController.Y, yTextView);
        axisToViewMap.put(PlotController.Z, zTextView);

    }

    public void setValue(String stringVal, @PlotController.AXIS String axis) {
        axisToViewMap.get(axis).setText(stringVal);
    }
}
