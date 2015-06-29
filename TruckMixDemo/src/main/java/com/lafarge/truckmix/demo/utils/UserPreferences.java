package com.lafarge.truckmix.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;

/**
 * stores the user object in SharedPreferences
 */
public class UserPreferences {

    /** This application's preferences label */
    private static final String PREFS_NAME = "com.our.package.UserPrefs";

    /** This application's preferences */
    private static SharedPreferences settings;

    /** This application's settings editor*/
    private static SharedPreferences.Editor editor;

    /** Constructor takes an android.content.Context argument*/
    public UserPreferences(Context ctx) {
        if (settings == null) {
            settings = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
       /*
        * Get a SharedPreferences editor instance.
        * SharedPreferences ensures that updates are atomic
        * and non-concurrent
        */
        editor = settings.edit();
    }

    /** Fields */
    private static final String KEY_TRUCK_PARAMETERS_T1 = "com.lafarge.truckmix.demo.KEY_T1";
    private static final String KEY_TRUCK_PARAMETERS_A11 = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_A11";
    private static final String KEY_TRUCK_PARAMETERS_A12 = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_A12";
    private static final String KEY_TRUCK_PARAMETERS_A13 = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_A13";
    private static final String KEY_TRUCK_PARAMETERS_MAGNET_QUANTITY = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_MAGNET_QUANTITY";
    private static final String KEY_TRUCK_PARAMETERS_TIME_PUMP = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_TIME_PUMP";
    private static final String KEY_TRUCK_PARAMETERS_TIME_DELAY_DRIVER = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_TIME_DELAY_DRIVER";
    private static final String KEY_TRUCK_PARAMETERS_PULSE_NUMBER = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_PULSE_NUMBER";
    private static final String KEY_TRUCK_PARAMETERS_FLOWMETER_FREQUENCY = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_FLOWMETER_FREQUENCY";
    private static final String KEY_TRUCK_PARAMETERS_COMMAND_PUMP_MODE = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_COMMAND_PUMP_MODE";
    private static final String KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_A = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_A";
    private static final String KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_B = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_B";
    private static final String KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_A = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_A";
    private static final String KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_B = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_B";
    private static final String KEY_TRUCK_PARAMETERS_EV1 = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_EV1";
    private static final String KEY_TRUCK_PARAMETERS_VA1 = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_VA1";
    private static final String KEY_TRUCK_PARAMETERS_TOLERANCE_COUNTING = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_TOLERANCE_COUNTING";
    private static final String KEY_TRUCK_PARAMETERS_WAITING_DURATION_AFTER_WATER_ADD = "com.lafarge.truckmix.demo.KEY_TRUCK_PARAMETERS_WAITING_DURATION_AFTER_WATER_ADD";
    private static final String KEY_TRUCK_PAREMETERS_MAX_DELAY_BEFORE_FLOWAGE = "com.lafarge.truckmix.demo.KEY_TRUCK_PAREMETERS_MAX_DELAY_BEFORE_FLOWAGE";
    private static final String KEY_TRUCK_PAREMETERS_MAX_FLOWAGE_ERROR = "com.lafarge.truckmix.demo.KEY_TRUCK_PAREMETERS_MAX_FLOWAGE_ERROR";
    private static final String KEY_TRUCK_PAREMETERS_MAX_COUTING_ERROR = "com.lafarge.truckmix.demo.KEY_TRUCK_PAREMETERS_MAX_COUTING_ERROR";

    private static final String KEY_DELIVERY_PARAMETERS_TARGET_SLUMP = "com.lafarge.truckmix.demo.KEY_DELIVERY_PARAMETERS_TARGET_SLUMP";
    private static final String KEY_DELIVERY_PARAMETERS_MAX_WATER = "com.lafarge.truckmix.demo.KEY_DELIVERY_PARAMETERS_MAX_WATER";
    private static final String KEY_DELIVERY_PARAMETERS_LOAD_VOLUME = "com.lafarge.truckmix.demo.KEY_DELIVERY_PARAMETERS_LOAD_VOLUME";

    /** Store or Update Truck parameters */
    public void setTruckParameters(TruckParameters parameters){
        if (parameters == null) return;

        editor.putFloat(KEY_TRUCK_PARAMETERS_T1, (float) parameters.T1);
        editor.putFloat(KEY_TRUCK_PARAMETERS_A11, (float) parameters.A11);
        editor.putFloat(KEY_TRUCK_PARAMETERS_A12, (float) parameters.A12);
        editor.putFloat(KEY_TRUCK_PARAMETERS_A13, (float) parameters.A13);
        editor.putInt(KEY_TRUCK_PARAMETERS_MAGNET_QUANTITY, parameters.magnetQuantity);
        editor.putInt(KEY_TRUCK_PARAMETERS_TIME_PUMP, parameters.timePump);
        editor.putInt(KEY_TRUCK_PARAMETERS_TIME_DELAY_DRIVER, parameters.timeDelayDriver);
        editor.putInt(KEY_TRUCK_PARAMETERS_PULSE_NUMBER, parameters.pulseNumber);
        editor.putInt(KEY_TRUCK_PARAMETERS_FLOWMETER_FREQUENCY, parameters.flowmeterFrequency);
        editor.putString(KEY_TRUCK_PARAMETERS_COMMAND_PUMP_MODE, String.valueOf(parameters.commandPumpMode));
        editor.putFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_A, (float) parameters.calibrationInputSensorA);
        editor.putFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_B, (float) parameters.calibrationInputSensorB);
        editor.putFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_A, (float) parameters.calibrationOutputSensorA);
        editor.putFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_B, (float) parameters.calibrationOutputSensorB);
        editor.putInt(KEY_TRUCK_PARAMETERS_EV1, parameters.openingTimeEV1);
        editor.putInt(KEY_TRUCK_PARAMETERS_VA1, parameters.openingTimeVA1);
        editor.putInt(KEY_TRUCK_PARAMETERS_TOLERANCE_COUNTING, parameters.toleranceCounting);
        editor.putInt(KEY_TRUCK_PARAMETERS_WAITING_DURATION_AFTER_WATER_ADD, parameters.waitingDurationAfterWaterAddition);
        editor.putInt(KEY_TRUCK_PAREMETERS_MAX_DELAY_BEFORE_FLOWAGE, parameters.maxDelayBeforeFlowage);
        editor.putInt(KEY_TRUCK_PAREMETERS_MAX_FLOWAGE_ERROR, parameters.maxFlowageError);
        editor.putInt(KEY_TRUCK_PAREMETERS_MAX_COUTING_ERROR, parameters.maxCountingError);

        editor.commit();
    }

    /** Retrieve Truck parameters */
    public TruckParameters getTruckParameters() {
        double T1 = settings.getFloat(KEY_TRUCK_PARAMETERS_T1, 3.4563f);
        double A11 = settings.getFloat(KEY_TRUCK_PARAMETERS_A11, 563.376f);
        double A12 = settings.getFloat(KEY_TRUCK_PARAMETERS_A12, -39.844f);
        double A13 = settings.getFloat(KEY_TRUCK_PARAMETERS_A13, 4.3254f);
        int magnetQuantity = settings.getInt(KEY_TRUCK_PARAMETERS_MAGNET_QUANTITY, 24);
        int timePump = settings.getInt(KEY_TRUCK_PARAMETERS_TIME_PUMP, 15);
        int timeDelayDriver = settings.getInt(KEY_TRUCK_PARAMETERS_TIME_DELAY_DRIVER, 120);
        int pulseNumber = settings.getInt(KEY_TRUCK_PARAMETERS_PULSE_NUMBER, 45);
        int flowmeterFrequency = settings.getInt(KEY_TRUCK_PARAMETERS_FLOWMETER_FREQUENCY, 60);
        TruckParameters.CommandPumpMode commandPumpMode = TruckParameters.CommandPumpMode.valueOf(settings.getString
                (KEY_TRUCK_PARAMETERS_COMMAND_PUMP_MODE, "SEMI_AUTO"));
        double calibrationInputSensorA = settings.getFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_A, 2.5f);
        double calibrationInputSensorB = settings.getFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_INPUT_SENSOR_B, 0.f);
        double calibrationOutputSensorA = settings.getFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_A, 2.5f);
        double calibrationOutputSensorB = settings.getFloat(KEY_TRUCK_PARAMETERS_CALIBRATION_OUTPUT_SENSOR_B, 0.f);
        int openingTimeEV1 = settings.getInt(KEY_TRUCK_PARAMETERS_EV1, 3);
        int openingTimeVA1 = settings.getInt(KEY_TRUCK_PARAMETERS_VA1, 180);
        int toleranceCounting = settings.getInt(KEY_TRUCK_PARAMETERS_TOLERANCE_COUNTING, 10);
        int waitingDurationAfterWaterAddition = settings.getInt(KEY_TRUCK_PARAMETERS_WAITING_DURATION_AFTER_WATER_ADD, 90);
        int maxDelayBeforeFlowage = settings.getInt(KEY_TRUCK_PAREMETERS_MAX_DELAY_BEFORE_FLOWAGE, 64);
        int maxFlowageError = settings.getInt(KEY_TRUCK_PAREMETERS_MAX_FLOWAGE_ERROR, 5);
        int maxCountingError = settings.getInt(KEY_TRUCK_PAREMETERS_MAX_COUTING_ERROR, 6);

        return new TruckParameters(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber,
                flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA, calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage, maxFlowageError, maxCountingError);
    }

    /** Store or update Delivery parameters */
    public void setDeliveryParameters(DeliveryParameters parameters) {
        if (parameters == null) return;

        editor.putInt(KEY_DELIVERY_PARAMETERS_TARGET_SLUMP, parameters.targetSlump);
        editor.putInt(KEY_DELIVERY_PARAMETERS_MAX_WATER, parameters.maxWater);
        editor.putInt(KEY_DELIVERY_PARAMETERS_LOAD_VOLUME, parameters.loadVolume);

        editor.commit();
    }

    /** Retrieve Delivery parameters */
    public DeliveryParameters getDeliveryParameters() {
        int targetSlump = settings.getInt(KEY_DELIVERY_PARAMETERS_TARGET_SLUMP, 150);
        int maxWater = settings.getInt(KEY_DELIVERY_PARAMETERS_MAX_WATER, 30);
        int loadVolume = settings.getInt(KEY_DELIVERY_PARAMETERS_LOAD_VOLUME, 6);

        return new DeliveryParameters(targetSlump, maxWater, loadVolume);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
