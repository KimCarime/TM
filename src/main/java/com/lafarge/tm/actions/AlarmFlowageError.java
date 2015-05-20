package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class AlarmFlowageError extends ReadAction {
    public AlarmFlowageError(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT);
        }

        // Inform listener
        if (listener != null) {
            listener.alarmFlowageError();
        }
    }
}
