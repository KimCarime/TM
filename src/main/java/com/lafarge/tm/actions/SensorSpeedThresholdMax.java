package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.utils.Convert;

public class SensorSpeedThresholdMax extends MessageType {
    public SensorSpeedThresholdMax(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX);

        // Extract parameters
        byte isOutOfRangeByte = data[0];

        // Check types
        checkIfBooleanByteIsValid(isOutOfRangeByte, "La valeur du boolean vitesse élevée obtenu depuis kerlink n'est pas conforme avec les specifications du protocole : " + Convert.byteToHex(isOutOfRangeByte));

        // Decode parameters
        boolean isOutOfRange = (isOutOfRangeByte != 0x00);

        // Inform listener
        if (listener != null) {
            listener.speedSensorHasExceedMaxThreshold(isOutOfRange);
        }
    }
}
