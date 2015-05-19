package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class WaterAdded extends MessageType {
    public WaterAdded(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE);

        // Extract parameters
        byte volumeByte = data[0];
        byte additionModeByte = data[1];

        // Check types
        checkIfBooleanByteIsValid(additionModeByte, "La valeur du mode d'ajout d'eau n'est pas conforme au protocole : " + Convert.byteToHex(additionModeByte));

        // Decode parameters
        int volume = volumeByte;
        MessageReceivedListener.WaterAdditionMode additionMode = (additionModeByte == 0x00) ? MessageReceivedListener.WaterAdditionMode.MANUAL : MessageReceivedListener.WaterAdditionMode.AUTO;

        // Inform listener
        if (listener != null) {
            listener.waterAdded(volume, additionMode);
        }
    }
}
