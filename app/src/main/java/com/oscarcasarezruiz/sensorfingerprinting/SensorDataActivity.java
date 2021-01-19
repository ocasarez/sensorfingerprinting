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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorInfo;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorDataActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SensorDataActivity extends AppCompatActivity implements SensorDataActivityPresenter.View, View.OnClickListener, SensorEventListener2 {

    public static final String TAG = "SensorDataActivity";
    private final String[] UPLOAD_STATUS_STATES = {"No upload in progress.", "Collecting data...", "Uploading...", "Upload Failed.", "Last Upload Succeeded."};

    SensorDataActivityPresenter presenter;

    // Sensor Info
    private TextView mRawSensorBiasTv;
    private TextView mSensorModelTv;
    private TextView mSensorVendorTv;
    private TextView mSensorMeasurementRangeTv;
    private TextView mSensorNoiseTv;
    private TextView mSensorResolutionTv;

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
    private ArrayList<float[]> sensorData;

    // File Writer
    private FileWriter mFileWriter;
    private File mFile;
    // Firebase Storage Ref
    private StorageReference mStorageReference;
    // Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data);
        initViews();
        initSensors();

        presenter = new SensorDataActivityPresenter(this);
        presenter.updateUploadState(UPLOAD_STATUS_STATES[0]);
        presenter.loadSensorInfo(collectSensorInfoData());
        isLogging = false;
        sensorData = new ArrayList<>();

        FirebaseApp.initializeApp(this.getApplicationContext());
        //Init Firebase Storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sensordata_activity_btn_start:
                presenter.startButtonClicked();
                startDataCollection();
                break;
            case R.id.sensordata_activity_btn_stop:
                presenter.stopButtonClicked();
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
    public void showSensorInfo(SensorInfo info) {
        float[] rawBias = info.getSensorRawBias();
        float[] noise = info.getSensorNoise();

        mRawSensorBiasTv.setText(String.format("[%f,\n%f,\n%f]", rawBias[0], rawBias[1], rawBias[2]));
        mSensorModelTv.setText(info.getSensorModel());
        mSensorVendorTv.setText(info.getSensorVendor());
        mSensorMeasurementRangeTv.setText(String.format("%f", info.getSensorMeasurementRange()));
        mSensorNoiseTv.setText(String.format("[%f,\n%f,\n%f]", noise[0], noise[1], noise[2]));
        mSensorResolutionTv.setText(String.format("%f", info.getSensorResolution()));
    }


    @Override
    public void updateUploadStateView(String s) {
        mUploadStatusTv.setText(s);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        if(isLogging){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                sensorData.add(sensorEvent.values);
                try {
                    mFileWriter.write(String.format("%s; %f; %f; %f\n", simpleDateFormat.format(new Date()), sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
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
        mRawSensorBiasTv = (TextView) findViewById(R.id.sensordata_activity_tv_rawbiasValue);
        mSensorModelTv = (TextView) findViewById(R.id.sensordata_activity_tv_modelValue);
        mSensorVendorTv = (TextView) findViewById(R.id.sensordata_activity_tv_vendorValue);
        mSensorMeasurementRangeTv = (TextView) findViewById(R.id.sensordata_activity_tv_measurementRangeValue);
        mSensorNoiseTv = (TextView) findViewById(R.id.sensordata_activity_tv_noiseValue);
        mSensorResolutionTv = (TextView) findViewById(R.id.sensordata_activity_tv_resolutionValue);

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
                if(chronometer.getText().equals("00:10")){
                    isLogging = true;
                }
            }
        });
    }

    private void initSensors(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void startDataCollection(){
        mStartBtn.setEnabled(false);
        mStopBtn.setEnabled(true);
        presenter.updateUploadState(UPLOAD_STATUS_STATES[1]);
        mFile = new File(getStorageDir(), Build.MODEL + "mobile_sensorData_" + System.currentTimeMillis() + ".csv");
        // Start Writing
        try {
            mFileWriter = new FileWriter(mFile);
            mFileWriter.write(String.format("%s; %s; %s; %s\n", "Time", "Accelerometer_X", "Accelerometer_Y", "Accelerometer_Z"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    private void stopDataCollection() {
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
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/" + sensorDataFile.getLastPathSegment());
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
                mFile.delete();
            }
        });
        //mFile.delete();
        // Calculate Sensor Noise
        presenter.updateSensorNoise(Utils.calculateSensorNoise(sensorData));
        // Calculate Sensor Bias
        float[] averageMeasurement = Utils.AverageTracesMeasured(sensorData);
        presenter.updateSensorRawBias(Utils.calculateSensorBias(new SensorTrace(averageMeasurement[0], averageMeasurement[1], averageMeasurement[2])));
        mStartBtn.setEnabled(true);
        mStopBtn.setEnabled(false);
    }

    private String getStorageDir() {
        return Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
    }

    private SensorInfo collectSensorInfoData(){
        SensorInfo sensorInfo = new SensorInfo();
        // Sensor Bias - Available after data collection
        // Sensor Model
        if(!mAccelerometer.getName().isEmpty()){
            sensorInfo.setSensorModel(mAccelerometer.getName());
        }
        // Sensor Vendor
        if(!mAccelerometer.getVendor().isEmpty()){
            sensorInfo.setSensorVendor(mAccelerometer.getVendor());
        }
        // Sensor Measurement Range
        sensorInfo.setSensorMeasurementRange(mAccelerometer.getMaximumRange());
        // Sensor Noise - Available after data collection
        // Sensor Resolution
        sensorInfo.setSensorResolution(mAccelerometer.getResolution());
        return sensorInfo;
    }
}