package com.oscarcasarezruiz.sensorfingerprinting.utils;

import android.hardware.SensorManager;
import android.util.Log;
import java.util.ArrayList;

public final class Utils {

    public static final String TAG = "Utils";

    public static float calculateSensorBias(float zAccelerometerValues){
        Log.d(TAG, "calculateSensorBias: Calculating Bias");
        return zAccelerometerValues - SensorManager.GRAVITY_EARTH;
    }

    public static float diffWithAbs(String flag, float value1, float value2){
        float diff = Math.abs(value1 - value2);
        Log.d(TAG, flag + " => " + diff);
        return diff;
    }


    public static float AverageTracesMeasured(ArrayList<Float> measurements){
        float average = 0.0f;

        // Add Values
        for (float f: measurements) {
            average += f;
        }

        // Divide by Length of Measurements
        average /= measurements.size();
        Log.d(TAG, "AverageTracesMeasured: Trace Average Result" + average);
        return average;
    }

    public static float standardDeviationMeasurements(ArrayList<Float> measurements){
        float average = AverageTracesMeasured(measurements);
        float meanSquareZ = 0.0f;

        // Calculate Mean Square and Sum
        for(float f : measurements){
            meanSquareZ += Math.pow((f - average), 2);
        }
        meanSquareZ = convertDoubleToFloat(Math.sqrt(meanSquareZ / measurements.size()));
        Log.d(TAG, "standardDeviationMeasurements: Z -> " + meanSquareZ);

        return meanSquareZ;
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
