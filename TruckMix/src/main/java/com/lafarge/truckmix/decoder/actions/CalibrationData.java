package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.utils.Convert;

public class CalibrationData extends ReadAction {
    public CalibrationData(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DONNEES_CALIBRATION);

        // Extract parameters
        byte[] inputPressureBytes = getBytes(data, 0, 3);
        byte[] outputPressureBytes = getBytes(data, 4, 7);
        byte[] rotationSpeedBytes = getBytes(data, 8, 11);

        // Decode parameters
        float inputPressure = Convert.bytesToFloat(inputPressureBytes);
        float outputPressure = Convert.bytesToFloat(outputPressureBytes);
        float rotationSpeed = Convert.bytesToFloat(rotationSpeedBytes);

        // Inform listener
        listener.calibrationData(inputPressure, outputPressure, rotationSpeed);
    }
}
