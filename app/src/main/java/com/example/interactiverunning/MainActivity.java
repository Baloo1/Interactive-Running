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

public class MainActivity extends AppCompatActivity implements SensorEventListener, IFragmentListener {
    private final DebugHelper debugHelper = new DebugHelper();
    public int sensorIndex = 0;
    SensorManager sensorManager;
    Boolean sensorIsRunning = false;
    TextView show_x;
    TextView show_y;
    TextView show_z;
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
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double t = (double) event.timestamp;

        show_x = findViewById(R.id.show_cadence);
        show_y = findViewById(R.id.show_stride_length);
        show_z = findViewById(R.id.show_gct);

        sensorIndex++;
        if (sensorIndex == maxDataSize) {

            double[] calculations = InteractiveRunning.calculateData(sensorDataX, sensorDataY, sensorDataZ, sensorDataT, speed);

            sensorIndex = 0;
            show_x.setText(Double.toString(calculations[0]));
            show_y.setText(Double.toString(calculations[1]));
            show_z.setText(Double.toString(calculations[2]));
            writeOutput(calculations[0], calculations[1], calculations[2]);
            for (int i = 0; i < sensorDataX.length; i++) {
                writeFile(sensorDataX[i], sensorDataY[i], sensorDataZ[i], sensorDataT[i]);
            }
            currentLap++;

        }
    }

    public void writeOutput(double cadence, double meanStrideLength, double GCT_mean) {
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("output.txt", Context.MODE_APPEND);
            byte[] dataToWrite = ("cadence: " + cadence + "\nmeanStrideLength: " + meanStrideLength + "\nGCT_mean: " + GCT_mean + "\n").getBytes();
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

    public void writeFile(double x, double y, double z, double ns) {
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
            dataToWrite = (ns + ", ").getBytes();
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
            new android.os.Handler().postDelayed(
                    new Runnable() {
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