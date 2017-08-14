package com.example.pprochniak.sensorreader.settings;

import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Henny on 2017-07-09.
 */

@EFragment(R.layout.settings_fragment)
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private SharedPreferencesController sharedPreferencesController;

    @ViewById(R.id.collected_sample_size) EditText sampleSizeEditText;
    @ViewById(R.id.real_time_plotting_check_box) CheckBox realTimePlottingCheckBox;
    @ViewById(R.id.continuous_plotting_check_box) CheckBox continuousPlottingCheckBox;
    @ViewById(R.id.continuous_plotting_layout) View continuousPlottingLayout;
    @ViewById(R.id.rms_sample_size) EditText rmsSampleSizeEditText;
    @ViewById(R.id.time_series_filtering_check_box) CheckBox timeSeriesFilteringCheckBox;
    @ViewById(R.id.rms_filtering_check_box) CheckBox rmsFilteringCheckBox;
    @ViewById(R.id.filter_weights_x) Button xFilterWeights;
    @ViewById(R.id.filter_weights_y) Button yFilterWeights;
    @ViewById(R.id.filter_weights_z) Button zFilterWeights;

    @AfterViews
    void afterViews() {
        sharedPreferencesController = new SharedPreferencesController(getContext());

        setViewStates();
        setRealTimePlottingOnClick();
    }

    @Override
    public void onPause() {
        saveSettings();

        super.onPause();
    }

    private void setRealTimePlottingOnClick() {
        realTimePlottingCheckBox.setOnClickListener((v) -> {
            boolean checked = ((CheckBox) v).isChecked();
            enableContinuousPlottingCheckBox(checked);
        });
    }

    private void setViewStates() {
        setSampleSizeState();
        setRealTimePlottingState();
        setContinuousPlottingState();
        setRmsSampleSizeState();
        setTimeSeriesFilteringCheckBox();
        setRmsFilteringCheckBox();
        setFilteringButtons();
    }

    private void saveSettings() {
        saveSampleSizeSetting();
        saveRealTimePlottingSetting();
        saveContinuousPlottingSetting();
        saveRmsSampleSizeSetting();
        saveRmsFilteringSetting();
        saveTimeSeriesFilteringSetting();
    }

    private void enableContinuousPlottingCheckBox(boolean isEnabled) {
        continuousPlottingCheckBox.setEnabled(isEnabled);
    }

    private void setSampleSizeState() {
        sampleSizeEditText.setText(String.valueOf(sharedPreferencesController.getCollectedSampleSize()));
    }

    private void setRealTimePlottingState() {
        realTimePlottingCheckBox.setChecked(sharedPreferencesController.getRealTimePlotting());
    }

    private void setContinuousPlottingState() {
        boolean realTimePlottingEnabled = sharedPreferencesController.getRealTimePlotting();
        enableContinuousPlottingCheckBox(realTimePlottingEnabled);
        continuousPlottingCheckBox.setChecked(sharedPreferencesController.getContinuousPlotting());
    }

    private void setRmsSampleSizeState() {
        rmsSampleSizeEditText.setText(String.valueOf(sharedPreferencesController.getRmsSampleSize()));
    }

    private void setRmsFilteringCheckBox() {
        rmsFilteringCheckBox.setChecked(sharedPreferencesController.getRmsFilteringFlag());
    }

    private void setTimeSeriesFilteringCheckBox() {
        timeSeriesFilteringCheckBox.setChecked(sharedPreferencesController.getTimeSeriesFilteringFlag());
    }

    private void saveRmsFilteringSetting() {
        sharedPreferencesController.saveRmsFilteringFlag(rmsFilteringCheckBox.isChecked());
    }

    private void saveTimeSeriesFilteringSetting() {
        sharedPreferencesController.saveTimeSeriesFilteringFlag(timeSeriesFilteringCheckBox.isChecked());
    }

    private void saveSampleSizeSetting() {
        Editable editTextEditable = sampleSizeEditText.getText();
        if (editTextEditable.length() > 0) {
            String editTextValue = editTextEditable.toString();
            sharedPreferencesController.saveCollectedSampleSize(Integer.valueOf(editTextValue));
            Log.d(TAG, "onPause: saving sample size: " + editTextValue);
        }
    }

    private void saveContinuousPlottingSetting() {
        if (continuousPlottingCheckBox.isEnabled()) {
            sharedPreferencesController.saveContinuousPlotting(continuousPlottingCheckBox.isChecked());
        }
    }

    private void saveRealTimePlottingSetting() {
        sharedPreferencesController.saveRealTimePlotting(realTimePlottingCheckBox.isChecked());
    }

    private void saveRmsSampleSizeSetting() {
        Editable editTextEditable = rmsSampleSizeEditText.getText();
        if (editTextEditable.length() > 0) {
            String editTextValue = editTextEditable.toString();
            sharedPreferencesController.saveRmsSampleSize(Integer.valueOf(editTextValue));
        }
    }

    private void setFilteringButtons() {
        xFilterWeights.setOnClickListener((v) -> new FilterWeightsDialog(getContext(), SignalProcessor.X).show());
        yFilterWeights.setOnClickListener((v) -> new FilterWeightsDialog(getContext(), SignalProcessor.Y).show());
        zFilterWeights.setOnClickListener((v) -> new FilterWeightsDialog(getContext(), SignalProcessor.Z).show());
    }
}
