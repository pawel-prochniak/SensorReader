package com.example.pprochniak.sensorreader.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.pprochniak.sensorreader.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Henny on 2017-03-27.
 */

/**
 * This is a custom log class that will manage logs in the project. Using the
 * <b>disableLog()</b> all the logs can be disabled in the project during the
 * production stage <b> enableLog()</b> will allow to enable the logs , by
 * default the logs will be visible.<br>
 * *
 */
public class Logger {
    private static final String TAG = "Logger";
    private static boolean mLogflag = true;
    private static File mDataLoggerDirectory;
    private static File mDataLoggerFile;
    private static File mDataLoggerOldFile;
    private static Context mContext;

    public static void datalog(String message) {
        // show(Log.INFO, mLogTag, message);
        // saveLogData(message);

    }

    /**
     * printStackTrace for exception *
     */
    private static void show(Exception exception) {
        try {
            if (mLogflag)
                exception.printStackTrace();

        } catch (NullPointerException e) {
            Logger.show(e);
        }
    }

    public static boolean enableLog() {
        mLogflag = true;
        return mLogflag;
    }

    public static boolean disableLog() {
        mLogflag = false;
        return mLogflag;
    }

    public static void createDataLoggerFile(Context context) {
        mContext = context;
        try {
            /**
             * Directory
             */
            mDataLoggerDirectory = new File(Environment.getExternalStorageDirectory() +
                    File.separator
                    + context.getResources().getString(R.string.dl_directory));
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

    public static void deleteOLDFiles() {
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
                mContext.getResources().getString(R.string.dl_file_extension));
        if (mDataLoggerOldFile.exists()) {
            mDataLoggerOldFile.delete();
        }

    }

    private static void saveLogData(String message) {
        mDataLoggerFile = new File(mDataLoggerDirectory.getAbsoluteFile() + File.separator
                + Utils.GetDate() + mContext.getResources().getString(R.string.dl_file_extension));
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
}
