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
    private SensorManager mSensorManager;
    private ArrayList<float[]> accelerometerMeasurement;
    private SensorFingerprint sensorFingerprint;
    private float[] sensorNoise;
    private float[] senorRawBias;
    private float[] sensorMinimum = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
    private float[] sensorMaximum = new float[]{-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
    private float[] sensorAverage;
    private float[] sensorStdev;

    //UI Properties
    private TextView mTraceView1;
    private Button mCollectAndNextButton;
    private boolean mIsLogging;
    private boolean mIdentify;

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
        mIdentify = data.getBoolean("Identify");
        initViews();
        initSensor();
        mIsLogging = false;

        presenter = new InstructionsActivityPresenter(this);
        accelerometerMeasurement = new ArrayList<>();
        sensorFingerprint = new SensorFingerprint();

        FirebaseApp.initializeApp(this.getApplicationContext());
        //Init Firebase Storage
        mStorageReference = FirebaseStorage.getInstance().getReference();
        createFile();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.instructions_activity_btn_collectAndNext){
            String buttonText = mCollectAndNextButton.getText().toString();
            if(buttonText.equals("Collect Trace")){
                mCollectAndNextButton.setEnabled(false);
                presenter.collectTraceButtonClicked();
            } else {
                stopWritingData();
                uploadFileToFireStorage();
                presenter.sensorFingerprintResultsButtonClicked();
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
    public void collectTrace() {
        Handler handler = new Handler();
        handler.postAtTime(this::startSensorDataCollection, 2000);
        handler.postDelayed(this::stopSensorDataCollection, 15000);
    }

    @Override
    public void navigateToSensorFingerprintResult() {
        retrieveSensorFingerprint();
        Intent intent = new Intent(this, SensorFingerprintActivity.class);
        intent.putExtra("sensorFingerprint", sensorFingerprint);
        intent.putExtra("Identify", mIdentify);
        startActivity(intent);
    }

    // Sensor Functions
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(mIsLogging){
            int SensorType = sensorEvent.sensor.getType();
            if(SensorType == Sensor.TYPE_ACCELEROMETER){
                float[] data = sensorEvent.values;
                accelerometerMeasurement.add(data);
                updateMinValueArr(data);
                updateMaxValueArr(data);
                writeToFile("TYPE_ACCELEROMETER", data);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void initViews(){
        updateActionBarTitle();
        mTraceView1 = (TextView) findViewById(R.id.firstInstructions_traceResult);
        mCollectAndNextButton = (Button) findViewById(R.id.instructions_activity_btn_collectAndNext);
        mCollectAndNextButton.setOnClickListener(this);
        mCurrentFragment = loadFragments(new FirstInstructionFragment(), "FirstInstructions");
    }

    private void saveTrace(){
        presenter.saveFirstTrace(Utils.AverageTracesMeasured(accelerometerMeasurement));
        sensorNoise = Utils.calculateSensorNoise(accelerometerMeasurement);
        sensorAverage = Utils.AverageTracesMeasured(accelerometerMeasurement);
        sensorStdev = Utils.standardDeviationMeasurements(accelerometerMeasurement);
        senorRawBias = Utils.calculateSensorBias(new SensorTrace(sensorAverage[0], sensorAverage[1], sensorAverage[2]));
        accelerometerMeasurement.clear();
    }

    private void initSensor(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void startSensorDataCollection(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        mIsLogging = true;
    }

    private void stopSensorDataCollection(){
        mIsLogging = false;
        mSensorManager.unregisterListener(this);
        saveTrace();
        mCollectAndNextButton.setText(R.string.instructions_goToSensorFingerprint);
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
        // Device Manufacturer
        if(!Build.MANUFACTURER.isEmpty()){
            sensorFingerprint.setDeviceMfg(Build.MANUFACTURER);
        }
        // Device OS
        sensorFingerprint.setDeviceOS(Utils.getDeviceOS());

        // Accelerometer Data
        // Accelerometer Model
        if(!mAccelerometer.getName().isEmpty()){
            sensorFingerprint.setSensorModel(mAccelerometer.getName());
        }
        // Accelerometer Vendor
        if(!mAccelerometer.getVendor().isEmpty()){
            sensorFingerprint.setSensorVendor(mAccelerometer.getVendor());
        }
        // Accelerometer Noise
        sensorFingerprint.setSensorNoise(sensorNoise);
        // Accelerometer Minimum
        sensorFingerprint.setAccelerometerMin(sensorMinimum);
        // Accelerometer Maximum
        sensorFingerprint.setAccelerometerMax(sensorMaximum);
        // Accelerometer Standard Deviation
        sensorFingerprint.setAccelerometerStandardDev(sensorStdev);
        // Accelerometer Average
        sensorFingerprint.setAccelerometerAvg(sensorAverage);
        // Accelerometer Bias
        sensorFingerprint.setSensorRawBias(senorRawBias);
        // Accelerometer Sensitivity
        sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace().getAccelerometerZ(), SensorManager.GRAVITY_EARTH * -1));
        // Accelerometer Linearity
        sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace().getAccelerometerZ(), SensorManager.GRAVITY_EARTH * -1, 0));
    }

    private void writeToFile(String sensor, float[] events){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            mFileWriter.write(String.format("%s; %s; %f; %f; %f\n", sdf.format(new Date()), sensor, events[0], events[1], events[2]));
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

    private void updateMinValueArr(float[] data){
        // X-Values
        if(data[0] < sensorMinimum[0]){
            sensorMinimum[0] = data[0];
        }
        // Y-Values
        if(data[1] < sensorMinimum[1]){
            sensorMinimum[1] = data[1];
        }
        // Z-Values
        if(data[2] < sensorMinimum[2]){
            sensorMinimum[2] = data[2];
        }
    }

    private void updateMaxValueArr(float[] data){
        // X-Values
        if(data[0] > sensorMaximum[0]){
            sensorMaximum[0] = data[0];
        }
        // Y-Values
        if(data[1] > sensorMaximum[1]){
            sensorMaximum[1] = data[1];
        }
        // Z-Values
        if(data[2] > sensorMaximum[2]){
            sensorMaximum[2] = data[2];
        }
    }
}
