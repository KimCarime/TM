package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.utils.Convert;

public class DerivedData extends ReadAction {
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
        MessageReceivedListener.RotationDirection rotationDirection = (rotationDirectionByte == 0x00) ? MessageReceivedListener.RotationDirection.MIXING : MessageReceivedListener.RotationDirection.UNLOADING;
        boolean isSlumpFrameStable = (isSlumpFrameStableByte == 0x00);
        int currentFrameSize = Convert.bytesToInt(currentFrameSizeBytes);
        int expectedFrameSize = Convert.bytesToInt(expectedFrameSizeBytes);

        // Inform listener
        listener.derivedData(rotationDirection, isSlumpFrameStable, currentFrameSize, expectedFrameSize);
    }
}
