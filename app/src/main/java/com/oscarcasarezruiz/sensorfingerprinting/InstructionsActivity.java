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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.oscarcasarezruiz.sensorfingerprinting.fragments.FirstInstructionFragment;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.InstructionsActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;
import java.util.ArrayList;

public class InstructionsActivity extends AppCompatActivity implements InstructionsActivityPresenter.View, View.OnClickListener, SensorEventListener {

    private static final String TAG = "InstructionsActivity";
    InstructionsActivityPresenter presenter;
    private String mCurrentFragment;

    // Motion Sensor
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private ArrayList<Float> accelerometerMeasurement;
    private SensorFingerprint sensorFingerprint;
    private float senorRawBias;
    private float sensorMinimum = Float.MAX_VALUE;
    private float sensorMaximum = -Float.MAX_VALUE;
    private float sensorAverage;
    private float sensorStdev;

    //UI Properties
    private TextView mTraceView1;
    private Button mCollectAndNextButton;
    private boolean mIsLogging;
    private boolean mIdentify;

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
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.instructions_activity_btn_collectAndNext){
            String buttonText = mCollectAndNextButton.getText().toString();
            if(buttonText.equals("Collect Trace")){
                mCollectAndNextButton.setEnabled(false);
                presenter.collectTraceButtonClicked();
            } else {
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
                float ZValues = sensorEvent.values[2];
                accelerometerMeasurement.add(ZValues);
                updateMinValueArr(ZValues);
                updateMaxValueArr(ZValues);
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
        sensorAverage = Utils.AverageTracesMeasured(accelerometerMeasurement);
        sensorStdev = Utils.standardDeviationMeasurements(accelerometerMeasurement);
        senorRawBias = Utils.calculateSensorBias(sensorAverage);
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
        // Accelerometer Data
        // Accelerometer Model
        if(!mAccelerometer.getName().isEmpty()){
            sensorFingerprint.setSensorModel(mAccelerometer.getName());
        }
        // Accelerometer Vendor
        if(!mAccelerometer.getVendor().isEmpty()){
            sensorFingerprint.setSensorVendor(mAccelerometer.getVendor());
        }
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
        sensorFingerprint.setSensorSensitivity(Utils.calculateSensorSensitivity(presenter.getFirstTrace(), SensorManager.GRAVITY_EARTH * -1));
        // Accelerometer Linearity
        sensorFingerprint.setSensorLinearity(Utils.calculateSensorLinearity(presenter.getFirstTrace(), SensorManager.GRAVITY_EARTH * -1, 0));
    }

    private void updateMinValueArr(float data){
        // Z-Values
        if(data < sensorMinimum){
            sensorMinimum = data;
        }
    }

    private void updateMaxValueArr(float data){
        // Z-Values
        if(data > sensorMaximum){
            sensorMaximum = data;
        }
    }
}
