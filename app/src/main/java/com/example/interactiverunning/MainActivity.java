package com.example.interactiverunning;

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
import com.example.interactiverunning.utils.WriteFile;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, IFragmentListener {
    private final int maxDataSize = 400;
    private final double[] sensorDataX = new double[maxDataSize];
    private final double[] sensorDataY = new double[maxDataSize];
    private final double[] sensorDataZ = new double[maxDataSize];
    private final double[] sensorDataT = new double[maxDataSize];
    private int sensorIndex = 0;
    private double speed = 0;
    private boolean sensorIsRunning = false;
    private boolean sensorIsStarting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugHelper.keepScreenOn(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void startAccelerometer() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(this, "SENSOR RUNNING", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "SENSOR MISSING", Toast.LENGTH_LONG).show();
        }
    }

    public void stopAccelerometer() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.unregisterListener(this, accelerometerSensor);
        Toast.makeText(this, "SENSOR STOPPED", Toast.LENGTH_LONG).show();

        TextView show_cadence = findViewById(R.id.show_cadence);
        TextView show_stride_length = findViewById(R.id.show_stride_length);
        TextView show_gct = findViewById(R.id.show_gct);

        double[] calculations = InteractiveRunning.calculateData(sensorDataX, sensorDataY, sensorDataZ, sensorDataT, speed);
        double cadence = calculations[0];
        double meanStrideLength = calculations[1];
        double GCT_mean = calculations[2];
        show_cadence.setText(String.format(Locale.UK, "%g", cadence));
        show_stride_length.setText(String.format(Locale.UK, "%g", meanStrideLength));
        show_gct.setText(String.format(Locale.UK, "%g", GCT_mean));
        for (int i = 0; i < sensorIndex; i++) {
            WriteFile.writeDataFiles(this, sensorDataX[i], sensorDataY[i], sensorDataZ[i], sensorDataT[i]);
        }
        WriteFile.writeCalculationFile(this, calculations);
        sensorIndex = 0;
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

        TextView show_cadence = findViewById(R.id.show_cadence);
        TextView show_stride_length = findViewById(R.id.show_stride_length);
        TextView show_gct = findViewById(R.id.show_gct);

        sensorIndex++;
        if (sensorIndex == maxDataSize) {
            double[] calculations = InteractiveRunning.calculateData(sensorDataX, sensorDataY, sensorDataZ, sensorDataT, speed);
            double cadence = calculations[0];
            double meanStrideLength = calculations[1];
            double GCT_mean = calculations[2];
            show_cadence.setText(String.format(Locale.UK, "%g", cadence));
            show_stride_length.setText(String.format(Locale.UK, "%g", meanStrideLength));
            show_gct.setText(String.format(Locale.UK, "%g", GCT_mean));
            for (int i = 0; i < sensorIndex; i++) {
                WriteFile.writeDataFiles(this, sensorDataX[i], sensorDataY[i], sensorDataZ[i], sensorDataT[i]);
            }
            WriteFile.writeCalculationFile(this, calculations);
            sensorIndex = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void handleStartButton(View button) {
        if (!sensorIsRunning && !sensorIsStarting) {
            sensorIsStarting = true;
            Toast.makeText(this, "STARTING...", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(() -> {
                button.setVisibility(View.GONE);
                findViewById(R.id.stop_button).setVisibility(View.VISIBLE);
                startAccelerometer();
                sensorIsRunning = true;
            }, 3000);
            sensorIsStarting = false;
        } else if (sensorIsRunning && !sensorIsStarting) {
            stopAccelerometer();
            button.setVisibility(View.GONE);
            findViewById(R.id.start_button).setVisibility(View.VISIBLE);
            sensorIsRunning = false;
        }
    }

    private void handleSpeedField(View view) {
        EditText editText = (EditText) view;
        if (!(editText.getText() == null)) {
            speed = Double.parseDouble(String.valueOf(editText.getText()));
        } else {
            /*Fallback speed*/
            /*TODO: Remove and add error handling on input*/
            speed = 8;
        }
    }

    @Override
    public void notifyListeners(View view) {
        if (view.getId() == R.id.start_button || view.getId() == R.id.stop_button) {
            handleStartButton(view);
        } else if (view.getId() == R.id.speed_field) {
            handleSpeedField(view);
        }
    }
}