package com.example.interactiverunning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, IFragmentListener {
    SensorManager sensorManager;
    Boolean sensorIsRunning = false;
    TextView show_x;
    TextView show_y;
    TextView show_z;
    int maxDataSize = 700;
    int currentLap = 1;
    public double [] sensorDataX = new double[maxDataSize];
    public double [] sensorDataY = new double[maxDataSize];
    public double [] sensorDataZ = new double[maxDataSize];
    public double [] sensorDataT = new double[maxDataSize];
    public int sensorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        debugHelper();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    public void debugHelper() {
        if (BuildConfig.DEBUG) {
            if (Debug.isDebuggerConnected()) {
                Log.d("SCREEN", "Keeping screen on for debugging, detach debugger and force an onResume to turn it off.");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d("SCREEN", "Keeping screen on for debugging is now deactivated.");
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        showAccelerometer(event);
    }

    public void showAccelerometer(SensorEvent event) {
        Log.d("SensorRunning", String.valueOf(event.timestamp));
        sensorDataX[sensorIndex] = (double) (event.values[0]);
        sensorDataY[sensorIndex] = (double) (event.values[1]);
        sensorDataZ[sensorIndex] = (double) (event.values[2]);
        sensorDataT[sensorIndex] =  (double) event.timestamp;
        double x = (double) (event.values[0]);
        double y = (double) (event.values[1]);
        double z = (double) (event.values[2]);
        double t = (double) event.timestamp;

        show_x = findViewById(R.id.show_x);
        show_y = findViewById(R.id.show_y);
        show_z = findViewById(R.id.show_z);

        sensorIndex++;
        if(sensorIndex==maxDataSize){

            double[] calculations = InteractiveRunning.calculateData(sensorDataX, sensorDataY, sensorDataZ, sensorDataT);

            sensorIndex=0;
            show_x.setText(Double.toString(calculations[0]));
            show_y.setText(Double.toString(calculations[1]));
            show_z.setText(Double.toString(calculations[2]));
            if (BuildConfig.DEBUG) {
                for (int i = 0; i < sensorIndex; i++) {
                    writeFile(sensorDataX[i], sensorDataY[i], sensorDataZ[i], sensorDataT[i]);
                }
                currentLap++;
            }
        }
    }

    public void writeFile(double x, double y, double z, double ns) {
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("dataX"+currentLap+".txt", Context.MODE_APPEND);
            byte[] dataToWrite = (x + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataY"+currentLap+".txt", Context.MODE_APPEND);
            dataToWrite = (y + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataZ"+currentLap+".txt", Context.MODE_APPEND);
            dataToWrite = (z + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataT"+currentLap+".txt", Context.MODE_APPEND);
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
            button.setVisibility(View.GONE);
            startAccelerometer();
        }
    }

    @Override
    public void notifyListeners(View button) {
        if (button.getId() == R.id.start_button) {
            handleStartButton(button);
        }
    }
}