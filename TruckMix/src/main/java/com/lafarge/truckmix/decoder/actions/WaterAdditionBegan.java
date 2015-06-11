package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

public class WaterAdditionBegan extends ReadAction {
    public WaterAdditionBegan(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_DEBUT_AJOUT_EAU);

        // Inform listener
        listener.waterAdditionBegan();
    }
}
