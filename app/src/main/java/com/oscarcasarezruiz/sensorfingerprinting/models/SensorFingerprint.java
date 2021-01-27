package com.oscarcasarezruiz.sensorfingerprinting.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SensorFingerprint implements Parcelable {

    private String mUUID;
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
        this.mUUID = (String) document.get("UUID");
        this.mDeviceModel = (String) document.get("deviceModel");
        this.mSensorModel = (String) document.get("sensorModel");
        this.mSensorVendor = (String) document.get("sensorVendor");
        this.mSensorMeasurementRange = Utils.convertDoubleToFloat((Double) document.get("sensorMeasurementRange"));
        ArrayList<Double> doubleList1 = (ArrayList<Double>) document.get("sensorNoise");
        this.mSensorNoise = new float[doubleList1.size()];
        int i = 0;
        for(Double d : doubleList1){
            this.mSensorNoise[i++] = Utils.convertDoubleToFloat(d);
        }
        this.mSensorResolution = Utils.convertDoubleToFloat((Double)document.get("sensorResolution"));
        this.mSensorSensitivity = Utils.convertDoubleToFloat((Double)document.get("sensorSensitivity"));
        this.mSensorLinearity = Utils.convertDoubleToFloat((Double) document.get("sensorLinearity"));
        ArrayList<Double> doubleList2 = (ArrayList<Double>) document.get("sensorRawBias");
        this.mSensorRawBias = new float[doubleList2.size()];
        i = 0;
        for(Double d: doubleList2){
            this.mSensorRawBias[i++] = Utils.convertDoubleToFloat(d);
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
        docData.put("UUID", this.mUUID);
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

    public boolean compareSensorFingerprint(SensorFingerprint o){
        int numberOfMatches = 0;
        int weight = 0;
        float percentage = 0.0001f;
        // Sensor Sensitivity - 9
        if(Utils.percentageRange(this.mSensorSensitivity, o.getSensorSensitivity(), percentage)){
            numberOfMatches++;
            weight += 9;
        }
        // Sensor Linearity - 8
        if(Utils.percentageRange(this.mSensorLinearity, o.getSensorLinearity(), percentage)){
            numberOfMatches++;
            weight += 8;
        }
        // Device Model - 7
        if(this.mDeviceModel.equals(o.getDeviceModel())){
            numberOfMatches++;
            weight += 7;
        }
        // Sensor Model - 6
        if(this.mSensorModel.equals(o.getSensorModel())){
            numberOfMatches++;
            weight += 6;
        }
        // Sensor Vendor - 5
        if(this.mSensorVendor.equals(o.getSensorVendor())){
            numberOfMatches++;
            weight += 5;
        }
        // Sensor Measurement Range - 4
        if(Utils.percentageRange(this.mSensorMeasurementRange, o.getSensorMeasurementRange(), percentage)){
            numberOfMatches++;
            weight += 4;
        }
        // Sensor Noise - 3
        boolean compareX = Utils.percentageRange(this.mSensorNoise[0], o.getSensorNoise()[0], percentage);
        boolean compareY = Utils.percentageRange(this.mSensorNoise[1], o.getSensorNoise()[1], percentage);
        boolean compareZ = Utils.percentageRange(this.mSensorNoise[2], o.getSensorNoise()[2], percentage);
        if(compareX && compareY && compareZ){
            numberOfMatches++;
            weight += 3;
        }
        // Sensor Resolution - 2
        if(Utils.percentageRange(this.mSensorResolution, o.getSensorResolution(), percentage)){
            numberOfMatches++;
            weight += 2;
        }
        // Raw Bias - 1
        compareX = Utils.percentageRange(this.mSensorRawBias[0], o.getSensorRawBias()[0], percentage);
        compareY = Utils.percentageRange(this.mSensorRawBias[1], o.getSensorRawBias()[1], percentage);
        compareZ = Utils.percentageRange(this.mSensorRawBias[2], o.getSensorRawBias()[2], percentage);
        if(compareX && compareY && compareZ){
            numberOfMatches++;
            weight += 1;
        }

        if(numberOfMatches == 9){
            return true;
        } else return numberOfMatches >= 7 || weight > 30;
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

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    @Override
    public String toString() {
        return "SensorFingerprint{" +
                "\n  mDeviceModel='" + mDeviceModel + '\'' +
                "\n, mSensorModel='" + mSensorModel + '\'' +
                "\n, mSensorVendor='" + mSensorVendor + '\'' +
                "\n, mSensorMeasurementRange=" + mSensorMeasurementRange +
                "\n, mSensorNoise=" + Arrays.toString(mSensorNoise) +
                "\n, mSensorResolution=" + mSensorResolution +
                "\n, mSensorSensitivity=" + mSensorSensitivity +
                "\n, mSensorLinearity=" + mSensorLinearity +
                "\n, mSensorRawBias=" + Arrays.toString(mSensorRawBias) +
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
