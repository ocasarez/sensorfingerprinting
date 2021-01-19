package com.oscarcasarezruiz.sensorfingerprinting.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorFingerprint implements Parcelable {

    private final String TAG = "SensorFingerprint";

    private String mDeviceModel;
    private String mSensorModel;
    private String mSensorVendor;
    private float mSensorMeasurementRange;
    private float[] mSensorNoise;
    private float mSensorResolution;
    private float mSensorSensitivity;
    private float mSensorLinearity;
    private float[] mSensorRawBias;

    public SensorFingerprint(){
        mSensorNoise = new float[3];
        mSensorRawBias = new float[3];
    }

    public SensorFingerprint(Map<String, Object> document){
        this.mDeviceModel = (String) document.get("deviceModel");
        this.mSensorModel = (String) document.get("sensorModel");
        this.mSensorVendor = (String) document.get("sensorVendor");
        this.mSensorMeasurementRange = Utils.convertDoubleToFloat((Double) document.get("sensorMeasurementRange"));
        List<Double> doubleList1 = (List<Double>) document.get("sensorNoise");
        this.mSensorNoise = new float[doubleList1.size()];
        int i = 0;
        for(Double d : doubleList1){
            float f = Utils.convertDoubleToFloat(d);
            this.mSensorNoise[i++] = f;
        }
        this.mSensorResolution = Utils.convertDoubleToFloat((Double)document.get("sensorResolution"));
        this.mSensorSensitivity = Utils.convertDoubleToFloat((Double)document.get("sensorSensitivity"));
        this.mSensorLinearity = Utils.convertDoubleToFloat((Double) document.get("sensorLinearity"));
        List<Double> doubleList2 = (List<Double>) document.get("sensorNoise");
        this.mSensorRawBias = new float[doubleList2.size()];
        i = 0;
        for(Double d: doubleList2){
            float f = Utils.convertDoubleToFloat(d);
            this.mSensorRawBias[i++] = f;
        }
    }

    protected SensorFingerprint(Parcel in) {
        mDeviceModel = in.readString();
        mSensorModel = in.readString();
        mSensorVendor = in.readString();
        mSensorMeasurementRange = in.readFloat();
        mSensorNoise = in.createFloatArray();
        mSensorResolution = in.readFloat();
        mSensorSensitivity = in.readFloat();
        mSensorLinearity = in.readFloat();
        mSensorRawBias = in.createFloatArray();
    }

    public static final Creator<SensorFingerprint> CREATOR = new Creator<SensorFingerprint>() {
        @Override
        public SensorFingerprint createFromParcel(Parcel in) {
            return new SensorFingerprint(in);
        }

        @Override
        public SensorFingerprint[] newArray(int size) {
            return new SensorFingerprint[size];
        }
    };

    public Map<String, Object> convertSensorFingerprintToHashMap(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("deviceModel", this.mDeviceModel);
        docData.put("sensorModel", this.mSensorModel);
        docData.put("sensorVendor", this.mSensorVendor);
        docData.put("sensorMeasurementRange", this.mSensorMeasurementRange);
        docData.put("sensorNoise", Arrays.asList(this.mSensorNoise[0], this.mSensorNoise[1], this.mSensorNoise[2]));
        docData.put("sensorResolution", this.mSensorResolution);
        docData.put("sensorSensitivity", this.mSensorSensitivity);
        docData.put("sensorLinearity", this.mSensorLinearity);
        docData.put("sensorRawBias", Arrays.asList(this.mSensorRawBias[0], this.mSensorRawBias[1], this.mSensorRawBias[2]));

        return docData;
    }

    /**
     * Setters and Getters
     */
    public String getDeviceModel() {
        return mDeviceModel;
    }

    public void setDeviceModel(String mDeviceModel) {
        this.mDeviceModel = mDeviceModel;
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

    public float getSensorSensitivity() {
        return mSensorSensitivity;
    }

    public void setSensorSensitivity(float mSensorSensitivity) {
        this.mSensorSensitivity = mSensorSensitivity;
    }

    public float getSensorLinearity() {
        return mSensorLinearity;
    }

    public void setSensorLinearity(float mSensorLinearity) {
        this.mSensorLinearity = mSensorLinearity;
    }

    public float[] getSensorRawBias() {
        return mSensorRawBias;
    }

    public void setSensorRawBias(float[] mSensorRawBias) {
        this.mSensorRawBias = mSensorRawBias;
    }

    public boolean compareSensorFingerprint(SensorFingerprint sensorFingerprint){
        Log.d(TAG, "compareSensorFingerprint: Sensor Fingerprint to be compared: " + sensorFingerprint.toString());
        int numberOfMatches = 0;
        int weight = 0;
        // Sensor Sensitivity - 9
        if(Utils.percentageRange(this.mSensorSensitivity, sensorFingerprint.getSensorSensitivity(), 0.025f)){
            numberOfMatches++;
            weight += 9;
        }
        // Sensor Linearity - 8
        if(Utils.percentageRange(this.mSensorLinearity, sensorFingerprint.getSensorLinearity(), 0.025f)){
            numberOfMatches++;
            weight += 8;
        }
        // Device Model - 7
        if(this.mDeviceModel.equals(sensorFingerprint.getDeviceModel())){
            numberOfMatches++;
            weight += 7;
        }
        // Sensor Model - 6
        if(this.mSensorModel.equals(sensorFingerprint.getSensorModel())){
            numberOfMatches++;
            weight += 6;
        }
        // Sensor Vendor - 5
        if(this.mSensorVendor.equals(sensorFingerprint.getSensorVendor())){
            numberOfMatches++;
            weight += 5;
        }
        // Sensor Measurement Range - 4
        if(Utils.percentageRange(this.mSensorMeasurementRange, sensorFingerprint.getSensorMeasurementRange(), 0.025f)){
            numberOfMatches++;
            weight += 4;
        }
        // Sensor Noise - 3
        boolean compareX = Utils.percentageRange(this.mSensorNoise[0], sensorFingerprint.getSensorNoise()[0], 0.025f);
        boolean compareY = Utils.percentageRange(this.mSensorNoise[1], sensorFingerprint.getSensorNoise()[1], 0.025f);
        boolean compareZ = Utils.percentageRange(this.mSensorNoise[2], sensorFingerprint.getSensorNoise()[2], 0.025f);
        if(compareX && compareY && compareZ){
            numberOfMatches++;
            weight += 3;
        }
        // Sensor Resolution - 2
        if(Utils.percentageRange(this.mSensorResolution, sensorFingerprint.getSensorResolution(), 0.025f)){
            numberOfMatches++;
            weight += 2;
        }
        // Raw Bias - 1
        compareX = Utils.percentageRange(this.mSensorRawBias[0], sensorFingerprint.getSensorRawBias()[0], 0.025f);
        compareY = Utils.percentageRange(this.mSensorRawBias[1], sensorFingerprint.getSensorRawBias()[1], 0.025f);
        compareZ = Utils.percentageRange(this.mSensorRawBias[2], sensorFingerprint.getSensorRawBias()[2], 0.025f);
        if(compareX && compareY && compareZ){
            numberOfMatches++;
            weight += 1;
        }

        Log.d(TAG, "compareSensorFingerprint: numberOfMatches: " + numberOfMatches);
        Log.d(TAG, "compareSensorFingerprint: weight: " + weight);
        if(numberOfMatches == 9){
            return true;
        } else if (numberOfMatches == 0){
            return  false;
        } else if (numberOfMatches >= 7 && weight > 36){
            return true;
        } else if (numberOfMatches >= 4 && weight > 30){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SensorFingerprint{" +
                ", mDeviceModel='" + mDeviceModel + '\'' +
                ", mSensorModel='" + mSensorModel + '\'' +
                ", mSensorVendor='" + mSensorVendor + '\'' +
                ", mSensorMeasurementRange=" + mSensorMeasurementRange +
                ", mSensorNoise=" + Arrays.toString(mSensorNoise) +
                ", mSensorResolution=" + mSensorResolution +
                ", mSensorSensitivity=" + mSensorSensitivity +
                ", mSensorLinearity=" + mSensorLinearity +
                ", mSensorRawBias=" + Arrays.toString(mSensorRawBias) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDeviceModel);
        dest.writeString(mSensorModel);
        dest.writeString(mSensorVendor);
        dest.writeFloat(mSensorMeasurementRange);
        dest.writeFloatArray(mSensorNoise);
        dest.writeFloat(mSensorResolution);
        dest.writeFloat(mSensorSensitivity);
        dest.writeFloat(mSensorLinearity);
        dest.writeFloatArray(mSensorRawBias);
    }
}
