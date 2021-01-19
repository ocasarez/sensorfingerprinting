package com.oscarcasarezruiz.sensorfingerprinting.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorTrace {
    
    private float mAccelerometerX;
    private float mAccelerometerY;
    private float mAccelerometerZ;
    
    public SensorTrace(){
        this.mAccelerometerX = 0;
        this.mAccelerometerY = 0;
        this.mAccelerometerZ = 0;
    }
    
    public SensorTrace(float mAccelerometerX, float mAccelerometerY, float mAccelerometerZ){
        this.mAccelerometerX = mAccelerometerX;
        this.mAccelerometerY = mAccelerometerY;
        this.mAccelerometerZ = mAccelerometerZ;
    }

    public float getAccelerometerX() {
        return mAccelerometerX;
    }

    public void setAccelerometerX(float mAccelerometerX) {
        this.mAccelerometerX = mAccelerometerX;
    }

    public float getAccelerometerY() {
        return mAccelerometerY;
    }

    public void setAccelerometerY(float mAccelerometerY) {
        this.mAccelerometerY = mAccelerometerY;
    }

    public float getAccelerometerZ() {
        return mAccelerometerZ;
    }

    public void setAccelerometerZ(float mAccelerometerZ) {
        this.mAccelerometerZ = mAccelerometerZ;
    }

    public boolean isEmpty(){
        if(mAccelerometerX == 0 && mAccelerometerY == 0 && mAccelerometerZ == 0){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SensorTrace{" +
                "X=" + this.mAccelerometerX +
                ", Y=" + this.mAccelerometerY +
                ", Z=" + this.mAccelerometerZ +
                '}';
    }

    protected SensorTrace(Parcel in) {
        mAccelerometerX = in.readFloat();
        mAccelerometerY = in.readFloat();
        mAccelerometerZ = in.readFloat();
    }

}
