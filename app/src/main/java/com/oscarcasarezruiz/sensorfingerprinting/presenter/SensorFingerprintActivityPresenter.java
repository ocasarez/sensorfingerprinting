package com.oscarcasarezruiz.sensorfingerprinting.presenter;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;

public class SensorFingerprintActivityPresenter {

    private SensorFingerprint mSensorFingerprint;
    private View mView;

    public SensorFingerprintActivityPresenter (View view){
        mSensorFingerprint = new SensorFingerprint();
        this.mView = view;
    }

    public void updateFingerprintResult(boolean result){
        if(result){ // Match Found
            mView.updateFingerprintResultView(" Existing Entry");
        } else { // No match found.
            mView.updateFingerprintResultView(" New Entry");
        }
    }

    public void updateSensorFingerprint(SensorFingerprint sensorFingerprint){
        this.mSensorFingerprint = sensorFingerprint;
        mView.updateSensorFingerprintView(this.mSensorFingerprint);
    }

    public void startOverButtonClicked(){
        mView.navigateToFeatures();
    }


    public interface View {
        void updateActionBarTitle();
        void updateSensorFingerprintView(SensorFingerprint sensorFingerprint);
        void updateFingerprintResultView(String s);
        void navigateToFeatures();
    }
}
