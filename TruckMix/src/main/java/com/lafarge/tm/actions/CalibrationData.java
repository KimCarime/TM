package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.utils.Convert;

public class CalibrationData extends ReadAction {
    public CalibrationData(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DONNEES_CALIBRATION);

        // Extract parameters
        byte[] inPressureBytes = getBytes(data, 0, 3);
        byte[] outPressureBytes = getBytes(data, 4, 7);
        byte[] rotationSpeedBytes = getBytes(data, 8, 11);

        // Decode parameters
        float inPressure = Convert.bytesToFloat(inPressureBytes);
        float outPressure = Convert.bytesToFloat(outPressureBytes);
        float rotationSpeed = Convert.bytesToFloat(rotationSpeedBytes);

        // Inform listener
        if (listener != null) {
            listener.calibrationData(inPressure, outPressure, rotationSpeed);
        }
    }
}
