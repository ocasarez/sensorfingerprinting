package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.FirstInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.SecondInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.ThirdInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.InstructionsActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.util.ArrayList;

public class InstructionsActivity extends AppCompatActivity implements InstructionsActivityPresenter.View, View.OnClickListener, SensorEventListener {

    InstructionsActivityPresenter presenter;
    private String mCurrentFragment;

    // Motion Sensor
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private ArrayList<float[]> accelerometerMeasurement;
    private SensorFingerprint sensorFingerprint;
    private float[] sensorNoise;
    private float[] senorRawBias;

    //UI Properties
    private TextView mTraceView1;
    private TextView mTraceView2;
    private TextView mTraceView3;
    private Button mCollectAndNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        initViews();
        initSensor();

        presenter = new InstructionsActivityPresenter(this);
        accelerometerMeasurement = new ArrayList<>();
        sensorFingerprint = new SensorFingerprint();

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
                } else { // Next
                    presenter.nextInstructionButtonClicked();
                    mCollectAndNextButton.setText(R.string.instructions_collectTrace);
                }
            } else if (mCurrentFragment.equals("SecondInstructions")){
                if(buttonText.equals("Collect Trace")){
                    mCollectAndNextButton.setEnabled(false);
                    presenter.collectTraceButtonClicked();
                } else { // Next
                    presenter.nextInstructionButtonClicked();
                    mCollectAndNextButton.setText(R.string.instructions_collectTrace);
                }
            } else {
                if(buttonText.equals("Collect Trace")){
                    mCollectAndNextButton.setEnabled(false);
                    presenter.collectTraceButtonClicked();
                } else { // Sensor Fingerprint Results
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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        // Stop Collecting Traces after 20 seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSensorDataCollection();
            }
        }, 10000);
    }

    @Override
    public void nextInstruction() {
        if(mCurrentFragment.equals("FirstInstructions")){
            mCurrentFragment = loadFragments(new SecondInstructionFragment(), "SecondInstructions");
        } else {
            mCurrentFragment = loadFragments(new ThirdInstructionFragment(), "ThirdInstructions");
        }
    }

    @Override
    public void navigateToSensorFingerprintResult() {
        retrieveSensorFingerprint();
        Intent intent = new Intent(this, SensorFingerprintActivity.class);
        intent.putExtra("sensorFingerprint", sensorFingerprint);
        startActivity(intent);
    }

    // Sensor Functions
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        if(sensorType == Sensor.TYPE_ACCELEROMETER){
            accelerometerMeasurement.add(sensorEvent.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void initViews(){
        updateActionBarTitle();
        mTraceView1 = (TextView) findViewById(R.id.firstInstructions_traceResult);
        mTraceView2 = (TextView) findViewById(R.id.secondInstructions_traceResult);
        mTraceView3 = (TextView) findViewById(R.id.thirdInstructions_traceResult);

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
    }

    private void stopSensorDataCollection(){
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
        if(mCurrentFragment.equals("ThirdInstructions")){
            mCollectAndNextButton.setText(R.string.instructions_goToSensorFingerprint);
        } else {
            mCollectAndNextButton.setText(R.string.instructions_next);
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
        // Device Model
        if(!Build.MODEL.isEmpty()){
            sensorFingerprint.setDeviceModel(Build.MODEL);
        }
        // Sensor Model
        if(!mAccelerometer.getName().isEmpty()){
            sensorFingerprint.setSensorModel(mAccelerometer.getName());
        }
        // Sensor Vendor
        if(!mAccelerometer.getVendor().isEmpty()){
            sensorFingerprint.setSensorVendor(mAccelerometer.getVendor());
        }
        // Sensor Measurement Range
        sensorFingerprint.setSensorMeasurementRange(mAccelerometer.getMaximumRange());
        // Sensor Noise
        sensorFingerprint.setSensorNoise(sensorNoise);
        // Sensor Resolution
        sensorFingerprint.setSensorResolution(mAccelerometer.getResolution());
        // Senor Raw Bias
        sensorFingerprint.setSensorRawBias(senorRawBias);
        // Sensor Sensitivity
        sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ()));
        // Sensor Linearity
        sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace().getAccelerometerZ(), presenter.getSecondTrace().getAccelerometerZ(), presenter.getThirdTrace().getAccelerometerZ()));
    }
}