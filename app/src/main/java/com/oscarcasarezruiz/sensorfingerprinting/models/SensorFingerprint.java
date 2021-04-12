package com.oscarcasarezruiz.sensorfingerprinting.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.oscarcasarezruiz.sensorfingerprinting.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.oscarcasarezruiz.sensorfingerprinting.utils.Utils.diffWithAbs;

public class SensorFingerprint implements Parcelable {

    private String mUUID;
    // Device Data
    private String mDeviceModel;
    private String mDeviceMfg;
    // Accelerometer Data
    private String mAccelerometerModel;
    private String mAccelerometerVendor;
    private float mAccelerometerSensitivity;
    private float mAccelerometerLinearity;
    private float mAccelerometerBias;
    private float mAccelerometerAvg;
    private float mAccelerometerMin;
    private float mAccelerometerMax;
    private float mAccelerometerStandardDev;

    public SensorFingerprint(){

    }

    public SensorFingerprint(Map<String, Object> document){
        this.mUUID = (String) document.get("Fingerprint ID");
        // Device Data
        this.mDeviceModel = (String) ((Map) document.get("Device Data")).get("Model");
        this.mDeviceMfg = (String) ((Map) document.get("Device Data")).get("Manufacturer");
        // Accelerometer Data
        this.mAccelerometerModel = (String) ((Map) document.get("Accelerometer Data")).get("Model");
        this.mAccelerometerVendor = (String) ((Map) document.get("Accelerometer Data")).get("Vendor");
        this.mAccelerometerLinearity = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Linearity"));
        this.mAccelerometerSensitivity = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Sensitivity"));
        this.mAccelerometerAvg = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Average"));
        this.mAccelerometerBias = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Bias"));
        this.mAccelerometerMin = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Minimum"));
        this.mAccelerometerMax = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Maximum"));
        this.mAccelerometerStandardDev = Utils.convertDoubleToFloat((Double) ((Map) document.get("Accelerometer Data")).get("Standard Deviation"));
    }

    protected SensorFingerprint(Parcel in) {
        // Device Data
        mDeviceModel = in.readString();
        mDeviceMfg = in.readString();
        // Accelerometer Data
        mAccelerometerModel = in.readString();
        mAccelerometerVendor = in.readString();
        mAccelerometerSensitivity = in.readFloat();
        mAccelerometerLinearity = in.readFloat();
        mAccelerometerBias = in.readFloat();
        mAccelerometerAvg = in.readFloat();
        mAccelerometerMin = in.readFloat();
        mAccelerometerMax = in.readFloat();
        mAccelerometerStandardDev = in.readFloat();

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
        // Accelerometer Data
        Map<String, Object> accelerometerData = new HashMap<>();
        accelerometerData.put("Model", this.mAccelerometerModel);
        accelerometerData.put("Vendor", this.mAccelerometerVendor);
        accelerometerData.put("Sensitivity", this.mAccelerometerSensitivity);
        accelerometerData.put("Linearity", this.mAccelerometerLinearity);
        accelerometerData.put("Bias", this.mAccelerometerBias);
        accelerometerData.put("Average", this.mAccelerometerAvg);
        accelerometerData.put("Minimum", this.mAccelerometerMin);
        accelerometerData.put("Maximum", this.mAccelerometerMax);
        accelerometerData.put("Standard Deviation", this.mAccelerometerStandardDev);

        docData.put("Device Data", deviceData);
        docData.put("Accelerometer Data", accelerometerData);
        return docData;
    }

