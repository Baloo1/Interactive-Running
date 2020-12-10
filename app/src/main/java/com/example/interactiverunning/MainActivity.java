package com.example.interactiverunning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.interactiverunning.utils.DebugHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, IFragmentListener {
    private final DebugHelper debugHelper = new DebugHelper();
    public int sensorIndex = 0;
    SensorManager sensorManager;
    Boolean sensorIsRunning = false;
    TextView show_cadence;
    TextView show_stride_length;
    TextView show_gct;
    double speed = 0;
    int maxDataSize = 400;
    public double[] sensorDataX = new double[maxDataSize];
    public double[] sensorDataY = new double[maxDataSize];
    public double[] sensorDataZ = new double[maxDataSize];
    public double[] sensorDataT = new double[maxDataSize];
    int currentLap = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        debugHelper.keepScreenOn(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void startAccelerometer() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(this, "SENSOR RUNNING", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "SENSOR MISSING", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        showAccelerometer(event);
    }

    public void showAccelerometer(SensorEvent event) {
        Log.d("SensorRunning", String.valueOf(event.timestamp));
        sensorDataX[sensorIndex] = event.values[0];
        sensorDataY[sensorIndex] = event.values[1];
        sensorDataZ[sensorIndex] = event.values[2];
        sensorDataT[sensorIndex] = (double) event.timestamp;

        show_cadence = findViewById(R.id.show_cadence);
        show_stride_length = findViewById(R.id.show_stride_length);
        show_gct = findViewById(R.id.show_gct);

        sensorIndex++;
        if (sensorIndex == maxDataSize) {

            double[] calculations = InteractiveRunning.calculateData(sensorDataX, sensorDataY, sensorDataZ, sensorDataT, speed);
            double cadence = calculations[0];
            double meanStrideLength = calculations[1];
            double GCT_mean = calculations[2];
            show_cadence.setText(String.format(Locale.UK, "%g", cadence));
            show_stride_length.setText(String.format(Locale.UK, "%g", meanStrideLength));
            show_gct.setText(String.format(Locale.UK, "%g", GCT_mean));
            for (int i = 0; i < sensorDataX.length; i++) {
                writeFile(sensorDataX[i], sensorDataY[i], sensorDataZ[i], sensorDataT[i], calculations);
            }
            currentLap++;
            sensorIndex = 0;
        }
    }

    public void writeFile(double x, double y, double z, double time, double[] calculations) {
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("dataX.txt", Context.MODE_APPEND);
            byte[] dataToWrite = (x + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataY.txt", Context.MODE_APPEND);
            dataToWrite = (y + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataZ.txt", Context.MODE_APPEND);
            dataToWrite = (z + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataT.txt", Context.MODE_APPEND);
            dataToWrite = (time + ", ").getBytes();
            stream.write(dataToWrite);

            double cadence = calculations[0];
            double meanStrideLength = calculations[1];
            double GCT_mean = calculations[2];
            stream = openFileOutput("calculations.txt", Context.MODE_APPEND);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void handleStartButton(View button) {
        if (!sensorIsRunning) {
            new android.os.Handler().postDelayed(new Runnable() {
                public void run() {
                    button.setVisibility(View.GONE);
                    startAccelerometer();
                }
            }, 3000);
        }
    }

    private void handleSpeedField(View view) {
        EditText editText = (EditText) view;
        speed = Double.parseDouble(String.valueOf(editText.getText()));
    }

    @Override
    public void notifyListeners(View view) {
        if (view.getId() == R.id.start_button) {
            handleStartButton(view);
        } else if (view.getId() == R.id.speed_field) {
            handleSpeedField(view);
        }
    }
}