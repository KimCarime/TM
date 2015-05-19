package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class OutputSensorStateChanged extends MessageType {
    public OutputSensorStateChanged (MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE);

        // Extract parameters
        byte isConnectedByte = data[0];

        // Check types
        checkIfBooleanByteIsValid(isConnectedByte, "La valeur du capteur de pression de sortie obtenu depuis kerlink n'est pas conforme avec les specifications du protocole : " + Convert.byteToHex(isConnectedByte));

        // Decode parameters
        boolean isConnected = (isConnectedByte == 0x00);

        // Inform listener
        if (listener != null) {
            listener.outputSensorStateChanged(isConnected);
        }
    }
}