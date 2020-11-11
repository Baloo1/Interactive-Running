package com.example.interactiverunning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    TextView show_x;
    TextView show_y;
    TextView show_z;
    TextView show_filterx;
    TextView show_filtery;
    TextView show_filterz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startAccelerometer();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        writeFile(1, 2, 3);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void startAccelerometer() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
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
        show_x = findViewById(R.id.show_x);
        show_y = findViewById(R.id.show_y);
        show_z = findViewById(R.id.show_z);

        show_filterx = findViewById(R.id.show_filterx);
        show_filtery = findViewById(R.id.show_filtery);
        show_filterz = findViewById(R.id.show_filterz);

        String xtext = "X: " + String.valueOf(event.values[0]);
        String ytext = "Y: " + String.valueOf(event.values[1]);
        String ztext = "Z: " + String.valueOf(event.values[2]);
        show_x.setText(xtext);
        show_y.setText(ytext);
        show_z.setText(ztext);

        String filterxtext = "Filter X: " + String.valueOf(event.values[0]);
        String filterytext = "Filter Y: " + String.valueOf(event.values[1]);
        String filterztext = "filter Z: " + String.valueOf(event.values[2]);
        show_filterx.setText(filterxtext);
        show_filtery.setText(filterytext);
        show_filterz.setText(filterztext);
    }

    public void filterData(float x, float y, float z) {

    }

    public void writeFile(float x, float y, float z) {
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("dataX.txt", Context.MODE_PRIVATE);
            byte[] dataToWrite = (x + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataY.txt", Context.MODE_PRIVATE);
            dataToWrite = (y + ", ").getBytes();
            stream.write(dataToWrite);

            stream = openFileOutput("dataZ.txt", Context.MODE_PRIVATE);
            dataToWrite = (z + ", ").getBytes();
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
}