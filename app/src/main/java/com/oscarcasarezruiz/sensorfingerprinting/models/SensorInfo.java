package com.oscarcasarezruiz.sensorfingerprinting.models;

import java.util.Arrays;

public class SensorInfo {

    private float[] mSensorRawBias;
    private String mSensorModel;
    private String mSensorVendor;
    private float mSensorMeasurementRange;
    private float[] mSensorNoise;
    private float mSensorResolution;

    public SensorInfo() {
        mSensorRawBias = new float[3];
        mSensorNoise = new float[3];
    }

    public SensorInfo(float[] mSensorRawBias, String mSensorModel, String mSensorVendor, float mSensorMeasurementRange, float[] mSensorNoise, float mSensorResolution) {
        this.mSensorRawBias = mSensorRawBias;
        this.mSensorModel = mSensorModel;
        this.mSensorVendor = mSensorVendor;
        this.mSensorMeasurementRange = mSensorMeasurementRange;
        this.mSensorNoise = mSensorNoise;
        this.mSensorResolution = mSensorResolution;
    }

    public float[] getSensorRawBias() {
        return mSensorRawBias;
    }

    public void setSensorRawBias(float[] mSensorRawBias) {
        this.mSensorRawBias = mSensorRawBias;
    }

    public String getSensorModel() {
        return mSensorModel;
    }

    public void setSensorModel(String mSensorModel) {
        this.mSensorModel = mSensorModel;
    }

    public String getSensorVendor() {
        return mSensorVendor;
    }

    public void setSensorVendor(String mSensorVendor) {
        this.mSensorVendor = mSensorVendor;
    }

    public float getSensorMeasurementRange() {
        return mSensorMeasurementRange;
    }

    public void setSensorMeasurementRange(float mSensorMeasurementRange) {
        this.mSensorMeasurementRange = mSensorMeasurementRange;
    }

    public float[] getSensorNoise() {
        return mSensorNoise;
    }

    public void setSensorNoise(float[] mSensorNoise) {
        this.mSensorNoise = mSensorNoise;
    }

    public float getSensorResolution() {
        return mSensorResolution;
    }

    public void setSensorResolution(float mSensorResolution) {
        this.mSensorResolution = mSensorResolution;
    }

    @Override
    public String toString() {
        return "SensorInfo{" +
                "mSensorRawBias=" + mSensorRawBias +
                ", mSensorModel='" + mSensorModel + '\'' +
                ", mSensorVendor='" + mSensorVendor + '\'' +
                ", mSensorMeasurementRange=" + mSensorMeasurementRange +
                ", mSensorNoise=" + Arrays.toString(mSensorNoise) +
                ", mSensorResolution=" + mSensorResolution +
                '}';
    }
}
