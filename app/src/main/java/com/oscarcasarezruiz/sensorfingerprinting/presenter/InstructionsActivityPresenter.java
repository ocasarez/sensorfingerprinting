package com.oscarcasarezruiz.sensorfingerprinting.presenter;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;

public class InstructionsActivityPresenter {

    private View mView;
    private SensorTrace firstTrace;
    private SensorTrace secondTrace;
    private SensorTrace thirdTrace;

    public InstructionsActivityPresenter (View view){
        mView = view;
        firstTrace = new SensorTrace();
        secondTrace = new SensorTrace();
        thirdTrace = new SensorTrace();
    }

    public void saveFirstTrace(float[] trace){
        firstTrace.setAccelerometerX(trace[0]);
        firstTrace.setAccelerometerY(trace[1]);
        firstTrace.setAccelerometerZ(trace[2]);
        mView.showFirstTrace(String.format("[%f,\n%f\n,%f]", trace[0], trace[1], trace[2]));
    }

    public void saveSecondTrace(float[] trace){
        secondTrace.setAccelerometerX(trace[0]);
        secondTrace.setAccelerometerY(trace[1]);
        secondTrace.setAccelerometerZ(trace[2]);
        mView.showSecondTrace(String.format("[%f,\n%f\n,%f]", trace[0], trace[1], trace[2]));
    }

    public void saveThirdTrace(float[] trace){
        thirdTrace.setAccelerometerX(trace[0]);
        thirdTrace.setAccelerometerY(trace[1]);
        thirdTrace.setAccelerometerZ(trace[2]);
        mView.showThirdTrace(String.format("[%f,\n%f\n,%f]", trace[0], trace[1], trace[2]));
    }

    public void collectTraceButtonClicked(){
        mView.collectTrace();
    }

    public void nextInstructionButtonClicked(){
        mView.nextInstruction();
    }

    public void sensorFingerprintResultsButtonClicked(){
        mView.navigateToSensorFingerprintResult();
    }

    public SensorTrace getFirstTrace() {
        return firstTrace;
    }

    public SensorTrace getSecondTrace() {
        return secondTrace;
    }

    public SensorTrace getThirdTrace() {
        return thirdTrace;
    }

    public interface View {
        void updateActionBarTitle();
        void showFirstTrace(String s);
        void showSecondTrace(String s);
        void showThirdTrace(String s);
        void collectTrace();
        void nextInstruction();
        void navigateToSensorFingerprintResult();
    }
}
