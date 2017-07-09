package com.example.pprochniak.sensorreader.settings;

import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.utils.SharedPreferencesController;

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

    @AfterViews
    void afterViews() {
        sharedPreferencesController = new SharedPreferencesController(getContext());

        sampleSizeEditText.setText(String.valueOf(sharedPreferencesController.getCollectedSampleSize()));
    }

    @Override
    public void onPause() {
        Editable editTextEditable = sampleSizeEditText.getText();
        if (editTextEditable.length() > 0) {
            String editTextValue = editTextEditable.toString();
            sharedPreferencesController.saveCollectedSampleSize(Integer.valueOf(editTextValue));
            Log.d(TAG, "onPause: saving sample size: " + editTextValue);
        }
        super.onPause();
    }
}
