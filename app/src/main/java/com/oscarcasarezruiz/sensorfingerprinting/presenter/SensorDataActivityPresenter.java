package com.oscarcasarezruiz.sensorfingerprinting.presenter;

public class SensorDataActivityPresenter {

    private View mView;

    public SensorDataActivityPresenter(View view){
        this.mView = view;
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
        void updateUploadStateView(String s);
    }
}
