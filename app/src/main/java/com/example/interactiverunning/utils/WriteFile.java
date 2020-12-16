package com.example.interactiverunning.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public final class WriteFile {
    private WriteFile() {
        //Disable default constructor
    }

    public static void writeDataFiles(final Activity activity, final double x, final double y, final double z, final double time) {
        byte[] dataToWrite = (x + ", ").getBytes();
        writeToFile(activity, "dataX.txt", dataToWrite);

        dataToWrite = (y + ", ").getBytes();
        writeToFile(activity, "dataY.txt", dataToWrite);

        dataToWrite = (z + ", ").getBytes();
        writeToFile(activity, "dataZ.txt", dataToWrite);

        dataToWrite = (time + ", ").getBytes();
        writeToFile(activity, "dataT.txt", dataToWrite);
    }

    public static void writeCalculationFile(final Activity activity, final double[] calculations) {
        double cadence = calculations[0];
        double meanStrideLength = calculations[1];
        double meanGCT = calculations[2];
        String fileName = "calculations.txt";
        byte[] dataToWrite = ("cadence: " + cadence + "\nmeanStrideLength: " + meanStrideLength + "\nGCT_mean: " + meanGCT + "\n").getBytes();
        writeToFile(activity, fileName, dataToWrite);
    }

    private static void writeToFile(final Activity activity, final String fileName, final byte[] dataToWrite) {
        FileOutputStream stream = null;
        try {
            stream = activity.openFileOutput(fileName, Context.MODE_APPEND);
            stream.write(dataToWrite);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        }
    }
}
