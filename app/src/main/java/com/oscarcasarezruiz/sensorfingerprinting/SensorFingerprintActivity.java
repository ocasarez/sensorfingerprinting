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
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.presenter.SensorFingerprintActivityPresenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;


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
    private File mLocalFile;
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
        FirebaseApp.initializeApp(this.getApplicationContext());
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
        switch (view.getId()){
            case R.id.sensorfingerprint_activity_btn_startover:
                presenter.startOverButtonClicked();
                break;
            case R.id.sensorfingerprint_activity_btn_correctIdentification:
                presenter.correctDeviceButtonClicked();
                break;
            case R.id.sensorfingerprint_activity_btn_wrongIdentification:
                presenter.wrongDeviceButtonClicked();
                break;
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
    public void updateScoreResult(float score) {
        mMatchScore.setText("Match Score: " + (100f - score));
    }

    @Override
    public void navigateToFeatures() {
        Intent intent = new Intent(this, FeatureActivity.class);
        startActivity(intent);
    }

    @Override
    public void logCorrectMatch() {
        updateLogFileOnFirebaseStorage("True");
    }

    @Override
    public void logWrongMatch() {
        updateLogFileOnFirebaseStorage("False");
    }

    private void initViews(){
        updateActionBarTitle();
        findViewById(R.id.sensorfingerprint_activity_btn_startover).setOnClickListener(this);
        findViewById(R.id.sensorfingerprint_activity_btn_wrongIdentification).setOnClickListener(this);
        findViewById(R.id.sensorfingerprint_activity_btn_correctIdentification).setOnClickListener(this);
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
                    float score = Float.MAX_VALUE;
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(!documentSnapshot.exists()){
                                break;
                            }
                            currentSensorFingerprint = new SensorFingerprint(documentSnapshot.getData());
                            Log.d(TAG, "fromCloud: sensorFingerprint: " + currentSensorFingerprint.toString());
                            float currentScore = currentSensorFingerprint.compareSensorFingerprint(mSensorFingerprint);
                            Log.d(TAG, "readSenorFingerprints: currentScore: " + currentScore);
                            if(currentScore <= score){
                                score = currentScore;
                                matchFound = true;
                                resultSensorFingerprint = currentSensorFingerprint;
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    presenter.updateFingerprintResult(matchFound);
                    float finalScore = resultSensorFingerprint.compareSensorFingerprint(mSensorFingerprint);
                    presenter.updateFingerprintScoreResult(finalScore);
                    presenter.updateSensorFingerprint(resultSensorFingerprint);
                });
    }

    public void downloadLogFile(String result){
        mLocalFile = new File(getStorageDir(), "ComparisonLogs.csv");
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/comparison_logs/ComparisonLogs.csv");
        sensorDataRef.getFile(mLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: File Downloaded Successfully");
                Toast.makeText(SensorFingerprintActivity.this, "File Download Succeeded.", Toast.LENGTH_LONG).show();
                writeFingerprintToFile(result);
                uploadFileToFireStorage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onSuccess: File Downloaded Failed");
                Toast.makeText(SensorFingerprintActivity.this, "File Download Failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getStorageDir() {
        return Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
    }

    private void uploadFileToFireStorage(){
        // Upload File to Firebase
        Uri sensorDataFile = Uri.fromFile(mLocalFile);
        StorageReference sensorDataRef = mStorageReference.child("mobile-sensordata/comparison_logs/ComparisonLogs.csv");
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("text/csv")
                .build();
        UploadTask uploadTask = sensorDataRef.putFile(sensorDataFile, metadata);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onSuccess: File Uploaded Failed");
                Toast.makeText(SensorFingerprintActivity.this, "File Upload Failed.", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: File Uploaded Successfully");
                Toast.makeText(SensorFingerprintActivity.this, "File Upload Succeeded", Toast.LENGTH_LONG).show();
                mLocalFile.delete();
            }
        });
    }

    public void updateLogFileOnFirebaseStorage(String result){
        downloadLogFile(result);
    }

    @SuppressLint({"DefaultLocale", "LongLogTag"})
    public void writeFingerprintToFile(String result){
        try {
            Scanner scanner = new Scanner(mLocalFile);
            while(scanner.hasNextLine()){
                Log.d(TAG, "File Contents: => " + scanner.nextLine());
            }
            scanner.close();
            mFileWriter = new FileWriter(mLocalFile, true);
            mFileWriter.write(String.format("\n%s,%s,%s,%s,%f,%f,%f,%f,%f,%f,%f,%f,%s,%s",
                    mSensorFingerprint.getDeviceModel(),
                    mSensorFingerprint.getDeviceMfg(),
                    mSensorFingerprint.getSensorModel(),
                    mSensorFingerprint.getSensorVendor(),
                    mSensorFingerprint.getSensorSensitivity(),
                    mSensorFingerprint.getSensorLinearity(),
                    mSensorFingerprint.getSensorNoise()[2],
                    mSensorFingerprint.getSensorRawBias()[2],
                    mSensorFingerprint.getAccelerometerAvg()[2],
                    mSensorFingerprint.getAccelerometerMin()[2],
                    mSensorFingerprint.getAccelerometerMax()[2],
                    mSensorFingerprint.getAccelerometerStandardDev()[2],
                    presenter.getSensorFingerprint().getUUID(),
                    result)); // Write new fingerprint
            mFileWriter.close();
            scanner = new Scanner(mLocalFile);
            while(scanner.hasNextLine()){
                Log.d(TAG, "File Contents: => " + scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}