package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorDataActivityPresenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class SensorDataActivity extends AppCompatActivity implements SensorDataActivityPresenter.View, View.OnClickListener, SensorEventListener2 {

    public static final String TAG = "SensorDataActivity";
    private final String[] UPLOAD_STATUS_STATES = {"No upload in progress.", "Collecting data...", "Uploading...", "Upload Failed.", "Last Upload Succeeded."};

    SensorDataActivityPresenter presenter;

    // Sensor Info

    // Upload UI
    private TextView mUploadStatusTv;
    private Button mStartBtn;
    private Button mStopBtn;

    // Chronometer
    Chronometer mChronometer;

    // Sensor
    private Boolean isLogging;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;

    // File Writer
    private FileWriter mFileWriter;
    private File mFile;
    // Firebase Storage Ref
    private StorageReference mStorageReference;

    // Counters
    private int mAccelerometerCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data);
        initViews();
        initSensors();

        presenter = new SensorDataActivityPresenter(this);
        presenter.updateUploadState(UPLOAD_STATUS_STATES[0]);
        isLogging = false;

        FirebaseApp.initializeApp(this.getApplicationContext());
        //Init Firebase Storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sensordata_activity_btn_start:
                startDataCollection();
                break;
            case R.id.sensordata_activity_btn_stop:
                stopDataCollection();
                break;
        }
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.sensordata_title);
    }

    @Override
    public void resetAndStartStopWatch() {
        Log.d(TAG, "resetAndStartStopWatch: SensorData Stop Watch Started");
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public void stopStopWatch() {
        Log.d(TAG, "stopStopWatch: SensorData Stop Watch Stopped. Time: " + mChronometer.getText());
        mChronometer.stop();
    }

    @Override
    public void updateUploadStateView(String s) {
        mUploadStatusTv.setText(s);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(isLogging){
            int SensorType = sensorEvent.sensor.getType();
            float[] gravity = new float[3];

            int mTargetCount = 10000;
            if(SensorType == Sensor.TYPE_GRAVITY && mAccelerometerCounter != mTargetCount){
                final float alpha = 0.8f;
                // Low Pass Filter
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if(SensorType == Sensor.TYPE_ACCELEROMETER && mAccelerometerCounter != mTargetCount){
                writeToFile("TYPE_ACCELEROMETER", sensorEvent.values);
                mAccelerometerCounter++;

                // High Pass Filter
                float[] calculateLinearAccelerometer = new float[3];
                calculateLinearAccelerometer[0] = sensorEvent.values[0] - gravity[0];
                calculateLinearAccelerometer[1] = sensorEvent.values[1] - gravity[1];
                calculateLinearAccelerometer[2] = sensorEvent.values[2] - gravity[2];

                writeToFile("CALCULATED_LINEAR_ACCELEROMETER", calculateLinearAccelerometer);

            }

            if(mAccelerometerCounter == mTargetCount){
                Toast.makeText(this, "Auto-Stopping Collection.", Toast.LENGTH_LONG).show();
                stopDataCollection();
            }
            Log.d(TAG, "mAccelerometerCounter =>: " + mAccelerometerCounter);
        }
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void initViews(){
        Log.d(TAG, "initViews: SensorData Views Initialized");
        updateActionBarTitle();
        mUploadStatusTv = (TextView) findViewById(R.id.sensordata_activity_tv_uploadstatus);
        mStartBtn = (Button) findViewById(R.id.sensordata_activity_btn_start);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.sensordata_activity_btn_stop);
        mStopBtn.setOnClickListener(this);
        mStopBtn.setEnabled(false);
        mChronometer = (Chronometer) findViewById(R.id.sensordata_activity_chm_stopwatch);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(chronometer.getText().equals("00:02")){
                    isLogging = true;
                }
            }
        });
    }

    private void initSensors(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void startDataCollection(){
        presenter.startButtonClicked();
        mStartBtn.setEnabled(false);
        mStopBtn.setEnabled(true);
        presenter.updateUploadState(UPLOAD_STATUS_STATES[1]);
        Format f = new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss", Locale.getDefault());
        String randShortId = UUID.randomUUID().toString().replace("-","").substring(0,8);
        mFile = new File(getStorageDir(), Build.MODEL.replace(" ", "") + "mobile_sensorData_" + f.format(new Date()) + randShortId +".csv");
        // Start Writing
        try {
            mFileWriter = new FileWriter(mFile);
            mFileWriter.write(String.format("%s; %s; %s; %s; %s\n", "Time", "Sensor", "X-Axis", "Y-Axis", "Z-Axis"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    private void stopDataCollection() {
        presenter.stopButtonClicked();
        isLogging = false;
        mSensorManager.flush(this);
        mSensorManager.unregisterListener(this);
        presenter.updateUploadState(UPLOAD_STATUS_STATES[2]);

        // Stop Writing
        try {
            mFileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        presenter.updateUploadState(UPLOAD_STATUS_STATES[2]); // Status Message: Uploading

        // Upload File to Firebase
        Uri sensorDataFile = Uri.fromFile(mFile);
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/measurements/" + sensorDataFile.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("text/csv")
                .setCustomMetadata("device", Build.ID)
                .build();
        UploadTask uploadTask = sensorDataRef.putFile(sensorDataFile, metadata);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                presenter.updateUploadState(UPLOAD_STATUS_STATES[3]); // Status Message: Upload failed
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                presenter.updateUploadState(UPLOAD_STATUS_STATES[4]); // Status Message: Succeeded
            }
        });
        mStartBtn.setEnabled(true);
        mStopBtn.setEnabled(false);
    }

    private void writeToFile(String sensor, float[] events){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        try {
            mFileWriter.write(String.format("%s; %s; %f; %f; %f\n", simpleDateFormat.format(new Date()),sensor,events[0],events[1],events[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStorageDir() {
        return Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
    }
}
