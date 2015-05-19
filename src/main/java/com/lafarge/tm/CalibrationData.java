package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class CalibrationData extends MessageType {
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
        float inPressure = Convert.buffToFloat(inPressureBytes);
        float outPressure = Convert.buffToFloat(outPressureBytes);
        float rotationSpeed = Convert.buffToFloat(rotationSpeedBytes);

        // Inform listener
        if (listener != null) {
            listener.calibrationData(inPressure, outPressure, rotationSpeed);
        }
    }
}
