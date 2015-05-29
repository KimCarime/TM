package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;
import com.lafarge.truckmix.utils.Convert;

public class SensorSpeedThresholdMin extends ReadAction {
    public SensorSpeedThresholdMin(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN);

        // Extract parameters
        byte thresholdExceedByte = data[0];

        // Check types
        checkIfBooleanByteIsValid(thresholdExceedByte, "La valeur du boolean vitesse faible obtenu depuis kerlink n'est pas conforme avec les specifications du protocole : " + Convert.byteToHex(thresholdExceedByte));

        // Decode parameters
        boolean thresholdExceed = (thresholdExceedByte != 0x00);

        // Inform listener
        if (listener != null) {
            listener.speedSensorHasExceedMinThreshold(thresholdExceed);
        }
    }
}
