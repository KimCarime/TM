package com.lafarge.truckmix.service;

import android.os.Bundle;
import android.os.Message;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.models.DeliveryParametersParcelable;
import com.lafarge.truckmix.models.EventParcelable;
import com.lafarge.truckmix.models.TruckParametersParcelable;

/**
 * Define several constants of the TruckMixService incoming and outgoing messages.
 */
public class TruckMixServiceMessages {

    //
    // Messages coming from TruckMixService
    //

    // CommunicatorListener
    public static final int MSG_SLUMP_UPDATED = 0x1001;
    public static final int MSG_WATER_ADDED = 0x1002;
    public static final int MSG_MIXING_MODE_ACTIVATED = 0x1003;
    public static final int MSG_UNLOADING_MODE_ACTIVATED = 0x1004;
    public static final int MSG_WATER_ADDITION_REQUEST = 0x1005;
    public static final int MSG_WATER_ADDITION_BEGAN = 0x1006;
    public static final int MSG_WATER_ADDITION_END = 0x1007;
    public static final int MSG_STATE_CHANGED = 0x1008;
    public static final int MSG_CALIBRATION_DATA = 0x1009;
    public static final int MSG_ALARM_WATER_ADDITION_BLOCK = 0x100A;
    public static final int MSG_ALARM_WATER_MAX = 0x100B;
    public static final int MSG_ALARM_FLOWAGE_ERROR = 0x100C;
    public static final int MSG_ALARM_COUNTING_ERROR = 0x100D;
    public static final int MSG_INPUT_SENSOR_CONNECTION_CHANGED = 0x100E;
    public static final int MSG_OUTPUT_SENSOR_CONNECTION_CHANGED = 0x100F;
    public static final int MSG_SPEED_SENSOR_MIN_EXCEED = 0x1010;
    public static final int MSG_SPEED_SENSOR_MAX_EXCEED = 0x1011;
    // LoggerListener
    public static final int MSG_LOG = 0x2000;
    // TruckMixConnectionState
    public static final int MSG_CALCULATOR_CONNECTED = 0x3000;
    public static final int MSG_CALCULATOR_DISCONNECTED = 0x3001;
    public static final int MSG_CALCULATOR_CONNECTING = 0x3002;
    // EventListener
    public static final int MSG_NEW_EVENT = 0x4000;

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
    public static final String KEY_MSG_NEW_EVENT_DATA = "event_data";

    //
    // Messages coming from clients
    //

    // Internal use
    public static final int MSG_REGISTER_CLIENT = 0x9000;
    public static final int MSG_UNREGISTER_CLIENT = 0x9001;
    // Options/Parameters
    public static final int MSG_CONNECT_DEVICE = 0x5000;
    public static final int MSG_ALLOW_WATER_REQUEST = 0x5001;
    public static final int MSG_ENABLE_QUALITY_TRACKING = 0x5002;
    // Calculator
    public static final int MSG_TRUCK_PARAMETERS = 0x6000;
    public static final int MSG_DELIVERY_PARAMETERS = 0x6001;
    public static final int MSG_ACCEPT_DELIVERY = 0x6002;
    public static final int MSG_END_DELIVERY = 0x6003;
    public static final int MSG_ADD_WATER_PERMISSION = 0x6004;
    public static final int MSG_CHANGE_EXTERNAL_DISPLAY_STATE = 0x6005;

    public static final String KEY_MSG_CONNECT_DEVICE_ADDRESS = "address";
    public static final String KEY_MSG_ALLOW_WATER_REQUEST = "allow_water_request";
    public static final String KEY_MSG_ENABLE_QUALITY_TRACKING = "quality_tracking_enabled";
    public static final String KEY_MSG_TRUCK_PARAMETERS_DATA = "truck_parameters";
    public static final String KEY_MSG_DELIVERY_PARAMETERS_DATA = "delivery_parameters";
    public static final String KEY_MSG_ACCEPT_DELIVERY_VALUE = "accept_delivery_value";
    public static final String KEY_MSG_ADD_WATER_PERMISSION_VALUE = "add_water_permission";
    public static final String KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE = "change_external_display_value";

    //
    // Factory of messages coming from TruckMixService
    //

    public static Message createCalculatorIsConnectedMessage() {
        return Message.obtain(null, MSG_CALCULATOR_CONNECTED);
    }

    public static Message createCalculatorIsConnectingMessage() {
        return Message.obtain(null, MSG_CALCULATOR_CONNECTING);
    }

    public static Message createCalculatorIsDisconnectedMessage() {
        return Message.obtain(null, MSG_CALCULATOR_DISCONNECTED);
    }

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

    public static Message createNewEventMessage(Event event) {
        Message msg = Message.obtain(null, MSG_NEW_EVENT);
        Bundle data = new Bundle();
        data.putParcelable(KEY_MSG_NEW_EVENT_DATA, new EventParcelable(event));
        msg.setData(data);
        return msg;
    }

    //
    // Getter of values coming from TruckMixService
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

    public static Event getDataFromNewEventMessage(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(EventParcelable.class.getClassLoader());
        return data.getParcelable(KEY_MSG_NEW_EVENT_DATA);
    }

    //
    // Factory of message coming from clients
    //

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

    public static Message createChangeExternalDisplayStateMessage(boolean activated) {
        Message msg = Message.obtain(null, MSG_CHANGE_EXTERNAL_DISPLAY_STATE);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE, activated);
        msg.setData(data);
        return msg;
    }

    public static Message createWaterRequestAllowedMessage(boolean waterRequestAllowed) {
        Message msg = Message.obtain(null, MSG_ALLOW_WATER_REQUEST);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_ALLOW_WATER_REQUEST, waterRequestAllowed);
        msg.setData(data);
        return msg;
    }

    public static Message createEnableQualityTrackingMessage(boolean qualityTrackingEnabled) {
        Message msg = Message.obtain(null, MSG_ENABLE_QUALITY_TRACKING);
        Bundle data = new Bundle();
        data.putBoolean(KEY_MSG_ENABLE_QUALITY_TRACKING, qualityTrackingEnabled);
        msg.setData(data);
        return msg;
    }

    //
    // Getter of values coming from clients
    //

    public static String getAddressFromConnectMessage(Message msg) {
        return msg.getData().getString(KEY_MSG_CONNECT_DEVICE_ADDRESS);
    }

    public static boolean getValueFromAllowWaterRequestMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_ALLOW_WATER_REQUEST);
    }

    public static boolean getValueFromEnableQualityTrackingMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_ENABLE_QUALITY_TRACKING);
    }

    public static TruckParameters getDataFromTruckParametersMessage(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(TruckParametersParcelable.class.getClassLoader());
        return data.getParcelable(KEY_MSG_TRUCK_PARAMETERS_DATA);
    }

    public static DeliveryParameters getDataFromDeliveryParametersMessage(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(DeliveryParametersParcelable.class.getClassLoader());
        return data.getParcelable(KEY_MSG_DELIVERY_PARAMETERS_DATA);
    }

    public static boolean getValueFromAcceptDeliveryMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_ACCEPT_DELIVERY_VALUE);
    }

    public static boolean getValueFromAddWaterPermissionMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_ADD_WATER_PERMISSION_VALUE);
    }

    public static boolean getValueFromChangeExternalDisplayStateMessage(Message msg) {
        return msg.getData().getBoolean(KEY_MSG_CHANGE_EXTERNAL_DISPLAY_STATE_VALUE);
    }
}
