package com.lafarge.truckmix.service;

import android.os.Bundle;
import android.os.Message;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.service.models.DeliveryParametersParcelable;
import com.lafarge.truckmix.service.models.TruckParametersParcelable;

public class TruckMixServiceMessages {
    //
    // Messages coming from TruckMixService
    //

    public static final int MSG_SLUMP_UPDATED = 0x42;
    public static final int MSG_MIXING_MODE_ACTIVATED = 0x01;
    public static final int MSG_UNLOADING_MODE_ACTIVATED = 0x02;
    public static final int MSG_WATER_ADDED = 0x03;
    public static final int MSG_WATER_ADDITION_REQUEST = 0x04;
    public static final int MSG_WATER_ADDITION_BEGAN = 0x05;
    public static final int MSG_WATER_ADDITION_END = 0x06;
    public static final int MSG_STATE_CHANGED = 0x07;
    public static final int MSG_CALIBRATION_DATA = 0x08;
    public static final int MSG_ALARM_WATER_ADDITION_BLOCK = 0x09;
    public static final int MSG_ALARM_WATER_MAX = 0x0A;
    public static final int MSG_ALARM_FLOWAGE_ERROR = 0x0B;
    public static final int MSG_ALARM_COUNTING_ERROR = 0x0C;
    public static final int MSG_INPUT_SENSOR_CONNECTION_CHANGED = 0x0D;
    public static final int MSG_OUTPUT_SENSOR_CONNECTION_CHANGED = 0x0E;
    public static final int MSG_SPEED_SENSOR_MIN_EXCEED = 0x0F;
    public static final int MSG_SPEED_SENSOR_MAX_EXCEED = 0x10;
    public static final int MSG_LOG = 0x11;

    public static final String KEY_MSG_SLUMP_UPDATED_VALUE = "slump";
    public static final String KEY_MSG_WATER_ADDITION_REQUEST_VOLUME = "volume";
    public static final String KEY_MSG_WATER_ADDED_VOLUME = "water_added_volume";
    public static final String KEY_MSG_WATER_ADDED_MODE = "water_added_mode";
    public static final String KEY_MSG_STATE_CHANGED_STEP = "step";
    public static final String KEY_MSG_STATE_CHANGED_SUBSTEP = "substep";
    public static final String KEY_MSG_DATA_CALIBRATION_INPUT_PRESSURE = "input_pressure";
    public static final String KEY_MSG_DATA_CALIBRATION_OUTPUT_PRESSION = "output_pressure";
    public static final String KEY_MSG_DATA_CALIBRATION_ROTATION_SPEED = "rotation_speed";
    public static final String KEY_MSG_INPUT_SENSOR_CONNECTION_CHANGED_VALUE = "input_sensor_state";
    public static final String KEY_MSG_OUTPUT_SENSOR_CONNECTION_CHANGED_VALUE = "output_sensor_state";
    public static final String KEY_MSG_SPEED_SENSOR_MIN_EXCEED_VALUE = "speed_sensor_min_exceed_threshold";
    public static final String KEY_MSG_SPEED_SENSOR_MAX_EXCEED_VALUE = "speed_sensor_max_exceed_threshold";
    public static final String KEY_MSG_LOG_VALUE = "log";

    //
    // Messages coming from clients
    //

    public static final int MSG_CONNECT_DEVICE = 0x12; // TODO: Delete
    public static final int MSG_TRUCK_PARAMETERS = 0x13;
    public static final int MSG_DELIVERY_PARAMETERS = 0x14;
    public static final int MSG_ACCEPT_DELIVERY = 0x15;
    public static final int MSG_END_DELIVERY = 0x16;
    public static final int MSG_ADD_WATER_PERMISSION = 0x17;
    public static final int MSG_CHANGE_EXTERNAL_DISPLAY_STATE = 0x18;
    public static final int MSG_REGISTER_CLIENT = 0x19;
    public static final int MSG_UNREGISTER_CLIENT = 0x20;

    public static final String KEY_MSG_CONNECT_DEVICE_ADDRESS = "address";
    public static final String KEY_MSG_TRUCK_PARAMETERS_DATA = "truck_parameters";
    public static final String KEY_MSG_DELIVERY_PARAMETERS_DATA = "delivery_parameters";
    public static final String KEY_MSG_ACCEPT_DELIVERY_VALUE = "accept_delivery_value";
    public static final String KEY_MSG_ADD_WATER_PERMISSION_VALUE = "add_water_permission";
    public static final String KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE = "change_external_display_value";

    //
    // Factory of message to send by the service
    //

