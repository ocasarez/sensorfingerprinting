package com.oscarcasarezruiz.sensorfingerprinting.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SensorFingerprint implements Parcelable {

    private String TAG = "SensorFingerprint";

    private String mUUID;
    // Device Data
    private String mDeviceModel;
    private String mDeviceMfg;
    private String mDeviceOS;
    // Accelerometer Data
    private String mAccelerometerModel;
    private String mAccelerometerVendor;
    private float mAccelerometerSensitivity;
    private float mAccelerometerLinearity;
    private float[] mAccelerometerNoise;
    private float[] mAccelerometerBias;
    private float[] mAccelerometerAvg;
    private float[] mAccelerometerMin;
    private float[] mAccelerometerMax;
    private float[] mAccelerometerStandardDev;

    public SensorFingerprint(){
        mAccelerometerNoise = new float[3];
        mAccelerometerBias = new float[3];
        mAccelerometerAvg = new float[3];
        mAccelerometerMin = new float[3];
        mAccelerometerMax = new float[3];
        mAccelerometerStandardDev = new float[3];
    }

    public SensorFingerprint(Map<String, Object> document){
        this.mUUID = (String) document.get("Fingerprint ID");
        // Device Data
        this.mDeviceModel = (String) ((Map) document.get("Device Data")).get("Model");
        this.mDeviceMfg = (String) ((Map) document.get("Device Data")).get("Manufacturer");
        this.mDeviceOS = (String) ((Map) document.get("Device Data")).get("OS Version");
        // Accelerometer Data
        this.mAccelerometerModel = (String) ((Map) document.get("Accelerometer Data")).get("Model");
        this.mAccelerometerVendor = (String) ((Map) document.get("Accelerometer Data")).get("Vendor");
        this.mAccelerometerLinearity = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Linearity"));
        this.mAccelerometerSensitivity = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Sensitivity"));
        ArrayList<Double> arrayListNoise = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Noise");
        ArrayList<Double> arrayListBias = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Bias");
        ArrayList<Double> arrayListAverage = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Average");
        ArrayList<Double> arrayListMinimum = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Minimum");
        ArrayList<Double> arrayListMaximum = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Maximum");
        ArrayList<Double> arrayListStandardDev = (ArrayList<Double>) ((Map) document.get("Accelerometer Data")).get("Standard Deviation");
        this.mAccelerometerNoise = new float[3];
        this.mAccelerometerAvg = new float[3];
        this.mAccelerometerBias = new float[3];
        this.mAccelerometerMin = new float[3];
        this.mAccelerometerMax = new float[3];
        this.mAccelerometerStandardDev = new float[3];
        for(int i = 0; i < 3; i++){
            this.mAccelerometerNoise[i] = Utils.convertDoubleToFloat(arrayListNoise.get(i));
            this.mAccelerometerBias[i] = Utils.convertDoubleToFloat(arrayListBias.get(i));
            this.mAccelerometerAvg[i] = Utils.convertDoubleToFloat(arrayListAverage.get(i));
            this.mAccelerometerMin[i] = Utils.convertDoubleToFloat(arrayListMinimum.get(i));
            this.mAccelerometerMax[i] = Utils.convertDoubleToFloat(arrayListMaximum.get(i));
            this.mAccelerometerStandardDev[i] = Utils.convertDoubleToFloat(arrayListStandardDev.get(i));
        }
    }

    protected SensorFingerprint(Parcel in) {
        // Device Data
        mDeviceModel = in.readString();
        mDeviceMfg = in.readString();
        mDeviceOS = in.readString();
        // Accelerometer Data
        mAccelerometerModel = in.readString();
        mAccelerometerVendor = in.readString();
        mAccelerometerNoise = in.createFloatArray();
        mAccelerometerSensitivity = in.readFloat();
        mAccelerometerLinearity = in.readFloat();
        mAccelerometerBias = in.createFloatArray();
        mAccelerometerAvg = in.createFloatArray();
        mAccelerometerMin = in.createFloatArray();
        mAccelerometerMax = in.createFloatArray();
        mAccelerometerStandardDev = in.createFloatArray();

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
        docData.put("Fingerprint ID", this.mUUID);
        // Device Data
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("Model", this.mDeviceModel);
        deviceData.put("Manufacturer", this.mDeviceMfg);
        deviceData.put("OS Version", this.mDeviceOS);
        // Accelerometer Data
        Map<String, Object> accelerometerData = new HashMap<>();
        accelerometerData.put("Model", this.mAccelerometerModel);
        accelerometerData.put("Vendor", this.mAccelerometerVendor);
        accelerometerData.put("Noise", Arrays.asList(this.mAccelerometerNoise[0], this.mAccelerometerNoise[1], this.mAccelerometerNoise[2]));
        accelerometerData.put("Sensitivity", this.mAccelerometerSensitivity);
        accelerometerData.put("Linearity", this.mAccelerometerLinearity);
        accelerometerData.put("Bias", Arrays.asList(this.mAccelerometerBias[0], this.mAccelerometerBias[1], this.mAccelerometerBias[2]));
        accelerometerData.put("Average", Arrays.asList(this.mAccelerometerAvg[0],this.mAccelerometerAvg[1], this.mAccelerometerAvg[2]));
        accelerometerData.put("Minimum", Arrays.asList(this.mAccelerometerMin[0],this.mAccelerometerMin[1], this.mAccelerometerMin[2]));
        accelerometerData.put("Maximum", Arrays.asList(this.mAccelerometerMax[0],this.mAccelerometerMax[1], this.mAccelerometerMax[2]));
        accelerometerData.put("Standard Deviation", Arrays.asList(this.mAccelerometerStandardDev[0],this.mAccelerometerStandardDev[1], this.mAccelerometerStandardDev[2]));

        docData.put("Device Data", deviceData);
        docData.put("Accelerometer Data", accelerometerData);
        return docData;
    }

    public int compareSensorFingerprint(SensorFingerprint o){
        int weight = 0;
        float percentage = 0.05f;

        // Device Data
        // Model - 1
        if(this.mDeviceModel.equals(o.getDeviceModel())){
            weight += 1;
        }
        // Manufacturer - 1
        if(this.mDeviceMfg.equals(o.getDeviceMfg())){
            weight += 1;
        }
        // OS - 1
        if(this.mDeviceOS.equals(o.getDeviceOS())){
            weight += 1;
        }

        // Accelerometer Data
        // Model - 1
        if(this.mAccelerometerModel.equals(o.getSensorModel())){
            weight += 1;
        }
        // Vendor - 1
        if(this.mAccelerometerVendor.equals(o.getSensorVendor())){
            weight += 1;
        }
        // Noise - 1
        boolean compareZ = Utils.percentageRange("Noise - Z", this.mAccelerometerNoise[2], o.getSensorNoise()[2], percentage);
        if(compareZ){
            weight += 1;
        }
        // Minimum - 2
        compareZ = Utils.percentageRange("Minimum - Z", this.mAccelerometerMin[2], o.getAccelerometerMin()[2], percentage);
        if(compareZ){
            weight += 2;
        }
        // Maximum - 2
        compareZ = Utils.percentageRange("Maximum - Z", this.mAccelerometerMax[2], o.getAccelerometerMax()[2], percentage);
        if(compareZ){
            weight += 2;
        }
        // Bias - 4
        compareZ = Utils.percentageRange("Bias - Z", this.mAccelerometerBias[2], o.getSensorRawBias()[2], percentage);
        if(compareZ){
            weight += 4;
        }
        // Standard Deviation - 8
        compareZ = Utils.percentageRange("Standard Deviation - Z", this.mAccelerometerStandardDev[2], o.getAccelerometerStandardDev()[2], percentage);
        if(compareZ){
            weight += 8;
        }
        // Average - 8
        compareZ = Utils.percentageRange("Average - Z", this.mAccelerometerAvg[2], o.getAccelerometerAvg()[2], percentage);
        if(compareZ){
            weight += 8;
        }
        // Sensitivity - 10
        if(Utils.percentageRange("Sensitivity", this.mAccelerometerSensitivity, o.getSensorSensitivity(), percentage)){
            weight += 10;
        }
        // Linearity - 10
        if(Utils.percentageRange("Linearity", this.mAccelerometerLinearity, o.getSensorLinearity(), percentage)){
            weight += 10;
        }

        return weight;
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
        return mAccelerometerModel;
    }

    public void setSensorModel(String mSensorModel) {
        this.mAccelerometerModel = mSensorModel;
    }

    public String getSensorVendor() {
        return mAccelerometerVendor;
    }

    public void setSensorVendor(String mSensorVendor) {
        this.mAccelerometerVendor = mSensorVendor;
    }

    public float[] getSensorNoise() {
        return mAccelerometerNoise;
    }

    public void setSensorNoise(float[] mSensorNoise) {
        this.mAccelerometerNoise = mSensorNoise;
    }

    public float getSensorSensitivity() {
        return mAccelerometerSensitivity;
    }

    public void setSensorSensitivity(float mSensorSensitivity) {
        this.mAccelerometerSensitivity = mSensorSensitivity;
    }

    public float getSensorLinearity() {
        return mAccelerometerLinearity;
    }

    public void setSensorLinearity(float mSensorLinearity) {
        this.mAccelerometerLinearity = mSensorLinearity;
    }

    public float[] getSensorRawBias() {
        return mAccelerometerBias;
    }

    public void setSensorRawBias(float[] mSensorRawBias) {
        this.mAccelerometerBias = mSensorRawBias;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String mUUID) {
        this.mUUID = mUUID;
    }


    public String getDeviceMfg() {
        return mDeviceMfg;
    }

    public void setDeviceMfg(String mDeviceMfg) {
        this.mDeviceMfg = mDeviceMfg;
    }

    public String getDeviceOS() {
        return mDeviceOS;
    }

    public void setDeviceOS(String mDeviceOS) {
        this.mDeviceOS = mDeviceOS;
    }

    public float[] getAccelerometerAvg() {
        return mAccelerometerAvg;
    }

    public void setAccelerometerAvg(float[] mAccelerometerAvg) {
        this.mAccelerometerAvg = mAccelerometerAvg;
    }

    public float[] getAccelerometerMin() {
        return mAccelerometerMin;
    }

    public void setAccelerometerMin(float[] mAccelerometerMin) {
        this.mAccelerometerMin = mAccelerometerMin;
    }

    public float[] getAccelerometerMax() {
        return mAccelerometerMax;
    }

    public void setAccelerometerMax(float[] mAccelerometerMax) {
        this.mAccelerometerMax = mAccelerometerMax;
    }

    public float[] getAccelerometerStandardDev() {
        return mAccelerometerStandardDev;
    }

    public void setAccelerometerStandardDev(float[] mAccelerometerStandardDev) {
        this.mAccelerometerStandardDev = mAccelerometerStandardDev;
    }

    @Override
    public String toString() {
        return "SensorFingerprint" + '\n' +
                "Fingerprint ID\n" + mUUID + '\n' +
                "DeviceModel\n" + mDeviceModel + '\n' +
                "DeviceMfg\n" + mDeviceMfg + '\n' +
                "DeviceOS\n" + mDeviceOS + '\n' +
                "AccelerometerModel\n" + mAccelerometerModel + '\n' +
                "AccelerometerVendor\n" + mAccelerometerVendor + '\n' +
                "AccelerometerSensitivity\n" + mAccelerometerSensitivity + '\n' +
                "AccelerometerLinearity\n" + mAccelerometerLinearity + '\n' +
                "AccelerometerNoise\n" + Arrays.toString(mAccelerometerNoise) + '\n' +
                "AccelerometerBias\n" + Arrays.toString(mAccelerometerBias) + '\n' +
                "AccelerometerAvg\n" + Arrays.toString(mAccelerometerAvg) + '\n' +
                "AccelerometerMin\n" + Arrays.toString(mAccelerometerMin) + '\n' +
                "AccelerometerMax\n" + Arrays.toString(mAccelerometerMax) + '\n' +
                "AccelerometerStandardDev\n" + Arrays.toString(mAccelerometerStandardDev);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Device Data
        dest.writeString(mDeviceModel);
        dest.writeString(mDeviceMfg);
        dest.writeString(mDeviceOS);
        // Accelerometer Data
        dest.writeString(mAccelerometerModel);
        dest.writeString(mAccelerometerVendor);
        dest.writeFloatArray(mAccelerometerNoise);
        dest.writeFloat(mAccelerometerSensitivity);
        dest.writeFloat(mAccelerometerLinearity);
        dest.writeFloatArray(mAccelerometerBias);
        dest.writeFloatArray(mAccelerometerAvg);
        dest.writeFloatArray(mAccelerometerMin);
        dest.writeFloatArray(mAccelerometerMax);
        dest.writeFloatArray(mAccelerometerStandardDev);
    }
}
