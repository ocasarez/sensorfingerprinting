package com.oscarcasarezruiz.sensorfingerprinting.presenter;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;

public class InstructionsActivityPresenter {

    private View mView;
    private SensorTrace firstTrace;

    public InstructionsActivityPresenter (View view){
        mView = view;
        firstTrace = new SensorTrace();
    }

    public void saveFirstTrace(float[] trace){
        firstTrace.setAccelerometerX(trace[0]);
        firstTrace.setAccelerometerY(trace[1]);
        firstTrace.setAccelerometerZ(trace[2]);
        mView.showFirstTrace(String.format("[%f,\n%f\n,%f]", trace[0], trace[1], trace[2]));
    }


    public void collectTraceButtonClicked(){
        mView.collectTrace();
    }

    public void sensorFingerprintResultsButtonClicked(){
        mView.navigateToSensorFingerprintResult();
    }

    public SensorTrace getFirstTrace() {
        return firstTrace;
    }

    public interface View {
        void updateActionBarTitle();
        void showFirstTrace(String s);
        void collectTrace();
        void navigateToSensorFingerprintResult();
    }
}