    public static Message createSlumpUpdatedMessage(int slump) {
        Message msg = Message.obtain(null, MSG_SLUMP_UPDATED, slump, 0);
        Bundle data = new Bundle();
        data.putInt(KEY_MSG_SLUMP_UPDATED_VALUE, slump);
        msg.setData(data);
        return msg;
    }

    public static Message createMixingModeActivatedMessage() {
        return Message.obtain(null, MSG_MIXING_MODE_ACTIVATED);
    }

    public static Message createUnloadingModeActivatedMessage() {
        return Message.obtain(null, MSG_UNLOADING_MODE_ACTIVATED);
    }

    public static Message createWaterAdditionRequestMessage(int volume) {
        Message msg = Message.obtain(null, MSG_WATER_ADDITION_REQUEST);
        Bundle data = new Bundle();
        data.putInt(KEY_MSG_WATER_ADDITION_REQUEST_VOLUME, volume);
        msg.setData(data);
        return msg;
    }

    public static Message createWaterAddedMessage(int volume, MessageReceivedListener.WaterAdditionMode additionMode) {
        Message msg = Message.obtain(null, MSG_WATER_ADDED);
        Bundle data = new Bundle();
        data.putInt(KEY_MSG_WATER_ADDED_VOLUME, volume);
        data.putSerializable(KEY_MSG_WATER_ADDED_MODE, additionMode);
        msg.setData(data);
        return msg;
    }

    public static Message createStateChangedMessage(int step, int subStep) {
        Message msg = Message.obtain(null, MSG_STATE_CHANGED, step, subStep);
//        Bundle data = new Bundle();
//        data.putInt(KEY_MSG_STATE_CHANGED_STEP, step);
//        data.putInt(KEY_MSG_STATE_CHANGED_SUBSTEP, subStep);
//        msg.setData(data);
        return msg;
    }

    public static Message createCalibrationDataMessage(float inPressure, float outPressure, float rotationSpeed) {
        Message msg = Message.obtain(null, MSG_CALIBRATION_DATA);
        Bundle data = new Bundle();
        data.putFloat(KEY_MSG_DATA_CALIBRATION_INPUT_PRESSURE, inPressure);
        data.putFloat(KEY_MSG_DATA_CALIBRATION_OUTPUT_PRESSION, outPressure);
        data.putFloat(KEY_MSG_DATA_CALIBRATION_ROTATION_SPEED, rotationSpeed);
        msg.setData(data);
        return msg;
    }

    public static Message createAlarmWaterAdditionBlockedMessage() {
        return Message.obtain(null, MSG_ALARM_WATER_ADDITION_BLOCK);
    }

    public static Message createAlarmWaterMaxMessage() {
        return Message.obtain(null, MSG_ALARM_WATER_MAX);
    }

    public static Message createAlarmFlowageErrorMessage() {
        return Message.obtain(null, MSG_ALARM_FLOWAGE_ERROR);
    }

    public static Message createAlarmCountingErrorMessage() {
        return Message.obtain(null, MSG_ALARM_COUNTING_ERROR);
    }

