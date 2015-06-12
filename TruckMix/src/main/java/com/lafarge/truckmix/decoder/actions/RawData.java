package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.Convert;

public class RawData extends ReadAction {
    public RawData(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DONNEES_BRUTES);

        // Extract parameters
        byte[] inPressureBytes = getBytes(data, 0, 3);
        byte[] outPressureBytes = getBytes(data, 4, 7);
        byte[] intervalBytes = getBytes(data, 8, 11);
        byte buttonHoldByte = data[12];

        // Check types
        checkIfBooleanByteIsValid(buttonHoldByte, "L'octet correspondant à la donnée bouton d'eau dans la trame données brutes est d'une valeur invalide : " + Convert.byteToHex(buttonHoldByte));

        // Decode parameters
        int inPressure = Convert.bytesToInt(inPressureBytes);
        int outPressure = Convert.bytesToInt(outPressureBytes);
        int interval = Convert.bytesToInt(intervalBytes);
        boolean buttonHold = (buttonHoldByte == 0x00);

        // Inform listener
        listener.rawData(inPressure, outPressure, interval, buttonHold);
    }
}
