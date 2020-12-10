package com.example.interactiverunning.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFile {
    public static void writeFile(Activity activity, double x, double y, double z, double time, double[] calculations) {
        FileOutputStream stream = null;
        try {
            stream = activity.openFileOutput("dataX.txt", Context.MODE_APPEND);
            byte[] dataToWrite = (x + ", ").getBytes();
            stream.write(dataToWrite);

            stream = activity.openFileOutput("dataY.txt", Context.MODE_APPEND);
            dataToWrite = (y + ", ").getBytes();
            stream.write(dataToWrite);

            stream = activity.openFileOutput("dataZ.txt", Context.MODE_APPEND);
            dataToWrite = (z + ", ").getBytes();
            stream.write(dataToWrite);

            stream = activity.openFileOutput("dataT.txt", Context.MODE_APPEND);
            dataToWrite = (time + ", ").getBytes();
            stream.write(dataToWrite);

            double cadence = calculations[0];
            double meanStrideLength = calculations[1];
            double GCT_mean = calculations[2];
            stream = activity.openFileOutput("calculations.txt", Context.MODE_APPEND);
            dataToWrite = ("cadence: " + cadence + "\nmeanStrideLength: " + meanStrideLength + "\nGCT_mean: " + GCT_mean + "\n").getBytes();
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