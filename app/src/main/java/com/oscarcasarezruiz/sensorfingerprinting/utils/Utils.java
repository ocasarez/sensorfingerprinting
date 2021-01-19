package com.oscarcasarezruiz.sensorfingerprinting.utils;

import android.hardware.SensorManager;
import android.util.Log;
import com.oscarcasarezruiz.sensorfingerprinting.models.SensorTrace;
import java.util.ArrayList;
import java.util.Arrays;

public final class Utils {

    public static final String TAG = "Utils";

    public static String findAxisAffectedByGravity(SensorTrace sensorTrace){
        if(percentageRange(sensorTrace.getAccelerometerX(), SensorManager.GRAVITY_EARTH, 0.5f)){
            return "AccelerometerX";
        } else if (percentageRange(sensorTrace.getAccelerometerY(), SensorManager.GRAVITY_EARTH, 0.5f)){
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

    public static boolean percentageRange(float actual, float expected, float percentage){
        if(actual <= (expected + (expected * percentage)) && actual >= (expected - (expected * percentage))){
            return true;
        } else {
            return false;
        }
    }

    public static float euclideanDistance(float x1, float y1, float x2, float y2){
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static float[] AverageTracesMeasured(ArrayList<float[]> measurements){
        Log.d(TAG, "AverageTracesMeasured: Reached");
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
