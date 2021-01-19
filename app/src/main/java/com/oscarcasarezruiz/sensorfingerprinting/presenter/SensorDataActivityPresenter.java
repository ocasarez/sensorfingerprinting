package com.oscarcasarezruiz.sensorfingerprinting.presenter;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorInfo;

public class SensorDataActivityPresenter {

    private SensorInfo sensorInfo;
    private View mView;

    public SensorDataActivityPresenter(View view){
        this.sensorInfo = new SensorInfo();
        this.mView = view;
    }

    public void loadSensorInfo(SensorInfo info){
        this.sensorInfo = info;
        mView.showSensorInfo(this.sensorInfo);
    }

    public void updateSensorNoise(float[] noise){
        this.sensorInfo.setSensorNoise(noise);
        mView.showSensorInfo(this.sensorInfo);
    }

    public void updateSensorRawBias(float[] bias){
        this.sensorInfo.setSensorRawBias(bias);
        mView.showSensorInfo(this.sensorInfo);
    }

    public void updateUploadState(String s){
        mView.updateUploadStateView(s);
    }

    public void startButtonClicked(){
        mView.resetAndStartStopWatch();
    }

    public void stopButtonClicked(){
        mView.stopStopWatch();
    }

    public interface View {
        void updateActionBarTitle();
        void resetAndStartStopWatch();
        void stopStopWatch();
        void showSensorInfo(SensorInfo info);
        void updateUploadStateView(String s);
    }
}
