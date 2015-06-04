package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.Convert;

public class SensorSpeedThresholdMax extends ReadAction {
    public SensorSpeedThresholdMax(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX);

        // Extract parameters
        byte thresholdExceedByte = data[0];

        // Check types
        checkIfBooleanByteIsValid(thresholdExceedByte, "La valeur du boolean vitesse élevée obtenu depuis kerlink n'est pas conforme avec les specifications du protocole : " + Convert.byteToHex(thresholdExceedByte));

        // Decode parameters
        boolean thresholdExceed = (thresholdExceedByte != 0x00);

        // Inform listener
        if (listener != null) {
            listener.speedSensorHasExceedMaxThreshold(thresholdExceed);
        }
    }
}
