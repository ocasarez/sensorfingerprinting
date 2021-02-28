package com.oscarcasarezruiz.sensorfingerprinting.models;

public class SensorTrace {
    
    private float mMeasurementX;
    private float mMeasurementY;
    private float mMeasurementZ;
    
    public SensorTrace(){
        this.mMeasurementX = 0;
        this.mMeasurementY = 0;
        this.mMeasurementZ = 0;
    }
    
    public SensorTrace(float mAccelerometerX, float mAccelerometerY, float mMeasurementZ){
        this.mMeasurementX = mAccelerometerX;
        this.mMeasurementY = mAccelerometerY;
        this.mMeasurementZ = mMeasurementZ;
    }

    public float getAccelerometerX() {
        return mMeasurementX;
    }

    public void setAccelerometerX(float mAccelerometerX) {
        this.mMeasurementX = mAccelerometerX;
    }

    public float getAccelerometerY() {
        return mMeasurementY;
    }

    public void setAccelerometerY(float mAccelerometerY) {
        this.mMeasurementY = mAccelerometerY;
    }

    public float getAccelerometerZ() {
        return mMeasurementZ;
    }

    public void setAccelerometerZ(float mAccelerometerZ) {
        this.mMeasurementZ = mAccelerometerZ;
    }

    public float[] returnArray(){
        return new float[]{mMeasurementX, mMeasurementY, mMeasurementZ};
    }

    @Override
    public String toString() {
        return "SensorTrace{" +
                "X=" + this.mMeasurementX +
                ", Y=" + this.mMeasurementY +
                ", Z=" + this.mMeasurementZ +
                '}';
    }

}
