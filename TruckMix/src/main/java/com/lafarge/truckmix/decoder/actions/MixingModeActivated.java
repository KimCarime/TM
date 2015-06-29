package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

public class MixingModeActivated extends ReadAction {
    public MixingModeActivated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE);

        // Inform listener
        listener.mixingModeActivated();
    }
}
