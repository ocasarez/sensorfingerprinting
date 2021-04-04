package com.oscarcasarezruiz.sensorfingerprinting.presenter;

public class FeatureActivityPresenter {

    private View  mView;

    public FeatureActivityPresenter(View view){
        this.mView = view;
    }

    public void sensorDataButtonClicked(){
        mView.navigateToSensorDate();
    }

    public void sensorFingerprintButtonClicked(){
        mView.navigateToSensorFingerprint();
    }

    public void sensorIdentifyDeviceButtonClicked(){
        mView.navigateToIdentifyDevice();
    }

    public interface View {
        void updateActionBarTitle();
        void navigateToSensorDate();
        void navigateToSensorFingerprint();
        void navigateToIdentifyDevice();
    }
}