    public float compareSensorFingerprint(SensorFingerprint o){
        // Compare Values by difference
        // Smaller the value of weight the more likely the sensor fingerprint is the same
        // Larger the value of weight the more likely the sensor fingerprint is not the same
        float weight = 0;

        // Device Data
        // Model
        if(this.mDeviceModel.equals(o.getDeviceModel())){
            weight += 0.0f;
        } else {
            weight += 1.0f;
        }
        // Manufacturer
        if(this.mDeviceMfg.equals(o.getDeviceMfg())){
            weight += 0.0f;
        } else {
            weight += 1.0f;
        }
        // Accelerometer Data
        // Model
        if(this.mAccelerometerModel.equals(o.getSensorModel())){
            weight += 0.0f;
        } else {
            weight += 1.0f;
        }
        // Vendor
        if(this.mAccelerometerVendor.equals(o.getSensorVendor())){
            weight += 0.0f;
        } else {
            weight += 1.0f;
        }
        // Minimum
        weight += diffWithAbs("Minimum", this.mAccelerometerMin, o.getAccelerometerMin());
        // Maximum
        weight += diffWithAbs("Maximum", this.mAccelerometerMax, o.getAccelerometerMax());
        // Bias
        weight += diffWithAbs("Bias", this.mAccelerometerBias, o.getSensorRawBias());
        // Standard Deviation
        weight += diffWithAbs("Standard Deviation", this.mAccelerometerStandardDev, o.getAccelerometerStandardDev());
        // Average
        weight += diffWithAbs("Average", this.mAccelerometerAvg, o.getAccelerometerAvg());
        // Sensitivity
        weight += diffWithAbs("Sensitivity", this.mAccelerometerSensitivity, o.getSensorSensitivity());
        // Linearity
        weight += diffWithAbs("Linearity", this.mAccelerometerLinearity, o.getSensorLinearity());

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

    public float getSensorRawBias() {
        return mAccelerometerBias;
    }

    public void setSensorRawBias(float mSensorRawBias) {
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

    public float getAccelerometerAvg() {
        return mAccelerometerAvg;
    }

    public void setAccelerometerAvg(float mAccelerometerAvg) {
        this.mAccelerometerAvg = mAccelerometerAvg;
    }

    public float getAccelerometerMin() {
        return mAccelerometerMin;
    }

    public void setAccelerometerMin(float mAccelerometerMin) {
        this.mAccelerometerMin = mAccelerometerMin;
    }

    public float getAccelerometerMax() {
        return mAccelerometerMax;
    }

    public void setAccelerometerMax(float mAccelerometerMax) {
        this.mAccelerometerMax = mAccelerometerMax;
    }

    public float getAccelerometerStandardDev() {
        return mAccelerometerStandardDev;
    }

    public void setAccelerometerStandardDev(float mAccelerometerStandardDev) {
        this.mAccelerometerStandardDev = mAccelerometerStandardDev;
    }

    @Override
    public String toString() {
        return "SensorFingerprint" + '\n' +
                "Fingerprint ID\n" + mUUID + '\n' +
                "DeviceModel\n" + mDeviceModel + '\n' +
                "DeviceMfg\n" + mDeviceMfg + '\n' +
                "AccelerometerModel\n" + mAccelerometerModel + '\n' +
                "AccelerometerVendor\n" + mAccelerometerVendor + '\n' +
                "AccelerometerSensitivity\n" + mAccelerometerSensitivity + '\n' +
                "AccelerometerLinearity\n" + mAccelerometerLinearity + '\n' +
                "AccelerometerBias\n" + mAccelerometerBias + '\n' +
                "AccelerometerAvg\n" + mAccelerometerAvg + '\n' +
                "AccelerometerMin\n" + mAccelerometerMin + '\n' +
                "AccelerometerMax\n" + mAccelerometerMax + '\n' +
                "AccelerometerStandardDev\n" + mAccelerometerStandardDev;
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
        // Accelerometer Data
        dest.writeString(mAccelerometerModel);
        dest.writeString(mAccelerometerVendor);
        dest.writeFloat(mAccelerometerSensitivity);
        dest.writeFloat(mAccelerometerLinearity);
        dest.writeFloat(mAccelerometerBias);
        dest.writeFloat(mAccelerometerAvg);
        dest.writeFloat(mAccelerometerMin);
        dest.writeFloat(mAccelerometerMax);
        dest.writeFloat(mAccelerometerStandardDev);
    }
}
