package com.example.pprochniak.sensorreader.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.signalProcessing.SignalProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * Created by Henny on 2017-03-27.
 */

public class FileIO {
    private static final String TAG = "FileIO";
    private static File mDataLoggerDirectory;
    private static File mDataLoggerFile;
    private static File mDataLoggerOldFile;

    private static String baseDirectory = Environment.getExternalStorageDirectory()
            + File.separator;

    public static double[] getWeightsFromFile(Context context, @SignalProcessor.AXIS String axis) throws FileNotFoundException, ClassCastException {
        File weightsFile = getWeightsFile(context, axis);

        if (weightsFile == null || !weightsFile.exists()) {
            Log.d(TAG, "getWeightsFromFile: no weighs file exists");
            throw new FileNotFoundException();
        }

        InputStreamReader reader = new InputStreamReader(new FileInputStream(weightsFile));
        BufferedReader br = new BufferedReader(reader);
        ArrayList<Double> doublesRead = new ArrayList<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) break;
                doublesRead.add(Double.valueOf(line.replaceAll(",", " ")));
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, "getWeightsFromFile: ", e);
            return null;
        }
        Log.d(TAG, "getWeightsFromFile: found weights for axis: "+axis);
        Double[] weights = new Double[doublesRead.size()];
        doublesRead.toArray(weights);
        return castDoubleObjectsToPrimitives(weights);
    }

    public static void saveWeightsToFile(Context context, @SignalProcessor.AXIS String axis, String message) {
        File weightsFile = getWeightsFile(context, axis);

        try {
            if (weightsFile != null && weightsFile.exists()) {
                boolean isDeleted = weightsFile.delete();
                if (!isDeleted) return;
            }
            boolean isCreated = weightsFile.createNewFile();
            if (!isCreated) return;
        } catch (IOException e) {
            Log.e(TAG, "saveWeightsToFile: ", e);
        }

        saveMessageToFile(context, weightsFile, message);
    }

    public static File createSignalLogFile(Context context, String deviceAddress) {
        File file = null;
        File directory;
        try {
            directory = new File(getAppDirectory(context));

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "createDataLoggerFile: failed mkdirs");
                }
            }

            String filename = getSignalLogFilename(deviceAddress);

            file = new File(directory.getAbsoluteFile() + File.separator
                    + filename + context.getResources().getString(R.string.dl_file_extension));
            int file_suffix = 1;
            // File overriding check
            while (file.exists()) {
                String suffixedFilename = filename + "_" + String.valueOf(file_suffix++);
                file = new File(directory.getAbsoluteFile() + File.separator
                        + suffixedFilename + context.getResources().getString(R.string.dl_file_extension));
            }
            boolean isFileFound = file.createNewFile();
            Log.d(TAG, "creating new file, is success: "+isFileFound);
        } catch (IOException ioExc) {
            Log.e(TAG, "createSignalLogFile: ", ioExc);
        }
        return file;
    }

    public static String getSignalLogFilename(String deviceAddress) {
        String formattedDeviceAddress = deviceAddress.replace(":","-");
        String timestamp = Utils.GetDate();
        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(timestamp);
        filenameBuilder.append("_");
        filenameBuilder.append(formattedDeviceAddress);
        return filenameBuilder.toString();
    }

    public static File getWeightsFile(Context context, @SignalProcessor.AXIS String axis) {
        File directory = new File(getAppDirectory(context));
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "createDataLoggerFile: failed mkdirs");
                return null;
            }
        }
        File weightsFile = new File(directory.getAbsolutePath() + File.separator
                + context.getResources().getString(R.string.weights_filename)
                + axis
                + context.getResources().getString(R.string.dl_file_extension));
        return weightsFile;
    }

    public static void saveMessageToFile (Context context, File file, String message) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file, true),
                    "UTF-8");
            BufferedWriter fbw = new BufferedWriter(writer);
            fbw.write(message);
            fbw.newLine();
            fbw.flush();
            fbw.close();

            MediaScannerConnection.scanFile(context, new String[] {file.toString()}, null, null);

            Log.d(TAG, "saveMessageToFile: finished writing to file: "+file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[] castDoubleObjectsToPrimitives(Double[] doubleObjects) throws ClassCastException {
        double[] primitives = new double[doubleObjects.length];
        Double obj;
        for (int i = 0; i < doubleObjects.length; i++) {
            obj = doubleObjects[i];
            if (obj == null) throw new ClassCastException();
            else {
                primitives[i] = obj;
            }
        }
        return primitives;
    }

    public static void createDataLoggerFile(Context context) {
        try {
            /**
             * Directory
             */
            mDataLoggerDirectory = new File(getAppDirectory(context));
            if (!mDataLoggerDirectory.exists()) {
                if (!mDataLoggerDirectory.mkdirs()) {
                    Log.e(TAG, "createDataLoggerFile: failed");
                }
            }
            /**
             * File  name
             */

            mDataLoggerFile = new File(mDataLoggerDirectory.getAbsoluteFile() + File.separator
                    + Utils.GetDate() + context.getResources().getString(R.string.dl_file_extension));
            if (!mDataLoggerFile.exists()) {
                boolean isFileFound = mDataLoggerFile.createNewFile();
                Log.e(TAG, "creating new file, is success: "+isFileFound);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void deleteOLDFiles(Context context) {
        /**
         * Delete old file
         */
        File[] allFilesList = mDataLoggerDirectory.listFiles();
        long cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        if (allFilesList!=null) {
            for (int pos = 0; pos < allFilesList.length; pos++) {
                File currentFile = allFilesList[pos];
                if (currentFile.lastModified() < cutoff) {
                    currentFile.delete();
                }

            }
        }
        mDataLoggerOldFile = new File(mDataLoggerDirectory.getAbsoluteFile() + File.separator
                + Utils.GetDateSevenDaysBack() +
                context.getResources().getString(R.string.dl_file_extension));
        if (mDataLoggerOldFile.exists()) {
            mDataLoggerOldFile.delete();
        }

    }

    private static void saveLogData(Context context, String message) {
        mDataLoggerFile = new File(mDataLoggerDirectory.getAbsoluteFile() + File.separator
                + Utils.GetDate() + context.getResources().getString(R.string.dl_file_extension));
        if (!mDataLoggerFile.exists()) {
            try {
                mDataLoggerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        message = Utils.GetTimeandDate() + message;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(mDataLoggerFile, true),
                    "UTF-8");
            BufferedWriter fbw = new BufferedWriter(writer);
            fbw.write(message);
            fbw.newLine();
            fbw.flush();
            fbw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getAppDirectory(Context context) {
        return baseDirectory + context.getResources().getString(R.string.dl_directory);
    }
}
