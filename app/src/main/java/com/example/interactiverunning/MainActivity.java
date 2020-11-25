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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Stop screen from sleep
        if (BuildConfig.DEBUG) {
            if (Debug.isDebuggerConnected()) {
                Log.d("SCREEN", "Keeping screen on for debugging, detach debugger and force an onResume to turn it off.");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d("SCREEN", "Keeping screen on for debugging is now deactivated.");
            }
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
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

    public void showCadence(SensorEvent event) {

    }

    public void showAccelerometer(SensorEvent event) {
        String x = Float.toString(event.values[0]);
        String y = Float.toString(event.values[1]);
        String z = Float.toString(event.values[2]);

        show_x = findViewById(R.id.show_x);
        show_y = findViewById(R.id.show_y);
        show_z = findViewById(R.id.show_z);

        String xText = "X: " + x;
        String yText = "Y: " + y;
        String zText = "Z: " + z;
        show_x.setText(xText);
        show_y.setText(yText);
        show_z.setText(zText);

//        writeFile(event.timestamp, event.values[0], event.values[1], event.values[2]);
    }

    public void writeFile(long ns, float x, float y, float z) {
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

            stream = openFileOutput("timestamp.txt", Context.MODE_APPEND);
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