    public static Message createInputSensorConnectionChangedMessage(boolean connected) {
        Message msg = Message.obtain(null, MSG_INPUT_SENSOR_CONNECTION_CHANGED);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_INPUT_SENSOR_CONNECTION_CHANGED_VALUE, connected);
        msg.setData(data);
        return msg;
    }

    public static Message createOutputSensorConnectionChangedMessage(boolean connected) {
        Message msg = Message.obtain(null, MSG_OUTPUT_SENSOR_CONNECTION_CHANGED);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_OUTPUT_SENSOR_CONNECTION_CHANGED_VALUE, connected);
        msg.setData(data);
        return msg;
    }

    public static Message createSpeedSensorHasExceedMinThresholdMessage(boolean isOutOfRange) {
        Message msg = Message.obtain(null, MSG_SPEED_SENSOR_MIN_EXCEED);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_SPEED_SENSOR_MIN_EXCEED_VALUE, isOutOfRange);
        msg.setData(data);
        return msg;
    }

    public static Message createSpeedSensorHasExceedMaxThresholdMessage(boolean isOutOfRange) {
        Message msg = Message.obtain(null, MSG_SPEED_SENSOR_MAX_EXCEED);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_SPEED_SENSOR_MAX_EXCEED_VALUE, isOutOfRange);
        msg.setData(data);
        return msg;
    }

    public static Message createLogMessage(String log) {
        Message msg = Message.obtain(null, MSG_LOG);
        Bundle data = new Bundle();
        data.putString(KEY_MSG_LOG_VALUE, log);
        msg.setData(data);
        return msg;
    }

    //
    // Getter of values sent by Service
    //

    public static int getSlumpFromSlumpUpdatedMessage(Message msg) {
        return msg.getData().getInt(KEY_MSG_SLUMP_UPDATED_VALUE);
    }

    public static int getVolumeFromWaterAdditionRequestMessage(Message msg) {
        return msg.getData().getInt(KEY_MSG_WATER_ADDITION_REQUEST_VOLUME);
    }

    public static int getVolumeFromWaterAddedMessage(Message msg) {
        return msg.getData().getInt(KEY_MSG_WATER_ADDED_VOLUME);
    }

    public static MessageReceivedListener.WaterAdditionMode getAdditionModeFromWaterAddedMessage(Message msg) {
        return (MessageReceivedListener.WaterAdditionMode) msg.getData().getSerializable(KEY_MSG_WATER_ADDED_MODE);
    }

    public static boolean getValueFromInputSensorConnectionChangedMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_INPUT_SENSOR_CONNECTION_CHANGED_VALUE);
    }

    public static boolean getValueFromOutputSensorConnectionChangedMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_OUTPUT_SENSOR_CONNECTION_CHANGED_VALUE);
    }

    public static boolean getValueFromSpeedSensorHasExceedMinThressThresholdMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_SPEED_SENSOR_MIN_EXCEED_VALUE);
    }

    public static boolean getValueFromSpeedSensorHasExceedMaxThressThresholdMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_SPEED_SENSOR_MAX_EXCEED_VALUE);
    }

    public static String getLogFromLogMessage(Message msg) {
        return msg.getData().getString(KEY_MSG_LOG_VALUE);
    }

    /** Factory of message to send by the client */

    public static Message createConnectMessage(String address) {
        Message msg = Message.obtain(null, TruckMixServiceMessages.MSG_CONNECT_DEVICE);
        Bundle data = new Bundle();
        data.putString(KEY_MSG_CONNECT_DEVICE_ADDRESS, address);
        msg.setData(data);
        return msg;
    }

    public static Message createTruckParametersMessage(TruckParameters parameters) {
        Message msg = Message.obtain(null, MSG_TRUCK_PARAMETERS);
        Bundle data = new Bundle();
        data.putParcelable(KEY_MSG_TRUCK_PARAMETERS_DATA, new TruckParametersParcelable(parameters));
        msg.setData(data);
        return msg;
    }

    public static Message createDeliveryParametersMessage(DeliveryParameters parameters) {
        Message msg = Message.obtain(null, MSG_DELIVERY_PARAMETERS);
        Bundle data = new Bundle();
        data.putParcelable(KEY_MSG_DELIVERY_PARAMETERS_DATA, new DeliveryParametersParcelable(parameters));
        msg.setData(data);
        return msg;
    }

    public static Message createAcceptDeliveryMessage(boolean accepted) {
        Message msg = Message.obtain(null, MSG_ACCEPT_DELIVERY);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_ACCEPT_DELIVERY_VALUE, accepted);
        msg.setData(data);
        return msg;
    }

    public static Message createEndDeliveryMessage() {
        return Message.obtain(null, MSG_END_DELIVERY);
    }

    public static Message createAllowWaterAdditionMessage(boolean allowWaterAddition) {
        Message msg = Message.obtain(null, MSG_ADD_WATER_PERMISSION);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_ADD_WATER_PERMISSION_VALUE, allowWaterAddition);
        msg.setData(data);
        return msg;
    }

    public static Message createChangeExternalDisplayState(boolean activated) {
        Message msg = Message.obtain(null, MSG_CHANGE_EXTERNAL_DISPLAY_STATE);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE, activated);
        msg.setData(data);
        return msg;
    }

    //
    // Getter of values sent by clients
    //

    public static String getAddressFromConnectMessage(Message msg) {
        return msg.getData().getString(KEY_MSG_CONNECT_DEVICE_ADDRESS);
    }

    public static TruckParameters getDataFromTruckParametersMessage(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(TruckParametersParcelable.class.getClassLoader());
        return data.getParcelable(KEY_MSG_TRUCK_PARAMETERS_DATA);
    }

    public static DeliveryParameters getDataFromDeliveryParametersMessage(Message msg) {
        Bundle data = msg.getData();
        data .setClassLoader(DeliveryParametersParcelable.class.getClassLoader());
        return data.getParcelable(KEY_MSG_DELIVERY_PARAMETERS_DATA);
    }

    public static boolean getValueFromAcceptDeliveryMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_ACCEPT_DELIVERY_VALUE);
    }

    public static boolean getValueFromAddWaterPermissionMessage(Message msg) {
        return (msg.getData().getBoolean(KEY_MSG_ADD_WATER_PERMISSION_VALUE));
    }

    public static boolean getValueFromChangeExternalDisplayStateMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE);
    }
}
