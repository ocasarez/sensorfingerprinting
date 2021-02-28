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
    // Accelerometer Data
    private String mAccelerometerModel;
    private String mAccelerometerVendor;
    private float mAccelerometerMeasurementRange;
    private float mAccelerometerResolution;
    private float mAccelerometerSensitivity;
    private float mAccelerometerLinearity;
    private float[] mAccelerometerNoise;
    private float[] mAccelerometerRawBias;
    private float[] mAccelerometerMidPoint;
    // Gyroscope Data
    private String mGyroscopeModel;
    private String mGyroscopeVendor;
    private float mGyroscopeResolution;
    private float mGyroscopeMeasurementRange;
    // Linear Acceleration Data
    private float[] mFilteredAccelerometerData;


    public SensorFingerprint(){
        mAccelerometerNoise = new float[3];
        mAccelerometerRawBias = new float[3];
        mFilteredAccelerometerData = new float[3];
    }

    public SensorFingerprint(Map<String, Object> document){
        this.mUUID = (String) document.get("UUID");
        // Device Data
        this.mDeviceModel = (String) document.get("deviceModel");
        // Accelerometer Data
        this.mAccelerometerModel = (String) document.get("accelerometerModel");
        this.mAccelerometerVendor = (String) document.get("accelerometerVendor");
        this.mAccelerometerMeasurementRange = Utils.convertDoubleToFloat((Double) document.get("accelerometerMeasurementRange"));
        ArrayList<Double> doubleList1 = (ArrayList<Double>) document.get("accelerometerNoise");
        this.mAccelerometerNoise = new float[doubleList1.size()];
        int i = 0;
        for(Double d : doubleList1){
            this.mAccelerometerNoise[i++] = Utils.convertDoubleToFloat(d);
        }
        this.mAccelerometerResolution = Utils.convertDoubleToFloat((Double)document.get("accelerometerResolution"));
        this.mAccelerometerSensitivity = Utils.convertDoubleToFloat((Double)document.get("accelerometerSensitivity"));
        this.mAccelerometerLinearity = Utils.convertDoubleToFloat((Double) document.get("accelerometerLinearity"));
        ArrayList<Double> doubleList2 = (ArrayList<Double>) document.get("accelerometerRawBias");
        this.mAccelerometerRawBias = new float[doubleList2.size()];
        i = 0;
        for(Double d: doubleList2){
            this.mAccelerometerRawBias[i++] = Utils.convertDoubleToFloat(d);
        }
        ArrayList<Double> doubleList3 = (ArrayList<Double>) document.get("accelerometerMidPoint");
        this.mAccelerometerMidPoint = new float[doubleList3.size()];
        i = 0;
        for(Double d: doubleList3){
            this.mAccelerometerMidPoint[i++] = Utils.convertDoubleToFloat(d);
        }
        // Gyroscope Data
        this.mGyroscopeModel = (String) document.get("gyroscopeModel");
        this.mGyroscopeVendor = (String) document.get("gyroscopeVendor");
        this.mGyroscopeResolution = Utils.convertDoubleToFloat((Double)document.get("gyroscopeResolution"));
        this.mGyroscopeMeasurementRange = Utils.convertDoubleToFloat((Double) document.get("gyroscopeMeasurementRange"));
        ArrayList<Double> doubleList5 = (ArrayList<Double>) document.get("filteredAccelerometerData");
        this.mFilteredAccelerometerData =  new float[doubleList5.size()];
        i = 0;
        for(Double d: doubleList5){
            this.mFilteredAccelerometerData[i++] = Utils.convertDoubleToFloat(d);
        }
    }

    protected SensorFingerprint(Parcel in) {
        // Device Data
        mDeviceModel = in.readString();
        // Accelerometer Data
        mAccelerometerModel = in.readString();
        mAccelerometerVendor = in.readString();
        mAccelerometerMeasurementRange = in.readFloat();
        mAccelerometerNoise = in.createFloatArray();
        mAccelerometerResolution = in.readFloat();
        mAccelerometerSensitivity = in.readFloat();
        mAccelerometerLinearity = in.readFloat();
        mAccelerometerRawBias = in.createFloatArray();
        mAccelerometerMidPoint = in.createFloatArray();
        // Gyroscope Data
        mGyroscopeModel = in.readString();
        mGyroscopeVendor = in.readString();
        mGyroscopeResolution = in.readFloat();
        mGyroscopeMeasurementRange = in.readFloat();
        // Linear Acceleration Data
        mFilteredAccelerometerData = in.createFloatArray();
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
        // Device Data
        docData.put("deviceModel", this.mDeviceModel);
        // Accelerometer Data
        docData.put("accelerometerModel", this.mAccelerometerModel);
        docData.put("accelerometerVendor", this.mAccelerometerVendor);
        docData.put("accelerometerMeasurementRange", this.mAccelerometerMeasurementRange);
        docData.put("accelerometerNoise", Arrays.asList(this.mAccelerometerNoise[0], this.mAccelerometerNoise[1], this.mAccelerometerNoise[2]));
        docData.put("accelerometerResolution", this.mAccelerometerResolution);
        docData.put("accelerometerSensitivity", this.mAccelerometerSensitivity);
        docData.put("accelerometerLinearity", this.mAccelerometerLinearity);
        docData.put("accelerometerRawBias", Arrays.asList(this.mAccelerometerRawBias[0], this.mAccelerometerRawBias[1], this.mAccelerometerRawBias[2]));
        docData.put("accelerometerMidPoint", Arrays.asList(this.mAccelerometerMidPoint[0], this.mAccelerometerMidPoint[1], this.mAccelerometerMidPoint[2]));
        // Gyroscope Data
        docData.put("gyroscopeModel", this.mGyroscopeModel);
        docData.put("gyroscopeVendor", this.mGyroscopeVendor);
        docData.put("gyroscopeResolution", this.mGyroscopeResolution);
        docData.put("gyroscopeMeasurementRange", this.mGyroscopeMeasurementRange);
        // Linear Acceleration Data
        docData.put("filteredAccelerometerData", Arrays.asList(this.mFilteredAccelerometerData[0], this.mFilteredAccelerometerData[1], this.mFilteredAccelerometerData[2]));

        return docData;
    }

    public int compareSensorFingerprint(SensorFingerprint o){
        int weight = 0;
        float percentage = 0.05f;

        // Device Data
        // Device Model - 1
        if(this.mDeviceModel.equals(o.getDeviceModel())){
            weight += 1;
        }
        // Accelerometer Data
        // Accelerometer Sensitivity - 14
        if(Utils.percentageRange("Accelerometer Sensitivity", this.mAccelerometerSensitivity, o.getSensorSensitivity(), percentage)){
            weight += 14;
        }
        // Accelerometer Linearity - 13
        if(Utils.percentageRange("Accelerometer Linearity", this.mAccelerometerLinearity, o.getSensorLinearity(), percentage)){
            weight += 13;
        }
        // Accelerometer Model - 2
        if(this.mAccelerometerModel.equals(o.getSensorModel())){
            weight += 2;
        }
        // Accelerometer Vendor - 4
        if(this.mAccelerometerVendor.equals(o.getSensorVendor())){
            weight += 4;
        }
        // Accelerometer Measurement Range - 8
        if(Utils.percentageRange("Accelerometer Measurement Range", this.mAccelerometerMeasurementRange, o.getSensorMeasurementRange(), percentage)){
            weight += 8;
        }
        // Accelerometer Noise - 11
        boolean compareX = Utils.percentageRange("Accelerometer Noise X Axis", this.mAccelerometerNoise[0], o.getSensorNoise()[0], percentage);
        boolean compareY = Utils.percentageRange("Accelerometer Noise Y Axis", this.mAccelerometerNoise[1], o.getSensorNoise()[1], percentage);
        boolean compareZ = Utils.percentageRange("Accelerometer Noise Z Axis", this.mAccelerometerNoise[2], o.getSensorNoise()[2], percentage);
        if(compareX && compareY && compareZ){
            weight += 11;
        }
        // Accelerometer Resolution - 6
        if(Utils.percentageRange("Accelerometer Resolution", this.mAccelerometerResolution, o.getSensorResolution(), percentage)){
            weight += 6;
        }
        // Accelerometer Bias - 10
        compareX = Utils.percentageRange("Accelerometer Raw Bias X Axis", this.mAccelerometerRawBias[0], o.getSensorRawBias()[0], percentage);
        compareY = Utils.percentageRange("Accelerometer Raw Bias Y Axis", this.mAccelerometerRawBias[1], o.getSensorRawBias()[1], percentage);
        compareZ = Utils.percentageRange("Accelerometer Raw Bias Z Axis", this.mAccelerometerRawBias[2], o.getSensorRawBias()[2], percentage);
        if(compareX && compareY && compareZ){
            weight += 10;
        }
        // Accelerometer Midpoint - 12
        compareX = Utils.percentageRange("Accelerometer Midpoint X Axis", this.mAccelerometerMidPoint[0], o.getAccelerometerMidPoint()[0], percentage);
        compareY = Utils.percentageRange("Accelerometer Midpoint Y Axis", this.mAccelerometerMidPoint[1], o.getAccelerometerMidPoint()[1], percentage);
        compareZ = Utils.percentageRange("Accelerometer Midpoint Z Axis", this.mAccelerometerMidPoint[2], o.getAccelerometerMidPoint()[2], percentage);
        if(compareX && compareY && compareZ){
            weight += 12;
        }
        // Gyroscope Data
        // Gyroscope Model - 3
        if(this.mGyroscopeModel.equals(o.getGyroscopeModel())){
            weight += 3;
        }
        // Gyroscope Vendor - 5
        if(this.mGyroscopeVendor.equals(o.getGyroscopeVendor())){
            weight += 5;
        }
        // Gyroscope Resolution - 7
        if(Utils.percentageRange("Gyroscope Resolution", this.mGyroscopeResolution, o.getGyroscopeResolution(), percentage)){
            weight += 7;
        }
        // Gyroscope Measurement Range - 9
        if(Utils.percentageRange("Gyroscope Measurement Range", this.mGyroscopeMeasurementRange, o.getGyroscopeMeasurementRange(), percentage)){
            weight += 9;
        }
        // Filtered Acceleration Data - 15
        compareX = Utils.percentageRange("Filtered Acceleration Data X Axis", this.mFilteredAccelerometerData[0], o.getFilteredAccelerometerData()[0], percentage);
        compareY = Utils.percentageRange("Filtered Acceleration Data Y Axis", this.mFilteredAccelerometerData[1], o.getFilteredAccelerometerData()[1], percentage);
        compareZ = Utils.percentageRange("Filtered Acceleration Data Z Axis", this.mFilteredAccelerometerData[2], o.getFilteredAccelerometerData()[2], percentage);
        if(compareX && compareY && compareZ){
            weight += 15;
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

    public float getSensorMeasurementRange() {
        return mAccelerometerMeasurementRange;
    }

    public void setSensorMeasurementRange(float mSensorMeasurementRange) {
        this.mAccelerometerMeasurementRange = mSensorMeasurementRange;
    }

    public float[] getSensorNoise() {
        return mAccelerometerNoise;
    }

    public void setSensorNoise(float[] mSensorNoise) {
        this.mAccelerometerNoise = mSensorNoise;
    }

    public float getSensorResolution() {
        return mAccelerometerResolution;
    }

    public void setSensorResolution(float mSensorResolution) {
        this.mAccelerometerResolution = mSensorResolution;
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
        return mAccelerometerRawBias;
    }

    public void setSensorRawBias(float[] mSensorRawBias) {
        this.mAccelerometerRawBias = mSensorRawBias;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    public String getGyroscopeModel() {
        return mGyroscopeModel;
    }

    public void setGyroscopeModel(String mGyroscopeModel) {
        this.mGyroscopeModel = mGyroscopeModel;
    }

    public String getGyroscopeVendor() {
        return mGyroscopeVendor;
    }

    public void setGyroscopeVendor(String mGyroscopeVendor) {
        this.mGyroscopeVendor = mGyroscopeVendor;
    }

    public float getGyroscopeResolution() {
        return mGyroscopeResolution;
    }

    public void setGyroscopeResolution(float mGyroscopeResolution) {
        this.mGyroscopeResolution = mGyroscopeResolution;
    }

    public float getGyroscopeMeasurementRange() {
        return mGyroscopeMeasurementRange;
    }

    public void setGyroscopeMeasurementRange(float mGyroscopeMeasurementRange) {
        this.mGyroscopeMeasurementRange = mGyroscopeMeasurementRange;
    }

    public float[] getFilteredAccelerometerData() {
        return mFilteredAccelerometerData;
    }

    public void setFilteredAccelerometerData(float[] mFilteredAccelerometerData) {
        this.mFilteredAccelerometerData = mFilteredAccelerometerData;
    }

    public float[] getAccelerometerMidPoint() {
        return mAccelerometerMidPoint;
    }

    public void setAccelerometerMidPoint(float[] mAccelerometerMidPoint) {
        this.mAccelerometerMidPoint = mAccelerometerMidPoint;
    }

    @Override
    public String toString() {
        return "SensorFingerprint" + '\n' +
                "UUID\n" + mUUID + '\n' +
                "DeviceModel\n" + mDeviceModel + '\n' +
                "AccelerometerModel\n" + mAccelerometerModel + '\n' +
                "AccelerometerVendor\n" + mAccelerometerVendor + '\n' +
                "AccelerometerMeasurementRange\n" + mAccelerometerMeasurementRange + '\n' +
                "AccelerometerResolution\n" + mAccelerometerResolution + '\n' +
                "AccelerometerSensitivity\n" + mAccelerometerSensitivity + '\n' +
                "AccelerometerLinearity\n" + mAccelerometerLinearity + '\n' +
                "AccelerometerNoise\n" + Arrays.toString(mAccelerometerNoise) + '\n' +
                "AccelerometerRawBias\n" + Arrays.toString(mAccelerometerRawBias) + '\n' +
                "AccelerometerMidPoint\n" + Arrays.toString(mAccelerometerMidPoint) + '\n' +
                "GyroscopeModel\n" + mGyroscopeModel + '\n' +
                "GyroscopeVendor\n" + mGyroscopeVendor + '\n' +
                "GyroscopeResolution\n" + mGyroscopeResolution + '\n' +
                "GyroscopeMeasurementRange\n" + mGyroscopeMeasurementRange + '\n' +
                "FilteredAccelerometerData\n" + Arrays.toString(mFilteredAccelerometerData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Device Data
        dest.writeString(mDeviceModel);
        // Accelerometer Data
        dest.writeString(mAccelerometerModel);
        dest.writeString(mAccelerometerVendor);
        dest.writeFloat(mAccelerometerMeasurementRange);
        dest.writeFloatArray(mAccelerometerNoise);
        dest.writeFloat(mAccelerometerResolution);
        dest.writeFloat(mAccelerometerSensitivity);
        dest.writeFloat(mAccelerometerLinearity);
        dest.writeFloatArray(mAccelerometerRawBias);
        dest.writeFloatArray(mAccelerometerMidPoint);
        // Gyroscope Data
        dest.writeString(mGyroscopeModel);
        dest.writeString(mGyroscopeVendor);
        dest.writeFloat(mGyroscopeResolution);
        dest.writeFloat(mGyroscopeMeasurementRange);
        // Linear Acceleration Data
        dest.writeFloatArray(mFilteredAccelerometerData);
    }
}
