package com.example.pprochniak.sensorreader.settings;

import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.pprochniak.sensorreader.R;

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

    @AfterViews
    void afterViews() {
        sharedPreferencesController = new SharedPreferencesController(getContext());

        setSampleSizeState();
        setRealTimePlottingState();
        setContinuousPlottingState();
        setRmsSampleSizeState();

        setRealTimePlottingOnClick();
    }

    @Override
    public void onPause() {
        saveSampleSizeSetting();
        saveRealTimePlottingSetting();
        saveContinuousPlottingSetting();
        saveRmsSampleSizeSetting();

        super.onPause();
    }

    private void setRealTimePlottingOnClick() {
        realTimePlottingCheckBox.setOnClickListener((v) -> {
            boolean checked = ((CheckBox) v).isChecked();
            enableContinuousPlottingCheckBox(checked);
        });
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
}
