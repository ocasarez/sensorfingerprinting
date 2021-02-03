package com.oscarcasarezruiz.sensorfingerprinting.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.util.Log;

import com.oscarcasarezruiz.sensorfingerprinting.models.SensorFingerprint;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import java.util.ArrayList;
import java.util.Arrays;
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
        Log.d(TAG, flag + "=> " + result);
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

    public static float calculateSensorSensitivity(float zGPositive, float zGNegative){
        return (zGPositive - zGNegative) / (2 * SensorManager.GRAVITY_EARTH);
    }

    public static float calculateSensorLinearity(float zGPositive, float zGNegative, float zGZero){
        return (zGZero - ((zGPositive + zGNegative) / 2));
    }

    public static float convertDoubleToFloat(Double value){
        return value.floatValue();
    }

}
