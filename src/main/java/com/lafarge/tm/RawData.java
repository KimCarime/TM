package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class RawData extends MessageType {
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
        int inPressure = Convert.buffToInt(inPressureBytes);
        int outPressure = Convert.buffToInt(outPressureBytes);
        int interval = Convert.buffToInt(intervalBytes);
        boolean buttonHold = (buttonHoldByte == 0x00);

        // Inform listener
        if (listener != null) {
            listener.rawData(inPressure, outPressure, interval, buttonHold);
        } else {
            System.out.println("listener OFF :(");
        }
    }
}
