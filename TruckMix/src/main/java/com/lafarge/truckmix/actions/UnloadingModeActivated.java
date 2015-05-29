package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;

public class UnloadingModeActivated extends ReadAction {
    public UnloadingModeActivated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE);
        }

        // Inform listener
        if (listener != null) {
            listener.unloadingModeActivated();
        }
    }
}
