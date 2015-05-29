package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;

public class AlarmCountingError extends ReadAction {
    public AlarmCountingError(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE);
        }

        // Inform listener
        if (listener != null) {
            listener.alarmCountingError();
        }
    }
}
