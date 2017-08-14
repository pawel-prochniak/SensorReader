package com.example.pprochniak.sensorreader.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;
import com.example.pprochniak.sensorreader.utils.FileIO;

import java.io.FileNotFoundException;

/**
 * Created by Henny on 2017-08-14.
 */

public class FilterWeightsDialog extends AlertDialog.Builder {
    private static final String TAG = "FilterWeightsDialog";

    private static final int EDIT_TEXT_PADDING = 16;
    
    public FilterWeightsDialog(@NonNull Context context, @SignalProcessor.AXIS String axis) {
        super(context);
        setDialog(axis);
    }

    private void setDialog(@SignalProcessor.AXIS String axis) {
        final EditText edittext = new EditText(getContext());
        edittext.setPadding(EDIT_TEXT_PADDING, EDIT_TEXT_PADDING, EDIT_TEXT_PADDING, EDIT_TEXT_PADDING);
        edittext.setText(getWeightsFromFile(axis));
        setTitle("Filter weights for axis "+axis);
        setMessage("Enter weights in separate lines with no other separator");
        setView(edittext);
        setPositiveButton("Apply", (v, __) -> {
            Log.d(TAG, "setDialog: apply");
            FileIO.saveWeightsToFile(getContext(), axis, edittext.getText().toString());
        });
        setNegativeButton("Cancel", (v, __) -> {
            Log.d(TAG, "setDialog: cancel");
        });
    }

    private String getWeightsFromFile(@SignalProcessor.AXIS String axis) {
        try {
            double[] array = FileIO.getWeightsFromFile(getContext(), axis);
            if (array == null || array.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            double val;
            for (double anArray : array) {
                val = anArray;
                sb.append(val);
                sb.append('\n');
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "setDialog: weights file not found");
            return "";
        }
    }


}
