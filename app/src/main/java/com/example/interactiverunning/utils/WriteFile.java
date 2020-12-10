package com.example.interactiverunning.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFile {
    public static void writeDataFiles(Activity activity, double x, double y, double z, double time) {
        byte[] dataToWrite = (x + ", ").getBytes();
        writeToFile(activity, "dataX.txt", dataToWrite);

        dataToWrite = (y + ", ").getBytes();
        writeToFile(activity, "dataY.txt", dataToWrite);

        dataToWrite = (z + ", ").getBytes();
        writeToFile(activity, "dataZ.txt", dataToWrite);

        dataToWrite = (time + ", ").getBytes();
        writeToFile(activity, "dataT.txt", dataToWrite);
    }

    public static void writeCalculationFile(Activity activity, double[] calculations) {
        double cadence = calculations[0];
        double meanStrideLength = calculations[1];
        double GCT_mean = calculations[2];
        String fileName = "calculations.txt";
        byte[] dataToWrite = ("cadence: " + cadence + "\nmeanStrideLength: " + meanStrideLength + "\nGCT_mean: " + GCT_mean + "\n").getBytes();
        writeToFile(activity, fileName, dataToWrite);
    }

    private static void writeToFile(Activity activity, String fileName, byte[] dataToWrite) {
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