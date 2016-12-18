package com.thangtv.ipsapp.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thangtv.ipsapp.R;
import com.thangtv.ipsapp.models.Record;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollectDataActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvAccuracy;
    private TextView tvXAxis;
    private TextView tvYAxis;
    private TextView tvZAxis;
    private EditText edCollector;
    private EditText edStudentCode;
    private EditText edGroup;
    private EditText edPosition;
    private Button btnRecord;
    private Button btnSave;
    private TextView tvRecording;


    private ProgressDialog progressDialog;

    private SensorManager sensorManager;

    private Sensor magneticSensor;
    private Sensor accelerometerSensor;

    private float[] currentGravity;
    private float[] currentMagnetic;

    private final float alpha = (float) 0.8;

    /**
     * Record interval in milliseconds
     */
    private final double recordInterval = 500;

    /**
     * Last record time
     */
    private Date lastRecordTime = new Date();


    /**
     * A list of recored data
     */
    private List<Record> records;

    /**
     * Is recording or not
     */
    private int status = STOPPED;
    private static final int RECORDING = 1;
    private static final int PAUSED = 2;
    private static final int STOPPED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);

        initViews();
        initSensors();
        checkPermissions();

    }


    private void initViews() {
        tvAccuracy = (TextView) findViewById(R.id.activity_main_accuracy);
        tvXAxis = (TextView) findViewById(R.id.activity_main_tv_x_axis);
        tvYAxis = (TextView) findViewById(R.id.activity_main_tv_y_axis);
        tvZAxis = (TextView) findViewById(R.id.activity_main_tv_z_axis);
        edCollector = (EditText) findViewById(R.id.activity_main_collector);
        edStudentCode = (EditText) findViewById(R.id.activity_main_student_code);
        edGroup = (EditText) findViewById(R.id.activity_main_group);
        edPosition = (EditText) findViewById(R.id.activity_main_position);
        btnRecord = (Button) findViewById(R.id.activity_main_record);
        btnSave = (Button) findViewById(R.id.activity_main_save);
        tvRecording = (TextView) findViewById(R.id.activity_main_recording);

        changeStateToStopped();
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

    private boolean checkSensor() {
        return (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);

    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void changeStateToRecording() {
        tvRecording.setVisibility(View.VISIBLE);
        tvRecording.setText("Recording...");
        btnSave.setVisibility(View.VISIBLE);
        btnRecord.setText("PAUSE");
        disableEditTextViews();
        status = RECORDING;
    }

    private void changeStateToPaused() {
        tvRecording.setVisibility(View.VISIBLE);
        tvRecording.setText("Paused");
        btnRecord.setText("RECORD");
        enableEditTextViews();
        status = PAUSED;
    }

    private void changeStateToStopped() {
        tvRecording.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnRecord.setText("RECORD");
        enableEditTextViews();
        status = STOPPED;
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


            Date currentTime = new Date();
            if (status == RECORDING &&
                    currentTime.getTime() - lastRecordTime.getTime() >= recordInterval) {

                Record record = new Record(currentTime,
                        currentMagnetic[0],
                        currentMagnetic[1],
                        currentMagnetic[2],
                        worldMagnetic[0],
                        worldMagnetic[1],
                        worldMagnetic[2],
                        edCollector.getText().toString(),
                        edStudentCode.getText().toString(),
                        edGroup.getText().toString(),
                        edPosition.getText().toString()
                );

                this.records.add(record);

                lastRecordTime = currentTime;

            }

        }
    }

    private void disableEditTextViews() {
        edCollector.setEnabled(false);
        edStudentCode.setEnabled(false);
        edGroup.setEnabled(false);
        edPosition.setEnabled(false);
    }

    private void enableEditTextViews() {
        edCollector.setEnabled(true);
        edStudentCode.setEnabled(true);
        edGroup.setEnabled(true);
        edPosition.setEnabled(true);
    }

    public void onClickRecord(View v) {
        if (status != RECORDING) {
            // clear list if the last state is STOPPED
            if (status == STOPPED) {
                this.records = new ArrayList<>();
            }
            // record
            changeStateToRecording();

        } else {
            // pause
            currentGravity = null;
            currentMagnetic = null;
            changeStateToPaused();
        }
    }

    public void onClickSave(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File name");


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16, 0, 16, 0);
        input.setLayoutParams(lp);
        builder.setView(input);


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                currentGravity = null;
                currentMagnetic = null;
                changeStateToStopped();
                new ExportToCSVTask().execute(input.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();



    }

    private class ExportToCSVTask extends AsyncTask<String, Void, Void> {

        private String phoneManufacturer;
        private String phoneModel;
        private String androidVersion;
        private List<Record> savingRecords;

        private String filename;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CollectDataActivity.this, "Saving..,", " Please wait.", true);

            phoneManufacturer = Build.MANUFACTURER;
            phoneModel = Build.MODEL;
            androidVersion = Build.VERSION.RELEASE;

            savingRecords = records;

        }

        @Override
        protected Void doInBackground(String... params) {

            File folder = new File(Environment.getExternalStorageDirectory()
                    + "/MagneticSensor");

            boolean var = false;
            if (!folder.exists()) {
                var = folder.mkdir();
            } else {
                var = true;
            }

            if (var) {
                // We've got a folder

                Date date = new Date();

                filename = folder.toString() + "/" + params[0] + ".csv";

                try {

                    FileWriter fw = new FileWriter(filename);

                    fw.append("DATE");
                    fw.append(',');

                    fw.append("MAGX");
                    fw.append(',');

                    fw.append("MAGY");
                    fw.append(',');

                    fw.append("MAGZ");
                    fw.append(',');

                    fw.append("WMAGX");
                    fw.append(',');

                    fw.append("WMAGY");
                    fw.append(',');

                    fw.append("WMAGZ");
                    fw.append(',');

                    fw.append("COLLECTOR");
                    fw.append(',');

                    fw.append("STUDENT CODE");
                    fw.append(',');

                    fw.append("GROUP");
                    fw.append(',');

                    fw.append("PHONE MANUFACTURER");
                    fw.append(',');

                    fw.append("PHONE MODEL");
                    fw.append(',');

                    fw.append("ANDROID VERSION");
                    fw.append(',');

                    fw.append("POSITION");
                    fw.append(',');

                    fw.append('\n');


                    for (int i = 0; i < savingRecords.size(); i++) {

                        Record record = savingRecords.get(i);

                        fw.append(record.getCreatedAt().toString());
                        fw.append(',');

                        fw.append(Float.toString(record.getMagX()));
                        fw.append(',');

                        fw.append(Float.toString(record.getMagY()));
                        fw.append(',');

                        fw.append(Float.toString(record.getMagZ()));
                        fw.append(',');

                        fw.append(Float.toString(record.getWmagX()));
                        fw.append(',');

                        fw.append(Float.toString(record.getWmagY()));
                        fw.append(',');

                        fw.append(Float.toString(record.getWmagZ()));
                        fw.append(',');

                        fw.append("\"" + record.getCollector()+ "\"");
                        fw.append(',');

                        fw.append("\"" + record.getStudentCode() + "\"");
                        fw.append(',');

                        fw.append("\"" + record.getGroup() + "\"");
                        fw.append(',');

                        fw.append(phoneManufacturer);
                        fw.append(',');

                        fw.append(phoneModel);
                        fw.append(',');

                        fw.append(androidVersion);
                        fw.append(',');

                        fw.append("\"" + record.getPosition() + "\"");
                        fw.append(',');

                        fw.append('\n');
                    }

                    fw.close();
                    records = null;

                } catch (Exception e) {
                    Log.e("CREATE_FILE", "doInBackground: " + e.getMessage());
                    cancel(true);
                }
            } else {
                Log.e("CREATE_FILE", "doInBackground: Can't create file");
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(CollectDataActivity.this, "Saved in " + filename, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
            Toast.makeText(CollectDataActivity.this, "Failed", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        currentGravity = null;
        currentMagnetic = null;
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (status == RECORDING) {
            // pause
            changeStateToPaused();
        }
        sensorManager.unregisterListener(this);
    }
}
