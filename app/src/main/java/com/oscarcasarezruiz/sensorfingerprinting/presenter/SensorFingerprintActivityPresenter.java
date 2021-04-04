package com.oscarcasarezruiz.sensorfingerprinting.presenter;

import android.annotation.SuppressLint;
import android.util.Log;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;

public class SensorFingerprintActivityPresenter {

    private final String TAG = "SensorFingerprintActivityPresenter";

    private SensorFingerprint mSensorFingerprint;
    private View mView;

    public SensorFingerprintActivityPresenter (View view){
        mSensorFingerprint = new SensorFingerprint();
        this.mView = view;
    }

    @SuppressLint("LongLogTag")
    public void updateFingerprintResult(boolean result){
        if(result){ // Match Found
            Log.d(TAG, "updateFingerprintResult: Existing Entry");
            mView.updateFingerprintResultView(" Existing Entry");
        } else { // No match found.
            Log.d(TAG, "updateFingerprintResult: New Entry");
            mView.updateFingerprintResultView(" New Entry");
        }
    }

    public void updateFingerprintScoreResult(int score){
        mView.updateScoreResult(score);
    }

    public void updateSensorFingerprint(SensorFingerprint sensorFingerprint){
        this.mSensorFingerprint = sensorFingerprint;
        mView.updateSensorFingerprintView(this.mSensorFingerprint);
    }

    public void startOverButtonClicked(){
        mView.navigateToFeatures();
    }

    public void exportButtonClicked() {mView.exportFingerprint();}

    public SensorFingerprint getSensorFingerprint(){
        return mSensorFingerprint;
    }

    public interface View {
        void updateActionBarTitle();
        void updateSensorFingerprintView(SensorFingerprint sensorFingerprint);
        void updateFingerprintResultView(String s);
        void updateScoreResult(int score);
        void navigateToFeatures();
        void exportFingerprint();
    }

}
