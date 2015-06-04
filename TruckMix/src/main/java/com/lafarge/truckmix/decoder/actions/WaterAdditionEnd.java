package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

public class WaterAdditionEnd extends ReadAction {
    public WaterAdditionEnd(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU);
        }

        // Inform listener
        if (listener != null) {
            listener.waterAdditionEnd();
        }
    }
}
