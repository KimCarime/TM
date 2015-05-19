package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class DerivedData extends MessageType {
    public DerivedData(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DONNEES_DERIVEES);

        // Extract bytes
        byte rotationDirectionByte = data[0];
        byte isSlumpFrameStableByte = data[1];
        byte[] currentFrameSizeBytes = getBytes(data, 2, 3);
        byte[] expectedFrameSizeBytes = getBytes(data, 4, 5);

        // Check types
        checkIfBooleanByteIsValid(rotationDirectionByte, "L'octet correspondant au sens de rotation de la toupie dans la trame donnees derivees est d'une valeur invalide : " + Convert.byteToHex(rotationDirectionByte));
        checkIfBooleanByteIsValid(isSlumpFrameStableByte, "L'octet correspondant à la stabilité de la trame dans la trame donnees derivees est d'une valeur invalide : " + Convert.byteToHex(isSlumpFrameStableByte));

        // Decode parameters
        MessageReceivedListener.RotationDirection rotationDirection = (rotationDirectionByte == 0x00) ? MessageReceivedListener.RotationDirection.MIXING : MessageReceivedListener.RotationDirection.EMPTYING;
        boolean isSlumpFrameStable = (isSlumpFrameStableByte == 0x00);
        int currentFrameSize = Convert.buffToInt(currentFrameSizeBytes);
        int expectedFrameSize = Convert.buffToInt(expectedFrameSizeBytes);

        // Inform listener
        if (listener != null) {
            listener.derivedData(rotationDirection, isSlumpFrameStable, currentFrameSize, expectedFrameSize);
        }
    }
}
