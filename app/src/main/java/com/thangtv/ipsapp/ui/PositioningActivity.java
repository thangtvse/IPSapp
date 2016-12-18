package com.thangtv.ipsapp.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thangtv.ipsapp.R;
import com.thangtv.ipsapp.helpers.KNN;
import com.thangtv.ipsapp.models.KNNPosition;
import com.thangtv.ipsapp.models.KNNRecord;
import com.thangtv.ipsapp.models.Record;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;

import static android.content.Intent.CATEGORY_OPENABLE;

public class PositioningActivity extends AppCompatActivity implements SensorEventListener {

    private static final int CHOOSE_FILE_REQUEST_CODE = 1101;
    private static final String TAG = "PositioningActivity";

    private SensorManager sensorManager;

    private Sensor magneticSensor;
    private Sensor accelerometerSensor;

    private float[] currentGravity;
    private float[] currentMagnetic;

    private final float alpha = (float) 0.8;

    private List<KNNRecord> trainSet;

    private TextView tvAccuracy;
    private TextView tvPosition;
    private TextView tvXAxis;
    private TextView tvYAxis;
    private TextView tvZAxis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positioning);

        checkPermissions();
        initSensors();
        initViews();


    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void initViews() {
        tvAccuracy = (TextView) findViewById(R.id.activity_positioning_accuracy);
        tvPosition = (TextView) findViewById(R.id.activity_positioning_position);
        tvXAxis = (TextView) findViewById(R.id.activity_positioning_tv_x_axis);
        tvYAxis = (TextView) findViewById(R.id.activity_positioning_tv_y_axis);
        tvZAxis = (TextView) findViewById(R.id.activity_positioning_tv_z_axis);
    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private boolean checkSensor() {
        return (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);

    }

    public void chooseFile(View v) {

        trainSet = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/MagneticSensor");

        boolean result = false;
        if (!folder.exists()) {
            result = folder.mkdir();
        } else {
            result = true;
        }

        if (result) {
            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory()
                    + "/MagneticSensor/"), "file/csv");
        } else {
            intent.setType("file/csv");
        }


        startActivityForResult(intent.createChooser(intent, "Open CSV"), CHOOSE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            proImportCSV(new File(data.getData().getPath()));
        }
    }

    private void proImportCSV(File from) {
        try {


            CSVReader dataRead = new CSVReader(new FileReader(from));

            String[] vv;
            int index = 0;
            while ((vv = dataRead.readNext()) != null) {
                Log.d(TAG, "proImportCSV: " + vv[4] + " " + vv[5] + " " + vv[6] + " " + vv[13]);

                if (index > 0) {
                    KNNRecord trainData = new KNNRecord(Float.parseFloat(vv[4]), Float.parseFloat(vv[5]), Float.parseFloat(vv[6]));
                    trainData.setPosition(vv[13]);
                    trainSet.add(trainData);
                }

                index++;
            }

        } catch (Exception e) {
            Log.e("TAG", e.toString());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (checkSensor()) {
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Can't find required sensors.")
                    .setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // quit this activity
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                currentMagnetic = sensorEvent.values;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                // Isolate the force of gravity with the low-pass filter
                if (currentGravity == null) {
                    currentGravity = new float[3];
                }

                //currentGravity = sensorEvent.values;

                currentGravity[0] = alpha * currentGravity[0] + (1 - alpha) * sensorEvent.values[0];
                currentGravity[1] = alpha * currentGravity[1] + (1 - alpha) * sensorEvent.values[1];
                currentGravity[2] = alpha * currentGravity[2] + (1 - alpha) * sensorEvent.values[2];
                break;
        }

        if (currentMagnetic != null && currentGravity != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            SensorManager.getRotationMatrix(R, I, currentGravity, currentMagnetic);

            float[] worldMagnetic = new float[3];
            worldMagnetic[0] = R[0] * currentMagnetic[0] + R[1] * currentMagnetic[1] + R[2] * currentMagnetic[2];
            worldMagnetic[1] = R[3] * currentMagnetic[0] + R[4] * currentMagnetic[1] + R[5] * currentMagnetic[2];
            worldMagnetic[2] = R[6] * currentMagnetic[0] + R[7] * currentMagnetic[1] + R[8] * currentMagnetic[2];

            Locale currentLocale = Locale.getDefault();
            tvXAxis.setText(String.format(currentLocale, "%f", worldMagnetic[0]));
            tvYAxis.setText(String.format(currentLocale, "%f", worldMagnetic[1]));
            tvZAxis.setText(String.format(currentLocale, "%f", worldMagnetic[2]));

            if (trainSet != null && trainSet.size() > 0) {
                KNNRecord testData = new KNNRecord(worldMagnetic[0], worldMagnetic[1], worldMagnetic[2]);
                String position = KNN.classify(trainSet, testData, 10);
                tvPosition.setText(position);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        switch (i) {
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                tvAccuracy.setText("LOW");
                tvAccuracy.setTextColor(Color.RED);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                tvAccuracy.setText("MEDIUM");
                tvAccuracy.setTextColor(Color.YELLOW);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                tvAccuracy.setText("HIGH");
                tvAccuracy.setTextColor(Color.GREEN);
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                tvAccuracy.setText("UNRELIABLE");
                tvAccuracy.setTextColor(Color.GRAY);
                break;
        }
    }

}
