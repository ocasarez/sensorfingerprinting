package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorFingerprintActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.DatabaseInstance;
import java.util.ArrayList;

public class SensorFingerprintActivity extends AppCompatActivity implements SensorFingerprintActivityPresenter.View, View.OnClickListener {

    private final String TAG = "SensorFingerprintAct";

    SensorFingerprintActivityPresenter presenter;

    // UI Elements
    private TextView mResult;
    private TextView mDeviceModel;
    private TextView mSensorModel;
    private TextView mSensorVendor;
    private TextView mSensorMeasurementRange;
    private TextView mSensorNoise;
    private TextView mSensorResolution;
    private TextView mSensorSensitivity;
    private TextView mSensorLinearity;
    private TextView mSensorRawBias;

    // Data
    SensorFingerprint sensorFingerprint;

    // Db
    DatabaseInstance db;
    ArrayList<SensorFingerprint> fingerprints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fingerprint);
        initViews();
        Bundle data = getIntent().getExtras();
        sensorFingerprint = (SensorFingerprint) data.getParcelable("sensorFingerprint");

        db = new DatabaseInstance();
        presenter = new SensorFingerprintActivityPresenter(this);
        presenter.updateSensorFingerprint(sensorFingerprint);
        if(checkMatches()){ // Match found.
            presenter.updateFingerprintResult(true);
        } else { // No existing matches found.
            presenter.updateFingerprintResult(false);
            db.writeNewSensorFingerprint(sensorFingerprint);
        }

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sensorfingerprint_activity_btn_startover){
            presenter.startOverButtonClicked();
        }
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.sensorfingerprint_title);
    }

    @Override
    public void updateSensorFingerprintView(SensorFingerprint fingerprint) {
        float[] noise = fingerprint.getSensorNoise();
        float[] rawBias = fingerprint.getSensorRawBias();

        mDeviceModel.setText(fingerprint.getDeviceModel());
        mSensorModel.setText(fingerprint.getSensorModel());
        mSensorVendor.setText(fingerprint.getSensorVendor());
        mSensorMeasurementRange.setText(String.format("%f", sensorFingerprint.getSensorMeasurementRange()));
        mSensorNoise.setText(String.format("[%f,\n%f,\n%f]", noise[0], noise[1], noise[2]));
        mSensorResolution.setText(String.format("%f", fingerprint.getSensorResolution()));
        mSensorSensitivity.setText(String.format("%f", fingerprint.getSensorSensitivity()));
        mSensorLinearity.setText(String.format("%f", fingerprint.getSensorLinearity()));
        mSensorRawBias.setText(String.format("[%f,\n%f,\n%f]", rawBias[0], rawBias[1], rawBias[2]));
    }

    @Override
    public void updateFingerprintResultView(String s) {
        mResult.append(s);
    }

    @Override
    public void navigateToFeatures() {
        Intent intent = new Intent(this, FeatureActivity.class);
        startActivity(intent);
    }

    private void initViews(){
        updateActionBarTitle();
        findViewById(R.id.sensorfingerprint_activity_btn_startover).setOnClickListener(this);
        mResult = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_result);
        mDeviceModel = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_deviceModelValue);
        mSensorModel = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorModelValue);
        mSensorVendor = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorVendorValue);
        mSensorMeasurementRange = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorMeasurementRangeValue);
        mSensorNoise = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorNoiseValue);
        mSensorResolution = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorResolutionValue);
        mSensorSensitivity = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorSensitivityValue);
        mSensorLinearity = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorLinearityValue);
        mSensorRawBias = (TextView) findViewById(R.id.sensorfingerprint_activity_tv_sensorRawBiasValue);
    }

    private boolean checkMatches(){
        boolean fingerprintExists = false;
        fingerprints = db.readSenorFingerprintBySensorVendor(sensorFingerprint.getSensorVendor());
        Log.d(TAG, "checkMatches: this.sensorFingerprint= " + sensorFingerprint.toString());
        for (SensorFingerprint fingerprint : fingerprints) {
            fingerprintExists = sensorFingerprint.compareSensorFingerprint(fingerprint);
        }
        return fingerprintExists;
    }

}