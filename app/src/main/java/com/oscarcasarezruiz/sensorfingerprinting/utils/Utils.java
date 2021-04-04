package com.oscarcasarezruiz.sensorfingerprinting.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public final class Utils {

    public static final String TAG = "Utils";

    public static String findAxisAffectedByGravity(SensorTrace sensorTrace){
        if(percentageRange("Gravity on X-Axis", sensorTrace.getAccelerometerX(), SensorManager.GRAVITY_EARTH, 0.25f)){
            return "AccelerometerX";
        } else if (percentageRange("Gravity on Y-Axis", sensorTrace.getAccelerometerY(), SensorManager.GRAVITY_EARTH, 0.25f)){
            return "AccelerometerY";
        } else {
            return "AccelerometerZ";
        }
    }

    public static float[] calculateSensorNoise(ArrayList<float[]> data){
        Log.d(TAG, "sensorNoise: Calculating Sensor Noise");
        float[] noise;
        float xNoise = 0.0f;
        float yNoise = 0.0f;
        float zNoise = 0.0f;
        int length = data.size();
        for (float[] trace: data ) {
            xNoise += (float) Math.pow(trace[0], 2);
            yNoise += (float) Math.pow(trace[1], 2);
            zNoise += (float) Math.pow(trace[2], 2);
        }
        xNoise = (float) Math.sqrt(xNoise/length);
        yNoise = (float) Math.sqrt(yNoise/length);
        zNoise = (float) Math.sqrt(zNoise/length);

        noise = new float[]{xNoise, yNoise, zNoise};
        return noise;
    }

    public static float[] calculateSensorBias(SensorTrace sensorTrace){
        Log.d(TAG, "calculateSensorBias: Calculating Bias");
        float[] bias = new float[3];
        // Case Default: [0, 0, Gravity]
        // Case AccelerometerY: [0, Gravity, 0]
        // Case AccelerometerX: [Gravity, 0, 0]
        switch (findAxisAffectedByGravity(sensorTrace)){
            case "AccelerometerY":
                bias[0] = sensorTrace.getAccelerometerX();
                bias[1] = sensorTrace.getAccelerometerY() - SensorManager.GRAVITY_EARTH;
                bias[2] = sensorTrace.getAccelerometerZ();
                return bias;
            case "AccelerometerX":
                bias[0] = sensorTrace.getAccelerometerX() - SensorManager.GRAVITY_EARTH;
                bias[1] = sensorTrace.getAccelerometerY();
                bias[2] = sensorTrace.getAccelerometerZ();
                return bias;
            default:
                bias[0] = sensorTrace.getAccelerometerX();
                bias[1] = sensorTrace.getAccelerometerY();
                bias[2] = sensorTrace.getAccelerometerZ() - SensorManager.GRAVITY_EARTH;
                return bias;
        }
    }

    public static boolean percentageRange(String flag, float actual, float expected, float percentage){
        final float absA = Math.abs(actual);
        final float absB = Math.abs(expected);
        final float diff = Math.abs(actual - expected);
        boolean result = false;

        if (actual == expected) { // shortcut, handles infinities
            result = true;
        } else if (actual == 0 || expected == 0 || diff < Float.MIN_NORMAL) {
            // a or b is zero or both are extremely close to it
            // relative error is less meaningful here
            result =  diff < (percentage * Float.MIN_NORMAL);
        } else { // use relative error
            result =  diff / (absA + absB) < percentage;
        }
        Log.d(TAG, flag + " => " + result);
        return result;
    }


    public static float[] AverageTracesMeasured(ArrayList<float[]> measurements){
        float[] average = new float[]{0.0f,0.0f,0.0f};

        // Add Values
        for (float[] arr: measurements) {
            average[0] += arr[0];
            average[1] += arr[1];
            average[2] += arr[2];
        }

        // Divide by Length of Measurements
        average[0] /= measurements.size();
        average[1] /= measurements.size();
        average[2] /= measurements.size();
        Log.d(TAG, "AverageTracesMeasured: Trace Average Result" + Arrays.toString(average));
        return average;
    }

    public static float[] maximumMeasurements(ArrayList<float[]> measurements){
        ArrayList<Float> XValues = new ArrayList<>();
        ArrayList<Float> YValues = new ArrayList<>();
        ArrayList<Float> ZValues = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            XValues.add(measurements.get(i)[0]);
            YValues.add(measurements.get(i)[1]);
            ZValues.add(measurements.get(i)[2]);
        }
        float[] maximumValuesArr = new float[3];
        maximumValuesArr[0] = Collections.max(XValues);
        maximumValuesArr[1] = Collections.max(YValues);
        maximumValuesArr[2] = Collections.max(ZValues);
        Log.d(TAG, "maximumMeasurements: Max => " + Arrays.toString(maximumValuesArr));
        return maximumValuesArr;
    }

    public static float[] minimumMeasurements(ArrayList<float[]> measurements){
        ArrayList<Float> XValues = new ArrayList<>();
        ArrayList<Float> YValues = new ArrayList<>();
        ArrayList<Float> ZValues = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            XValues.add(measurements.get(i)[0]);
            YValues.add(measurements.get(i)[1]);
            ZValues.add(measurements.get(i)[2]);
        }
        float[] minimumValueArr = new float[3];
        minimumValueArr[0] = Collections.min(XValues);
        minimumValueArr[1] = Collections.min(YValues);
        minimumValueArr[2] = Collections.min(ZValues);
        Log.d(TAG, "minimumMeasurements: Min => " + Arrays.toString(minimumValueArr));
        return minimumValueArr;
    }

    public static float[] standardDeviationMeasurements(ArrayList<float[]> measurements){
        float[] average = AverageTracesMeasured(measurements);
        float meanSquareX = 0.0f;
        float meanSquareY = 0.0f;
        float meanSquareZ = 0.0f;

        // Calculate Mean Square and Sum
        for(float[] arr : measurements){
            meanSquareX += Math.pow((arr[0] - average[0]), 2);
            meanSquareY += Math.pow((arr[1] - average[1]), 2);
            meanSquareZ += Math.pow((arr[2] - average[2]), 2);
        }
        Log.d(TAG, "standardDeviationMeasurements: X -> " + meanSquareX);
        Log.d(TAG, "standardDeviationMeasurements: Y -> " + meanSquareY);
        Log.d(TAG, "standardDeviationMeasurements: Z -> " + meanSquareZ);
        meanSquareX = convertDoubleToFloat(Math.sqrt(meanSquareX / measurements.size()));
        meanSquareY = convertDoubleToFloat(Math.sqrt(meanSquareY / measurements.size()));
        meanSquareZ = convertDoubleToFloat(Math.sqrt(meanSquareZ / measurements.size()));

        Log.d(TAG, "standardDeviationMeasurements: X -> " + meanSquareX);
        Log.d(TAG, "standardDeviationMeasurements: Y -> " + meanSquareY);
        Log.d(TAG, "standardDeviationMeasurements: Z -> " + meanSquareZ);

        return new float[]{meanSquareX, meanSquareY, meanSquareZ};
    }

    public static float calculateSensorSensitivity(float zGPositive, float zGNegative){
        return (zGPositive - zGNegative) / (2 * SensorManager.GRAVITY_EARTH);
    }

    public static float calculateSensorLinearity(float zGPositive, float zGNegative, float zGZero){
        return (zGZero - ((zGPositive + zGNegative) / 2));
    }

    public static float convertDoubleToFloat(Double value){
        return value.floatValue();
    }

    public static String getDeviceOS(){
        int sdkVersion = Build.VERSION.SDK_INT;
        String deviceOS = "";
        switch (sdkVersion){
            case 30:
                deviceOS += 11;
                break;
            case 29:
                deviceOS += 10;
                break;
            case 28:
                deviceOS += 9;
                break;
            case 27:
                deviceOS += "8.1.0";
                break;
            case 26:
                deviceOS += "8.0.0";
                break;
            case 25:
                deviceOS += "7.1";
                break;
            case 24:
                deviceOS += "7.0";
                break;
            case 23:
                deviceOS += "6.0";
                break;
            default:
                deviceOS += "< 6.0";
                break;
        }

        return deviceOS;
    }
}
