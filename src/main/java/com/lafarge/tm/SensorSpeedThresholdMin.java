package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class SensorSpeedThresholdMin extends MessageType {
    public SensorSpeedThresholdMin(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN);

        // Extract parameters
        byte hasThresholdExceedByte = data[0];

        // Check types
        checkIfBooleanByteIsValid(hasThresholdExceedByte, "La valeur du boolean vitesse faible obtenu depuis kerlink n'est pas conforme avec les specifications du protocole : " + Convert.byteToHex(hasThresholdExceedByte));

        // Decode parameters
        boolean hasThresholdExceed = (hasThresholdExceedByte == 0x00);

        // Inform listener
        if (listener != null) {
            listener.speedSensorHasExceedMinThreshold(hasThresholdExceed);
        }
    }
}
