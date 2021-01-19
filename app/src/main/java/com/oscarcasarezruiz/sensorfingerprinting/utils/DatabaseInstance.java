package com.oscarcasarezruiz.sensorfingerprinting.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Sensor Fingerprint
 * "firebaseID" : {
 *     "deviceModel": String,
 *     "sensorModel": String,
 *     "sensorVendor": String,
 *     "sensorMeasurementRange": float,
 *     "sensorNoise" : List
 *     "sensorResolution": float,
 *     "sensorSensitivity": float,
 *     "sensorLinearity": float,
 *     "sensorRawBias: List
 * }
 */

public class DatabaseInstance {

    private final String TAG = "DatabaseInstance";
    private final String COLLECTION_PATH = "sensorFingerprints";

    FirebaseFirestore db;

    public DatabaseInstance(){
        db = FirebaseFirestore.getInstance();
    }

    public void writeNewSensorFingerprint(SensorFingerprint fingerprint){
        db.collection(COLLECTION_PATH).document(Long.toString(new Date().getTime())).set(fingerprint.convertSensorFingerprintToHashMap());
    }

    public ArrayList<SensorFingerprint> readSenorFingerprintBySensorVendor(String mSensorVendor){
        Log.d(TAG, "readSenorFingerprintBySensorVendor: Entered");
        ArrayList<SensorFingerprint> matches = new ArrayList<>();
        db.collection(COLLECTION_PATH)
                .whereEqualTo("sensorVendor", mSensorVendor)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Task Completed Successfully.");
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                Map<String, Object> sensorFingerprint = documentSnapshot.getData();
                                matches.add(new SensorFingerprint(sensorFingerprint));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        Log.d(TAG, "readSenorFingerprintBySensorVendor: Results: " + matches.toArray().toString());
        return matches;
    }

}
