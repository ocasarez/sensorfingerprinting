package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.FirstInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.SecondInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.ThirdInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.InstructionsActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class InstructionsActivity extends AppCompatActivity implements InstructionsActivityPresenter.View, View.OnClickListener, SensorEventListener {

    private static final String TAG = "InstructionsActivity";
    InstructionsActivityPresenter presenter;
    private String mCurrentFragment;

    // Motion Sensor
    private Sensor mAccelerometer;
    private Sensor mLinearAcceleration;
    private Sensor mGyroscope;
    private Sensor mGravity;
    private SensorManager mSensorManager;
    private ArrayList<float[]> accelerometerMeasurement;
    private ArrayList<float[]> calculatedLinearAccelerationMeasurement;
    private SensorFingerprint sensorFingerprint;
    private float[] sensorNoise;
    private float[] senorRawBias;

    //UI Properties
    private TextView mTraceView1;
    private TextView mTraceView2;
    private TextView mTraceView3;
    private Button mCollectAndNextButton;

    private String mTraceCount;
    private boolean mIsLogging;

    // File Writer
    private FileWriter mFileWriter;
    private File mFile;
    // Firebase Storage Ref
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Bundle data = getIntent().getExtras();
        mTraceCount = data.getString("TraceCount");
        initViews();
        initSensor();
        mIsLogging = false;

        presenter = new InstructionsActivityPresenter(this);
        accelerometerMeasurement = new ArrayList<>();
        calculatedLinearAccelerationMeasurement = new ArrayList<>();
        sensorFingerprint = new SensorFingerprint();

        FirebaseApp.initializeApp(this.getApplicationContext());
        //Init Firebase Storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
        createFile();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.instructions_activity_btn_collectAndNext){
            // First Instructions
            String buttonText = mCollectAndNextButton.getText().toString();
            if(mCurrentFragment.equals("FirstInstructions")){
                if(buttonText.equals("Collect Trace")){
                    mCollectAndNextButton.setEnabled(false);
                    presenter.collectTraceButtonClicked();
                } else if (buttonText.equals("Next")){ // Next
                    presenter.nextInstructionButtonClicked();
                    mCollectAndNextButton.setText(R.string.instructions_collectTrace);
                } else {
                    stopWritingData();
                    uploadFileToFireStorage();
                    presenter.sensorFingerprintResultsButtonClicked();
                }
            } else if (mCurrentFragment.equals("SecondInstructions")){
                if(buttonText.equals("Collect Trace")){
                    mCollectAndNextButton.setEnabled(false);
                    presenter.collectTraceButtonClicked();
                } else if(buttonText.equals("Next")){ // Next+
                    presenter.nextInstructionButtonClicked();
                    mCollectAndNextButton.setText(R.string.instructions_collectTrace);
                } else {
                    stopWritingData();
                    uploadFileToFireStorage();
                    presenter.sensorFingerprintResultsButtonClicked();
                }
            } else {
                if(buttonText.equals("Collect Trace")){
                    mCollectAndNextButton.setEnabled(false);
                    presenter.collectTraceButtonClicked();
                } else { // Sensor Fingerprint Results
                    stopWritingData();
                    uploadFileToFireStorage();
                    presenter.sensorFingerprintResultsButtonClicked();
                }
            }
        }
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.instructions_title);
    }

    @Override
    public void showFirstTrace(String s) {
        mTraceView1.setText(s);
    }

    @Override
    public void showSecondTrace(String s) {
        mTraceView2.setText(s);
    }

    @Override
    public void showThirdTrace(String s) {
        mTraceView3.setText(s);
    }

    @Override
    public void collectTrace() {
        Handler handler = new Handler();
        handler.postAtTime(this::startSensorDataCollection, 2000);
        handler.postDelayed(this::stopSensorDataCollection, 15000);
    }

    @Override
    public void nextInstruction() {
        switch (mTraceCount){
            case "Three":
                if(mCurrentFragment.equals("FirstInstructions")){
                    mCurrentFragment = loadFragments(new SecondInstructionFragment(), "SecondInstructions");
                } else {
                    mCurrentFragment = loadFragments(new ThirdInstructionFragment(), "ThirdInstructions");
                }
                break;
            case "Two":
                if(mCurrentFragment.equals("FirstInstructions")){
                    mCurrentFragment = loadFragments(new SecondInstructionFragment(), "SecondInstructions");
                }
        }
    }

    @Override
    public void navigateToSensorFingerprintResult() {
        retrieveSensorFingerprint();
        Intent intent = new Intent(this, SensorFingerprintActivity.class);
        intent.putExtra("sensorFingerprint", sensorFingerprint);
        intent.putExtra("TraceCount", mTraceCount);
        startActivity(intent);
    }

    // Sensor Functions
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(mIsLogging){
            int SensorType = sensorEvent.sensor.getType();
            float[] gravity = new float[3];

            if(SensorType == Sensor.TYPE_GRAVITY){
                final float alpha = 0.8f;
                // Low Pass Filter
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
                Log.d(TAG, String.format("Gravity - [%f, %f, %f]", gravity[0], gravity[1], gravity[2]));
            }

            if(SensorType == Sensor.TYPE_ACCELEROMETER){
                accelerometerMeasurement.add(sensorEvent.values);
                writeToFile("TYPE_ACCELEROMETER", sensorEvent.values);

                if(mCurrentFragment.equals("FirstInstructions")){
                    // High Pass Filter
                    float[] calculateLinearAccelerometer = new float[3];
                    calculateLinearAccelerometer[0] = sensorEvent.values[0] - gravity[0];
                    calculateLinearAccelerometer[1] = sensorEvent.values[1] - gravity[1];
                    calculateLinearAccelerometer[2] = sensorEvent.values[2] - gravity[2];

                    calculatedLinearAccelerationMeasurement.add(new float[]{
                            sensorEvent.values[0] - gravity[0],
                            sensorEvent.values[1] - gravity[1],
                            sensorEvent.values[2] - gravity[2]
                    });
                    writeToFile("CALCULATED_LINEAR_ACCELEROMETER", calculateLinearAccelerometer);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void initViews(){
        updateActionBarTitle();
        switch (mTraceCount){
            case "Three":
                mTraceView1 = (TextView) findViewById(R.id.firstInstructions_traceResult);
                mTraceView2 = (TextView) findViewById(R.id.secondInstructions_traceResult);
                mTraceView3 = (TextView) findViewById(R.id.thirdInstructions_traceResult);
                break;
            case "Two":
                mTraceView1 = (TextView) findViewById(R.id.firstInstructions_traceResult);
                mTraceView2 = (TextView) findViewById(R.id.secondInstructions_traceResult);
                findViewById(R.id.instructions_activity_tr_thirdTrace).setVisibility(View.GONE);
                break;
            case "One":
                mTraceView1 = (TextView) findViewById(R.id.firstInstructions_traceResult);
                findViewById(R.id.instructions_activity_tr_secondTrace).setVisibility(View.GONE);
                findViewById(R.id.instructions_activity_tr_thirdTrace).setVisibility(View.GONE);
                break;
        }

        mCollectAndNextButton = (Button) findViewById(R.id.instructions_activity_btn_collectAndNext);
        mCollectAndNextButton.setOnClickListener(this);

        mCurrentFragment = loadFragments(new FirstInstructionFragment(), "FirstInstructions");

    }

    private void saveTrace(){
        if(mCurrentFragment.equals("FirstInstructions")){
            presenter.saveFirstTrace(Utils.AverageTracesMeasured(accelerometerMeasurement));
            sensorNoise = Utils.calculateSensorNoise(accelerometerMeasurement);
            float[] averageMeasurement = Utils.AverageTracesMeasured(accelerometerMeasurement);
            senorRawBias = Utils.calculateSensorBias(new SensorTrace(averageMeasurement[0], averageMeasurement[1], averageMeasurement[2]));
            accelerometerMeasurement.clear();
            // Calculated Linear Acceleration Trace
            sensorFingerprint.setFilteredAccelerometerData(Utils.AverageTracesMeasured(calculatedLinearAccelerationMeasurement));
            calculatedLinearAccelerationMeasurement.clear();
        } else if(mCurrentFragment.equals("SecondInstructions")){
            presenter.saveSecondTrace(Utils.AverageTracesMeasured(accelerometerMeasurement));
            accelerometerMeasurement.clear();
        } else {
            presenter.saveThirdTrace(Utils.AverageTracesMeasured(accelerometerMeasurement));
            accelerometerMeasurement.clear();
        }
    }

    private void initSensor(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void startSensorDataCollection(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        mIsLogging = true;
    }

    private void stopSensorDataCollection(){
        mIsLogging = false;
        mSensorManager.unregisterListener(this);
        if(mCurrentFragment.equals("SecondInstructions")){
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
        saveTrace();
        switch (mTraceCount){
            case "Three":
                if(mCurrentFragment.equals("ThirdInstructions")){
                    mCollectAndNextButton.setText(R.string.instructions_goToSensorFingerprint);
                } else {
                    mCollectAndNextButton.setText(R.string.instructions_next);
                }
                break;
            case "Two":
                if(mCurrentFragment.equals("SecondInstructions")){
                    mCollectAndNextButton.setText(R.string.instructions_goToSensorFingerprint);
                } else {
                    mCollectAndNextButton.setText(R.string.instructions_next);
                }
                break;
            case "One":
                mCollectAndNextButton.setText(R.string.instructions_goToSensorFingerprint);
                break;
        }
        mCollectAndNextButton.setEnabled(true);
    }

    public String loadFragments(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.instructions_activity_fl_fragements, fragment, tag);
        ft.commit();
        return tag;
    }

    private void retrieveSensorFingerprint(){
        // Device Data
        // Device Model
        if(!Build.MODEL.isEmpty()){
            sensorFingerprint.setDeviceModel(Build.MODEL);
        }

        // Accelerometer Data
        // Accelerometer Model
        if(!mAccelerometer.getName().isEmpty()){
            sensorFingerprint.setSensorModel(mAccelerometer.getName());
        }
        // Accelerometer Vendor
        if(!mAccelerometer.getVendor().isEmpty()){
            sensorFingerprint.setSensorVendor(mAccelerometer.getVendor());
        }
        // Accelerometer Measurement Range
        sensorFingerprint.setSensorMeasurementRange(mAccelerometer.getMaximumRange());
        // Accelerometer Noise
        sensorFingerprint.setSensorNoise(sensorNoise);
        // Accelerometer Resolution
        sensorFingerprint.setSensorResolution(mAccelerometer.getResolution());
        // Accelerometer Raw Bias
        sensorFingerprint.setSensorRawBias(senorRawBias);
        switch (mTraceCount){
            case "Three":
                // Accelerometer Sensitivity
                sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ()));
                // Accelerometer Linearity
                sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ(), presenter.getThirdTrace().getAccelerometerZ()));
                // Accelerometer Midpoint
                sensorFingerprint.setAccelerometerMidPoint(Utils.calculateMidpoint(new SensorTrace[]{presenter.getFirstTrace(), presenter.getSecondTrace(), presenter.getThirdTrace()}));
                break;
            case "Two":
                // Accelerometer Sensitivity
                sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ()));
                // Accelerometer Linearity
                sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ(), 0));
                // Accelerometer Midpoint
                sensorFingerprint.setAccelerometerMidPoint(Utils.calculateMidpoint(new SensorTrace[]{presenter.getFirstTrace(), presenter.getSecondTrace()}));
                break;
            case "One":
                // Accelerometer Sensitivity
                sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace().getAccelerometerZ(), SensorManager.GRAVITY_EARTH * -1));
                // Accelerometer Linearity
                sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace().getAccelerometerZ(), SensorManager.GRAVITY_EARTH * -1, 0));
                // Accelerometer Midpoint
                sensorFingerprint.setAccelerometerMidPoint(presenter.getFirstTrace().returnArray());
                break;
        }

        // Gyroscope Data
        // Gyroscope Model
        if(!mGyroscope.getName().isEmpty()){
            sensorFingerprint.setGyroscopeModel(mGyroscope.getName());
        }
        // Gyroscope Vendor
        if(!mGyroscope.getVendor().isEmpty()){
            sensorFingerprint.setGyroscopeVendor(mGyroscope.getVendor());
        }
        // Gyroscope Measurement Range
        sensorFingerprint.setGyroscopeMeasurementRange(mGyroscope.getMaximumRange());
        // Gyroscope Resolution
        sensorFingerprint.setGyroscopeResolution(mGyroscope.getResolution());
    }

    private void writeToFile(String sensor, float[] events){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        try {
            mFileWriter.write(String.format("%s; %s; %f; %f; %f\n", simpleDateFormat.format(new Date()),sensor,events[0],events[1],events[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile(){
        // Create File
        Format f = new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss", Locale.getDefault());
        mFile = new File(getStorageDir(), Build.MODEL + "mobile_sensorData_" + f.format(new Date()) + ".csv");
        // Write File Header
        try {
            mFileWriter = new FileWriter(mFile);
            mFileWriter.write(String.format("%s; %s; %s; %s; %s\n", "Time", "Sensor", "X-Axis", "Y-Axis", "Z-Axis"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopWritingData(){
        // Stop Writing to File
        try {
            mFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStorageDir() {
        return Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
    }

    private void uploadFileToFireStorage(){
        // Upload File to Firebase
        Uri sensorDataFile = Uri.fromFile(mFile);
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/sensorfingerprint_measurements/" + sensorDataFile.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("text/csv")
                .setCustomMetadata("device", Build.ID)
                .build();
        UploadTask uploadTask = sensorDataRef.putFile(sensorDataFile, metadata);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InstructionsActivity.this, "Measurements upload failed.", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(InstructionsActivity.this, "Measurements uploaded successfully.", Toast.LENGTH_LONG).show();
            }
        });

    }
}
