package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class WaterAdditionLocked extends MessageType {
    public WaterAdditionLocked(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_AUTORISATION_REFUS_AJOUT_EAU);
        }

        // Inform listener
        if (listener != null) {
            listener.waterAdditionLocked();
        }
    }
}
