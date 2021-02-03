package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorFingerprintActivityPresenter;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class SensorFingerprintActivity extends AppCompatActivity implements SensorFingerprintActivityPresenter.View, View.OnClickListener {


    private static final String TAG = "SensorFingerprintActivity";
    private SensorFingerprintActivityPresenter presenter;
    private final String COLLECTION_PATH = "sensorFingerprints";

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
    private TextView mUUID;

    // Database
    private FirebaseFirestore db;

    // Fingerprint
    private SensorFingerprint mSensorFingerprint;

    private String mTraceCount;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fingerprint);
        initViews();
        Bundle data = getIntent().getExtras();
        mSensorFingerprint = data.getParcelable("sensorFingerprint");
        mTraceCount = data.getString("TraceCount");
        Log.d(TAG, "SensorFingerprint Local: " + mSensorFingerprint.toString());

        // Init Database
        db = FirebaseFirestore.getInstance();

        presenter = new SensorFingerprintActivityPresenter(this);
        readSenorFingerprints();
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

        mUUID.setText(fingerprint.getUUID());
        mDeviceModel.setText(fingerprint.getDeviceModel());
        mSensorModel.setText(fingerprint.getSensorModel());
        mSensorVendor.setText(fingerprint.getSensorVendor());
        mSensorMeasurementRange.setText(String.format("%f", fingerprint.getSensorMeasurementRange()));
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
        mResult = findViewById(R.id.sensorfingerprint_activity_tv_result);
        mDeviceModel = findViewById(R.id.sensorfingerprint_activity_tv_deviceModelValue);
        mSensorModel = findViewById(R.id.sensorfingerprint_activity_tv_sensorModelValue);
        mSensorVendor = findViewById(R.id.sensorfingerprint_activity_tv_sensorVendorValue);
        mSensorMeasurementRange = findViewById(R.id.sensorfingerprint_activity_tv_sensorMeasurementRangeValue);
        mSensorNoise = findViewById(R.id.sensorfingerprint_activity_tv_sensorNoiseValue);
        mSensorResolution = findViewById(R.id.sensorfingerprint_activity_tv_sensorResolutionValue);
        mSensorSensitivity = findViewById(R.id.sensorfingerprint_activity_tv_sensorSensitivityValue);
        mSensorLinearity = findViewById(R.id.sensorfingerprint_activity_tv_sensorLinearityValue);
        mSensorRawBias = findViewById(R.id.sensorfingerprint_activity_tv_sensorRawBiasValue);
        mUUID = findViewById(R.id.sensorfingerprint_activity_tv_UUIDValue);
    }

    private void writeNewSensorFingerprint(){
        db.collection(COLLECTION_PATH + "_" + mTraceCount + "Traces").document(Long.toString(new Date().getTime())).set(mSensorFingerprint.convertSensorFingerprintToHashMap());
    }

    @SuppressLint("LongLogTag")
    private void readSenorFingerprints(){
        db.collection(COLLECTION_PATH + "_" + mTraceCount + "Traces")
                .get()
                .addOnCompleteListener(task -> {
                    boolean matchFound = false;
                    int score = 0;
                    SensorFingerprint sensorFingerprint = new SensorFingerprint();
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(!documentSnapshot.exists()){
                                break;
                            }
                            sensorFingerprint = new SensorFingerprint(documentSnapshot.getData());
                            Log.d(TAG, "fromCloud: sensorFingerprint: " + sensorFingerprint.toString());
                            int currentScore = sensorFingerprint.compareSensorFingerprint(mSensorFingerprint);
                            Log.d(TAG, "readSenorFingerprints: currentScore: " + currentScore);
                            if(currentScore > score && currentScore >= 26){
                                score = currentScore;
                                matchFound = true;
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    presenter.updateFingerprintResult(matchFound);
                    if(!matchFound){
                        mSensorFingerprint.setUUID(UUID.randomUUID().toString());
                        writeNewSensorFingerprint();
                        presenter.updateSensorFingerprint(mSensorFingerprint);
                    } else {
                        presenter.updateSensorFingerprint(sensorFingerprint);
                    }
                });
    }

}