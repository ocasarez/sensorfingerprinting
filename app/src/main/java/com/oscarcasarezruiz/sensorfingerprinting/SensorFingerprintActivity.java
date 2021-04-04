package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorFingerprintActivityPresenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class SensorFingerprintActivity extends AppCompatActivity implements SensorFingerprintActivityPresenter.View, View.OnClickListener {


    private static final String TAG = "SensorFingerprintActivity";
    private SensorFingerprintActivityPresenter presenter;
    private final String COLLECTION_PATH = "sensorFingerprints";

    // UI Elements
    private TextView mResult;
    private TextView mSensorFingerprintResult;
    private TextView mMatchScore;

    // Database
    private FirebaseFirestore db;

    // Fingerprint
    private SensorFingerprint mSensorFingerprint;
    private boolean mIdentify;
    // File Writer
    private FileWriter mFileWriter;
    private File mFile;
    // Firebase Storage Ref
    private StorageReference mStorageReference;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_fingerprint);
        initViews();
        Bundle data = getIntent().getExtras();
        mSensorFingerprint = data.getParcelable("sensorFingerprint");
        mIdentify = data.getBoolean("Identify");
        Log.d(TAG, "SensorFingerprint Local: " + mSensorFingerprint.toString());

        // Init Database
        db = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        presenter = new SensorFingerprintActivityPresenter(this);

        if(mIdentify){ // Identify Device
            identifyDevice();
        } else { // Create new Fingerprint
            mSensorFingerprint.setUUID(UUID.randomUUID().toString());
            writeNewSensorFingerprint();
            presenter.updateSensorFingerprint(mSensorFingerprint);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sensorfingerprint_activity_btn_startover){
            presenter.startOverButtonClicked();
        } else if (view.getId() == R.id.sensorfingerprint_activity_btn_exportFingerprint){
            presenter.exportButtonClicked();
        }
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.sensorfingerprint_title);
    }

    @Override
    public void updateSensorFingerprintView(SensorFingerprint fingerprint) {
        mSensorFingerprintResult.setText(fingerprint.toString());
    }

    @Override
    public void updateFingerprintResultView(String s) {
        mResult.append(s);
    }

    @Override
    public void updateScoreResult(int score) {
        int percentageScore = (score * 2);
        mMatchScore.setText("Match Score: " + percentageScore + " %");
    }

    @Override
    public void navigateToFeatures() {
        Intent intent = new Intent(this, FeatureActivity.class);
        startActivity(intent);
    }

    private void initViews(){
        updateActionBarTitle();
        findViewById(R.id.sensorfingerprint_activity_btn_startover).setOnClickListener(this);
        findViewById(R.id.sensorfingerprint_activity_btn_exportFingerprint).setOnClickListener(this);
        mResult = findViewById(R.id.sensorfingerprint_activity_tv_result);
        mSensorFingerprintResult = findViewById(R.id.sensorfingerprint_activity_tv_fingerprintResult);
        mMatchScore = findViewById(R.id.sensorfingerprint_activity_tv_matchScore);
    }

    private void writeNewSensorFingerprint(){
        db.collection(COLLECTION_PATH + "Smartphone").document(Long.toString(new Date().getTime())).set(mSensorFingerprint.convertSensorFingerprintToHashMap());
    }

    @SuppressLint("LongLogTag")
    private void identifyDevice(){
        db.collection(COLLECTION_PATH + "Smartphone")
                .get()
                .addOnCompleteListener(task -> {
                    boolean matchFound = false;
                    SensorFingerprint currentSensorFingerprint;
                    SensorFingerprint resultSensorFingerprint = new SensorFingerprint();
                    int score = 0;
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(!documentSnapshot.exists()){
                                break;
                            }
                            currentSensorFingerprint = new SensorFingerprint(documentSnapshot.getData());
                            Log.d(TAG, "fromCloud: sensorFingerprint: " + currentSensorFingerprint.toString());
                            int currentScore = currentSensorFingerprint.compareSensorFingerprint(mSensorFingerprint);
                            Log.d(TAG, "readSenorFingerprints: currentScore: " + currentScore);
                            if(currentScore > score){
                                score = currentScore;
                                matchFound = true;
                                resultSensorFingerprint = currentSensorFingerprint;
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    presenter.updateFingerprintResult(matchFound);
                    int finalScore = resultSensorFingerprint.compareSensorFingerprint(mSensorFingerprint);
                    presenter.updateFingerprintScoreResult(finalScore);
                    presenter.updateSensorFingerprint(resultSensorFingerprint);
                });
    }

    @Override
    public void exportFingerprint(){
        Map<String, Object> fingerprintMap = presenter.getSensorFingerprint().convertSensorFingerprintToHashMap();
        Gson gson = new Gson();
        String json = gson.toJson(fingerprintMap);

        // Create File
        mFile = new File(getStorageDir(), fingerprintMap.get("UUID") + ".json");
        // Write File
        try {
            mFileWriter = new FileWriter(mFile);
            BufferedWriter bw = new BufferedWriter(mFileWriter);
            bw.write(json);
            bw.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        // Upload to Firebase Storage
        uploadFileToFireStorage();
    }

    private String getStorageDir() {
        return Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
    }

    private void uploadFileToFireStorage(){
        // Upload File to Firebase
        Uri sensorDataFile = Uri.fromFile(mFile);
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/sensorfingerprints/" + sensorDataFile.getLastPathSegment());
        UploadTask uploadTask = sensorDataRef.putFile(sensorDataFile);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SensorFingerprintActivity.this, "Measurements upload failed.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SensorFingerprintActivity.this, "Measurements uploaded successfully.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}